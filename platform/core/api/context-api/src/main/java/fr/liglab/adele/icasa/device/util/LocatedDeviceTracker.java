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
package fr.liglab.adele.icasa.device.util;

import java.util.*;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedDeviceListener;
import fr.liglab.adele.icasa.location.Position;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to track iCasa devices.
 *
 */
public class LocatedDeviceTracker implements LocatedDeviceTrackerCustomizer {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG);
    /**
     * The bundle context against which this LocatedDeviceTracker object is tracking.
     */
    protected BundleContext m_context;

    /**
     * Tracker of technical service used to listen device events.
     */
    protected Tracker contextMgrTracker;

    /**
     * the filter specifying type criteria for the devices to track.
     */
    protected LocatedDeviceFilter m_typeFilter;

    /**
     * the filter specifying prop matching criteria for the devices to track.
     */
    protected LocatedDeviceFilter m_propFilter;

    /**
     * The LocatedDeviceTrackerCustomizer object for this tracker.
     */
    protected LocatedDeviceTrackerCustomizer m_customizer;

    /**
     * The class name to be tracked. If this field is set, then we are
     * tracking by class name.
     */
    private final Class<? extends GenericDevice> m_trackClass;

    /**
     * The tracked devices: String device serial number -> device object.
     */
    private Tracked m_tracked;

    /**
     * Creates a LocatedDeviceTracker object on the specified class name.
     * Services registered under the specified class name will be tracked by this LocatedDeviceTracker object.
     * @param context the BundleContext object against which the tracking is done.
     * @param clazz the Class name of the services to be tracked.
     * @param customizer the customizer object to call when services are added, modified, or removed in this LocatedDeviceTracker object. If customizer is null, then this LocatedDeviceTracker object will be used as
     *            the TrackerCustomizer object and the LocatedDeviceTracker object will call the TrackerCustomizer methods on itself.
     */
    public LocatedDeviceTracker(BundleContext context, final Class<? extends GenericDevice> clazz, LocatedDeviceTrackerCustomizer customizer, final String... mandatoryProperties) {

        this.m_context = context;
        this.m_trackClass = clazz;
        if (customizer == null) {
            m_customizer = this;
        } else {
            m_customizer = customizer;
        }
        this.m_typeFilter = new DeviceTypeFilter(clazz);
        if (mandatoryProperties.length == 0)
            this.m_propFilter = new DefaultTrueFilter();
        else
            this.m_propFilter = new MandatoryPropFilter(mandatoryProperties);
    }

    /**
     * Opens this LocatedDeviceTracker object and begin tracking services.
     * <p>
     * Services which match the search criteria specified when this LocatedDeviceTracker object was created are now tracked by this LocatedDeviceTracker object.
     */
    public synchronized void open() {
        logger.trace("[DeviceTracker] open");
        if (m_tracked != null) { return; }

        m_tracked = new Tracked();
        contextMgrTracker = new Tracker(m_context, ContextManager.class.getName(), new TrackerCustomizer() {
            @Override
            public boolean addingService(ServiceReference serviceReference) {
                return true;
            }

            @Override
            public void addedService(ServiceReference serviceReference) {
                logger.trace("[DeviceTracker] iCasa ContextManager added");
                ContextManager contextMgr = (ContextManager) m_context.getService(serviceReference);
                if (m_tracked == null)
                    return;

                synchronized (m_tracked) {
                    m_tracked.startTracking(contextMgr);
                }
                m_tracked.trackInitialDevices();
            }

            @Override
            public void modifiedService(ServiceReference serviceReference, Object o) {
                // Nothing to do.
            }

            @Override
            public void removedService(ServiceReference serviceReference, Object o) {
                logger.trace("[DeviceTracker] iCasa ContextManager removed");
                if (m_tracked == null)
                    return;

                synchronized (m_tracked) {
                    m_tracked.stopTracking();
                }
            }
        });
        contextMgrTracker.open();
    }

    /**
     * Closes this LocatedDeviceTracker object.
     * <p>
     * This method should be called when this LocatedDeviceTracker object should end the tracking of services.
     */
    public synchronized void close() {
        logger.trace("[DeviceTracker] close");
        if (contextMgrTracker != null) {
            contextMgrTracker.close();
            contextMgrTracker = null;
        }

        if (m_tracked == null) { return; }

        m_tracked.close();
        m_tracked = null;

    }

    /**
     * Default implementation of the LocatedDeviceTrackerCustomizer.addingDevice method.
     */
    public boolean addingDevice(LocatedDevice device) {
        return true;
    }

    /**
     * Default implementation of the LocatedDeviceTrackerCustomizer.addedDevice method.
     */
    public void addedDevice(LocatedDevice device) {
        // Nothing to do.
    }

    /**
     * Default implementation of the LocatedDeviceTrackerCustomizer.modifiedDevice method.
     */
    public void modifiedDevice(LocatedDevice device, String propertyName, Object oldValue, Object newValue) {
        // Nothing to do.
    }

    @Override
    public void movedDevice(LocatedDevice device, Position oldPosition, Position newPosition) {
        // Nothing to do.
    }

    /**
     * Default implementation of the LocatedDeviceTrackerCustomizer.removedDevice method.
     */
    public void removedDevice(LocatedDevice device) {
        // Nothing to do.
    }

    /**
     * Gets the list of stored devices.
     * @return the list containing devices
     */
    public List<LocatedDevice> getDevices() {
        Tracked tracked = this.m_tracked; // use local var since we are not synchronized
        if (tracked == null) { // if LocatedDeviceTracker is not open
            return null;
        }
        synchronized (tracked) {
            int length = tracked.size();
            if (length == 0) { return null; }
            List<LocatedDevice> references = new ArrayList<LocatedDevice>(length);
            references.addAll(tracked.values());
            // The resulting array is sorted by ranking.
            return references;
        }
    }

    /**
     * Returns the number of devices being tracked by this LocatedDeviceTracker object.
     * @return the Number of devices being tracked.
     */
    public int size() {
        Tracked tracked = this.m_tracked; //use local var since we are not synchronized
        if (tracked == null) { /* if LocatedDeviceTracker is not open */
            return 0;
        }
        return tracked.size();
    }

    /**
     * Inner class to track services. If a LocatedDeviceTracker object is reused (closed then reopened), then a new Tracked object is used. This class is a hashtable mapping ServiceReference object -> customized Object. This
     * class is the ServiceListener object for the tracker. This class is used to synchronize access to the tracked services. This is not a public class. It is only for use by the implementation of the LocatedDeviceTracker
     * class.
     */
    class Tracked extends HashMap<String, LocatedDevice> {

        /**
         * The list of ServiceReferences in the process of being added. This is used to deal with nesting of ServiceEvents. Since ServiceEvents are synchronously delivered, ServiceEvents can be nested. For example, when processing the adding of a service
         * and the customizer causes the service to be unregistered, notification to the nested call to untrack that the service was unregistered can be made to the track method. Since the ArrayList implementation is not synchronized, all access to
         * this list must be protected by the same synchronized object for thread safety.
         */
        private List<LocatedDevice> m_adding;

        /**
         * <code>true</code> if the tracked object is closed. This field is volatile because it is set by one thread and read by another.
         */
        private volatile boolean m_closed;

        /**
         * The Initial list of ServiceReferences for the tracker. This is used to correctly process the initial services which could become unregistered before they are tracked. This is necessary since the initial set of tracked services are not
         * "announced" by ServiceEvents and therefore the ServiceEvent for unregistration could be delivered before we track the service. A service must not be in both the initial and adding lists at the same time. A service must be moved from the
         * initial list to the adding list "atomically" before we begin tracking it. Since the LinkedList implementation is not synchronized, all access to this list must be protected by the same synchronized object for thread safety.
         */
        private List<LocatedDevice> m_initial;

        /**
         * Context Manager used to track device events.
         */
        private ContextManager m_contextMgr;

        private List<LocatedDevice> m_listenedDevices = new ArrayList<LocatedDevice>();

        /**
         * Tracked constructor.
         */
        protected Tracked() {
            super();
            m_closed = false;
            m_adding = new ArrayList<LocatedDevice>(6);
            m_initial = new LinkedList<LocatedDevice>();
        }

        /**
         * Sets initial list of services into tracker before ServiceEvents begin to be received. This method must be called from LocatedDeviceTracker.open while synchronized on this object in the same synchronized block as the addServiceListener call.
         * @param devices The initial list of services to be tracked.
         */
        protected void setInitialDevices(LocatedDevice[] devices) {
            if (devices == null) { return; }
            int size = devices.length;
            m_initial.addAll(Arrays.asList(devices).subList(0, size));
        }

        /**
         * Tracks the initial list of services. This is called after ServiceEvents can begin to be received. This method must be called from LocatedDeviceTracker.open while not synchronized on this object after the addServiceListener call.
         */
        protected void trackInitialDevices() {
            logger.trace("[DeviceTracker] tracking initial devices");
            while (true) {
                LocatedDevice device;
                synchronized (this) {
                    if (m_initial.isEmpty()) { //  if there are no more initial services
                        return; // we are done
                    }

                    // move the first service from the initial list to the adding list within this synchronized block.
                    device = (LocatedDevice) ((LinkedList) m_initial).removeFirst();
                    if (this.containsKey(device.getSerialNumber())) { //Check if the device is already tracked.
                        //if we are already tracking this service
                        continue; /* skip this service */
                    }
                    if (m_adding.contains(device)) {
                        // if this service is already in the process of being added.
                        continue; // skip this service
                    }
                    // m_adding.add(device);
                }
                m_globalDeviceListener.deviceAdded(device);
                // trackAdding(device); // Begin tracking it. We call trackAdding since we have already put the reference in the adding list.
            }
        }

        /**
         * Called by the owning LocatedDeviceTracker object when it is closed.
         */
        protected void close() {
            m_closed = true;
        }

        /**
         * Begins to track the specified device.
         * @param device the device to be tracked.
         */
        protected void trackIfMatch(LocatedDevice device) {
            boolean alreadyTracked = isTracked(device);
            String deviceSerialNumber = device.getSerialNumber();


            if (alreadyTracked) { // we are already tracking the device
                // Call customizer outside of synchronized region

                return;
            }
            if (!m_propFilter.match(device))
                return; // should not be tracked if mandatory properties do not exist

            synchronized (this) {
                if (m_adding.contains(device)) { // if this service is already in the process of being added.
                    return;
                }
                m_adding.add(device); // mark this service is being added
            }

            trackAdding(device); // call trackAdding now that we have put the device in the adding list
        }

        /**
         * Common logic to add a device to the tracker used by track and trackInitialDevices.
         * The specified device must have been placed in the adding list before calling this method.
         * @param device the device to be tracked.
         */
        private void trackAdding(LocatedDevice device) {
            logger.trace("[DeviceTracker] device tracked " + device.getSerialNumber());
            boolean mustBeTracked = false;
            boolean becameUntracked = false;
            boolean mustCallAdded = false;
            //Call customizer outside of synchronized region
            try {
                mustBeTracked = m_customizer.addingDevice(device);
            } finally {
                synchronized (this) {
                    if (m_adding.remove(device)) { // if the device was not untracked during the customizer callback
                        if (mustBeTracked) {
                            this.put(device.getSerialNumber(), device);
                            mustCallAdded = true;
                        }
                    } else {
                        becameUntracked = true;
                        // If already get during the customizer callback
                    }
                }
            }

            // Call customizer outside of synchronized region
            if (becameUntracked) {
                // The service became untracked during the customizer callback.
                m_customizer.removedDevice(device);
            } else {
                if (mustCallAdded) {
                    m_customizer.addedDevice(device);
                }
            }
        }

        /**
         * Discontinues tracking the device.
         * Do nothing if specified device is not tracked.
         * @param device the tracked device.
         */
        protected void untrack(LocatedDevice device, boolean onlyIfNotMatch) {
            if (onlyIfNotMatch && m_propFilter.match(device))
                return;

            synchronized (this) {
                if (m_initial.remove(device)) { // if this device is already in the list of initial references to process
                    return; // we have removed it from the list and it will not be processed
                }

                if (m_adding.remove(device)) { // if the device is in the process of being added
                    return; // in case the device is untracked while in the process of adding
                }

                String deviceSerialNumber = device.getSerialNumber();
                boolean isTraked = isTracked(device); // Check if we was tracking the reference
                this.remove(deviceSerialNumber); // must remove from tracker before calling customizer callback

                if (!isTraked)
                    return;
            }
            // Call customizer outside of synchronized region and only if we are not closed
            if (! m_closed) {
            	m_customizer.removedDevice(device);
            }
            // If the customizer throws an unchecked exception, it is safe to let it propagate
        }

        public synchronized void startTracking(ContextManager contextMgr) {
            m_contextMgr = contextMgr;
            m_contextMgr.addListener(m_globalDeviceListener);

            List<LocatedDevice> devices = m_contextMgr.getDevices();
            List<LocatedDevice> initialMatchingDevices = new ArrayList<LocatedDevice>(devices);                       
            setInitialDevices(initialMatchingDevices.toArray(new LocatedDevice[initialMatchingDevices.size()]));            
        }

        public synchronized void stopTracking() {
            try {
                m_contextMgr.removeListener(m_globalDeviceListener);
            } catch (Exception e) {
                // ignore it
            }

            List<LocatedDevice> locatedDevices = null;
            synchronized (m_listenedDevices){
                locatedDevices = new ArrayList<LocatedDevice>(m_listenedDevices);
            }

            for (LocatedDevice device : locatedDevices) {
                m_globalDeviceListener.deviceRemoved(device);
            }
        }

        private synchronized boolean isTracked(LocatedDevice device) {
            return this.containsKey(device.getSerialNumber());
        }

        /*
        * Device listener used to filter on device type
        */
        private LocatedDeviceListener m_deviceListener = new EmptyLocatedDeviceListener() {

            @Override
            public void deviceAdded(LocatedDevice device) {
                // do nothing, already taken into account by type listener
            }

            @Override
            public void deviceRemoved(LocatedDevice device) {
                // do nothing, already taken into account by type listener
            }

            @Override
            public void deviceMoved(LocatedDevice device, Position oldPosition, Position newPosition) {
                if (isTracked(device))
                    m_customizer.movedDevice(device, oldPosition, newPosition);
            }

            @Override
            public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue, Object newValue) {
                if (isTracked(device))
                    m_customizer.modifiedDevice(device, propertyName, oldValue, newValue);
                //TODO manage case of more complex property filter based on prop values
            }

            @Override
            public void devicePropertyAdded(LocatedDevice device, String propertyName) {
                trackIfMatch(device);
            }

            @Override
            public void devicePropertyRemoved(LocatedDevice device, String propertyName) {
                untrack(device, true);
            }
        };

        /*
         * Located Device listener used to filter on device properties
         */
        private LocatedDeviceListener m_globalDeviceListener = new EmptyLocatedDeviceListener() {

            @Override
            public void deviceAdded(LocatedDevice device) {
                logger.trace("[DeviceTracker] device appear " + device.getSerialNumber());
                if (m_typeFilter.match(device)) {
                    synchronized (m_listenedDevices) {
                        if (!m_listenedDevices.contains(device)) {
                            m_listenedDevices.add(device);
                            device.addListener(m_deviceListener);
                            trackIfMatch(device);
                        }
                    }
                }
            }

            @Override
            public void deviceRemoved(LocatedDevice device) {
                logger.trace("[DeviceTracker] device disappear " + device.getSerialNumber());
                if (m_typeFilter.match(device)) {
                    synchronized (m_listenedDevices) {
                        if (m_listenedDevices.contains(device)) {
                            m_listenedDevices.remove(device);
                            device.removeListener(m_deviceListener);
                            untrack(device, false);
                        }
                    }
                }
            }
        };
    }

    class DeviceTypeFilter implements LocatedDeviceFilter {

        private Class _clazz;

        public DeviceTypeFilter(Class clazz) {
            _clazz = clazz;
        }

        @Override
        public boolean match(LocatedDevice device) {
            GenericDevice deviceObj = device.getDeviceObject();
            if (deviceObj == null)
                return false;
            return (_clazz.isInstance(deviceObj));
        }
    };

    class DefaultTrueFilter implements LocatedDeviceFilter {

        @Override
        public boolean match(LocatedDevice device) {
            return true;
        }
    }

    class MandatoryPropFilter implements LocatedDeviceFilter {

        private String[] _mandatoryProperties;

        public MandatoryPropFilter(String[] mandatoryProps) {
            _mandatoryProperties = mandatoryProps;
        }

        @Override
        public boolean match(LocatedDevice device) {
            Set<String> deviceProps = device.getProperties();
            for (String prop : _mandatoryProperties) {
                if (!deviceProps.contains(prop))
                    return false;
            }
            return true;
        }
    };
}
