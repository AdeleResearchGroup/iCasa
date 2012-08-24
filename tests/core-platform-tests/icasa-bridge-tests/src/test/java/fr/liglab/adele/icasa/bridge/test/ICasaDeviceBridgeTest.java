/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.bridge.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static fr.liglab.adele.m2mappbuilder.test.common.ICasaHelper.waitForIt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.newBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import fr.liglab.adele.m2mappbuilder.application.Application;
import fr.liglab.adele.m2mappbuilder.application.ApplicationManager;
import fr.liglab.adele.m2mappbuilder.common.StateVariable;
import fr.liglab.adele.m2mappbuilder.common.StateVariableListener;
import fr.liglab.adele.m2mappbuilder.device.manager.ApplicationDevice;
import fr.liglab.adele.m2mappbuilder.device.manager.ApplicationDeviceProxy;
import fr.liglab.adele.m2mappbuilder.device.manager.AvailableDevice;
import fr.liglab.adele.m2mappbuilder.device.manager.DependRegistration;
import fr.liglab.adele.m2mappbuilder.device.manager.Device;
import fr.liglab.adele.m2mappbuilder.device.manager.DeviceDependencies;
import fr.liglab.adele.m2mappbuilder.device.manager.DeviceManager;
import fr.liglab.adele.m2mappbuilder.device.manager.Fault;
import fr.liglab.adele.m2mappbuilder.device.manager.GlobalDeviceManager;
import fr.liglab.adele.m2mappbuilder.device.manager.KnownDevice;
import fr.liglab.adele.m2mappbuilder.device.manager.ProvidedDevice;

import fr.liglab.adele.icasa.bridge.test.app1.App1;
import fr.liglab.adele.icasa.bridge.test.app1.App1Activator;
import fr.liglab.adele.icasa.bridge.test.app2.App2;
import fr.liglab.adele.icasa.bridge.test.app2.App2Activator;
import fr.liglab.adele.icasa.device.GenericDevice;

import fr.liglab.adele.m2mappbuilder.test.common.ICasaAbstractTest;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.PaxRunnerOptions;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 * Integration test for the device manager component.
 * @author Thomas Leveque
 */
@RunWith(JUnit4TestRunner.class)
public class ICasaDeviceBridgeTest extends ICasaAbstractTest {
    

    private static final String STATE_GENERIC_PROP_NAME = "State";
	private static final String APP1_ID = "app1";
	private static final String APP2_ID = "app2";

	@Before
    @Override
    public void setUp() {
    	super.setUp();
    }
    
    @Configuration
	public static Option[] deployBundles() {
		return CoreOptions.options(
				
				CoreOptions.provision(
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.handler.extender").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.composite").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.api").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.http.jetty").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.gogo.command").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.gogo.shell").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.arch.gogo").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.webconsole").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.webconsole").versionAsInProject(),
                mavenBundle().groupId("commons-logging").artifactId("org.ow2.chameleon.commons.logging").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("test.common").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("common").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("application.api").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("application.impl").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("device.manager.api").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("device.manager.impl").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.m2mappbuilder").artifactId("icasa.device.model.bridge").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.icasa").artifactId("device.api").versionAsInProject()
				),
				
				CoreOptions.provision(
						newBundle().add(ICasaDeviceBridgeTest.class)
						.set(Constants.IMPORT_PACKAGE, "org.osgi.service.http,org.osgi.util.tracker,org.osgi.framework,org.junit," + 
						"fr.liglab.adele.m2mappbuilder.test.common,fr.liglab.adele.m2mappbuilder.application,fr.liglab.adele.m2mappbuilder.device.manager," +
								"fr.liglab.adele.icasa.device")
						.set(Constants.EXPORT_PACKAGE, "fr.liglab.adele.icasa.bridge.test")
						.set(Constants.BUNDLE_SYMBOLICNAME, ICasaDeviceBridgeTest.class.getSimpleName()).build(),
						
						newBundle().add(App1.class).add(App1Activator.class)
						.set(Constants.BUNDLE_ACTIVATOR, App1Activator.class.getName())
						.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
						.set(Constants.EXPORT_PACKAGE, "fr.liglab.adele.icasa.bridge.test.app1")
						.set(Constants.BUNDLE_SYMBOLICNAME, App1.class.getSimpleName())
						.set(Application.APP_ID_BUNDLE_HEADER, "app1")
						.set(Application.APP_VERSION_BUNDLE_HEADER, "1.1")
						.build(),
						
						newBundle().add(App2.class).add(App2Activator.class)
						.set(Constants.BUNDLE_ACTIVATOR, App2Activator.class.getName())
						.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
						.set(Constants.EXPORT_PACKAGE, "fr.liglab.adele.icasa.bridge.test.app2")
						.set(Constants.BUNDLE_SYMBOLICNAME, App2.class.getSimpleName())
						.set(Application.APP_ID_BUNDLE_HEADER, "app2")
						.set(Application.APP_VERSION_BUNDLE_HEADER, "3.2.5")
						.build()
				),
				
				CoreOptions.when( Boolean.getBoolean( "isDebugEnabled" ) ).useOptions(
						PaxRunnerOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"),
						CoreOptions.waitForFrameworkStartup()
				)
				
				);
	}
    
    @Test
    public void testGenericDeviceVisibility() {
    	//wait for the service to be available.
        waitForIt(100);
        
        GenericDevice deviceImpl = mock(GenericDevice.class);
        final String devId = "123f4";
		when(deviceImpl.getSerialNumber()).thenReturn(devId);
		when(deviceImpl.getFault()).thenReturn(GenericDevice.FAULT_YES);
		when(deviceImpl.getState()).thenReturn(GenericDevice.STATE_ACTIVATED);
		when(deviceImpl.getLocation()).thenReturn("Undefined");
		ServiceRegistration sr = icasa.registerService(deviceImpl, GenericDevice.class);
        
        BundleContext app1Context = getBundleContext(APP1_ID);
        GenericDevice app1GenDevice = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNull(app1GenDevice);
        
        DeviceManager deviceMgr = (DeviceManager) getServiceObject(DeviceManager.class, app1Context);
        DeviceDependencies dependencies = new DeviceDependencies();
        dependencies.includes().all();
        
        DependRegistration depReg = deviceMgr.addDependencies(dependencies);
        waitForResolution(depReg);
        
        waitForService(ApplicationDevice.class, app1Context);
        ApplicationDevice app1device = (ApplicationDevice) getServiceObject(ApplicationDevice.class, app1Context);
        assertNotNull(app1device);
        assertEquals(deviceImpl.getSerialNumber(), app1device.getId());
        assertEquals(deviceImpl.getLocation(), app1device.getVariableValue("Location"));
        
        app1GenDevice = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNull(app1GenDevice);
        
        depReg.getDependencies().exportsTo(GenericDevice.class);
        depReg.updates();
        
        app1GenDevice = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNotNull(app1GenDevice);
        assertEquals(deviceImpl.getSerialNumber(), app1GenDevice.getSerialNumber());
        
        //cleanup
		depReg.unregister();
        sr.unregister();
    }
    
    @Test
    public void testDeviceAttr() {
    	//wait for the service to be available.
        waitForIt(100);
        
        GenericDevice deviceImpl = mock(PPDevice.class);
        final String devId = "123f5";
		when(deviceImpl.getSerialNumber()).thenReturn(devId);
		when(deviceImpl.getFault()).thenReturn(GenericDevice.FAULT_YES);
		when(deviceImpl.getState()).thenReturn(GenericDevice.STATE_ACTIVATED);
		when(deviceImpl.getLocation()).thenReturn("Undefined");
		ServiceRegistration sr = icasa.registerService(deviceImpl, GenericDevice.class);
        
        BundleContext app1Context = getBundleContext(APP1_ID);
        GenericDevice app1GenDevice = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNull(app1GenDevice);
        
        DeviceManager deviceMgr = (DeviceManager) getServiceObject(DeviceManager.class, app1Context);
        DeviceDependencies dependencies = new DeviceDependencies();
        dependencies.includes().all();
        
        DependRegistration depReg = deviceMgr.addDependencies(dependencies);
        waitForResolution(depReg);
        
        ApplicationDevice app1device = (ApplicationDevice) getServiceObject(ApplicationDevice.class, app1Context);
        assertNotNull(app1device);
        assertEquals(deviceImpl.getSerialNumber(), app1device.getId());
        assertEquals(deviceImpl.getLocation(), app1device.getVariableValue("Location"));
        assertEquals(deviceImpl.getState(), app1device.getVariableValue("State"));
        assertEquals(deviceImpl.getFault().equals(GenericDevice.FAULT_YES), app1device.getGlobalFault().equals(Fault.YES));
        
        //cleanup
		depReg.unregister();
        sr.unregister();
    }
    
    @Test
    public void testDeviceAttrNotifs() {
    	//wait for the service to be available.
        waitForIt(100);
        
        final String devId = "123f5";
        String state = GenericDevice.STATE_ACTIVATED;
        GenericDevice deviceImpl = new PPDeviceMockImpl(devId, state, "Undefined", GenericDevice.FAULT_YES);
		ServiceRegistration sr = icasa.registerService(deviceImpl, GenericDevice.class);
        
        BundleContext app1Context = getBundleContext(APP1_ID);
       
        DeviceManager deviceMgr = (DeviceManager) getServiceObject(DeviceManager.class, app1Context);
        DeviceDependencies dependencies = new DeviceDependencies();
        dependencies.includes().all();
        
        DependRegistration depReg = deviceMgr.addDependencies(dependencies);
        waitForResolution(depReg);
        
        ApplicationDevice app1device = (ApplicationDevice) getServiceObject(ApplicationDevice.class, app1Context);
        assertNotNull(app1device);
        
        VariableNotifListener varListener = new VariableNotifListener();
        app1device.addVariableListener(varListener);
        
        state = GenericDevice.STATE_DEACTIVATED;
        deviceImpl.setState(state);

        waitForIt(1000);
        assertEquals(deviceImpl.getState(), app1device.getVariableValue(STATE_GENERIC_PROP_NAME));
        assertTrue(varListener.hasValueChangeEvent(STATE_GENERIC_PROP_NAME));
        assertEquals(state, varListener.getLastValueChangeEvent(STATE_GENERIC_PROP_NAME).getNewValue());
        
        //cleanup
		depReg.unregister();
        sr.unregister();
    }
    
    @Test
    public void testExportedGenericDeviceAttr() {
    	//wait for the service to be available.
        waitForIt(100);
        
        GenericDevice deviceImpl = mock(GenericDevice.class);
        final String devId = "123f5";
		when(deviceImpl.getSerialNumber()).thenReturn(devId);
		when(deviceImpl.getFault()).thenReturn(GenericDevice.FAULT_NO);
		when(deviceImpl.getState()).thenReturn(GenericDevice.STATE_ACTIVATED);
		when(deviceImpl.getLocation()).thenReturn("Undefined");
		ServiceRegistration sr = icasa.registerService(deviceImpl, GenericDevice.class);
        
        BundleContext app1Context = getBundleContext(APP1_ID);
        GenericDevice app1GenDevice = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNull(app1GenDevice);
        
        DeviceManager deviceMgr = (DeviceManager) getServiceObject(DeviceManager.class, app1Context);
        DeviceDependencies dependencies = new DeviceDependencies();
        dependencies.exportsTo(GenericDevice.class).includes().all();
        
        DependRegistration depReg = deviceMgr.addDependencies(dependencies);
        waitForResolution(depReg);
        
        GenericDevice app1device = (GenericDevice) getServiceObject(GenericDevice.class, app1Context);
        assertNotNull(app1device);
        assertEquals(deviceImpl.getSerialNumber(), app1device.getSerialNumber());
        assertEquals(deviceImpl.getFault(), app1device.getFault());
        assertEquals(deviceImpl.getState(), app1device.getState());
        assertEquals(deviceImpl.getLocation(), app1device.getLocation());
        
        //TODO check device service attributes
        
        //cleanup
		depReg.unregister();
        sr.unregister();
    }
    
    @Test
    public void testGenericDeviceVisibilityFromInternal() {
    	//wait for the service to be available.
        waitForIt(100);
        
        GenericDevice deviceImpl = mock(GenericDevice.class);
        final String devId = "123f56";
		when(deviceImpl.getSerialNumber()).thenReturn(devId);
		when(deviceImpl.getFault()).thenReturn(GenericDevice.FAULT_NO);
		when(deviceImpl.getState()).thenReturn(GenericDevice.STATE_ACTIVATED);
		when(deviceImpl.getLocation()).thenReturn("Undefined");
		ServiceRegistration sr = icasa.registerService(deviceImpl, GenericDevice.class);
        
        BundleContext app1Context = getBundleContext(APP1_ID);
    
        DeviceManager deviceMgr = (DeviceManager) getServiceObject(DeviceManager.class, app1Context);
        DeviceDependencies dependencies = new DeviceDependencies();
        dependencies.exportsTo(GenericDevice.class).includes().all();
        
        DependRegistration depReg = deviceMgr.addDependencies(dependencies);
        waitForResolution(depReg);
        
        // should only see the original generic device
        waitForService(GenericDevice.class, context);
        checkNoService(GenericDevice.class, context, ApplicationDeviceProxy.class);
        
        //cleanup
		depReg.unregister();
        sr.unregister();
    }

	private void waitForResolution(DependRegistration depReg) {
		int NB_TIMES = 5;
		for (int i = 0; i < NB_TIMES; i++) {
			if (depReg.isResolved()) {
				break;
			}
			if (i == NB_TIMES - 1)
				waitForIt(100);
		}
	}

	private Application getApplication(String appId) {
		return getApplicationManagerService().getApplication(appId); 
	}
	
	private BundleContext getBundleContext(String appId) {
		Application app = getApplicationManagerService().getApplication(appId);
		
		return app.getBundles().iterator().next().getBundleContext();
	}

	private ApplicationManager getApplicationManagerService() {
		return icasa.getServiceObject(ApplicationManager.class);
	}

	private GlobalDeviceManager getGlobalDeviceManagerService() {
		return icasa.getServiceObject(GlobalDeviceManager.class);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T> T getServiceObject(Class<T> klass, BundleContext specContext) {
		ServiceReference sref = null;

		sref = specContext.getServiceReference(klass.getName());

		if (sref != null) {
			T service = (T) specContext.getService(sref);
			specContext.ungetService(sref);
			return service;
		} else {
			return null;
		}
	}
	
	public class VariableEvent {

		private StateVariable _var;
		private Object _oldValue;
		private Object _newValue;
		private Object _sourceObj;
		private boolean _isAddition = false;
		private boolean _isRemoval = false;
		private boolean _isValChange = false;

		public VariableEvent(StateVariable variable, Object oldValue,
				Object newValue, Object sourceObject) {
			_var = variable;
			_oldValue = oldValue;
			_newValue = newValue;
			_sourceObj = sourceObject;
			_isValChange = true;
		}
		
		public VariableEvent(StateVariable variable, Object sourceObject, boolean isAddition) {
			_var = variable;
			_sourceObj = sourceObject;
			_isAddition = isAddition;
			_isRemoval = !isAddition;
		}
		
		public StateVariable getVariable() {
			return _var;
		}

		public Object getOldValue() {
			return _oldValue;
		}
		
		public Object getNewValue() {
			return _newValue;
		}

		public Object getSourceObj() {
			return _sourceObj;
		}
		
		public boolean isAddition() {
			return _isAddition;
		}
		
		public boolean isRemoval() {
			return _isRemoval;
		}
		
		public boolean isValChange() {
			return _isValChange;
		}
	}
	
	public class VariableNotifListener implements StateVariableListener  {

		private List<VariableEvent> events = new ArrayList<VariableEvent>();
		
		public boolean hasValueChangeEvent() {
			for (VariableEvent event : events) {
				if (event.isValChange())
					return true;
			}
			return false;
		}
		
		public boolean hasAdditionEvent() {
			for (VariableEvent event : events) {
				if (event.isAddition())
					return true;
			}
			return false;
		}
		
		public boolean hasRemovalEvent() {
			for (VariableEvent event : events) {
				if (event.isRemoval())
					return true;
			}
			return false;
		}
		
		public boolean hasValueChangeEvent(String propName) {
			return (getLastValueChangeEvent(propName) != null);
		}
		
		public VariableEvent getLastValueChangeEvent(String propName) {
			VariableEvent valueChangeEvent = null;
			for (VariableEvent event : events) {
				if (event.getVariable().getName().equals(propName) &&  event.isValChange())
					valueChangeEvent = event;
			}
			
			return valueChangeEvent;
		}

		@Override
		public void addVariable(StateVariable variable, Object sourceObject) {
			events.add(new VariableEvent(variable, sourceObject, true));
		}

		@Override
		public void removeVariable(StateVariable variable, Object sourceObject) {
			events.add(new VariableEvent(variable, sourceObject, false));
		}

		@Override
		public void notifValueChange(StateVariable variable, Object oldValue, Object newValue,
				Object sourceObject) {
			events.add(new VariableEvent(variable, oldValue, newValue, sourceObject));
		}
		
		public void reset() {
			events.clear();
		}
	}
	
	public class DevServListener implements ServiceListener {

		public boolean knownDevEvent = false;
		public boolean providedDevEvent = false;
		public boolean appDevEvent = false;
		public boolean availableDevEvent = false;
		public boolean devEvent = false;
		
		public void serviceChanged(ServiceEvent event) {
			final ServiceReference sr = event.getServiceReference();
			String[] interfaces = (String[]) sr.getProperty(Constants.OBJECTCLASS);
			for (String interf : interfaces) {
				if (ProvidedDevice.class.getName().equals(interf))
					providedDevEvent = true;
				if (KnownDevice.class.getName().equals(interf))
					knownDevEvent = true;
				if (AvailableDevice.class.getName().equals(interf))
					availableDevEvent = true;
				if (ApplicationDevice.class.getName().equals(interf))
					appDevEvent = true;
				if (Device.class.getName().equals(interf))
					devEvent = true;
			}
		}
		
		public boolean hasProvidedDevEvent() {
			return providedDevEvent;
		}

		public boolean hasOnlyDevEvent() {
			return !providedDevEvent && !knownDevEvent && !appDevEvent && !availableDevEvent && devEvent; 
		}
		
		public boolean hasDevEvent() {
			return devEvent; 
		}
		
	}
}

