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
package fr.liglab.adele.icasa.context.impl;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.TechnicalService;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.DeviceTypeListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.listener.IcasaListener;
import fr.liglab.adele.icasa.location.*;
import fr.liglab.adele.icasa.location.impl.LocatedDeviceImpl;
import fr.liglab.adele.icasa.location.impl.ZoneImpl;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Provides
@Instantiate(name = "ContextManager-0")
public class ContextManagerImpl implements ContextManager {

	@Requires(optional = true)
	private TechnicalService[] _technicalServices;

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG);

    private Map<String, Zone> zones = new HashMap<String, Zone>();

	private Map<String, LocatedDevice> locatedDevices = new HashMap<String, LocatedDevice>();

	private Map<String, GenericDevice> m_devices = new HashMap<String, GenericDevice>();

    private Map<String, String[]> m_deviceSpecifications = new HashMap<String, String[]>();

	private Map<String, Factory> m_factories = new HashMap<String, Factory>();

	private List<DeviceTypeListener> deviceTypeListeners = new ArrayList<DeviceTypeListener>();

	private List<DeviceListener> deviceListeners = new ArrayList<DeviceListener>();

	private List<LocatedDeviceListener> locatedDeviceListeners = new ArrayList<LocatedDeviceListener>();

	private List<ZoneListener> zoneListeners = new ArrayList<ZoneListener>();

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private Lock readLock = lock.readLock();

	private Lock writeLock = lock.writeLock();

    public ContextManagerImpl() {
		// do nothing
	}

	@Override
	public Zone createZone(String id, int leftX, int topY, int bottomZ, int width, int height, int depth) {
		Zone zone = new ZoneImpl(id, leftX, topY, bottomZ, width, height, depth);
		List<ZoneListener> snapshotZoneListener;
		boolean exists = false;
		readLock.lock();
		try {
			exists = zones.containsKey(id);
		} finally {
			readLock.unlock();
		}
		if (exists) {
            logger.error("Unable to create zone. Already exists " + id);
			throw new IllegalArgumentException("Zone already exist.");
		}
		writeLock.lock();
		try {
			zones.put(id, zone);
			snapshotZoneListener = getZoneListeners();
		} finally {
			writeLock.unlock();
		}
        logger.debug("Creating zone " + id);

		// Listeners notification
		for (ZoneListener listener : snapshotZoneListener) {
			try {
				listener.zoneAdded(zone);
				zone.addListener(listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        //Update the device locations in the given zone.
        Set<LocatedDevice> devicesInZone = getDeviceInZone(id);
        //Update the device locations in the given zone.
        for(LocatedDevice device: devicesInZone ){
            updateDeviceZone(device, Collections.EMPTY_LIST);
        }

        //updateDeviceLocations(zone);
		return zone;
	}

	public Zone createZone(String id, Position center, int detectionScope) {
		return createZone(id, center.x - detectionScope, center.y - detectionScope, center.z - detectionScope,
		      detectionScope * 2, detectionScope * 2, detectionScope * 2);
	}

	@Override
	public void removeZone(String id) {
		Zone zone;
        Set<LocatedDevice> devicesInZone = null;
        List<ZoneListener> snapshotZoneListener;
		writeLock.lock();
		try {
            devicesInZone = getDeviceInZone(id);
			zone = zones.remove(id);
			snapshotZoneListener = getZoneListeners();
		} finally {
			writeLock.unlock();
		}
		if (zone == null){
            logger.warn("Unable to remove zone. It does not exist " + id);
			return;
        }
		
        logger.debug("Removing zone " + id);
		// Listeners notification
		for (ZoneListener listener : snapshotZoneListener) {
			try {
				zone.removeListener(listener);
				listener.zoneRemoved(zone);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        List oldZones = Collections.singletonList(zone);

        //Update the device locations in the given zone.
        for(LocatedDevice device: devicesInZone ){
            updateDeviceZone(device, oldZones);
        }
	}


    /**
     * Updates the device location of a given zone.
     * @param zone The zone
     */

    private void updateDeviceLocations(Zone zone){
        List<LocatedDevice> localDevices = getDevices();
        for (LocatedDevice locatedDevice : localDevices) {
            if (zone.contains(locatedDevice)) {
                // This is done only to force the located device to change its location property value
                Position position = locatedDevice.getCenterAbsolutePosition();
                locatedDevice.setCenterAbsolutePosition(position);
            }
        }
    }

	@Override
	public void moveZone(String id, int leftX, int topY, int bottomZ) throws Exception {
		Zone zone = getZone(id);
		if (zone == null) {
            logger.warn("Unable to move zone. It does not exist " + id);
			return;
		}
        logger.debug("Moving zone " + id);

        Set<LocatedDevice> devicesInZone = getDeviceInZone(id);
        List oldZones = Collections.singletonList(zone);

        Position newPosition = new Position(leftX, topY, bottomZ);
		zone.setLeftTopRelativePosition(newPosition);
        //Update the device locations in the given zone.
        for(LocatedDevice device: devicesInZone ){
            updateDeviceZone(device, oldZones);
        }
        if(devicesInZone.isEmpty()){
            updateDeviceLocations(zone);
        }
	}

	@Override
	public void resizeZone(String id, int width, int height, int depth) throws Exception {
		Zone zone = getZone(id);
		if (zone == null){
            logger.warn("Unable to resize zone. It does not exist " + id);
			return;
        }
        logger.debug("Moving zone " + id);
        Set<LocatedDevice> devicesInZone = getDeviceInZone(id);
        List oldZones = Collections.singletonList(zone);

		zone.resize(width, height, depth);
        //Update the device locations in the given zone.
        for(LocatedDevice device: devicesInZone ){
            updateDeviceZone(device, oldZones);
        }
        if(devicesInZone.isEmpty()){
            updateDeviceLocations(zone);
        }
	}

	@Override
	public void removeAllZones() {
        logger.debug("Removing all zones");
		List<Zone> tempZones = getZones();
		for (Zone zone : tempZones) {
			removeZone(zone.getId());
		}
	}

	@Override
	public void addZoneVariable(String zoneId, String variable) {
		Zone zone = getZone(zoneId);
		if (zone == null){
            logger.error("Unable to add a variable to a zone. It does not exist " + zoneId);
			return;
        }
		zone.addVariable(variable);
	}

	@Override
	public Set<String> getZoneVariables(String zoneId) {
		Zone zone = getZone(zoneId);
		if (zone == null){
            logger.error("Unable to retrieve variables from zone. It does not exist " + zoneId);
			return null;
        }
		return zone.getVariableNames();
	}

	@Override
	public Object getZoneVariableValue(String zoneId, String variable) {
		Zone zone = getZone(zoneId);
		if (zone == null) {
            logger.error("Unable to retrieve variables from zone. It does not exist " + zoneId);
            return null;
        }
		return zone.getVariableValue(variable);
	}

	@Override
	public void setZoneVariable(String zoneId, String variableName, Object value) {
		Zone zone = getZone(zoneId);
		if (zone == null){
            logger.error("Unable to set a variable to a zone. It does not exist " + zoneId);
			return;
        }
		zone.setVariableValue(variableName, value);
	}

	@Override
	public List<Zone> getZones() {
		readLock.lock();
		try {
			return Collections.unmodifiableList(new ArrayList<Zone>(zones.values()));
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Set<String> getZoneIds() {
		readLock.lock();
		try {
			return Collections.unmodifiableSet(new HashSet<String>(zones.keySet()));
		} finally {
			readLock.unlock();
		}

	}

	@Override
	public Zone getZone(String zoneId) {
		readLock.lock();
		try {
			return zones.get(zoneId);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Zone getZoneFromPosition(Position position) {
		List<Zone> tempList = new ArrayList<Zone>();
		List<Zone> zonesSnapshot = getZones();
		for (Zone zone : zonesSnapshot) {
			if (zone.contains(position)) {
				tempList.add(zone);
			}
		}
		if (tempList.size() > 0) {
			Collections.sort(tempList, new ZoneComparable());
			return tempList.get(0);
		}
		return null;
	}

	@Override
	public void setParentZone(String zoneId, String parentId) throws Exception {
		lock.readLock().lock();
		Zone zone = getZone(zoneId);
		Zone parent = getZone(parentId);
		lock.readLock().unlock();
		if (zone == null || parent == null)
			return;
		boolean ok = parent.addZone(zone);
		if (!ok){
            logger.error("Zone does not fit in parent: " + zoneId + " parent: " + parentId);
			throw new Exception("Zone does not fit in its parent");
        }
	}

	@Override
	public Set<String> getDeviceIds() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableSet(new HashSet<String>(locatedDevices.keySet()));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<LocatedDevice> getDevices() {
		lock.readLock().lock();
		try {
			return new ArrayList<LocatedDevice>(locatedDevices.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Position getDevicePosition(String deviceId) {
		LocatedDevice device = getDevice(deviceId);
		if (device != null)
			return device.getCenterAbsolutePosition().clone();
		return null;
	}

	@Override
	public void setDevicePosition(String deviceId, Position position) {

		LocatedDevice device = getDevice(deviceId);

		if (device != null) {
            logger.debug("Setting device position: " + deviceId);
			List<Zone> oldZones = getObjectZones(device);
			device.setCenterAbsolutePosition(position);
			updateDeviceZone(device, oldZones);
		} else {
            logger.error("Unable to set position. Device does not exist: " + deviceId);
        }
	}

    private void updateDeviceZone(LocatedDevice device, List<Zone> oldZones){
            List<Zone> newZones = getObjectZones(device);
            // When the zones are different, the device is notified
            if (!oldZones.equals(newZones)) {
                Collections.sort(newZones, new ZoneComparable());
                if(!oldZones.isEmpty()){
                    device.leavingZones(oldZones);
                }
                if(!newZones.isEmpty()){
                    device.enterInZones(newZones);
                }
            }
    }

	@Override
	public void moveDeviceIntoZone(String deviceId, String zoneId) {
        logger.debug("To move device"+ deviceId + " into zone " + zoneId);
		Position newPosition = getRandomPositionIntoZone(zoneId);
		if (newPosition != null) {
			setDevicePosition(deviceId, newPosition);
		}
	}

	// TODO: Maybe a public method in the interface
	private List<Zone> getObjectZones(LocatedObject object) {
		if (object == null)
			return null;
		List<Zone> allZones = getZones();
		List<Zone> zones = new ArrayList<Zone>();
		for (Zone zone : allZones) {
			if (zone.contains(object))
				zones.add(zone);
		}
		return zones;
	}

	@Override
	public void setDeviceState(String deviceId, boolean value) {

		GenericDevice device = getGenericDevice(deviceId);

		if (device == null && !(device instanceof GenericDevice)) {
            logger.warn("Unable to set state to device. It does not exist: " + deviceId);
			return;
        }
		if (value) {
            logger.debug("Activating device: " + deviceId);
			device.setState(GenericDevice.STATE_ACTIVATED);
        }
		else {
            logger.debug("Deactivating device: " + deviceId);
			device.setState(GenericDevice.STATE_DEACTIVATED);
        }
	}

	@Override
	public LocatedDevice getDevice(String deviceId) {
		lock.readLock().lock();
		try {
			return locatedDevices.get(deviceId);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public GenericDevice getGenericDevice(String deviceId) {
		lock.readLock().lock();
		try {
			return m_devices.get(deviceId);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getDeviceTypes() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableSet(new HashSet<String>(m_factories.keySet()));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getProvidedServices(String deviceType) {
		Factory factory = getFactory(deviceType);
		if (factory == null) {
			return null;
		}
		String[] specifications = factory.getComponentDescription().getprovidedServiceSpecification();
		if (specifications == null) {
			return null;
		}
		return new HashSet(Arrays.asList(specifications));
	}

    @Override
    public Set<String> getProvidedServices(LocatedDevice device) {
        String deviceType = device.getType();
        Set<String> specifications = null;
        if (deviceType != null)
            specifications = getProvidedServices(deviceType);
        if (specifications == null) {
            specifications = new HashSet<String>();
            lock.readLock().lock();
            try {
                String[] deviceSpecifications = m_deviceSpecifications.get(device.getSerialNumber());
                if ((deviceSpecifications != null) && (deviceSpecifications.length > 0)) {
                    for (String deviceSpec : deviceSpecifications) {
                        specifications.add(deviceSpec);
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }

        return specifications;
    }

	@Override
	public Set<Variable> getGlobalVariables() {
		return null; // TODO implement it
	}

	@Override
	public Object getGlobalVariableValue(String variableName) {
		return null; // TODO implement it
	}

	@Override
	public void addGlobalVariable(String variableName) {
		// TODO implement it
	}

	@Override
	public void setGlobalVariable(String variableName, Object value) {
		// TODO implement it
	}

	private Factory getFactory(String deviceType) {
		lock.readLock().lock();
		try {
			return m_factories.get(deviceType);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Bind(id = "devices", aggregate = true, optional = true )
	public void bindDevice(GenericDevice genericDevice, Map<String, Object> properties) {
        String[] specifications = (String[]) properties.get(org.osgi.framework.Constants.OBJECTCLASS);

		String sn = genericDevice.getSerialNumber();
        logger.debug("A new Device OSGi service has appear " + sn);
		if (m_devices.containsKey(sn)) {
            logger.error("A device with the same ID: " + sn + " was already registered");
			return;
		}
		
		boolean contained = false;
		String deviceType = null;
		LocatedDevice locatedDevice = null;
		List<LocatedDeviceListener> snapshotLocatedDeviceListeners = null;
		List<DeviceListener> snapshotDeviceListeners = null;
		if (genericDevice instanceof Pojo) {
			try {
				deviceType = ((Pojo) genericDevice).getComponentInstance().getFactory().getFactoryName();
			} catch (Exception e) {
                logger.error("Unable to get device Type for " + sn);
                e.printStackTrace();
			}
		}
		
		lock.writeLock().lock();
		try {
			m_devices.put(sn, genericDevice);
            m_deviceSpecifications.put(sn, specifications);
			contained = locatedDevices.containsKey(sn);
			if (!contained) {
                logger.debug("Creating a LocatedDevice for " + sn);
				locatedDevice = new LocatedDeviceImpl(sn, new Position(-1, -1), genericDevice, deviceType, this);
				locatedDevices.put(sn, locatedDevice);
				snapshotLocatedDeviceListeners = getLocatedDeviceListeners();
				snapshotDeviceListeners = getDeviceListeners();
			}

		} finally {
			lock.writeLock().unlock();
		}
		// notify only if not already added.
		if (!contained) {
			// Listeners notification
			for (DeviceListener listener : snapshotDeviceListeners) {
				try {
					listener.deviceAdded(genericDevice);
					genericDevice.addListener(listener);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (LocatedDeviceListener listener : snapshotLocatedDeviceListeners) {
				try {
					listener.deviceAdded(locatedDevice);
					locatedDevice.addListener(listener);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// SimulatedDevice listener added
			genericDevice.addListener((LocatedDeviceImpl) locatedDevice);
		}
	}

	@Unbind(id = "devices")
	public void unbindDevice(GenericDevice genericDevice) {
		String sn = genericDevice.getSerialNumber();

        logger.debug("A Device service has disappear " + sn);

        List<LocatedDeviceListener> snapshotLocatedDeviceListeners = null;
		List<DeviceListener> snapshotDeviceListeners = null;
		LocatedDevice locatedDevice;
		lock.writeLock().lock();
		try {
			m_devices.remove(sn);
            m_deviceSpecifications.remove(sn);
			locatedDevice = locatedDevices.remove(sn);
            logger.debug("Removing LocatedDevice for " + sn);
			snapshotLocatedDeviceListeners = getLocatedDeviceListeners();
			snapshotDeviceListeners = getDeviceListeners();
		} finally {
			lock.writeLock().unlock();
		}

		// Listeners notification
		for (DeviceListener listener : snapshotDeviceListeners) {
			try {
				listener.deviceRemoved(genericDevice);
				genericDevice.removeListener(listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (LocatedDeviceListener listener : snapshotLocatedDeviceListeners) {
			try {
				// If two devices with the same ID where registered the locatedDevice can be null in one of unBinds callbacks
				if (locatedDevice!=null) {
					listener.deviceRemoved(locatedDevice);
					locatedDevice.removeListener(listener);					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// SimulatedDevice listener removed
		genericDevice.removeListener((LocatedDeviceImpl) locatedDevice);
	}

	@Bind(id = "factories", aggregate = true, optional = true, filter = "(component.providedServiceSpecifications=fr.liglab.adele.icasa.device.GenericDevice)")
	public void bindFactory(Factory factory) {
		String deviceType = factory.getName();
        logger.debug("A new Device Type has appear " + deviceType);
		List<DeviceTypeListener> snapshotListeners = null;
		lock.writeLock().lock();
		try {
			m_factories.put(deviceType, factory);
			snapshotListeners = getDeviceTypeListeners();
		} finally {
			lock.writeLock().unlock();
		}

		// Listeners notification
		for (DeviceTypeListener listener : snapshotListeners) {
			try {
				listener.deviceTypeAdded(deviceType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Unbind(id = "factories")
	public void unbindFactory(Factory factory) {
		String deviceType = factory.getName();
        logger.debug("A Device Type has disappear " + deviceType);
        List<DeviceTypeListener> snapshotListeners = null;
		lock.writeLock().lock();
		try {
			m_factories.remove(deviceType);
			snapshotListeners = getDeviceTypeListeners();
		} finally {
			lock.writeLock().unlock();
		}
		// Listeners notification
		for (DeviceTypeListener listener : snapshotListeners) {
			try {
				listener.deviceTypeRemoved(deviceType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addListener(IcasaListener listener) {

		if (listener instanceof ZoneListener) {
			ZoneListener zoneListener = (ZoneListener) listener;
			List<Zone> snapshotZones = null;
			lock.writeLock().lock();
			try {
				zoneListeners.add(zoneListener);
				snapshotZones = getZones();
			} finally {
				lock.writeLock().unlock();
			}
			for (Zone zone : snapshotZones)
				zone.addListener(zoneListener);
		}

		if (listener instanceof DeviceListener) {
			DeviceListener deviceListener = (DeviceListener) listener;
			List<LocatedDevice> snapshotDevices;
			writeLock.lock();
			try {
				deviceListeners.add(deviceListener);
				snapshotDevices = getDevices();
			} finally {
				writeLock.unlock();
			}
			for (LocatedDevice device : snapshotDevices) {
				GenericDevice deviceObj = device.getDeviceObject();
				if (deviceObj != null)
					deviceObj.addListener(deviceListener);
			}
		}

		if (listener instanceof LocatedDeviceListener) {
			LocatedDeviceListener deviceListener = (LocatedDeviceListener) listener;
			List<LocatedDevice> snapshotDevices;
			writeLock.lock();
			try {
				locatedDeviceListeners.add(deviceListener);
				snapshotDevices = getDevices();
			} finally {
				writeLock.unlock();
			}
			for (LocatedDevice device : snapshotDevices) {
				device.addListener(deviceListener);
			}
		}

		if (listener instanceof DeviceTypeListener) {
			DeviceTypeListener deviceTypeListener = (DeviceTypeListener) listener;
			writeLock.lock();
			try {
				deviceTypeListeners.add(deviceTypeListener);
			} finally {
				writeLock.unlock();
			}
		}

	}

	@Override
	public void removeListener(IcasaListener listener) {
		if (listener instanceof ZoneListener) {
			ZoneListener zoneListener = (ZoneListener) listener;
			List<Zone> zoneListSnapshot;
			writeLock.lock();
			try {
				zoneListSnapshot = getZones();
				zoneListeners.remove(zoneListener);
			} finally {
				writeLock.unlock();
			}
			for (Zone zone : zoneListSnapshot) {
				zone.removeListener(zoneListener);
			}
		}

		if (listener instanceof DeviceListener) {
			DeviceListener deviceListener = (DeviceListener) listener;
			List<LocatedDevice> locatedDeviceListSnapshot;
			writeLock.lock();
			try {
				locatedDeviceListSnapshot = getDevices();
				deviceListeners.remove(deviceListener);
			} finally {
				writeLock.unlock();
			}
			for (LocatedDevice device : locatedDeviceListSnapshot) {
				GenericDevice deviceObj = device.getDeviceObject();
				if (deviceObj != null)
					deviceObj.removeListener(deviceListener);
			}
		}

		if (listener instanceof LocatedDeviceListener) {
			LocatedDeviceListener deviceListener = (LocatedDeviceListener) listener;
			List<LocatedDevice> locatedDeviceListSnapshot;
			writeLock.lock();
			try {
				locatedDeviceListSnapshot = getDevices();
				locatedDeviceListeners.remove(deviceListener);
			} finally {
				writeLock.unlock();
			}
			for (LocatedDevice device : locatedDeviceListSnapshot) {
				device.removeListener(deviceListener);
			}
		}

		if (listener instanceof DeviceTypeListener) {
			DeviceTypeListener deviceTypeListener = (DeviceTypeListener) listener;
			writeLock.lock();
			try {
				deviceTypeListeners.remove(deviceTypeListener);
			} finally {
				writeLock.unlock();
			}
		}
	}

	private int random(int min, int max) {
		// final double range = (max - 10) - (min + 10);
		final double range = max - min;
		if (range <= 0.0) {
			throw new IllegalArgumentException("min >= max" + min + ">=" + max );
		}
		return min + (int) (range * Math.random());
	}

	private Position getRandomPositionIntoZone(String zoneId) {
		Zone zone = getZone(zoneId);
		if (zone == null)
			return null;
		int minX = zone.getLeftTopAbsolutePosition().x;
		int minY = zone.getLeftTopAbsolutePosition().y;
        int marge = GenericDevice.DEFAULT_HEIGHT/2;
		int newX = random(minX + marge, minX + zone.getXLength() - marge);
		int newY = random(minY + marge, minY + zone.getYLength() - marge);

		return new Position(newX, newY);
	}

	@Override
	public void resetContext() {
		removeAllZones();
	}

	private List<ZoneListener> getZoneListeners() {
		readLock.lock();
		try {
			return new ArrayList<ZoneListener>(zoneListeners);
		} finally {
			readLock.unlock();
		}
	}

	private List<DeviceTypeListener> getDeviceTypeListeners() {
		readLock.lock();
		try {
			return new ArrayList<DeviceTypeListener>(deviceTypeListeners);
		} finally {
			readLock.unlock();
		}
	}

	private List<LocatedDeviceListener> getLocatedDeviceListeners() {
		readLock.lock();
		try {
			return new ArrayList<LocatedDeviceListener>(locatedDeviceListeners);
		} finally {
			readLock.unlock();
		}
	}

	private List<DeviceListener> getDeviceListeners() {
		readLock.lock();
		try {
			return new ArrayList<DeviceListener>(deviceListeners);
		} finally {
			readLock.unlock();
		}
	}

	private class ZoneComparable implements Comparator<Zone> {
		@Override
		public int compare(Zone zone0, Zone zone1) {
			return zone1.getLayer() - zone0.getLayer();
		}
	}

    /**
     * Get the list of LocatedDevice contained in a given zone.
     * @param zoneId the zone identifier
     * @return a Set of all the devices contained in a given zone. If any, return an empty set.
     */
    private Set<LocatedDevice> getDeviceInZone(String zoneId){
        Set<LocatedDevice> containedDevices = new HashSet<LocatedDevice>();

        Zone zone = getZone(zoneId);
        List<LocatedDevice> devices = getDevices();
        for (LocatedDevice device : devices) {
            if (zone.contains(device)){
                containedDevices.add(device);
            }
        }
        return containedDevices;
    }
}
