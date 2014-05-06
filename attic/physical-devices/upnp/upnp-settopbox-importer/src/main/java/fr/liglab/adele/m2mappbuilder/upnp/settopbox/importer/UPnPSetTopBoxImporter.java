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
package fr.liglab.adele.m2mappbuilder.upnp.settopbox.importer;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.ow2.chameleon.rose.AbstractImporterComponent;
import org.ow2.chameleon.rose.RoseMachine;

/**
 * This class implements a ROSE importer for the UPnP Devices providing a PresenceDetector Service
 *
 *
 */
public class UPnPSetTopBoxImporter extends AbstractImporterComponent {

	/**
	 * The factory of proxies
	 */
	private Factory proxyFactory;

	/**
	 * The ROSE machine instance
	 */
	private RoseMachine roseMachine;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.chameleon.rose.AbstractImporterComponent#createProxy(org.osgi.
	 * service.remoteserviceadmin.EndpointDescription, java.util.Map)
	 */
	@Override
	protected ServiceRegistration createProxy(EndpointDescription epd, Map arg1) {
		ComponentInstance instance;
		try {
			Dictionary<String, Object> filter = new Hashtable<String, Object>();
			
			Object serviceId = epd.getProperties().get("sensor.service.id");
			filter.put("upnp-device", "("+ Constants.SERVICE_ID + "="+ serviceId +")");
			
			Dictionary<String, Object> conf = new Hashtable<String, Object>();
			conf.put("status", "activated");
			conf.put("fault", "yes");
			conf.put("device.serialNumber", epd.getId());
			conf.put("requires.filters", filter);
			
	        instance = proxyFactory.createComponentInstance(conf);
	      
			if (instance != null) {
				ServiceRegistration sr = new MedicalServiceregistration(instance);
				return sr;
			}
      } catch (UnacceptableConfiguration e) {
	      e.printStackTrace();
      } catch (MissingHandlerException e) {
	      e.printStackTrace();
      } catch (ConfigurationException e) {
	      e.printStackTrace();
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
	@Override
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
		list.add("upnp.settopbox");
		list.add("upnp");
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
	 */
	protected void start() {
		super.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
	 */
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
