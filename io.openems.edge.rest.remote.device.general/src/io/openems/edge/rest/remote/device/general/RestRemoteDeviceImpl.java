package io.openems.edge.rest.remote.device.general;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.api.RestBridge;
import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.rest.remote.device.general.api.RestRemoteChannel;
import io.openems.edge.rest.remote.device.general.api.RestRemoteDevice;
import io.openems.edge.rest.remote.device.general.task.RestRemoteReadTask;
import io.openems.edge.rest.remote.device.general.task.RestRemoteWriteTask;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Rest.Remote.Device", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class RestRemoteDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, RestRemoteDevice, RestRemoteChannel {

    @Reference
    ComponentManager cpm;

    private RestBridge restBridge;

    private String restBridgeId;
    private RestRequest task;

    public RestRemoteDeviceImpl() {

        super(OpenemsComponent.ChannelId.values(),
                RestRemoteChannel.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException, OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {

            if (cpm.getComponent(config.restBridgeId()) instanceof RestBridge) {
                restBridge = cpm.getComponent(config.restBridgeId());
                this.restBridgeId = config.restBridgeId();

                restBridge.addRestRequest(super.id(), createNewTask(config.deviceType(), config.deviceChannel(),
                        config.id(), config.realDeviceId(), config.autoAdapt(), config.deviceMode()));
            } else {
                throw new ConfigurationException(config.restBridgeId(), "Master Slave Id Incorrect or not configured yet!");
            }
            this.getAllowRequest().setNextValue(true);
            this.getUnit().setNextValue(config.deviceUnit());
        }
    }

    /***/
    private RestRequest createNewTask(String deviceType, String deviceChannel, String remoteDeviceId,
                                      String realDeviceId, boolean autoAdapt, String deviceMode) throws ConfigurationException {

        if (deviceMode.equals("Write")) {

            if (deviceType.toLowerCase().equals("temperaturesensor")) {
                throw new ConfigurationException("TemperatureSensor write not allowed", "Warning!"
                        + " TemperatureSensor does not support Write Tasks!");
            } else {
                this.getTypeSet().setNextValue("Write");
                task = new RestRemoteWriteTask(remoteDeviceId, realDeviceId, deviceChannel, autoAdapt, getWriteValue(),
                        deviceType, this.getAllowRequest());
                return task;
            }
        } else if (deviceMode.equals("Read")) {
            this.getTypeSet().setNextValue("Read");
            //String deviceId, String masterSlaveId, boolean master, String realTemperatureSensor, Channel<Integer> temperature
            task = new RestRemoteReadTask(remoteDeviceId, realDeviceId, deviceChannel, autoAdapt,
                    getReadValue(), deviceType, this.getUnit());
            return task;
        }

        throw new ConfigurationException("Impossible Error", "Error shouldn't Occur because of Fix options");
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        restBridge.removeRestRemoteDevice(super.id());
    }

    @Override
    public String debugLog() {
        if (restBridge.getRemoteRequest(super.id()) != null) {
            return task.getDeviceType() + " " + this.getValue() + " of " + super.id() + " \n";
        }
        return "";
    }


    @Override
    public boolean setValue(String value) {
        if (!this.getTypeSet().getNextValue().isDefined()) {
            System.out.println("Not Defined Yet");
            return false;
        }
        if (this.getTypeSet().getNextValue().get().equals("Read")) {
            System.out.println("Can't write into ReadTasks");
        }

        try {
            this.getWriteValue().setNextWriteValue(value);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    public String getValue() {
        if (this.getTypeSet().getNextValue().get().equals("Write")) {
            if (this.getWriteValue().getNextValue().isDefined()) {
                return this.getWriteValue().getNextValue().get() + " " +  this.getUnit().getNextValue().get();
            } else {
                return "Value not available yet!";
            }
        } else if (this.getReadValue().getNextValue().isDefined()) {
            return this.getReadValue().getNextValue().get() + " " + this.getUnit().getNextValue().get();
        }
        return "Read Value not available yet";
    }

    @Override
    public String getType() {
        return this.getTypeSet().getNextValue().get();
    }

    @Override
    public boolean setAllowRequest(boolean allow) {
        try {
            this.getAllowRequest().setNextWriteValue(allow);
            return true;
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
