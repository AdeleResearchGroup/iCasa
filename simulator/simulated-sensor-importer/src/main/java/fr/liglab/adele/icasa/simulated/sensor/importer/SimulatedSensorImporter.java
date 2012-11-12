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
package fr.liglab.adele.icasa.simulated.sensor.importer;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.ow2.chameleon.rose.AbstractImporterComponent;
import org.ow2.chameleon.rose.RoseMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component implementation capable of creating proxies for presence detector devices of type
 * fr.liglab.adele.icasa.device.impl.SimulatedPresenceSensorImpl
 * 
 * @author Gabriel Pedraza Ferreira
 * 
 */
public class SimulatedSensorImporter extends AbstractImporterComponent {

	private Factory sensorFactory;

	private RoseMachine roseMachine;

	//private final DynamicImporter dynaimp;
	
	private static final Logger logger = LoggerFactory.getLogger(SimulatedSensorImporter.class);

	/**
    * Default constructor
    */
	public SimulatedSensorImporter(BundleContext context) throws InvalidSyntaxException {
		// DynamicImporter is used to bind the Importer with the ROSE registry
		//String descriptionFilter = "(" + RemoteConstants.SERVICE_IMPORTED_CONFIGS + "=simulated-zigbee)";
		//dynaimp = new DynamicImporter.Builder(context, descriptionFilter).protocol(getConfigPrefix()).build();
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
			
			logger.info("Creating component instance");
			
			Hashtable properties = new Hashtable(epd.getProperties());
			properties.put("updaterThread.period", new Long(500));
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
		list.add("zigbee");
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
	 */
	protected void start() {
		super.start();
		//dynaimp.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
	 */
	protected void stop() {
		//dynaimp.stop();
		super.stop();

	}

}

/**
 * A wrapper for ipojo Component instances
 * 
 * @author Gabriel Pedraza Ferreira
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
