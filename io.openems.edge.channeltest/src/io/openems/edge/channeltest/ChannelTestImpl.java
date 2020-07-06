package io.openems.edge.channeltest;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.channeltest.api.ChanneltestChannel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Module to demonstrate how channels work.
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "Channel.Test", immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
		})
public class ChannelTestImpl extends AbstractOpenemsComponent
		implements OpenemsComponent, ChanneltestChannel, EventHandler {


	private final Logger log = LoggerFactory.getLogger(ChannelTestImpl.class);
	private int counter = 0;

	/**
	 * Channels can be defined in the class itself or in an extra interface. Having them in an extra interface makes it
	 * easier to use/find/understand for other programmers that want to communicate with your module.
	 *
	 * You do not need to declare the access type or the unit for a channel. The default access type is a read channel.
	 * A read channel can still be used to write values. The main difference is that a write channel has the
	 * method "setNextWriteValue" that also accepts input from external sources like JSON/REST. Both read and
	 * write channels have the "setNextValue" method that can be used by any OpenEMS module.
	 */

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		TEST(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.NONE));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}


	@Activate
	void activate(ComponentContext context, Config config) throws IOException, OpenemsError.OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		//this.noError().setNextValue(true);  // Not used to demonstrate what happens with an empty channel.
		this.writeBoolean().setNextValue(true);  // <- Note that setNextValue is used here and not setNextWriteValue.
		//this.writeBoolean().setNextWriteValue(true); // setNextWriteValue also works, but was not used to demonstrate what happens when a channel is empty.

	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}


	/**
	 * All channels and implemented interfaces that have channels need to be listed in the constructor. Otherwise the
	 * associated channels do not work.
	 * This class defines one integer channel "ChannelId.TEST". Three more channel are in the ChanneltestChannel interface.
	 * In ChanneltestChannel is one boolean read channel noError, one boolean write channel writeBoolean and one integer
	 * write channel writeInteger.
	 * The channels in ChanneltestChannel have a method for calling them to shorten the call. To get the value of the
	 * TEST channel, one needs to write "channel(ChannelId.TEST).value().get()". The same for the writeInteger channel is
	 * "writeInteger().value().get()".
	 *
	 */

	public ChannelTestImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), // <- An interface that is implemented by this class and has channels.
				ChanneltestChannel.ChannelId.values(), // <- Interface specifically written for this class where channels are defined.
				ChannelId.values() // <- Channel defined directly in this module
		);
	}



	/**
	 * From the JavaDoc of Channel.java:
	 * "Channels implement a 'Process Image' pattern. They provide an 'active' value
	 * which should be used for any operations on the channel value. The 'next'
	 * value is filled by asynchronous workers in the background. At the 'Process
	 * Image Switch' the 'next' value is copied to the 'current' value."
	 *
	 * Channels automatically write their nextValue into the value field at the "switch process image" event. Nothing
	 * special is required to make this happen. Data can be put into channels anywhere, and their use is not restricted
	 * to a specific method of the framework. The recurring method used here is called "updateChannels" and triggered
	 * by the event handler. Placing the same code that is in "updateChannels" in the "run()" method of a controller
	 * would work just as well. The only difference would be a different timing in regard to other OpenEMS modules,
	 * which might not matter.
	 * The try-catch block here is needed because write channels are used, which throw the OpenemsNamedException. If
	 * only read channels are used the OpenemsNamedException does not need to be addressed.
	 *
	 */
	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
				try {
					this.updateChannels();
				} catch (OpenemsError.OpenemsNamedException e) {
					e.printStackTrace();
				}
				break;
		}
	}


	private void updateChannels() throws OpenemsError.OpenemsNamedException {

		// The counter is just used to fill the channels with changing values
		counter++;

		/**
		 * The standard method to put a value xxx in a channel is the ".setNextValue(xxx)" method.
		 *
		 */
		this.channel(ChannelId.TEST).setNextValue(counter);

		/**
		 * A write channel is an extension of a read channel. The extension is that the write channel also has the
		 * setNextWriteValue method. This is an extra storage field that can be accessed by external sources such as
		 * JSON/REST calls. JSON/REST cannot access setNextValue of the read channel.
		 *
		 */
		 this.writeInteger().setNextWriteValue(counter);

		 /**
		 * A write channel then has three storage fields that each can have a different value: value, nextValue and
		 * nextWriteValue. The three fields can also be accessed individually with ".value()", ".getNextValue()" and
		 * ".getNextWriteValue()".
		 * The nextWriteValue field is NOT automatically copied into the value field at the process image switch event.
		 * Consequently, calling ".nextProcessImage()" does not do anything regarding the nextWriteValue field.
		 * Although the value in nextWriteValue can be accessed with ".getNextWriteValue()", it is better to transfer
		 * nextWriteValue into nextValue and then use the same access methods as for a read channel. Otherwise nextValue
		 * and value might remain empty, even though the channel is in use. This can be very confusing to other
		 * programmers trying to access the channel. The recommended way to access a channel is the ".value()" method,
		 * so a programmer needs to make sure that this field is getting the data written into setNextWriteValue.
		 * Here is one way to copy information from the nextWriteValue into the nextValue field:
		 *
		 */
		if (this.writeInteger().getNextWriteValue().isPresent()) {
			this.writeInteger().setNextValue(this.writeInteger().getNextWriteValue().get());
		}

		/**
		 * Once the information has been transferred to nextValue, on switch process image it will be further
		 * transferred from nextValue to value. The information now gets passed to all three storage fields of the write
		 * channel.
		 * The channel storage fields can be null and should be checked for a value before requesting the data. This is
		 * quite similar to an optional, and the method ".isPresent()" actually uses an optional. Also similar to an
		 * optional is that to get the actual value, a further call of ".get()" is needed.
		 * If the channel is null and .get() is used, this will result in a null pointer exception. Intellij can
		 * at times recognize that and warn that a ".isPresent()" or ".isDefined()" needs to be called first.
		 *
		 * Another method to transfer data from nextWriteValue to nextValue is demonstrated in ChanneltestChannel.java
		 * for the writeBoolean channel.
		 *
		 * Open questions that I (Basti) have not been able to answer yet:
		 * - Why use setNextWriteValue and not setNextValue in an OpenEMS module? When you just want to communicate with
		 * another OpenEMS module, setNextValue is all that is needed. Why use setNextWriteValue at all?
		 * - Connection of setNextWriteValue and Modbus. Looking at the code of other modules, setNextWriteValue is used
		 * alot when Modbus is used. Maybe setNextWriteValue is needed for Modbus.
		 *
		 */




		/**
		 * Here we demonstrate the use of ".nextProcessImage()". Calling this will immediately put the nextValue into
		 * the value field. The writeInteger channel nextValue is filled with the same data as the TEST channel nextValue.
		 * ".nextProcessImage()" is called just for the TEST channel to show the difference.
		 *
		 */
		this.channel(ChannelId.TEST).nextProcessImage();

		/**
		 * Here the noError channel is filled with data. The writeBoolean channel is filled with the same data, but at
		 * the end of the method. This is done to show how placement in the code and switch process image interact.
		 *
		 */
		if (counter == 2) {
			this.noError().setNextValue(true);
		}
		if (counter == 5) {
			this.noError().setNextValue(false);
		}


		/**
		 * Now we print out the data of all storage fields of every channel. An "else" path has been added for the channels
		 * that are null at some point because of how the module was coded.
		 *
		 */
		if (this.channel(ChannelId.TEST).getNextValue().isDefined()) {
			this.logInfo(this.log, "TEST channel nextValue: " + this.channel(ChannelId.TEST).getNextValue().get());
		}
		if (this.channel(ChannelId.TEST).value().isDefined()) {
			this.logInfo(this.log, "TEST channel value: " + this.channel(ChannelId.TEST).value().get());
		} else {
			this.logInfo(this.log, "TEST channel value: null");
		}

		if (this.writeInteger().getNextWriteValue().isPresent()) {
			this.logInfo(this.log, "writeChannel nextWriteValue: " + this.writeInteger().getNextWriteValue().get());
		}
		if (this.writeInteger().getNextValue().isDefined()) {
			this.logInfo(this.log, "writeChannel nextValue: " + this.writeInteger().getNextValue().get());
		}
		if (this.writeInteger().value().isDefined()) {
			this.logInfo(this.log, "writeChannel value: " + this.writeInteger().value().get());
		} else {
			this.logInfo(this.log, "writeChannel value: null");
		}

		if (this.noError().getNextValue().isDefined()) {
			this.logInfo(this.log, "noError Channel nextValue: " + this.noError().getNextValue().get());
		} else {
			this.logInfo(this.log, "noError Channel nextValue: null");
		}
		if (this.noError().value().isDefined()) {
			this.logInfo(this.log, "Channel value: " + this.noError().value().get());
		} else {
			this.logInfo(this.log, "noError Channel value: null");
		}

		if (this.writeBoolean().getNextWriteValue().isPresent()) {
			this.logInfo(this.log, "writeChannel nextWriteValue: " + this.writeBoolean().getNextWriteValue().get());
		} else {
			this.logInfo(this.log, "writeChannel nextWriteValue: null");
		}
		if (this.writeBoolean().getNextValue().isDefined()) {
			this.logInfo(this.log, "writeChannel next value: " + this.writeBoolean().getNextValue().get());
		}
		if (this.writeBoolean().value().isDefined()) {
			this.logInfo(this.log, "writeChannel value: " + this.writeBoolean().value().get());
		} else {
			this.logInfo(this.log, "writeChannel value: null");
		}

		/**
		 * Here the writeBoolean channel is filled with data. Note that this happens after the channel values are
		 * called. Before the next call, switch process image has happened and so nextValue and value contain the same
		 * data when called.
		 *
		 */
		if (counter == 2) {
			this.writeBoolean().setNextWriteValue(true);
		}
		if (counter == 5) {
			this.writeBoolean().setNextWriteValue(false);
			counter = 0;
		}



	}

}

