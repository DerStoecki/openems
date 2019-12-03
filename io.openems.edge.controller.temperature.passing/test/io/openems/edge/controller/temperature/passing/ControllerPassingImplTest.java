package io.openems.edge.controller.temperature.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.test.AbstractComponentConfig;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.controller.temperature.passing.api.ControllerPassingChannel;
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.junit.Before;

import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ControllerPassingImplTest {

    private static class MyConfig extends AbstractComponentConfig implements Config {

        private final String id;
        private final String alias;
        private final boolean enabled;
        private final String service_pid;
        private final String primary_Forward_Sensor;
        private final String primary_Rewind_Sensor;
        private final String secundary_Forward_Sensor;
        private final String secundary_Rewind_Sensor;
        private final String valve_Open_Relais;
        private final String valve_Close_Relais;
        private final String pump_id;
        private final int heating_Time;


        public MyConfig(String id, String alias, boolean enabled, String service_pid, String primary_Forward_Sensor, String primary_Rewind_Sensor, String secundary_Forward_Sensor, String secundary_Rewind_Sensor, String valve_Open_Relais,
                        String valve_Close_Relais, String pump_id, int heating_Time) {
            super(Config.class, id);
            this.id = id;
            this.alias = alias;
            this.enabled = enabled;
            this.service_pid = service_pid;
            this.primary_Forward_Sensor = primary_Forward_Sensor;
            this.primary_Rewind_Sensor = primary_Rewind_Sensor;
            this.secundary_Forward_Sensor = secundary_Forward_Sensor;
            this.secundary_Rewind_Sensor = secundary_Rewind_Sensor;
            this.valve_Open_Relais = valve_Open_Relais;
            this.valve_Close_Relais = valve_Close_Relais;
            this.pump_id = pump_id;
            this.heating_Time = heating_Time;
        }

        @Override
        public String service_pid() {
            return this.service_pid;
        }

        @Override
        public String primary_Forward_Sensor() {
            return this.primary_Forward_Sensor;
        }

        @Override
        public String primary_Rewind_Sensor() {
            return primary_Rewind_Sensor;
        }

        @Override
        public String secundary_Forward_Sensor() {
            return secundary_Forward_Sensor;
        }

        @Override
        public String secundary_Rewind_Sensor() {
            return secundary_Rewind_Sensor;
        }

        @Override
        public String valve_Open_Relais() {
            return valve_Open_Relais;
        }

        @Override
        public String valve_Close_Relais() {
            return valve_Close_Relais;
        }

        @Override
        public String pump_id() {
            return pump_id;
        }

        @Override
        public int heating_Time() {
            return this.heating_Time;
        }
    }

    private static ControllerPassingImpl passing;
    private static DummyComponentManager cpm;
    private ControllerPassingChannel ch;
    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secundaryForward;
    private Thermometer secundaryRewind;
    private ActuatorRelaisChannel pump;
    private ActuatorRelaisChannel valveOpen;
    private ActuatorRelaisChannel valveClose;
    private MyConfig config;

    @Before
    public void setUp() throws Exception {
        passing = new ControllerPassingImpl();
        cpm = new DummyComponentManager();
        passing.cpm = this.cpm;



        ch = mock(ControllerPassingChannel.class);

        config = new MyConfig("ControllerPassing0", "", true, "",
                "TemperatureSensor0", "TemperatureSensor1",
                "TemperatureSensor2", "TemperatureSensor3",
                "Relais0", "Relais1", "Relais2", 500);
    }

    /*	6 TestCases

     * 1. Everythings working fine
     * 2. Too Hot --> Everythings fine Except T SR + Buffer >0 T SF
     * 3. Valve Defect Exception
     * 4. Heat To Low
     * 5. Configuration Exception
     * 5.1 Configuration Exception Thermometer
     * 5.2 Configuration Exception Relais
     *
     */

    @Test
    public void testEverythingsFine() throws OpenemsError.OpenemsNamedException {
        passing.run();
        when(passing.getOnOff_PassingController().getNextValue().get()).thenReturn(true);
        when(passing.getMinTemperature().value().isDefined()).thenReturn(true);
        when(this.valveOpen.isCloser().value().get()).thenReturn(true);

    }

    @Test(expected = io.openems.common.exceptions.NoHeatNeededException.class)
    public void testTooHot() throws Exception {

    }

    @Test(expected = io.openems.common.exceptions.HeatToLowException.class)
    public void testHeatToLow() throws Exception {
    }

    @Test(expected = ConfigurationException.class)
    public void testThermometerException() throws Exception {

    }

    @Test(expected = ConfigurationException.class)
    public void testRelaisException() throws Exception {

    }

    @Test(expected = OpenemsError.OpenemsNamedException.class)
    public void testCpmException() throws Exception {

    }

}
