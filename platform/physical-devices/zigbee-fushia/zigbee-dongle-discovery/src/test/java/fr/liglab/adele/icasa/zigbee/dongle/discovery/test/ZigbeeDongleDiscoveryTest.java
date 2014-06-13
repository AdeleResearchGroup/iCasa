package fr.liglab.adele.icasa.zigbee.dongle.discovery.test;

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


import junit.framework.Assert;
import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ZigbeeDongleDiscoveryTest extends ZigbeeDongleDiscoveryAbstractTest {


    @Test
    public void testDiscoveryCreateNewImport() throws BinderException {

        zigbeeDongleDiscovery.addingService(zigbeeReference);
        Set<ImportDeclaration> importDeclarationSet = zigbeeDongleDiscovery.getImportDeclarations();
        Assert.assertEquals(importDeclarationSet.size(),1);
        for(ImportDeclaration declaration : importDeclarationSet){
            Assert.assertEquals(declaration.getMetadata().get("zigbee.module.adress"),ZIGBEE_MODULE_ADRESS);
            Assert.assertEquals(declaration.getMetadata().get("zigbee.device.type.code"),ZIGBEE_TYPE_CODE.toString());
        }
    }

    @Test
    public void testZigbeeSerialPortListenerNotifyBatteryLevelChange() throws BinderException {

        zigbeeDongleDiscovery.addListener(zigbeeDeviceListener, ZIGBEE_MODULE_ADRESS); 
        zigbeeDongleDiscovery.notifyBatteryLevelChange(deviceInfo);

        verify(zigbeeDeviceListener,times(1)).deviceBatteryLevelChanged(ZIGBEE_BATTERY);

        zigbeeDongleDiscovery.removeListener(zigbeeDeviceListener);

        zigbeeDongleDiscovery.notifyBatteryLevelChange(deviceInfo);

        verify(zigbeeDeviceListener,times(1)).deviceBatteryLevelChanged(ZIGBEE_BATTERY);

    }

    @Test
    public void testZigbeeSerialPortListenerNotifyData() throws BinderException {

        zigbeeDongleDiscovery.addListener(zigbeeDeviceListener, ZIGBEE_MODULE_ADRESS);
        zigbeeDongleDiscovery.notifyDataChange(deviceInfo);

        verify(zigbeeDeviceListener,times(1)).deviceDataChanged(ZIGBEE_DATA);

        zigbeeDongleDiscovery.removeListener(zigbeeDeviceListener);

        zigbeeDongleDiscovery.notifyDataChange(deviceInfo);

        verify(zigbeeDeviceListener,times(1)).deviceDataChanged(ZIGBEE_DATA);

    }


}
