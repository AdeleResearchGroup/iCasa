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


import org.apache.felix.ipojo.ComponentInstance;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import static org.mockito.Mockito.when;

public abstract class ZigbeeDeviceFactoryAbstractTest {

    @Mock
    protected BundleContext context;

    @Mock
    Bundle bundle;

    @Mock
    ComponentInstance deviceInstance;

    @Mock
    PackageAdmin packageAdmin;

    @Mock
    ServiceReference serviceReference;

    @Mock
    ServiceReference packageAdminServiceReference;


    protected void setupGeneralInterceptors(){
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(context.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(context.getBundle()).thenReturn(bundle);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);

    }

}
