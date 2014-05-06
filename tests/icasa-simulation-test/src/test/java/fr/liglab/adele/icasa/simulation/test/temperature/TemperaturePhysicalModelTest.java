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
package fr.liglab.adele.icasa.simulation.test.temperature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.felix.ipojo.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulation.test.temperature.TemperatureDifferentThanCondition.SizeCondition;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.runner.test.utils.TestUtils;

/**
 * Tests for temperature physical model.
 *
 */
@RunWith(ChameleonRunner.class)
@Ignore
public class TemperaturePhysicalModelTest  {

    public static final String TEMPERATURE_VAR_NAME = "Temperature";

    @Inject
    public BundleContext context;

    @Inject
    public ContextManager contextMgr;

    @Inject
    private SimulationManager simulationMgr;


    @Before
    public void setUp() {
        createXMLSharedPreferencesInstance();
    }

    @After
    public void tearDown() {
        try {
            simulationMgr.resetContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createXMLSharedPreferencesInstance() {
        try {
            Collection<ServiceReference<Factory>> refs = context.getServiceReferences(Factory.class,
                    "(factory.name=org.ow2.chameleon.sharedprefs.XmlSharedPreferences)");
            if (refs.size() > 0) {
                ArrayList<ServiceReference<Factory>> arrayReferences = new ArrayList<ServiceReference<Factory>>(refs);
                ServiceReference<Factory> reference = arrayReferences.get(0);

                Factory preferencesFactory = context.getService(reference);

                Properties props = new Properties();
                props.put("instance.name", "XmlSharedPreferences-102");
                props.put("location", "preferences");
                try {
                    preferencesFactory.createComponentInstance(props);
                } catch (Exception e) {
                    fail("Cannot create the instance : " + e.getMessage());
                }
            }

        } catch (InvalidSyntaxException e) {
            fail("Cannot create the instance : " + e.getMessage());
        }
    }
    
    @Test
    public void tempVarExistsTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 5;
        Position position = new Position(10, 10);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        waitForTemperatureExists(zone);

        // cleanup
        contextMgr.removeZone(zoneId);
    }

    @Test
    public void defaultTempTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 5;
        Position position = new Position(10, 10);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        Double temp = (Double) zone.getVariableValue(TEMPERATURE_VAR_NAME);
        
        
        waitForTemperatureEqualsTo(zone, 293.15);

        // cleanup
        contextMgr.removeZone(zoneId);
    }

    @Test
    public void setTempTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 5;
        Position position = new Position(10, 10);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        double newTemp = 300.20;
        zone.setVariableValue(TEMPERATURE_VAR_NAME, newTemp);
        
        waitForTemperatureEqualsTo(zone, newTemp);

        // cleanup
        contextMgr.removeZone(zoneId);
    }

    @Test
    public void tempWithOneHeaterTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 100;
        Position position = new Position(100, 100);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        LocatedDevice locatedDevice = simulationMgr.createDevice("iCasa.Heater", "heater1", new Hashtable());
        
        Heater heater = (Heater) locatedDevice.getDeviceObject();
        
        heater.setPowerLevel(0.0d);

        double newTemp = 283.20;
        zone.setVariableValue(TEMPERATURE_VAR_NAME, newTemp);
        
        assertEquals(newTemp, zone.getVariableValue(TEMPERATURE_VAR_NAME));

        heater.setPowerLevel(1.0d);
        simulationMgr.moveDeviceIntoZone(locatedDevice.getSerialNumber(), zone.getId());
        
        waitForTemperatureGreaterThan(zone, newTemp, 0.2d);

        // cleanup
        contextMgr.removeZone(zoneId);
        simulationMgr.removeDevice(locatedDevice.getSerialNumber());
    }

    @Test
    public void tempWithOneCoolerTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 100;
        Position position = new Position(100, 100);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        LocatedDevice locatedDevice = simulationMgr.createDevice("iCasa.Cooler", "heater2", new Hashtable());
        
        Cooler cooler = (Cooler) locatedDevice.getDeviceObject();
        
        cooler.setPowerLevel(0.0d);

        double newTemp = 299.20;
        zone.setVariableValue(TEMPERATURE_VAR_NAME, newTemp);

        
        assertEquals(newTemp, zone.getVariableValue(TEMPERATURE_VAR_NAME));

        cooler.setPowerLevel(1.0d);
        simulationMgr.moveDeviceIntoZone(locatedDevice.getSerialNumber(), zone.getId());
        
        waitForTemperatureSmallerThan(zone, newTemp, 0.2d);

        // cleanup
        contextMgr.removeZone(zoneId);
        simulationMgr.removeDevice(locatedDevice.getSerialNumber());
    }
    
    @Test
    public void tempWithOneHeaterAndCoolerTest() {
        assertNotNull(contextMgr);

        String zoneId = "tempZone-0";
        int zoneScope = 100;
        Position position = new Position(100, 100);
        Zone zone = contextMgr.createZone(zoneId, position, zoneScope);

        LocatedDevice coolerLocatedDevice = simulationMgr.createDevice("iCasa.Cooler", "cooler1", new Hashtable());
        LocatedDevice heaterLocatedDevice = simulationMgr.createDevice("iCasa.Heater", "heater1", new Hashtable());
        
        Cooler cooler = (Cooler) coolerLocatedDevice.getDeviceObject();
        Heater heater = (Heater) heaterLocatedDevice.getDeviceObject();
        
        cooler.setPowerLevel(0.0d);

        double newTemp = 299.20;
        zone.setVariableValue(TEMPERATURE_VAR_NAME, newTemp);

        
        assertEquals(newTemp, zone.getVariableValue(TEMPERATURE_VAR_NAME));

        cooler.setPowerLevel(1.0d);
        heater.setPowerLevel(1.0d);
                       
        simulationMgr.moveDeviceIntoZone(coolerLocatedDevice.getSerialNumber(), zone.getId());
        simulationMgr.moveDeviceIntoZone(heaterLocatedDevice.getSerialNumber(), zone.getId());
        
        waitForTemperatureStability(zone, newTemp);

        // cleanup
        contextMgr.removeZone(zoneId);
        simulationMgr.removeDevice(coolerLocatedDevice.getSerialNumber());
    }    
    
    
    @Test // -> Functionality no implemented yet
    public void twoCloseZonesTest() {
        assertNotNull(contextMgr);
        Zone zone1 = contextMgr.createZone("tempZone-0", 100, 100, 100, 200, 200, 100);
        Zone zone2 = contextMgr.createZone("tempZone-1", 320, 100, 100, 200, 200, 100);
        
        double temperatureZone1 = 293.2d;
        double temperatureZone2 = 283.2d;
        
        zone1.setVariableValue(TEMPERATURE_VAR_NAME, temperatureZone1);
        zone2.setVariableValue(TEMPERATURE_VAR_NAME, temperatureZone2);
        
        waitForTemperatureGreaterThan(zone2, temperatureZone2, 0.001d);
        waitForTemperatureGreaterThan(zone1, temperatureZone2, 0.001d);

    }
    
    @Test
    public void twoNotClosesZonesTest() {
        assertNotNull(contextMgr);
        Zone zone1 = contextMgr.createZone("tempZone-0", 100, 100, 100, 200, 200, 100);
        Zone zone2 = contextMgr.createZone("tempZone-1", 520, 100, 100, 200, 200, 100);
        
        double temperatureZone1 = 293.2d;
        double temperatureZone2 = 283.2d;
        
        zone1.setVariableValue(TEMPERATURE_VAR_NAME, temperatureZone1);
        zone2.setVariableValue(TEMPERATURE_VAR_NAME, temperatureZone2);
               
        waitForTemperatureStability(zone2, temperatureZone2);
        waitForTemperatureStability(zone1, temperatureZone1);        
        
    }    
    
    private void waitForTemperatureExists(Zone zone) {
        TestUtils.testConditionWithTimeout(new TemperatureVarExistsCondition(zone), 5000, 30);
    }

    private void waitForTemperatureEqualsTo(Zone zone, double originalValue) {
        TestUtils.testConditionWithTimeout(new TemperatureEqualsToCondition(zone, originalValue), 5000, 30);
    }

    private void waitForTemperatureGreaterThan(Zone zone, double originalValue, double delta) {
        TestUtils.testConditionWithTimeout(new TemperatureDifferentThanCondition(zone, originalValue, delta, SizeCondition.BIGGER), 20000, 40);
    }

    private void waitForTemperatureSmallerThan(Zone zone, double originalValue, double delta) {
        TestUtils.testConditionWithTimeout(new TemperatureDifferentThanCondition(zone, originalValue, delta, SizeCondition.SMALLER), 20000, 40);
    }
 
    private void waitForTemperatureStability(Zone zone, double originalValue) {
        TestUtils.testConditionWithTimeout(new TemperatureStableCondition(zone, originalValue), 20000, 40);
    }
}
