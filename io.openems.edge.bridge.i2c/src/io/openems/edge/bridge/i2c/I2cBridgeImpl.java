package io.openems.edge.bridge.i2c;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import io.openems.edge.bridge.i2c.api.I2cBridge;
import io.openems.edge.i2c.mcp.api.Mcp;
import io.openems.edge.i2c.mcp.api.McpChannelRegister;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.i2c.task.I2cTask;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.pwm.module.api.PcaGpioProvider;


@Designate(ocd = Config.class, factory = true)
@Component(name = "I2CBridge",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE)
public class I2cBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, I2cBridge, EventHandler {

    private final I2cWorker worker = new I2cWorker();
    private List<Mcp> mcpList = new ArrayList<>();
    //String --> PwmModule Id
    private Map<String, PcaGpioProvider> gpioMap = new ConcurrentHashMap<>();
    //String --> PwmDevice Id
    private Map<String, I2cTask> tasks = new ConcurrentHashMap<>();

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            this.worker.activate(super.id());
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.worker.deactivate();
        //Relays will be default (depending on opener and closer) and Chp will be 0
        mcpList.forEach(McpChannelRegister::deactivate);

        // should always be empty already but to make sure..
        this.gpioMap.keySet().forEach(this::removeGpioDevice);

    }

    public I2cBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Override
    public void addMcp(Mcp mcp) {
        if (mcp != null) {
            this.mcpList.add(mcp);
        }
    }

    @Override
    public List<Mcp> getMcpList() {
        return this.mcpList;
    }

    @Override
    public void removeMcp(Mcp toRemove) {
        this.mcpList.removeIf(value -> value.getParentCircuitBoard().equals(toRemove.getParentCircuitBoard()));
    }

    @Override
    public void addGpioDevice(String id, PcaGpioProvider gpio) {
        this.gpioMap.put(id, gpio);
    }


    /**
     * This method removes the tasks and pwm module linked to the given param.
     *
     * @param id The Unique Id of the Pwm Module
     *           If the pwm module is deactivated, all it's tasks needs to be removed too.
     *           In the end the pwm module will be removed from the gpioMap and be set off.
     */
    @Override
    public void removeGpioDevice(String id) {
        this.tasks.values().stream().filter(task -> (task.getPwmModuleId().equals(id))).forEach(value -> {
            removeI2cTask(value.getDeviceId());
        });
        this.gpioMap.remove(id);
    }

    /**
     * Adds an I2c task (GpioDevice) to the Map --> handled by the worker.
     *
     * @param id      Unique Id of the GpioDevice
     * @param i2cTask i2cTask by the GpioDevice.
     */
    @Override
    public void addI2cTask(String id, I2cTask i2cTask) throws OpenemsException {
        if (!this.tasks.containsKey(id)) {
            this.tasks.put(id, i2cTask);
        } else {
            throw new OpenemsException("Attention, id " + id + "is already Key, activate again with a new name.");
        }
    }

    @Override
    public void removeI2cTask(String id) {
        shutdown(id);
        this.tasks.remove(id);
    }

    /**
     * If an I2c Task will be removed, it'll be set off.
     *
     * @param id unique id of the I2cTask.
     */
    private void shutdown(String id) {
        PcaGpioProvider gpio = gpioMap.get(tasks.get(id).getPwmModuleId());
        if (gpio != null) {
            if (tasks.get(id).isInverse()) {
                gpio.setAlwaysOn(tasks.get(id).getPinPosition());
            } else {
                gpio.setAlwaysOff(tasks.get(id).getPinPosition());
            }
        }

    }

    private Map<String, PcaGpioProvider> getGpioMap() {
        return gpioMap;
    }

    private class I2cWorker extends AbstractCycleWorker {
        @Override
        public void activate(String name) {
            super.activate(name);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }

        /**
         * The I2c shifts all it's containing mcps. e.g. all relays OnOff values will be set.
         * Furthermore every I2c Task (Pwm tasks) will be handled. So every digit-value will be calculated
         * and written in the device.
         */
        @Override
        public void forever() throws Throwable {
            for (Mcp mcp : getMcpList()) {
                mcp.shift();
            }
            tasks.values().forEach(task -> {
                Optional.ofNullable(getGpioMap().get(task.getPwmModuleId())).ifPresent(gpio -> {

                    int digit = task.calculateDigit(4096);

                    if (digit <= 0) {
                        if (task.isInverse()) {
                            gpio.setAlwaysOn(task.getPinPosition());
                        } else {
                            gpio.setAlwaysOff(task.getPinPosition());
                        }
                    } else if (digit >= 4095) {
                        if (task.isInverse()) {
                            gpio.setAlwaysOff(task.getPinPosition());
                        } else {
                            gpio.setAlwaysOn(task.getPinPosition());
                        }
                    } else {
                        gpio.setPwm(task.getPinPosition(), digit);
                    }
                });
            });
        }
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE)) {
            this.worker.triggerNextRun();
        }
    }


}
