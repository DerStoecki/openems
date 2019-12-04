package io.openems.edge.controller.temperature.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.test.AbstractComponentConfig;
import io.openems.edge.common.test.AbstractComponentTest;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.controller.test.ControllerTest;
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.relais.api.test.DummyRelais;
import io.openems.edge.thermometer.api.Thermometer;
import io.openems.edge.thermometer.api.test.DummyThermometer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;

import static org.junit.Assert.fail;


public class ControllerPassingImplTest {

    private static class MyConfig extends AbstractComponentConfig implements Config {

        private final String id;
        private final String alias;
        private final boolean enabled;
        private final String service_pid;
        private String primary_Forward_Sensor;
        private final String primary_Rewind_Sensor;
        private final String secundary_Forward_Sensor;
        private final String secundary_Rewind_Sensor;
        private  String valve_Open_Relais;
        private final String valve_Close_Relais;
        private final String pump_id;
        private int heating_Time;
        private int valveTime;


        public MyConfig(String id, String alias, boolean enabled, String service_pid, String primary_Forward_Sensor, String primary_Rewind_Sensor, String secundary_Forward_Sensor, String secundary_Rewind_Sensor, String valve_Open_Relais,
                        String valve_Close_Relais, String pump_id, int heating_Time, int valveTime) {
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
            this.valveTime = valveTime;
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

        @Override
        public int valve_Time() {
            return this.valveTime;
        }
    }

    private static ControllerPassingImpl passing;
    private static DummyComponentManager cpm;
    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secundaryForward;
    private Thermometer secundaryRewind;
    private Thermometer testForFailR;
    private ActuatorRelaisChannel pump;
    private ActuatorRelaisChannel valveOpen;
    private ActuatorRelaisChannel valveClose;
    private ActuatorRelaisChannel testForFailT;
    private MyConfig config;
    private ChannelAddress pF;
    private ChannelAddress pR;
    private ChannelAddress sF;
    private ChannelAddress sR;
    private ChannelAddress p;
    private ChannelAddress pO;
    private ChannelAddress vO;
    private ChannelAddress vOC;
    private ChannelAddress vC;
    private ChannelAddress vCc;

    @Before
    public void setUp() throws Exception {
        passing = new ControllerPassingImpl();
        cpm = new DummyComponentManager();
        passing.cpm = cpm;

        config = new MyConfig("ControllerPassing0", "", true, "",
                "TemperatureSensor0", "TemperatureSensor1",
                "TemperatureSensor2", "TemperatureSensor3",
                "Relais0", "Relais1", "Relais2", 1, 1);

        primaryForward = new DummyThermometer(config.primary_Forward_Sensor());
        primaryRewind = new DummyThermometer(config.primary_Rewind_Sensor());
        secundaryForward = new DummyThermometer(config.secundary_Forward_Sensor());
        secundaryRewind = new DummyThermometer(config.secundary_Rewind_Sensor());

        valveOpen = new DummyRelais(config.valve_Open_Relais());
        valveClose = new DummyRelais(config.valve_Close_Relais());
        pump = new DummyRelais(config.pump_id());

        testForFailR = new DummyThermometer("TemperatureSensor4");
        testForFailT = new DummyRelais("Relais4");

        pF = new ChannelAddress(config.primary_Forward_Sensor(), "Temperature");
        pR = new ChannelAddress(config.primary_Rewind_Sensor(), "Temperature");
        sF = new ChannelAddress(config.secundary_Forward_Sensor(), "Temperature");
        sR = new ChannelAddress(config.secundary_Rewind_Sensor(), "Temperature");

        vO = new ChannelAddress(config.valve_Open_Relais(), "OnOff");
        vOC = new ChannelAddress(config.valve_Open_Relais(), "IsCloser");

        vC = new ChannelAddress(config.valve_Close_Relais(), "OnOff");
        vCc = new ChannelAddress(config.valve_Close_Relais(), "IsCloser");

        p = new ChannelAddress(config.pump_id(), "OnOff");
        pO = new ChannelAddress(config.pump_id(), "IsCloser");

        cpm.addComponent(primaryForward);
        cpm.addComponent(primaryRewind);
        cpm.addComponent(secundaryForward);
        cpm.addComponent(secundaryRewind);
        cpm.addComponent(valveOpen);
        cpm.addComponent(valveClose);
        cpm.addComponent(pump);
        cpm.addComponent(testForFailT);
        cpm.addComponent(testForFailR);


    }

    /*	7 TestCases

     * 1. Everythings working fine On
       2. Everythings working fine first On then off
     * 3. Too Hot --> Everythings fine Except T SR + Buffer >0 T SF
     * 4. Valve Defect Exception
     * 5. Heat To Low
     * 6. Configuration Exception
     * 6.1 Configuration Exception Thermometer
     * 6.2 Configuration Exception Relais
     *
     */

    @Test
    public void testEverythingsFineOn() {
        try {
            primaryRewind.getTemperature().setNextValue(200);
            passing.activate(null, config);
            passing.activate(null, config);
            passing.getMinTemperature().setNextValue(500);
            passing.getOnOff_PassingController().setNextValue(true);

            AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                    secundaryRewind, valveOpen, valveClose, pump, passing).next(
                    new TestCase()
                            .input(pF, 700)
                            .input(pR, 400)
                            .input(sF, 650)
                            .input(sR, 500)
                            .input(vO, false)
                            .input(vOC, true)
                            .input(vC, false)
                            .input(vCc, true)
                            .input(p, false)
                            .input(pO, true)
            );
            int count = 0;
            while (count < 2) {
                controllerTest.run();
                Thread.sleep(1000);
                count++;
            }
        }catch (Exception e) {
            fail();
        }
        passing.deactivate();
        //Bc of waiting time outputs can't be controlled, but as long no exception is thrown everythings fine
        Assert.assertTrue(true);


    }

    @Test
    public void testEverythingsFineOnRelaisOpener() {
        try {
            primaryRewind.getTemperature().setNextValue(200);
            passing.activate(null, config);
            passing.activate(null, config);
            passing.getMinTemperature().setNextValue(500);
            passing.getOnOff_PassingController().setNextValue(true);

            AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                    secundaryRewind, valveOpen, valveClose, pump, passing).next(
                    new TestCase()
                            .input(pF, 700)
                            .input(pR, 400)
                            .input(sF, 650)
                            .input(sR, 500)
                            .input(vO, false)
                            .input(vOC, false)
                            .input(vC, false)
                            .input(vCc, false)
                            .input(p, false)
                            .input(pO, false)
            );
            int count = 0;
            while (count < 2) {
                controllerTest.run();
                Thread.sleep(1000);
                count++;
            }
        }catch (Exception e) {
            fail();
        }
        passing.deactivate();
        //Bc of waiting time outputs can't be controlled, but as long no exception is thrown everythings fine
        Assert.assertTrue(true);


    }


    @Test
    public void testEverythingsFineOffWithWaitingTime() {
        try {
            config.valveTime = 10;
            config.heating_Time = 0;
            primaryRewind.getTemperature().setNextValue(200);
            passing.activate(null, config);
            passing.activate(null, config);
            passing.getMinTemperature().setNextValue(500);
            passing.getOnOff_PassingController().setNextValue(false);

            AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                    secundaryRewind, valveOpen, valveClose, pump, passing).next(
                    new TestCase()
                            .input(pF, 700)
                            .input(pR, 400)
                            .input(sF, 650)
                            .input(sR, 500)
                            .input(vO, false)
                            .input(vOC, true)
                            .input(vC, false)
                            .input(vCc, true)
                            .input(p, false)
                            .input(pO, true)
            );
            int count = 0;
            while (count < 20) {
                controllerTest.run();
                Thread.sleep(1000);
                count++;
            }
        }catch (Exception e) {
            fail();
        }
        passing.deactivate();
        //Bc of waiting time outputs can't be controlled, but as long no exception is thrown everythings fine
        Assert.assertTrue(true);


    }

    @Test
    public void testEverythingsFineOff(){

        try {
            primaryRewind.getTemperature().setNextValue(200);
            passing.activate(null, config);
            passing.activate(null, config);
            passing.getMinTemperature().setNextValue(500);
            passing.getOnOff_PassingController().setNextValue(true);

            AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                    secundaryRewind, valveOpen, valveClose, pump, passing).next(
                    new TestCase()
                            .input(pF, 700)
                            .input(pR, 400)
                            .input(sF, 650)
                            .input(sR, 500)
                            .input(vO, false)
                            .input(vOC, true)
                            .input(vC, false)
                            .input(vCc, true)
                            .input(p, false)
                            .input(pO, true)
            );
            controllerTest.run();
            passing.getOnOff_PassingController().setNextValue(false);
            int count = 0;
            while (count < 2) {
                controllerTest.run();
                Thread.sleep(1000);
                count++;
            }
        }catch (Exception e) {
            fail();
        }
        //Bc of waiting time outputs can't be controlled, but as long no exception is thrown everythings fine
        Assert.assertTrue(true);


    }

    @Test(expected = io.openems.common.exceptions.NoHeatNeededException.class)
    public void testTooHot() throws Exception {
            primaryRewind.getTemperature().setNextValue(200);
            passing.activate(null, config);
            passing.activate(null, config);
            passing.getMinTemperature().setNextValue(500);
            passing.getOnOff_PassingController().setNextValue(true);

            AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                    secundaryRewind, valveOpen, valveClose, pump, passing).next(
                    new TestCase()
                            .input(pF, 900)
                            .input(pR, 800)
                            .input(sF, 650)
                            .input(sR, 650)
                            .input(vO, false)
                            .input(vOC, true)
                            .input(vC, false)
                            .input(vCc, true)
                            .input(p, false)
                            .input(pO, true)
            );
            int count = 0;
            while (count < 6) {
                controllerTest.run();
                Thread.sleep(1000);
                count++;
            }

    }

    @Test(expected = io.openems.common.exceptions.NoHeatNeededException.class)
    public void testTooHotPumpOpener() throws Exception {
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 900)
                        .input(pR, 800)
                        .input(sF, 650)
                        .input(sR, 650)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, false)
        );
        int count = 0;
        while (count < 6) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }

    }

    @Test(expected = io.openems.common.exceptions.HeatToLowException.class)
    public void testHeatToLow() throws Exception {
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 400)
                        .input(pR, 400)
                        .input(sF, 200)
                        .input(sR, 200)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, true)
        );
        int count = 0;
        while (count < 8) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }
    }

    @Test(expected = io.openems.common.exceptions.ValveDefectException.class)
    public void testValveDefect() throws Exception {
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 400)
                        .input(pR, 200)
                        .input(sF, 200)
                        .input(sR, 200)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, true)
        );
        int count = 0;
        while (count < 8) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }

    }

    @Test(expected = ConfigurationException.class)
    public void testThermometerException() throws Exception {
        config.primary_Forward_Sensor = "Relais4";
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 400)
                        .input(pR, 400)
                        .input(sF, 200)
                        .input(sR, 200)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, true)
        );
        int count = 0;
        while (count < 8) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }
    }

    @Test(expected = ConfigurationException.class)
    public void testRelaisException() throws Exception {
        config.valve_Open_Relais = "TemperatureSensor4";
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 400)
                        .input(pR, 400)
                        .input(sF, 200)
                        .input(sR, 200)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, true)
        );
        int count = 0;
        while (count < 8) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }
    }

    @Test(expected = OpenemsError.OpenemsNamedException.class)
    public void testComponentManager() throws Exception {
        config.valve_Open_Relais = "Relais6";
        primaryRewind.getTemperature().setNextValue(200);
        passing.activate(null, config);
        passing.activate(null, config);
        passing.getMinTemperature().setNextValue(500);
        passing.getOnOff_PassingController().setNextValue(true);

        AbstractComponentTest controllerTest = new ControllerTest(passing, cpm, primaryForward, primaryRewind, secundaryForward,
                secundaryRewind, valveOpen, valveClose, pump, passing).next(
                new TestCase()
                        .input(pF, 400)
                        .input(pR, 400)
                        .input(sF, 200)
                        .input(sR, 200)
                        .input(vO, false)
                        .input(vOC, true)
                        .input(vC, false)
                        .input(vCc, true)
                        .input(p, false)
                        .input(pO, true)
        );
        int count = 0;
        while (count < 8) {
            controllerTest.run();
            Thread.sleep(1000);
            count++;
        }
    }


}
