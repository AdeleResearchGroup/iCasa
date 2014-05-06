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
package fr.liglab.adele.icasa.context.interceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import fr.liglab.adele.icasa.device.util.EmptyLocatedDeviceListener;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.dependency.interceptors.ServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;


/**
 * This interceptor adds context (location) to iCasa devices.
 *
 *
 */
@Component(name = "ICasaTrackingInterceptor")
@Provides(properties = { @StaticServiceProperty(name = "target", value = "(objectClass=fr.liglab.adele.icasa.device.GenericDevice)", type = "java.lang.String") })
@Instantiate
public class ICasaTrackingInterceptor extends EmptyLocatedDeviceListener implements ServiceTrackingInterceptor {

	/**
	 * The set of managed dependencies. Access must be guarded by the monitor lock.
	 */
	protected final List<DependencyModel> dependencies = new ArrayList<DependencyModel>();

	@Requires
	private ContextManager contextManager;

	@Override
	public void open(DependencyModel dependency) {
		if (isPlatformComponent(dependency))
			return;
		synchronized (this) {
			dependencies.add(dependency);
		}
	}

	@Override
	public void close(DependencyModel dependency) {
		if (isPlatformComponent(dependency))
			return;
		synchronized (this) {
			dependencies.remove(dependency);
		}
	}

	@Override
	public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context,
	      TransformedServiceReference<S> serviceReference) {
		
		if (isPlatformComponent(dependency)) {
			return serviceReference;
		}

		String deviceId = (String) serviceReference.getProperty(GenericDevice.DEVICE_SERIAL_NUMBER);
		Position position = contextManager.getDevicePosition(deviceId);

		if (position == null) { // The device has not been associated to the context yet
			return null;
		}

		// If context (location) exists if is added to the service reference
		serviceReference.addProperty("location", getLocationFromPosition(position));

		return serviceReference;
	}

	/**
	 * Get the location (Zone->Id) from a Position
	 * @param position
	 * @return
	 */
	private String getLocationFromPosition(Position position) {
		Zone zone = contextManager.getZoneFromPosition(position);
		if (zone == null)
			return "unknown";
		return zone.getId();
	}

	/**
	 * Determines if the component having this dependency is a iCasa Platform component
	 * 
	 * @param dependency
	 * @return
	 */
	private boolean isPlatformComponent(DependencyModel dependency) {
		String[] specs = dependency.getComponentInstance().getFactory().getComponentDescription()
		      .getprovidedServiceSpecification();

		for (String specification : specs) {
			if (specification.equals(ContextManager.class.getName()))
				return true;
		}

		return false;
	}

	@Validate
	protected void start() {
		contextManager.addListener(this);
	}

	@Invalidate
	protected void stop() {
		contextManager.removeListener(this);
	}

	/**
	 * Invalidate services dependencies
	 * 
	 * @param ldevice
	 */
	private void invalidateMatchingServices(LocatedDevice ldevice) {
		List<DependencyModel> list = new ArrayList<DependencyModel>();
		synchronized (this) {
			list.addAll(dependencies);
		}

		for (DependencyModel dep : list) {			
			List<Class<?>> interfaces = getAllInterfaces(ldevice.getDeviceObject().getClass());
					
			for (Class interfaze : interfaces) {
				if (dep.getSpecification().equals(interfaze)) { // Only dependencies using this kind of device
					dep.invalidateMatchingServices();
				}
			}
		}
	}

	@Override
	public void deviceAdded(LocatedDevice ldevice) {
		invalidateMatchingServices(ldevice);
	}

	@Override
	public void deviceMoved(LocatedDevice ldevice, Position oldPosition, Position newPosition) {
		if (getLocationFromPosition(oldPosition).equals(getLocationFromPosition(newPosition))) // Same Location
			return;
		invalidateMatchingServices(ldevice);
	}

	
	
   public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
      if (cls == null) {
          return null;
      }

      final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
      getAllInterfaces(cls, interfacesFound);

      return new ArrayList<Class<?>>(interfacesFound);
  }
	
	private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
      while (cls != null) {
          final Class<?>[] interfaces = cls.getInterfaces();

          for (final Class<?> i : interfaces) {
              if (interfacesFound.add(i)) {
                  getAllInterfaces(i, interfacesFound);
              }
          }

          cls = cls.getSuperclass();
       }
   }
	
}
