package io.openems.edge.controller.heatnetwork.master;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.heatnetwork.master.api.HeatNetworkMaster;
import io.openems.edge.controller.passing.controlcenter.api.PassingControlCenterChannel;
import io.openems.edge.rest.remote.device.general.api.RestRemoteDevice;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Heatnetwork.Master")
public class HeatNetworkMasterImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller, HeatNetworkMaster {

    @Reference
    ComponentManager cpm;

    private List<RestRemoteDevice> heatTankRequests = new ArrayList<>();
    private List<RestRemoteDevice> heatNetworkReady = new ArrayList<>();
    private PassingControlCenterChannel allocatedController;
    private int lastTemperature;


    public HeatNetworkMasterImpl() {
        super(OpenemsComponent.ChannelId.values(),
                HeatNetworkMaster.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        OpenemsError.OpenemsNamedException[] ex = {null};
        ConfigurationException[] exC = {null};
        Arrays.stream(config.requests()).forEach(consumer -> {
            try {
                if (cpm.getComponent(consumer) instanceof RestRemoteDevice) {
                    this.heatTankRequests.add(cpm.getComponent(consumer));
                } else {
                    throw new ConfigurationException("Wrong Component Name", "RestReadRemote Devices Wrong");
                }
            } catch (OpenemsError.OpenemsNamedException e) {
                ex[0] = e;
            } catch (ConfigurationException e) {
                exC[0] = e;
            }
        });
        Arrays.stream(config.readyResponse()).forEach(consumer -> {
            try {
                if (cpm.getComponent(consumer) instanceof RestRemoteDevice) {
                    this.heatNetworkReady.add(cpm.getComponent(consumer));
                } else {
                    throw new ConfigurationException("Wrong Component", "RestWriteRemote Device is Wrong.");
                }
            } catch (OpenemsError.OpenemsNamedException e) {
                ex[0] = e;
            } catch (ConfigurationException e) {
                exC[0] = e;
            }
        });

        if (ex[0] != null) {
            throw ex[0];
        } else if (exC[0] != null) {
            throw exC[0];
        }

        if (cpm.getComponent(config.allocatedController()) instanceof PassingControlCenterChannel) {
            this.allocatedController = cpm.getComponent(config.allocatedController());
        }
        this.temperatureSetPointChannel().setNextValue(config.temperatureSetPoint());
        this.allocatedController.setOverrideTemperature().setNextWriteValue(config.temperatureSetPoint());
        this.allocatedController.activateTemperatureOverride().setNextWriteValue(false);
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();

    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        //NO DEMAND!

        if (this.heatTankRequests.stream().noneMatch(consumer -> consumer.getValue().equals("1"))) {
            this.heatNetworkReady.forEach(consumer -> consumer.setValue("false"));
            this.lastTemperature = -1;
            this.allocatedController.activateTemperatureOverride().setNextWriteValue(false);

        } else {

            if (this.temperatureSetPointChannel().value().isDefined()) {
                if (this.allocatedController.activateTemperatureOverride().value().isDefined()) {
                    if (!this.allocatedController.activateTemperatureOverride().value().get()) {
                        this.allocatedController.activateTemperatureOverride().setNextValue(true);
                        this.allocatedController.setOverrideTemperature().setNextWriteValue(this.temperatureSetPointChannel().value().get());
                        this.heatNetworkReady.forEach(consumer -> consumer.setValue("true"));
                        lastTemperature = this.temperatureSetPointChannel().value().get();
                        return;

                    } else if (this.allocatedController.activateTemperatureOverride().value().get() && (this.temperatureSetPointChannel().value().get() != lastTemperature)) {
                        this.allocatedController.setOverrideTemperature().setNextWriteValue(temperatureSetPointChannel().value().get());
                        lastTemperature = this.temperatureSetPointChannel().value().get();
                        // }
                    }
                }
            }
        }

    }
}
