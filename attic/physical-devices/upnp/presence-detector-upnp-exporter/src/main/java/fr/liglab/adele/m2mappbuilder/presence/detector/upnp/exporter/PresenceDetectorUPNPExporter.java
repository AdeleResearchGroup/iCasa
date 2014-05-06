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
package fr.liglab.adele.m2mappbuilder.presence.detector.upnp.exporter;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.ow2.chameleon.rose.RoseMachine;

/**
 * Component implementation capable of creating UPNP proxies for presence detector
 * 
 */
@Component(name="upnp.presence.sensor.importer")
@Provides(properties = {
        @StaticServiceProperty(type = "java.lang.String", name = "rose.protos.configs", value="medical.device") })
public class PresenceDetectorUPNPExporter extends AbstractFilteredServiceImporter {

	private static final String MEDICAL_DEVICE_CONFIG_PREFIX = "medical.device";

	@Requires(filter="(factory.name=upnp.presence.detector)")
	private Factory sensorFactory;

	@Requires
	private RoseMachine roseMachine;
	
	/**
	 * Default constructor
	 */
	public PresenceDetectorUPNPExporter(BundleContext context) throws InvalidSyntaxException {
		// do nothing
	}
	
	protected boolean isManaged(EndpointDescription description) {
		
		Map<String, Object> edpProperties = description.getProperties();
		String[] deviceInterfaces = (String[]) edpProperties.get("objectClass");
		if (deviceInterfaces == null) 
			return false;
		
		for (String deviceInterface : deviceInterfaces) {
			if (PresenceSensor.class.getName().equals(deviceInterface))
				return true;
		}
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.chameleon.rose.AbstractImporterComponent#createProxy(org.osgi.
	 * service.remoteserviceadmin.EndpointDescription, java.util.Map)
	 */
	protected ServiceRegistration createProxy(EndpointDescription epd, Map arg1) {
		ComponentInstance instance;
		try {

			Map<String, Object> epdProps = epd.getProperties();
			// use presence sensor service id to link proxy with corresponding service
			Object sensorServiceId = (Long) epdProps.get("sensor.service.id"); 
			
			Hashtable properties = new Hashtable();
			properties.put(PresenceSensor.DEVICE_SERIAL_NUMBER, epd.getId());
			properties.put(PresenceDetectorUPNPDeviceProxy.SENSOR_SERVICE_ID_PROP, sensorServiceId);
			
			instance = sensorFactory.createComponentInstance(properties);
			
			if (instance != null) {
				ServiceRegistration sr = new MedicalServiceregistration(instance);
				return sr;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.chameleon.rose.AbstractImporterComponent#destroyProxy(org.osgi
	 * .service.remoteserviceadmin.EndpointDescription,
	 * org.osgi.framework.ServiceRegistration)
	 */
	protected void destroyProxy(EndpointDescription arg0, ServiceRegistration sr) {
		sr.unregister();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#getLogService()
	 */
	protected LogService getLogService() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#getRoseMachine()
	 */
	public RoseMachine getRoseMachine() {
		return roseMachine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.ImporterService#getConfigPrefix()
	 */
	public List<String> getConfigPrefix() {
		List<String> list = new ArrayList<String>();
		list.add("medical.device");
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
	 */
	@Validate
	protected void start() {
		super.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
	 */
	@Invalidate
	protected void stop() {
		super.stop();

	}

}

/**
 * A wrapper for ipojo Component instances
 *
 * 
 */
class MedicalServiceregistration implements ServiceRegistration {

	ComponentInstance instance;

	public MedicalServiceregistration(ComponentInstance instance) {
		super();
		this.instance = instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.ServiceRegistration#getReference()
	 */
	public ServiceReference getReference() {
		try {
			ServiceReference[] references = instance.getContext().getServiceReferences(
			      instance.getClass().getCanonicalName(), "(instance.name=" + instance.getInstanceName() + ")");
			if (references.length > 0)
				return references[0];
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.ServiceRegistration#setProperties(java.util.Dictionary)
	 */
	public void setProperties(Dictionary properties) {
		instance.reconfigure(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.ServiceRegistration#unregister()
	 */
	public void unregister() {
		instance.dispose();
	}

}
