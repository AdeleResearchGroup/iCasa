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


import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.zigbee.dongle.discovery.ZigbeeDongleDiscovery;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.*;
import org.apache.felix.ipojo.ComponentInstance;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Dictionary;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ZigbeeDongleDiscoveryAbstractTest {

    protected  TypeCode ZIGBEE_TYPE_CODE= TypeCode.getTypeCodeByFriendlyName("TEMPERATURE_SENSOR");

    protected final String ZIGBEE_MODULE_ADRESS="2" ;

    protected final String ZIGBEE_DATA="1" ;

    protected final float ZIGBEE_BATTERY= (float)1.0 ;

    @Mock
    protected BundleContext context;

    @Mock
    Bundle bundle;

    @Mock
    ZigbeeDriver driver;

    @Mock
    ComponentInstance deviceInstance;

    @Mock
    PackageAdmin packageAdmin;

    @Mock
    ServiceReference serviceReference;

    @Mock
    ServiceReference packageAdminServiceReference;

    @Mock
    ServiceReference zigbeeReference;

    @Mock
    ServiceRegistration importDeclarationRef;

    @Mock
    DeviceInfo deviceInfo;

    @Mock
    Data data;

    @Mock
    ZigbeeDeviceListener zigbeeDeviceListener;

    ZigbeeDongleDiscovery zigbeeDongleDiscovery;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        zigbeeDongleDiscovery=spy(Reflection.constructor().withParameterTypes(BundleContext.class).in(ZigbeeDongleDiscovery.class).newInstance(context));
        setupInterceptors();
        field("driver").ofType(ZigbeeDriver.class).in(zigbeeDongleDiscovery).set(driver);
        method("start").in(zigbeeDongleDiscovery).invoke();
    }

    protected void setupInterceptors(){

        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(context.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(context.getBundle()).thenReturn(bundle);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);

        when(context.registerService(any(String.class),any(Object.class),any(Dictionary.class))).thenReturn(importDeclarationRef);


        when(deviceInfo.getModuleAddress()).thenReturn(ZIGBEE_MODULE_ADRESS);
        when(deviceInfo.getTypeCode()).thenReturn(ZIGBEE_TYPE_CODE);
        when(deviceInfo.getBatteryLevel()).thenReturn(ZIGBEE_BATTERY);
        when(deviceInfo.getDeviceData()).thenReturn(data);

        when(data.getData()).thenReturn(ZIGBEE_DATA);

        when(context.getService(zigbeeReference)).thenReturn(deviceInfo);

    }

}
