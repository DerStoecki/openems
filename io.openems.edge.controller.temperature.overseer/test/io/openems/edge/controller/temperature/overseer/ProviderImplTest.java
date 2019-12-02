package io.openems.edge.controller.temperature.overseer;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.openems.edge.controller.temperature.overseer.ProviderImpl;

/*
 * Example JUNit test case
 *
 */

public class ProviderImplTest {

	/*
	 * Example test method
	 */

	@Test
	public void simple() {
		ProviderImpl impl = new ProviderImpl();
		assertNotNull(impl);
	}

}
