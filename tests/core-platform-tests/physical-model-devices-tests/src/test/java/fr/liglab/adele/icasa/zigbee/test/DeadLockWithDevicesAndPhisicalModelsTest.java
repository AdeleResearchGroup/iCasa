/*
 * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 * Group Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.icasa.zigbee.test;

import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import fr.liglab.adele.commons.test.utils.Condition;
import fr.liglab.adele.commons.test.utils.TestUtils;
import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * User: garciai@imag.fr
 * Date: 8/12/13
 * Time: 11:13 AM
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class DeadLockWithDevicesAndPhisicalModelsTest extends AbstractDistributionBaseTest {

    @Inject
    public BundleContext context;

    @Inject
    public SimulationManager icasa;

    @Inject
    public Preferences preference;


    @Before
    public void setUp() {
        waitForStability(context);
    }

    @After
    public void tearDown() {
        // do nothing
    }

    public static Option addSystemProperties() {
        return new DefaultCompositeOption(
                systemProperty( Constants.DISABLE_ACCESS_POLICY_PROPERTY).value("true"),
                systemProperty( "zigbee.driver.port").value( "/dev/ttyUSB0" )
        );
    }

    @org.ops4j.pax.exam.Configuration
    public Option[] configuration() {

        List<Option> lst = super.config();
        lst.add(addSystemProperties());
        Option conf[] = lst.toArray(new Option[0]);
        return conf;
    }

   @Test
    public void testDeadlockInBinaryLightDeviceAndIlluminance() throws IOException, DeploymentException {
        String device1 = "zigbee#1000";
        String device2 = "zigbee#1001";
        String device3 = "zigbee#1002";
        String device4 = "zigbee#1003";
        String zoneId = "zone1";
        int threadNumber = 2;
        int longTimes = 10000;
        int shortTimes = 2000;
        boolean turnOn = true;
        boolean running = true;
        long endTime;
        long executionTime = 6000;

        icasa.createZone(zoneId, 50, 50, 50, 600, 600, 600);
        List<BinaryLight> lights = new ArrayList();
        //create binary lights simulated devices and move them to zoneId
        lights.add((BinaryLight) getDeviceAndMoveToZone("iCasa.BinaryLight", device1, zoneId));
        lights.add((BinaryLight) getDeviceAndMoveToZone("iCasa.BinaryLight", device2, zoneId));
        lights.add((BinaryLight) getDeviceAndMoveToZone("iCasa.BinaryLight", device3, zoneId));
        lights.add((BinaryLight) getDeviceAndMoveToZone("iCasa.BinaryLight", device4, zoneId));

        List threads = new ArrayList();
        //create threads that will blink binary lights.
        for (int i = 0; i < threadNumber; i++) {
            threads.add(createBinaryLightBlinkThread(shortTimes, lights));
            try {
                Thread.sleep(i*2);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        endTime = System.currentTimeMillis() + executionTime;
        //this test is the bad and ugly thread. To block and notice deadlock.
        while(running){
            for(int i = 0; i < longTimes; i++){
                for(BinaryLight light: lights){
                    light.setPowerStatus(turnOn);
                }
                turnOn = !turnOn;
            }
            System.out.println("Execution: " + System.currentTimeMillis());
            if(System.currentTimeMillis() >= endTime){
                running = false;
            }
        }
        //If test exit the while block, it pass the test
        //and stop blink threads.
        for (int i = 0; i < threads.size(); i++) {
            BlinkLightsThread th = (BlinkLightsThread) threads.get(i);
            th.stop();
        }

    }

    @Test
    public void testDeadlockInHeaterDeviceAndTemperatureModel() throws IOException, DeploymentException {
        String device1 = "zigbee#1000";
        String device2 = "zigbee#1001";
        String device3 = "zigbee#1002";
        String device4 = "zigbee#1003";
        String zoneId = "zone1";
        int threadNumber = 2;
        int longTimes = 10000;
        int shortTimes = 2000;
        boolean running = true;
        long endTime;
        long executionTime = 6000;

        icasa.createZone(zoneId, 50, 50, 50, 6000, 6000, 6000);
        icasa.setZoneVariable(zoneId, "Temperature", new Double(285.0));
        List heaters = new ArrayList();
        //create heater simulated devices and move them to zoneId
        heaters.add(getDeviceAndMoveToZone("iCasa.Heater", device1, zoneId));
        heaters.add(getDeviceAndMoveToZone("iCasa.Heater", device1+"ez", zoneId));
        heaters.add(getDeviceAndMoveToZone("iCasa.Heater", device2, zoneId));
        heaters.add(getDeviceAndMoveToZone("iCasa.Cooler", device3, zoneId));
        heaters.add(getDeviceAndMoveToZone("iCasa.Cooler", device4, zoneId));

        List threads = new ArrayList();
        //create threads that will modify heater/cooler max power level.
        for (int i = 0; i < threadNumber; i++) {
            threads.add(createHeaterThread(shortTimes, heaters));
            try {
                Thread.sleep(i*2);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        endTime = System.currentTimeMillis() + executionTime;
        //this test is the bad and ugly thread. To block and notice deadlock.
        while(running){
            Random rnd = new Random();
            synchronized (this){
                for(int i = 0; i < longTimes; i++){
                    for(int j = 0; j < heaters.size(); j++){
                        GenericDevice obj = (GenericDevice) heaters.get(j);
                        if(obj instanceof Heater){
                            ((Heater)obj).setPowerLevel(rnd.nextDouble());
                        } else if (obj instanceof Cooler){
                            ((Cooler)obj).setPowerLevel(rnd.nextDouble());
                        }
                    }
                }
            }
            System.out.println("Zone temperature: " + icasa.getZoneVariableValue(zoneId, "Temperature"));
            if(System.currentTimeMillis() >= endTime){
                running = false;
            }
        }
        //If test exit the while block, it pass the test
        //and stop heater updater threads.
        for (int i = 0; i < threads.size(); i++) {
            ChangeHeatherValueThread th = (ChangeHeatherValueThread) threads.get(i);
            th.stop();
        }

    }


    /**
     * Create a new binary light device and move it to the given zone.
     * @param device the device Id
     * @param zoneId the zoneId where the device will be moved.
     * @return the binary light.
     */
    GenericDevice getDeviceAndMoveToZone(String type,String device, String zoneId){
        icasa.createDevice(type, device, new Hashtable());
        TestUtils.testConditionWithTimeout(new DevicePresentCondition(device), 10000, 1000);
        GenericDevice light = icasa.getDevice(device).getDeviceObject();
        icasa.moveDeviceIntoZone(device, zoneId);
        return light;
    }

    /**
     * Creates a thread that will have access to the BinaryLights objects.
     * @param times
     * @param lights
     * @return the blinking thread.
     */
    private BlinkLightsThread createBinaryLightBlinkThread(int times, List lights){
        BlinkLightsThread blink1 = new BlinkLightsThread(times, lights);
        new Thread(blink1).start();
        return blink1;
    }

    /**
     * This class will blink the binary lights.
     */
    private class BlinkLightsThread implements Runnable {

        private List<BinaryLight> lights;

        private volatile boolean running = true;

        private volatile boolean turnOn = true;

        private int times = 0;

        public BlinkLightsThread(int times, List lights){
            this.lights = lights;
            this.times = times;
        }

        public void stop(){
            running = false;
        }

        public void run() {
            while(running){
                for(int i = 0; i < times; i++){
                    for(BinaryLight light: lights){
                        light.setPowerStatus(turnOn);
                     }
                    turnOn = !turnOn;
                }
            }

        }
    }

    /**
     * Creates a thread that will have access to the BinaryLights objects.
     * @param times
     * @param lights
     * @return the blinking thread.
     */
    private ChangeHeatherValueThread createHeaterThread(int times, List lights){
        ChangeHeatherValueThread blink1 = new ChangeHeatherValueThread(times, lights);
        new Thread(blink1).start();
        return blink1;
    }

    /**
     * This class will change heater power level.
     */
    private class ChangeHeatherValueThread implements Runnable {

        private List<GenericDevice> heaters;

        private volatile boolean running = true;

        private volatile boolean turnOn = true;

        private int times = 0;

        public ChangeHeatherValueThread(int times, List heaters){
            this.heaters = heaters;
            this.times = times;
        }

        public void stop(){
            running = false;
        }

        public void run() {
            Random rnd = new Random();
            while(running){
                synchronized (this){
                    for(int i = 0; i < times; i++){
                        for(int j = 0; j < heaters.size(); j++){
                            GenericDevice obj = heaters.get(j);
                            if(obj instanceof Heater){
                                ((Heater)obj).setPowerLevel(rnd.nextDouble());
                            } else if (obj instanceof Cooler){
                                ((Cooler)obj).setPowerLevel(rnd.nextDouble());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Test if the given device is present in the platform.
     */
    class DevicePresentCondition implements Condition {
        private String  deviceId;

        public DevicePresentCondition(String deviceId) {
            this.deviceId = deviceId;
        }

        public boolean isChecked() {
            return (icasa.getDevice(deviceId) != null);
        }

        public String getDescription() {
            return "Expected " + deviceId + " device in iCasa";
        }
    }

}
