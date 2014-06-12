package fr.liglab.adele.icasa.zigbee.device.importer.test;

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


import fr.liglab.adele.icasa.zigbee.device.importer.ZigbeeDeviceImporter;
import junit.framework.Assert;
import org.apache.felix.ipojo.*;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.Dictionary;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ZigbeeDeviceImporterAbstractTest {

    @Mock
    protected BundleContext context;

    @Mock
    Bundle bundle;

    @Mock
    Factory deviceFactory;

    @Mock
    ComponentInstance deviceInstance;

    @Mock
    PackageAdmin packageAdmin;

    @Mock
    ServiceReference serviceReference;

    @Mock
    ServiceReference packageAdminServiceReference;

    ZigbeeDeviceImporter importer;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        importer=spy(Reflection.constructor().withParameterTypes().in(ZigbeeDeviceImporter.class).newInstance());
       field("thermometerFactory").ofType(Factory.class).in(importer).set(deviceFactory);
        setupInterceptors();
        method("start").in(importer).invoke();
    }

    protected void setupInterceptors(){

        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(context.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(context.getBundle()).thenReturn(bundle);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
       // when(deviceFactory.getName()).thenReturn("zigbeeThermometer");
      try {
            when(deviceFactory.createComponentInstance(any(Dictionary.class))).thenReturn(deviceInstance);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
          Assert.fail("An exception  shouldn't have been thrown.");
        } catch (MissingHandlerException e) {
          Assert.fail("An exception  shouldn't have been thrown.");
        } catch (ConfigurationException e) {
          Assert.fail("An exception  shouldn't have been thrown.");
        }
    }

}
