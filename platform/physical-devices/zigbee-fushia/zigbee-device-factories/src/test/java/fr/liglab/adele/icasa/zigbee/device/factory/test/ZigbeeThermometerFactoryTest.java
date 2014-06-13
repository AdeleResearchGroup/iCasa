package fr.liglab.adele.icasa.zigbee.device.factory.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.zigbee.device.factory.ZigbeeBinaryLight;
import fr.liglab.adele.icasa.zigbee.device.factory.ZigbeeDevice;
import fr.liglab.adele.icasa.zigbee.device.factory.ZigbeeThermometer;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeModuleDriver;
import junit.framework.Assert;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Mockito.spy;

public class ZigbeeThermometerFactoryTest extends ZigbeeDeviceFactoryAbstractTest{

    @Mock
    ZigbeeModuleDriver zigbeeDriver;

    @Mock
    Data data;

    private final String ZIGBEE_SERIAL_NUMBER= "zigbee#2";

    private final String ZIGBEE_MODULE_ADRESS="2" ;

    private final String ZIGBEE_NEW_DATA="1" ;

    private final float ZIGBEE_BATTERY_LEVEL = (float)1.0 ;

    ZigbeeThermometer zigbeeThermometer;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        zigbeeThermometer=spy(Reflection.constructor().withParameterTypes().in(ZigbeeThermometer.class).newInstance());
        field("moduleAddress").ofType(String.class).in(zigbeeThermometer).set(ZIGBEE_MODULE_ADRESS);
        field("serialNumber").ofType(String.class).in(zigbeeThermometer).set(ZIGBEE_SERIAL_NUMBER);
        field("driver").ofType(ZigbeeModuleDriver.class).in(zigbeeThermometer).set(zigbeeDriver);
        setupGeneralInterceptors();
        setupSpecificInterceptors();
        method("start").in(zigbeeThermometer).invoke();
    }

    public void setupSpecificInterceptors(){
        // when(zigbeeDriver.addListener(any(ZigbeeSerialPortListener.class))).thenReturn(bundle);
    }

    @Test
    public void testGenericDeviceGetName() {
        Assert.assertEquals(zigbeeThermometer.getSerialNumber(),ZIGBEE_SERIAL_NUMBER);
    }

    @Test
    public void testZigbeeDeviceListenerBatteryLevelChange() {
        zigbeeThermometer.deviceBatteryLevelChanged(ZIGBEE_BATTERY_LEVEL);
        Float status = (Float) zigbeeThermometer.getPropertyValue(ZigbeeDevice.BATTERY_LEVEL);

        Assert.assertEquals(ZIGBEE_BATTERY_LEVEL, status.floatValue());
    }

    @Test
    public void testPositiveTemperatureConversion() {
        String result = zigbeeThermometer.computeTemperature("13>0");
        Assert.assertEquals("19.88", result);

        String result2 = zigbeeThermometer.computeTemperature("13?0");
        Assert.assertEquals("19.94", result2);

    }

    @Test
    public void testNegativeTemperatureConversion() {
        String result = zigbeeThermometer.computeTemperature(">6?0");
        Assert.assertEquals("-25.06", result);

        String result2 = zigbeeThermometer.computeTemperature(">710");
        Assert.assertEquals("-24.94", result2);
    }

    @Test
    public void testInvalidTemperatureData() {
        String result = zigbeeThermometer.computeTemperature(">6?");
        Assert.assertEquals(null, result);

    }

}
