package io.openems.edge.bridge.mqtt.dummys;

import io.openems.edge.bridge.mqtt.api.MqttComponent;
import io.openems.edge.bridge.mqtt.api.PayloadStyle;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractMqttComponent;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Dummy.Mqtt.Component",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class DummyComponentMqtt extends AbstractOpenemsComponent implements OpenemsComponent, MqttComponent, DummyChannels {


    private MqttComponentDummyImpl component;


    public DummyComponentMqtt() {
        super(OpenemsComponent.ChannelId.values(),
                MqttComponent.ChannelId.values(),
                DummyChannels.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws MqttException, ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        List<String> subList = Arrays.asList(config.subscriptionList());
        List<String> pubList = Arrays.asList(config.publishList());
        List<String> payloads = Arrays.asList(config.payloads());
        List<Channel<?>> channels = new ArrayList<>(this.channels());
        this.component = new MqttComponentDummyImpl(super.id(), this.servicePid(), "channelIdList", subList, pubList, payloads, channels,
                config.createdByOsgiConfig(), PayloadStyle.valueOf(config.payloadStyle().toUpperCase()));

        if (this.component.hasBeenConfigured()) {
            this.component.initTasks(channels);
        }

    }


    private class MqttComponentDummyImpl extends AbstractMqttComponent {

        /**
         * Initially update Config and after that set params for initTasks.
         *
         * @param id            id of this Component, usually from configuredDevice and it's config.
         * @param servicePid    servicePid of the Concrete Component.
         * @param configTarget  the Target to update usually something like ChannelList ... Usually from Component.
         * @param subConfigList Subscribe ConfigList, containing the Configuration for the subscribeTasks.
         * @param pubConfigList Publish Configlist, containing the Configuration for the publishTasks.
         * @param payloads      containing all the Payloads. ConfigList got the Payload list as well.
         * @param channelIds    List of all the Channels the Component has. This will not be saved, only the Map of used Channels.
         * @param createdByOsgi is this Component configured by OSGi or not. If not --> Read JSON File/Listen to Configuration Channel.
         * @param style         PayloadStyle of publish and Subscribe Message.
         */
        MqttComponentDummyImpl(String id, String servicePid, String configTarget, List<String> subConfigList,
                               List<String> pubConfigList, List<String> payloads, List<Channel<?>> channelIds,
                               boolean createdByOsgi, PayloadStyle style) {
            super(id, servicePid, configTarget, subConfigList, pubConfigList, payloads, channelIds, createdByOsgi, style);
        }
    }

}
