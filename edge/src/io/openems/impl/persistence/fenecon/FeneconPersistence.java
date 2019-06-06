/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.impl.persistence.fenecon;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.openems.api.channel.Channel;
import io.openems.api.channel.ChannelChangeListener;
import io.openems.api.channel.ConfigChannel;
import io.openems.api.channel.ReadChannel;
import io.openems.api.channel.thingstate.ThingStateChannels;
import io.openems.api.controller.ThingMap;
import io.openems.api.device.nature.DeviceNature;
import io.openems.api.doc.ChannelInfo;
import io.openems.api.doc.ThingInfo;
import io.openems.api.exception.ConfigException;
import io.openems.api.persistence.Persistence;
import io.openems.api.thing.Thing;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.session.Role;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.ChannelEnum;
import io.openems.common.types.FieldValue;
import io.openems.common.types.NullFieldValue;
import io.openems.common.types.NumberFieldValue;
import io.openems.common.types.StringFieldValue;
import io.openems.common.utils.StringUtils;
import io.openems.common.websocket.DefaultMessages;
import io.openems.core.Config;
import io.openems.core.ConfigFormat;
import io.openems.core.Databus;
import io.openems.core.ThingRepository;
import io.openems.core.utilities.OnConfigUpdate;

// TODO make sure this is registered as ChannelChangeListener also to ConfigChannels
@ThingInfo(title = "FENECON Persistence", description = "Establishes the connection to FENECON Cloud.")
public class FeneconPersistence extends Persistence implements ChannelChangeListener {

	private final Logger log = LoggerFactory.getLogger(FeneconPersistence.class);
	private final static String DEFAULT_CONFIG_LANGUAGE = "en";

	private ThingStateChannels thingState;

	/*
	 * Config
	 */
	@ChannelInfo(title = "Apikey", description = "Sets the apikey for FENECON Cloud.", type = String.class, readRoles = {
			Role.ADMIN })
	public final ConfigChannel<String> apikey = new ConfigChannel<String>("apikey", this).doNotPersist();

	@ChannelInfo(title = "Uri", description = "Sets the connection Uri to FENECON Cloud.", type = String.class, defaultValue = "\"wss://fenecon.de:443/openems-backend2\"")
	public final ConfigChannel<String> uri = new ConfigChannel<String>("uri", this).doNotPersist();

	@ChannelInfo(title = "Sets the duration of each cycle in milliseconds", type = Integer.class)
	public ConfigChannel<Integer> cycleTime = new ConfigChannel<Integer>("cycleTime", this)
	.defaultValue(DEFAULT_CYCLETIME);

	@ChannelInfo(title = "ProxyAddress", description = "Sets the proxy address IP or hostname.", type = String.class, isOptional = true)
	public final ConfigChannel<String> proxyAddress = new ConfigChannel<String>("proxyAddress", this);

	@ChannelInfo(title = "ProxyPort", description = "Sets the proxy port.", type = Integer.class, isOptional = true)
	public final ConfigChannel<Integer> proxyPort = new ConfigChannel<Integer>("proxyPort", this);

	@ChannelInfo(title = "ProxyType", description = "Sets the proxy type (e.g. 'http').", type = String.class, isOptional = true)
	public final ConfigChannel<String> proxyType = new ConfigChannel<String>("proxyType", this);

	/*
	 * Constructor
	 */
	public FeneconPersistence() {
		this.thingState = new ThingStateChannels(this);
		// TODO with version 1.3.8 comes a new client reconnect feature. Use it to replace ReconnectingWebsocket
		// (https://github.com/TooTallNate/Java-WebSocket/releases/tag/v1.3.8)
		// TODO evaluate if onMessage tasks need to be done in separate Executor
		// (https://github.com/TooTallNate/Java-WebSocket/issues/688)
		this.reconnectingWebsocket = new ReconnectingWebsocket((websocket) -> {
			/*
			 * onOpen
			 */
			Optional<String> proxyInfoOpt = this.proxyInfo();
			log.info("FENECON persistence connected [" + uri.valueOptional().orElse("") + "]"
					+ (proxyInfoOpt.isPresent() ? ", " + proxyInfoOpt.get() : ""));
			// Add current status of all channels to queue
			this.addCurrentValueOfAllChannelsToQueue();
			// Send current config
			this.onConfigUpdate.call();
		}, () -> {
			/*
			 * onClose
			 */
			Optional<String> proxyInfoOpt = this.proxyInfo();
			log.error("FENECON persistence closed connection to uri [" + uri.valueOptional().orElse("") + "]"
					+ (proxyInfoOpt.isPresent() ? ", " + proxyInfoOpt.get() : ""));
		});

		/*
		 * Listen to config updates
		 */
		onConfigUpdate = () -> {
			try {
				if (reconnectingWebsocket != null) {
					reconnectingWebsocket.send(DefaultMessages.configQueryReply(new JsonObject(), Config.getInstance()
							.getJson(ConfigFormat.OPENEMS_UI, Role.ADMIN, DEFAULT_CONFIG_LANGUAGE)));
				}
				log.info("Sent config to FENECON persistence.");
			} catch (OpenemsException e) {
				log.error("Unable to send config: " + e.getMessage());
			}
		};
		try {
			Config config = Config.getInstance();
			config.addOnConfigUpdateListener(this.onConfigUpdate);
		} catch (ConfigException e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public void init() {
		this.updateWebsocketParams();
	}

	/*
	 * Fields
	 */
	private static final int DEFAULT_CYCLETIME = 10000;
	private final ReconnectingWebsocket reconnectingWebsocket;
	private volatile OnConfigUpdate onConfigUpdate = null;

	// Queue of data for the next cycle
	private HashMap<ChannelAddress, FieldValue<?>> queue = new HashMap<>();
	// Unsent queue (FIFO)
	private EvictingQueue<JsonObject> unsentCache = EvictingQueue.create(1000);
	private volatile Optional<Integer> increasedCycleTime = Optional.empty();

	/*
	 * Methods
	 */
	private void updateWebsocketParams() {
		// TODO call on channel update URI + apikey
		Optional<String> apikeyOpt = this.apikey.valueOptional();
		if (apikeyOpt.isPresent()) {
			// set apikey header
			this.reconnectingWebsocket.addHttpHeader("apikey", apikeyOpt.get());

			// get proxy
			Optional<Proxy> proxyOpt = Optional.empty();
			Optional<String> proxyAddressOpt = this.proxyAddress.valueOptional();
			Optional<Integer> proxyPortOpt = this.proxyPort.valueOptional();
			Optional<String> proxyTypeStringOpt = this.proxyType.valueOptional();
			if (proxyAddressOpt.isPresent() && proxyPortOpt.isPresent() && proxyTypeStringOpt.isPresent()) {
				Optional<Proxy.Type> proxyTypeOpt = Optional.empty();
				switch (proxyTypeStringOpt.get().toLowerCase()) {
				case "http":
					proxyTypeOpt = Optional.of(Proxy.Type.HTTP);
				}
				if (proxyTypeOpt.isPresent()) {
					proxyOpt = Optional.of(new Proxy(proxyTypeOpt.get(),
							new InetSocketAddress(proxyAddressOpt.get(), proxyPortOpt.get())));
				}
			}

			// connect
			Optional<String> uriStringOpt = this.uri.valueOptional();
			if (uriStringOpt.isPresent()) {
				try {
					URI uri = new URI(uriStringOpt.get());
					this.reconnectingWebsocket.setUri(Optional.of(uri), proxyOpt);
				} catch (URISyntaxException e) {
					log.error("URI [" + uriStringOpt.get() + "] is invalid: " + e.getMessage());
					this.reconnectingWebsocket.setUri(Optional.empty(), proxyOpt);
					return;
				}
			} else {
				// URI is not present
				this.reconnectingWebsocket.setUri(Optional.empty(), proxyOpt);
			}
		}
	}

	/**
	 * Receives update events for all {@link ReadChannel}s, excluding {@link ConfigChannel}s via the {@link Databus}.
	 */
	@Override
	public void channelChanged(Channel channel, Optional<?> newValue, Optional<?> oldValue) {
		this.addChannelValueToQueue(channel, newValue);
	}

	@Override
	protected void forever() {
		// Get timestamp and round to Cycle-Time
		int cycleTime = this.getCycleTime();
		Long timestamp = System.currentTimeMillis() / cycleTime * cycleTime;

		// Convert FieldVales in queue to JsonObject
		JsonObject j;
		synchronized (queue) {
			j = DefaultMessages.timestampedData(timestamp, queue);
			queue.clear();
		}

		// Send data to Server
		if (this.sendOrLogError(j)) {
			// Successful

			// reset cycleTime
			resetCycleTime();

			// resend from cache
			for (Iterator<JsonObject> iterator = unsentCache.iterator(); iterator.hasNext();) {
				JsonObject jCached = iterator.next();
				boolean cacheWasSent = this.sendOrLogError(jCached);
				if (cacheWasSent) {
					iterator.remove();
				}
			}
		} else {
			// Failed to send

			// increase cycleTime
			increaseCycleTime();

			// cache data for later
			unsentCache.add(j);
		}
	}

	@Override
	protected void dispose() {
		this.reconnectingWebsocket.dispose();
		try {
			Config config = Config.getInstance();
			config.removeOnConfigUpdateListener(this.onConfigUpdate);
		} catch (ConfigException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Send message to websocket
	 *
	 * @param j
	 * @return
	 * @throws OpenemsException
	 */
	private boolean sendOrLogError(JsonObject j) {
		try {
			this.reconnectingWebsocket.send(j);
			return true;
		} catch (OpenemsException e) {
			log.warn("Unable to send: " + StringUtils.toShortString(j, 100));
			return false;
		}
	}

	/**
	 * Gets the websocket handler
	 *
	 * @return
	 */
	// public EdgeWebsocketHandler getWebsocketHandler() {
	// return this.websocketHandler;
	// }

	private void increaseCycleTime() {
		int currentCycleTime = this.getCycleTime();
		int newCycleTime;
		if (currentCycleTime < 30000 /* 30 seconds */) {
			newCycleTime = currentCycleTime * 2;
		} else {
			newCycleTime = currentCycleTime;
		}
		if (currentCycleTime != newCycleTime) {
			this.increasedCycleTime = Optional.of(newCycleTime);
		}
	}

	/**
	 * Cycletime is adjusted if connection to Backend fails. This method resets it to configured or default value.
	 */
	private void resetCycleTime() {
		this.increasedCycleTime = Optional.empty();
	}

	/**
	 * Add a channel value to the send queue
	 *
	 * @param channel
	 * @param valueOpt
	 */
	private void addChannelValueToQueue(Channel channel) {
		if (!(channel instanceof ReadChannel<?>)) {
			// TODO check for more types - see other addChannelValueToQueue method
			return;
		}
		ReadChannel<?> readChannel = (ReadChannel<?>) channel;
		this.addChannelValueToQueue(channel, readChannel.valueOptional());
	}

	/**
	 * Add a channel value to the send queue
	 *
	 * @param channel
	 * @param valueOpt
	 */
	private void addChannelValueToQueue(Channel channel, Optional<?> valueOpt) {
		// Ignore anything that is not a ReadChannel
		if (!(channel instanceof ReadChannel<?>)) {
			return;
		}
		ReadChannel<?> readChannel = (ReadChannel<?>) channel;
		// Ignore channels that shall not be persisted
		if (readChannel.isDoNotPersist()) {
			return;
		}

		// Read and format value from channel
		FieldValue<?> fieldValue;
		if (!valueOpt.isPresent()) {
			fieldValue = new NullFieldValue();
		} else {
			Object value = valueOpt.get();
			if (value instanceof Number) {
				fieldValue = new NumberFieldValue((Number) value);
			} else if (value instanceof String) {
				fieldValue = new StringFieldValue((String) value);
			} else if (value instanceof Inet4Address) {
				fieldValue = new StringFieldValue(((Inet4Address) value).getHostAddress());
			} else if (value instanceof Boolean) {
				fieldValue = new NumberFieldValue(((Boolean) value) ? 1 : 0);
			} else if (value instanceof ChannelEnum) {
				fieldValue = new NumberFieldValue(((ChannelEnum) value).getValue());
			} else if (value instanceof DeviceNature || value instanceof JsonElement || value instanceof Map
					|| value instanceof Set || value instanceof List || value instanceof ThingMap) {
				// ignore
				return;
			} else {
				log.warn("FENECON Persistence for value type [" + value.getClass().getName() + "] of channel ["
						+ channel.address() + "] is not implemented.");
				return;
			}
		}

		// Add timestamp + value to queue
		synchronized (queue) {
			queue.put(readChannel.address(), fieldValue);
		}
	}

	/**
	 * On websocket open, add current values of all channels to queue. This is to prepare upcoming "channelChanged"
	 * events, where only changes are sent
	 */
	private void addCurrentValueOfAllChannelsToQueue() {
		ThingRepository thingRepository = ThingRepository.getInstance();
		for (Thing thing : thingRepository.getThings()) {
			for (Channel channel : thingRepository.getChannels(thing)) {
				this.addChannelValueToQueue(channel);
			}
		}
	}

	@Override
	protected int getCycleTime() {
		return this.increasedCycleTime.orElse(this.cycleTime.valueOptional().orElse(DEFAULT_CYCLETIME));
	}

	@Override
	protected boolean initialize() {
		return this.reconnectingWebsocket.websocketIsOpen();
	}

	private Optional<String> proxyInfo() {
		if (this.proxyAddress.valueOptional().isPresent() && this.proxyPort.valueOptional().isPresent()
				&& this.proxyType.valueOptional().isPresent()) {
			return Optional.of("proxy [" + this.proxyAddress.valueOptional().get() + ":"
					+ this.proxyPort.valueOptional().get() + ":" + this.proxyType.valueOptional().get() + "]");
		} else {
			return Optional.empty();
		}
	}

	@Override
	public ThingStateChannels getStateChannel() {
		return this.thingState;
	}

	public void sendLog(long timestamp, String level, String source, String message) {
		this.reconnectingWebsocket.sendLog(timestamp, level, source, message);
	}
}