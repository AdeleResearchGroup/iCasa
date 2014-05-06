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

import static org.osgi.service.log.LogService.LOG_WARNING;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.ow2.chameleon.rose.ImporterService;
import org.ow2.chameleon.rose.RoseMachine;
import org.ow2.chameleon.rose.util.ConcurrentMapOfSet;

/**
 * Abstract implementation of an proxy-creator which provides an {@link ImporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 */
public abstract class AbstractFilteredServiceImporter implements ImporterService {
	private final ConcurrentMapOfSet<EndpointDescription, MyImportRegistration> registrations;
	private volatile boolean isValid = false;
	
	
	public  AbstractFilteredServiceImporter() {
		registrations = new ConcurrentMapOfSet<EndpointDescription, MyImportRegistration>();
	}
	
	/*--------------------------*
	 * EndpointCreator methods. *
	 *--------------------------*/
	
	/**
	 */
	protected abstract ServiceRegistration createProxy(final EndpointDescription description,final Map<String,Object> extraProperties);
	
	/**
	 * Abstract method, is called when the sub class must destoy the proxy. Do not forget to unregister the proxy, call: 
	 * <code>registration.unregister()</code>
	 */
	protected abstract void destroyProxy(final EndpointDescription description,final ServiceRegistration registration);
	
	/**
	 * Stop the proxy-creator, iPOJO Invalidate instance callback.
	 * Must be override !
	 * Close all endpoints.
	 */
	 protected void stop(){
		
		synchronized (registrations) {
			Collection<EndpointDescription> descs = registrations.keySet();
		
			for (EndpointDescription desc : descs) {
				MyImportReference iref = registrations.getElem(desc).getImportReference();
				destroyProxy(iref.getImportedEndpoint(),iref.getRegistration()); //TODO check != null
				iref.close();//unregister the proxy
			}
			
			registrations.clear();
			isValid = false;
		}
	 }
	 
	 /**
	  * Start the endpoint-creator component, iPOJO Validate instance callback.
	  * Must be override !
	  */
	 protected void start(){
		 synchronized (registrations) {
			
			if (registrations.size() > 0){
				getLogService().log(LOG_WARNING, "Internal structures have not been clean while stopping the instance.");
				stop();
			}
			
			isValid = true;
		 }
	 }
	 
	 /**
	  * @return <code>true</code> if the {@link ImporterService} is in a valid state, <code>false</code> otherwise.
	  */
	 protected final boolean isValid(){
			return isValid;
	 }
	
	/*--------------------------------------------*
	 *  Convenient access to some useful service. *
	 *--------------------------------------------*/
	
	/**
	 * @return The {@link LogService} service.
	 */
	protected abstract LogService getLogService();
	
	
	/**
	 * @return The {@link RoseMachine} service.
	 */
	public abstract RoseMachine getRoseMachine();
	
	/**
	 * Returns true if this end point is managed by this service importer.
	 * 
	 * @param description an end point description
	 * @return true if this end point is managed by this service importer.
	 */
	protected abstract boolean isManaged(EndpointDescription description) ;
	
	/*---------------------------------*
	 *  ExporterService implementation *
	 *---------------------------------*/
	
	/**
	 * @param sref
	 * @param extraProperties
	 * @return
	 */
	public ImportRegistration importService(EndpointDescription description,Map<String, Object> properties) {
		final ImportRegistration ireg;
		
		synchronized (registrations) {
			
			if (!isValid){
				return new BadImportRegistration(new Throwable("This ExporterService is no more available. !"));
			}
			
			if (registrations.containsKey(description)) { 
				//Clone Registration
				ireg = new MyImportRegistration(registrations.getElem(description));
			} else if (!isManaged(description)) {
				 // not interested in this end point
				ireg = new BadImportRegistration(new Throwable("This ExporterService does not manage this end point. !" + 
				 description.toString()));
			} else {
				//First registration, create the proxy
				ServiceRegistration registration = createProxy(description, properties);
				
				ireg = new MyImportRegistration(description,registration);
			}
		}
		
		return ireg;
	}
	
	
	public Collection<ImportReference> getAllImportReference(){
		Collection<ImportReference> irefs = new HashSet<ImportReference>();
		
		for (EndpointDescription desc : registrations.keySet()) {
			irefs.add(registrations.getElem(desc).getImportReference());
		}
		
		return irefs;
	}
	
	
	/*--------------------------------*
	 *         INNER CLASS            *
	 *--------------------------------*/
	
	/**
	 * Implementation of an {@link ImportReference}.
	 *
	 */
	private final class MyImportReference implements ImportReference {
		private EndpointDescription desc;
		private ServiceRegistration reg;
		
		/**
		 * Register the {@code pEndesc} thanks to {@code context}.
		 * 
		 * @param pSref
		 * @param pEnddesc
		 * @param context
		 */
		public MyImportReference(EndpointDescription description, ServiceRegistration sreg) {
			desc = description;
			reg = sreg;
		}


		public ServiceReference getImportedService() {
			return reg.getReference();
		}

		public EndpointDescription getImportedEndpoint() {
			return desc;
		}
		
		private ServiceRegistration getRegistration(){
			return reg;
		}
		
		private void close(){
			try{
				reg.unregister();
			}catch(IllegalStateException ie){
				//no pb
			}
			desc=null;
			reg=null;
		}
	}
	
	/**
	 * Implementation of an {@link ImportRegistration}.
	 *
	 */
	private final class MyImportRegistration implements ImportRegistration {
		private volatile MyImportReference iref;
		
		private MyImportRegistration(MyImportRegistration reg) {
			iref=reg.getImportReference();
			
			//Add the registration to the registrations mapOfSet
			registrations.add(reg.getImportReference().getImportedEndpoint(), this);
		}
		
		private MyImportRegistration(EndpointDescription desc,ServiceRegistration registration) {
			//Create the MyImportReference
			iref = new MyImportReference(desc,registration); 
			
			//Add the registration to the registrations mapOfSet
			registrations.add(iref.getImportedEndpoint(), this);
		}
		
		
		public MyImportReference getImportReference() {
			return iref;
		}

		public void close() {
			if (iref != null) {
				// Last registration, remove the ExportReference from the ExportRegistry
				if (registrations.remove(iref.getImportedEndpoint(), this)) {
					getRoseMachine().removeLocal(iref);
					destroyProxy(iref.getImportedEndpoint(),iref.getRegistration());
					iref.close(); //Safe close (unregister the proxy if forgotten)
				}
				iref = null; // is now closed
			}
		}

		public Throwable getException() {
			return null;
		}
	}
	
	public class BadImportRegistration implements ImportRegistration {
		private Throwable exception;

		public BadImportRegistration(Throwable throwable) {
			exception = throwable;
		}

		public ImportReference getImportReference() {
			return null;
		}

		public void close() {
			if (exception != null){
				exception = null;
			}
		}

		public Throwable getException() {
			return exception;
		}

	}
	
}
