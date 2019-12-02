package io.openems.edge.controller.temperature.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.relais.api.RelaisActuator;
import io.openems.edge.thermometer.api.Thermometer;

import org.junit.Before;
import org.junit.Test;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OsgiContextExtension.class)
public class ControllerPassingImplTest {

    private ControllerPassingImpl passing;
    private Thermometer primaryForward;
	private Thermometer primaryRewind;
	private Thermometer secundaryForward;
	private Thermometer secundaryRewind;
    private RelaisActuator pump;
    private RelaisActuator valveOpen;
    private RelaisActuator valveClose;
    private ComponentManager cpm;
    private ComponentContext context;
    private Config configuration;

	@Before
	public void setUp() throws Exception {
		passing = new ControllerPassingImpl();

		primaryForward = mock(Thermometer.class);
		primaryRewind = mock(Thermometer.class);
		secundaryForward = mock(Thermometer.class);
		secundaryRewind = mock(Thermometer.class);

		pump = mock(RelaisActuator.class);
		valveOpen = mock(RelaisActuator.class);
		valveClose = mock(RelaisActuator.class);

		cpm = mock(ComponentManager.class);
		context = mock(ComponentContext.class);
		configuration = mock(Config.class);
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
    	ControllerPassingImpl test = new ControllerPassingImpl();
    	test.activate(context,  configuration);
    	when((configuration).id()).thenReturn("ControllerPassing0");
    	when(configuration.alias()).thenReturn("TestController");
    	when(configuration.enabled()).thenReturn(true);
    	when(configuration.heating_Time()).thenReturn(300);
		when(configuration.primary_Forward_Sensor()).thenReturn("TemperatureSensor0");
		when(configuration.primary_Rewind_Sensor()).thenReturn("TemperatureSensor1");
		when(configuration.secundary_Forward_Sensor()).thenReturn("TemperatureSensor2");
		when(configuration.secundary_Rewind_Sensor()).thenReturn("TemperatureSensor3");
		when(configuration.pump_id()).thenReturn("Relais0");
		when(configuration.valve_Open_Relais()).thenReturn("Relais1");
		when(configuration.valve_Close_Relais()).thenReturn("Relais2");

		when(cpm.getComponent("TemperatureSensor0")).thenReturn(primaryForward);
		when(cpm.getComponent("TemperatureSensor1")).thenReturn(primaryRewind);
		when(cpm.getComponent("TemperatureSensor2")).thenReturn(secundaryForward);
		when(cpm.getComponent("TemperatureSensor3")).thenReturn(secundaryRewind);
		when(cpm.getComponent("Relais0")).thenReturn(pump);
		when(cpm.getComponent("Relais1")).thenReturn(valveOpen);
		when(cpm.getComponent("Relais2")).thenReturn(valveClose);
		when(primaryRewind.getTemperature().getNextValue().get()).thenReturn(200);
		test.run();
		when(test.getOnOff_PassingController().getNextValue().get()).thenReturn(true);
		when(this.valveOpen.isCloser()).thenReturn(true);
	}

    @Test(expected = io.openems.common.exceptions.NoHeatNeededException.class)
	public void testTooHot() throws Exception{

	}

	@Test(expected = io.openems.common.exceptions.HeatToLowException.class)
	public void testHeatToLow() throws Exception{}

	@Test(expected = ConfigurationException.class)
	public void testThermometerException() throws Exception{

	}
	@Test(expected = ConfigurationException.class)
	public void testRelaisException() throws Exception{

	}

	@Test(expected = OpenemsError.OpenemsNamedException.class)
	public void testCpmException() throws Exception{

	}

}
