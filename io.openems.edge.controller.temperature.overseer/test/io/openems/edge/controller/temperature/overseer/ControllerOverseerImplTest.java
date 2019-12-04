package io.openems.edge.controller.temperature.overseer;

import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.test.AbstractComponentConfig;
import io.openems.edge.common.test.AbstractComponentTest;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.controller.temperature.passing.api.test.DummyControllerPassing;
import io.openems.edge.controller.test.ControllerTest;
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.relais.api.test.DummyRelais;
import io.openems.edge.thermometer.api.Thermometer;
import io.openems.edge.thermometer.api.test.DummyThermometer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class ControllerOverseerImplTest {

    private static class MyConfig extends AbstractComponentConfig implements Config {

        private final String id;
        private final String alias;
        private final boolean enabled;
        private final String service_pid;
        private final String allocated_Passing_Controller;
        private final int minTemperature;
        private final int toleratedTemperatureRange;
        private final String allocatedTemperatureSensor;

        public MyConfig(String id, String alias, boolean enabled, String service_pid, String allocated_Passing_Controller, int minTemperature, int toleratedTemperatureRange, String allocatedTemperatureSensor) {
            super(Config.class, id);
            this.id = id;
            this.alias = alias;
            this.enabled = enabled;
            this.service_pid = service_pid;
            this.allocated_Passing_Controller = allocated_Passing_Controller;
            this.minTemperature = minTemperature;
            this.toleratedTemperatureRange = toleratedTemperatureRange;
            this.allocatedTemperatureSensor = allocatedTemperatureSensor;
        }

        @Override
        public String service_pid() {
            return this.service_pid;
        }

        @Override
        public String allocated_Passing_Controller() {
            return this.allocated_Passing_Controller;
        }

        @Override
        public int min_Temperature() {
            return this.minTemperature;
        }

        @Override
        public int tolerated_Temperature_Range() {
            return this.toleratedTemperatureRange;
        }

        @Override
        public String allocated_Temperature_Sensor() {
            return this.allocatedTemperatureSensor;
        }
    }

    private static ControllerOverseerImpl overseer;
    private static DummyComponentManager cpm;
    private Thermometer allocatedThermometer;
    private ChannelAddress thermometer;
    private DummyControllerPassing passing;
    private ChannelAddress passingOnOff;
    private ChannelAddress passingMinTemp;
    private ActuatorRelaisChannel allocatedRelais;
    private ChannelAddress relaisOnOff;
    private ChannelAddress relaisIsCloser;
    private MyConfig config;


    @Before
    public void setUp() throws Exception {

        overseer = new ControllerOverseerImpl();
        cpm = new DummyComponentManager();
        overseer.cpm = cpm;

        config = new MyConfig("ControllerOverseer0", "", true, "", "ControllerPassing0",
                400, 20, "TemperatureSensor8");
        allocatedThermometer = new DummyThermometer(config.allocated_Temperature_Sensor());
        passing = new DummyControllerPassing(config.allocated_Passing_Controller());
        allocatedRelais = new DummyRelais("Relais1");
        passingOnOff = new ChannelAddress(config.allocated_Passing_Controller(), "OnOff");
        passingMinTemp = new ChannelAddress(config.allocated_Passing_Controller(), "MinTemperature");
        relaisOnOff = new ChannelAddress("Relais1", "OnOff");
        relaisIsCloser = new ChannelAddress("Relais1", "IsCloser");
        thermometer = new ChannelAddress(config.allocated_Temperature_Sensor(), "Temperature");


        cpm.addComponent(allocatedThermometer);
        cpm.addComponent(passing);
        cpm.addComponent(allocatedRelais);
        passing.valveTime().setNextValue(5);
    }

    @Test
    public void simple() {
        ControllerOverseerImpl impl = new ControllerOverseerImpl();
        assertNotNull(impl);
    }


    @Test
    public void testHeatingNotReachedNoError() {
        try {
            overseer.activate(null, config);
            overseer.activate(null, config);
            overseer.passing.getMinTemperature().setNextValue(config.minTemperature);
            overseer.waitingTimeValveToClose = passing.valveTime().getNextValue().get();

            AbstractComponentTest controllerTest = new ControllerTest(overseer, cpm, allocatedThermometer, passing, allocatedRelais, overseer)
                    .next(
                            new TestCase()
                                    .input(passingOnOff, false)
                                    .input(passingMinTemp, 400)
                                    .input(relaisOnOff, false)
                                    .input(relaisIsCloser, true)
                                    .input(thermometer, 100)
                                    .output(passingOnOff, true)
                    );

        } catch (Exception e) {
            fail();
        }

        assertTrue(true);
    }

    @Test
    public void testHeatingNotReachedError() {

    }

    @Test
    public void testHeatingReachedNoError() {


    }

    @Test
    public void testHeatingReachedError() {
    }

    @Test
    public void testHeatingNotReachedNull() {

    }


}
