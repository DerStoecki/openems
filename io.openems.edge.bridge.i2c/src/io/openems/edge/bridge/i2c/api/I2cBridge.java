package io.openems.edge.bridge.i2c.api;

import io.openems.edge.bridge.i2c.task.I2cPcaTask;
import io.openems.edge.bridge.i2c.task.I2cTask;
import io.openems.edge.consolinno.leaflet.mainmodule.api.PcaMainModuleProvider;
import io.openems.edge.i2c.mcp.api.Mcp;
import io.openems.edge.pwm.module.api.PcaGpioProvider;
import io.openems.common.exceptions.OpenemsException;

import java.util.List;

public interface I2cBridge {
    /**
     * adds the Mcp of a Relays or Chp Module.
     *
     * @param mcp the mcp provided by the Dac or Relays module.
     */
    void addMcp(Mcp mcp);

    List<Mcp> getMcpList();

    void removeMcp(Mcp mcp);

    /**
     * Adds the device of the Pwm Module.
     *
     * @param id   Unique id of the Pwm Module.
     * @param gpio the created gpioProvider e.g.Pca9685GpioProvider.
     */
    void addGpioDevice(String id, PcaGpioProvider gpio);

    void removeGpioDevice(String id);

    /**
     * Adds a I2c Task == Pwm Task.
     *
     * @param id      Unique id of the Pwm Device.
     * @param i2cTask The Task instantiated by the Pwm Device.
     * @throws OpenemsException If the id is already in the Map.
     */

    void addI2cTask(String id, I2cTask i2cTask) throws OpenemsException;

    void removeI2cTask(String id);

    void addMainModulePca(String id, PcaMainModuleProvider pcaMain);

    void removeMainModulePca(String id);

    void addMainModulePcaTask(String id, I2cPcaTask pca);

    void removeMainModulePcaTask(String id);



}
