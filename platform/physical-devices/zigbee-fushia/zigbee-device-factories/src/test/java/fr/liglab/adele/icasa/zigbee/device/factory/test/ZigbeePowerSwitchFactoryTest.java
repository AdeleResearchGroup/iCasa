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


import fr.liglab.adele.icasa.device.power.PowerSwitch;
import fr.liglab.adele.icasa.zigbee.device.factory.ZigbeeDevice;
import fr.liglab.adele.icasa.zigbee.device.factory.ZigbeePowerSwitch;
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

public class ZigbeePowerSwitchFactoryTest extends ZigbeeDeviceFactoryAbstractTest{

    @Mock
    ZigbeeModuleDriver zigbeeDriver;

    @Mock
    Data data;

    private final String ZIGBEE_SERIAL_NUMBER= "zigbee#2";

    private final String ZIGBEE_MODULE_ADRESS="2" ;

    private final String ZIGBEE_NEW_DATA="1" ;

    private final float ZIGBEE_BATTERY_LEVEL = (float)1.0 ;

    ZigbeePowerSwitch zigbeePowerSwitch;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        zigbeePowerSwitch=spy(Reflection.constructor().withParameterTypes().in(ZigbeePowerSwitch.class).newInstance());
        field("moduleAddress").ofType(String.class).in(zigbeePowerSwitch).set(ZIGBEE_MODULE_ADRESS);
        field("serialNumber").ofType(String.class).in(zigbeePowerSwitch).set(ZIGBEE_SERIAL_NUMBER);
        field("driver").ofType(ZigbeeModuleDriver.class).in(zigbeePowerSwitch).set(zigbeeDriver);
        setupGeneralInterceptors();
        setupSpecificInterceptors();
        method("start").in(zigbeePowerSwitch).invoke();
    }

    public void setupSpecificInterceptors(){
    }

    @Test
    public void testGenericDeviceGetName() {
        Assert.assertEquals(zigbeePowerSwitch.getSerialNumber(),ZIGBEE_SERIAL_NUMBER);
    }

    @Test
    public void testZigbeeDeviceListenerDeviceDataChange() {
        zigbeePowerSwitch.deviceDataChanged(ZIGBEE_NEW_DATA);
        Boolean status = (Boolean) zigbeePowerSwitch.getPropertyValue(PowerSwitch.POWER_SWITCH_CURRENT_STATUS);

        Assert.assertEquals(true, status.booleanValue());

    }

    @Test
    public void testZigbeeDeviceListenerBatteryLevelChange() {
        zigbeePowerSwitch.deviceBatteryLevelChanged(ZIGBEE_BATTERY_LEVEL);
        Float status = (Float) zigbeePowerSwitch.getPropertyValue(ZigbeeDevice.BATTERY_LEVEL);

        Assert.assertEquals(ZIGBEE_BATTERY_LEVEL, status.floatValue());
    }



}
