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
package fr.liglab.adele.m2mappbuilder.upnp.presence.detector.importer;

import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.rose.RoseMachine;

/**
 * 
 * This class implements UPnP discovery based in the Felix UPnP Base Driver
 *
 *
 */
public class UPnPPresenceDetectorRoseDiscoveryImpl implements ServiceTrackerCustomizer {
		
	/**
	 * The bundle context
	 */
	private BundleContext context;
	
	/**
	 * A service tracker to detect apparition and disparition of UPnPDevice services instances
	 */
	private ServiceTracker serviceTracker;
	
	/**
	 * The ROSE machine instance
	 */
   private RoseMachine roseMachine;
	
	public UPnPPresenceDetectorRoseDiscoveryImpl(BundleContext context) {
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	public Object addingService(ServiceReference reference) {	
		String deviceID = (String)reference.getProperty(UPnPDevice.UDN);
		
		registerSensorInROSE(deviceID, reference);
		
		return context.getService(reference);
	}
	
		
	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	public void removedService(ServiceReference reference, Object service) {
		String deviceID = (String)reference.getProperty(UPnPDevice.UDN);
		roseMachine.removeRemote(deviceID);
	}
	
	/**
	 * Starts the @ServiceTracker using a filter to listen only UPnP Devices Services
	 */
	public void start() {
		String listenerFilter = "(" + Constants.OBJECTCLASS + "=" + UPnPDevice.class.getName() + ")";
		Filter filter;
      try {
	      filter = context.createFilter(listenerFilter);
			serviceTracker = new ServiceTracker(context, filter, this);
			serviceTracker.open();
      } catch (InvalidSyntaxException e) {
	      e.printStackTrace();
      }

	}
	
	/**
	 * Stops the @ServiceTracker
	 */
	public void stop() {
		if (serviceTracker!=null)
			serviceTracker.close();
	}

	
	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	public void modifiedService(ServiceReference reference, Object service) {
	   
   }

	/**
	 * Registers the Endpoint of the Device in ROSE machine
	 * @param deviceId the deviceID will be used as key
	 * @param reference the service reference
	 */
   private void registerSensorInROSE(String deviceId, ServiceReference reference) {   	
      Map props = new Properties();
      props.put(RemoteConstants.ENDPOINT_ID, deviceId);
      props.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS, "upnp");
      props.put("objectClass", new String[] { "someObject" });
      props.put("sensor.service.id", reference.getProperty(Constants.SERVICE_ID));
      props.put(UPnPDevice.TYPE, reference.getProperty(UPnPDevice.TYPE));

      EndpointDescription epd = new EndpointDescription(props);
      roseMachine.putRemote(deviceId, epd);
   }

	
}
