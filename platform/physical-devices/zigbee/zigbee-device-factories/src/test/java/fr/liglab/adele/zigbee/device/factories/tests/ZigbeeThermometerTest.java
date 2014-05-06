/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.zigbee.device.factories.tests;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.liglab.adele.zigbee.device.factories.ZigbeeThermometer;

/**
 * Test class for zigbee temperature sensor.
 *
 * 
 */
public class ZigbeeThermometerTest {

	private ZigbeeThermometer sensor = new ZigbeeThermometer();

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testPositiveTemperatureConversion() {
		String result = sensor.computeTemperature("13>0");
		Assert.assertEquals("19.88", result);

		String result2 = sensor.computeTemperature("13?0");
		Assert.assertEquals("19.94", result2);

	}

	@Test
	public void testNegativeTemperatureConversion() {
		String result = sensor.computeTemperature(">6?0");
		Assert.assertEquals("-25.06", result);

		String result2 = sensor.computeTemperature(">710");
		Assert.assertEquals("-24.94", result2);
	}

	@Test
	public void testInvalidTemperatureData() {
		String result = sensor.computeTemperature(">6?");
		Assert.assertEquals(null, result);

	}
}
