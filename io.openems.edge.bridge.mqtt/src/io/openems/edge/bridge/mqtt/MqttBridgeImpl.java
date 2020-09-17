package io.openems.edge.bridge.mqtt;

import io.openems.edge.bridge.mqtt.api.MqttBridge;
import io.openems.edge.bridge.mqtt.api.MqttPublishTask;
import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.api.MqttTask;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Mqtt")
public class MqttBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, MqttBridge {
    //Add to Manager
    private Map<String, List<MqttPublishTask>> publishTasks;
    private Map<String, List<MqttSubscribeTask>> subscribeTasks;

    private MqttPublishManager publishManager;
    private MqttSubscribeManager subscribeManager;


    public MqttBridgeImpl() {
        super(OpenemsComponent.ChannelId.values(),
                MqttBridge.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        //ClientId --> + CLIENT_0 // CLIENT_1 // CLIENT_2
        publishManager = new MqttPublishManager(publishTasks, config.username(), config.password(), config.connection(), config.clientId(), config.);
        //ClientId --> +CLIENT_3
        subscribeManager = new MqttSubscribeManager(subscribeTasks);

    }

    @Deactivate
    public void deactivate() {
    }


    /*
    *  if (this.tasks.containsKey(deviceId)) {
            if (this.tasks.get(deviceId).keySet().stream().anyMatch(header -> header.equals(task.getHeader()))) {
                this.tasks.get(deviceId).keySet().stream().filter(header -> header.equals(task.getHeader())).findFirst().ifPresent(
                        header -> this.tasks.get(deviceId).get(header).add(task));
            } else {
                List<GenibusTask> taskForNewHead = new ArrayList<>();
                taskForNewHead.add(task);
                this.tasks.get(deviceId).put(task.getHeader(), taskForNewHead);
            }
        } else {
            List<GenibusTask> list = new ArrayList<>();
            list.add(task);
            Map<Integer, List<GenibusTask>> map = new HashMap<>();
            map.put(task.getHeader(), list);
            this.tasks.put(deviceId, map);
        }
    *
    * */

    @Override
    public boolean addMqttTask(String id, MqttTask mqttTask) {
        if (mqttTask instanceof MqttPublishManager) {
            if (this.publishTasks.containsKey(id)) {
                this.publishTasks.get(id).add((MqttPublishTask) mqttTask);
            } else {
                List<MqttPublishTask> task = new ArrayList<>();
                task.add((MqttPublishTask) mqttTask);
                this.publishTasks.put(id, task);
            }
        }

        if (mqttTask instanceof MqttSubscribeManager) {
            if (this.subscribeTasks.containsKey(id)) {
                this.subscribeTasks.get(id).add((MqttSubscribeTask) mqttTask);
            } else {
                List<MqttSubscribeTask> task = new ArrayList<>();
                task.add((MqttSubscribeTask) mqttTask);
                this.subscribeTasks.put(id, task);
            }
        }
        return true;
    }

    @Override
    public boolean removeMqttTasks(String id) {
        this.subscribeTasks.remove(id);
        this.publishTasks.remove(id);

        return true;
    }
}
