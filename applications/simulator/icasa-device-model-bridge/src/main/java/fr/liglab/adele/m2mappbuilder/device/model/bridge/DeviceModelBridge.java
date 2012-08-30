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
package fr.liglab.adele.m2mappbuilder.device.model.bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.*;
import fr.liglab.adele.m2mappbuilder.application.Application;
import fr.liglab.adele.m2mappbuilder.common.StateVariable;
import fr.liglab.adele.m2mappbuilder.common.impl.StateVariableProxy;
import fr.liglab.adele.m2mappbuilder.device.manager.ApplicationDevice;
import fr.liglab.adele.m2mappbuilder.device.manager.ApplicationDeviceProxy;
import fr.liglab.adele.m2mappbuilder.device.manager.Device;
import fr.liglab.adele.m2mappbuilder.device.manager.DeviceExporter;
import fr.liglab.adele.m2mappbuilder.device.manager.ProvidedDevice;
import fr.liglab.adele.m2mappbuilder.device.manager.FilterDeviceContrib;
import fr.liglab.adele.m2mappbuilder.device.manager.ServPropManager;
import fr.liglab.adele.m2mappbuilder.device.manager.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * An ipojo component which exposes all icasa generic devices into the device model expected by the device manager.
 * It also exposes in the opposite direction from device model to icasa generic devices.
 * This component uses heavily java reflection to discover interfaces, attributes and methods.
 * 
 * @author Thomas Leveque
 *
 */
@Component(name="icasa.device.model.bridge")
@Instantiate(name="icasa.device.model.bridge-1")
@Provides
public class DeviceModelBridge implements FilterDeviceContrib, DeviceExporter {
	
	private BundleContext _context;
	
	private ServiceTracker _genericDeviceTracker;
	
	private Map<String, ProvidedDevice> _providedProxyDevs = new HashMap<String, ProvidedDevice>();
	private Map<String, ServiceRegistration> _providedProxyRegs = new HashMap<String, ServiceRegistration>();

	private PollingThread _thread;

	private ServPropManager _servPropMgr = new ServPropManager() {

		@Override
		public void registerService(ApplicationDevice device, Object proxy,
				Properties props) {
			props.put(GenericDevice.DEVICE_SERIAL_NUMBER, device.getId());
		}
	};
	
	private class PollingThread extends Thread {
		
		public PollingThread() {
			super("iCasa device model bridge updater");
		}
		
		private boolean _killed = false;
		
		@Override
		public void run() {
			while(!_killed) {
				
				Collection<ProvidedDevice> providedDevices = null;
				synchronized (_providedProxyDevs) {
					providedDevices = _providedProxyDevs.values();

					updateDevices(providedDevices);
				}
				
				synchronized (this) {
					try {
						wait(500);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		}

		public void kill() {
			_killed = true;
		}
		
	}

	public DeviceModelBridge(BundleContext context) {
		_context = context;
		_genericDeviceTracker = new ServiceTracker(_context, GenericDevice.class.getName(), new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				GenericDevice device = (GenericDevice) service;
				// test if it is already an exported device proxy
				if (device instanceof ApplicationDeviceProxy) {
					_context.ungetService(reference);
					return;
				}
				
				synchronized(_providedProxyDevs) {
					ProvidedDevice providedDev = _providedProxyDevs.remove(device.getSerialNumber());
					
					if (providedDev != null) {
						ServiceRegistration sreg = _providedProxyRegs.remove(device.getSerialNumber());
						sreg.unregister();
					}
				}
				
				_context.ungetService(reference);
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				if (!(service instanceof GenericDevice))
					return;
				
				GenericDevice device = (GenericDevice) service;
				ProvidedDevice provDev = null;
				synchronized (_providedProxyDevs) {
					provDev = _providedProxyDevs.get(device.getSerialNumber());
					updateDevice(provDev);
				}
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				GenericDevice device = (GenericDevice) _context.getService(reference);
				// test if it is already an exported device proxy
				if (device instanceof ApplicationDeviceProxy) {
					_context.ungetService(reference);
					return null;
				}
				
				ProvidedDevice providedDev = ReflectUtil.createProvidedDev(device, _context);
				synchronized(_providedProxyDevs) {
					final String serialNumber = device.getSerialNumber();
					_providedProxyDevs.put(serialNumber, providedDev);
					
					ServiceRegistration sreg = _context.registerService(ProvidedDevice.class.getName(), providedDev, new Hashtable());
					_providedProxyRegs.put(serialNumber, sreg);
				}
				
				return device;
			}
		});
	}
	
	private void updateDevices(Collection<ProvidedDevice> providedDevices) {
		for (ProvidedDevice device : providedDevices) {
			updateDevice(device);
		}
	}

	private void updateDevice(ProvidedDevice device) {
		updateDerivedVariables(device.getStateVariables());
		
		for (Service serv : device.getServices()) {
			updateDerivedVariables(serv.getStateVariables());
		}
	}
	
	private void updateDerivedVariables(List<StateVariable> variables) {
		for (StateVariable var : variables) {
			if (var instanceof StateVariableProxy)
				var = ((StateVariableProxy) var).getInternalVariable();
			if (var instanceof DerivedStateVariableFromIntf)
				var.getValue(); // use it to update value
		}
	}

	@Validate
	public void start() {
		_genericDeviceTracker.open();
		_thread = new PollingThread();
		_thread.start();
	}
	
	@Invalidate
	public void stop() {
		if (_thread != null) {
			_thread.kill();
			_thread = null;
		}
		if (_genericDeviceTracker != null)
			_genericDeviceTracker.close();
	}

	@Override
	public boolean hideDevice(Application app, ServiceReference sr,
			String[] interfaces) {
		Arrays.sort(interfaces);
		boolean isFromApp = (app != null);
		Object service = _context.getService(sr);
		boolean isAppProxy = service instanceof ApplicationDeviceProxy;
		boolean isGenDevice = service instanceof GenericDevice;
		_context.ungetService(sr);
		
		// hide icasa devices from applications
		if (isFromApp && isGenDevice && !isAppProxy) {
			return true;
		}
		
		// hide application devices from not owner applications
		if (isFromApp && isGenDevice && isAppProxy) {
			ApplicationDeviceProxy appProxy = (ApplicationDeviceProxy) service;
			boolean hide = !(appProxy.getApplication().getId().equals(app.getId()));
			
			return hide;
		}
		
		if (!isFromApp && isGenDevice && isAppProxy)
			return true;
		
		return false;
	}

	@Override
	public boolean canExportTo(Device device, Class... interfaces) {
		boolean isGenDev = false;
		for (Class interf : interfaces) {
			if (!interf.isInterface())
				return false;
			
			if (GenericDevice.class.isAssignableFrom(interf))
				isGenDev = true;
		}
		
		//TODO check interface compatibility
		
		return isGenDev;
	}

	@Override
	public Object createsProxy(ApplicationDevice device, Class... interfaces) {
		
		List<Class> interfacesList = new ArrayList<Class>();
		interfacesList.add(ApplicationDeviceProxy.class);
		for (Class interf : interfaces)
			interfacesList.add(interf);
		for (Service serv : device.getServices()) {
			String servInterfName = serv.getTypeId();
			Class interf = null;
			try {
				interf = _context.getBundle().loadClass(servInterfName);
			} catch (Exception e) {
				interf = null;
			}
			if (interf != null)
				interfacesList.add(interf);
		}
		
		DynamicProxyFactory proxyFactory = getProxyFactory(interfacesList.toArray(new Class[interfacesList.size()]));
		
		return proxyFactory.getProxy(device);
	}

	private DynamicProxyFactory getProxyFactory(Class... proxyClasses) {
		return new DynamicProxyFactory(DeviceModelBridge.class.getClassLoader(), proxyClasses); //TODO optimize with map of proxy factory
	}

	@Override
	public void destructsProxy(Object proxy) {
		// do nothing
	}

	@Override
	public ServPropManager getCustomServicePropManager() {
		return _servPropMgr;
	}

}
