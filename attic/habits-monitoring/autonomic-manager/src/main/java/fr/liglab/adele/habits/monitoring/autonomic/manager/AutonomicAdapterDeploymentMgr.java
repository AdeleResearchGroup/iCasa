package fr.liglab.adele.habits.monitoring.autonomic.manager;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.habits.monitoring.autonomic.manager.dbadapter.IDBAdapter;
import fr.liglab.adele.habits.monitoring.autonomic.manager.listeners.DPInfos;
import fr.liglab.adele.habits.monitoring.autonomic.manager.listeners.DPInfosListener;
import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

/**
 *
 */
@Component(name = "AutonomicAdapterDeploymentMgr")
@Instantiate(name = "AutonomicAdapterDeploymentMgr-1")
public class AutonomicAdapterDeploymentMgr implements
		DeviceListener<GenericDevice>, DPInfosListener {

    private static final Logger logger = LoggerFactory
            .getLogger(fr.liglab.adele.icasa.Constants.ICASA_LOG + ".apps.habits-monitoring");
    @RequiresDevice(id = "pushButtonDevices", type = "field", optional = true)
    private PushButton[] pushButtonDevices;
    @RequiresDevice(id = "presenceSensorDevices", type = "field", optional = true)
    private PresenceSensor[] presenceSensorDevices;
    @RequiresDevice(id = "motionSensorDevices", type = "field", optional = true)
    private MotionSensor[] motionSensorDevices;
    
    private Map<String, GenericDevice> devices = new HashMap<String, GenericDevice>();
	private Map<String, Set<String>> uninstalledDevices = new HashMap<String, Set<String>>();

	// DP installer
	@Requires
	private DeploymentAdmin dadmin;

	@Requires
	private PackageAdmin padmin;

	// DB Adapter Service
	@Requires
	private IDBAdapter dbAdapter;

	/**
	 * Private constructor to get bundleContext.
	 * 
	 * @param context
	 */
	private AutonomicAdapterDeploymentMgr(BundleContext context) {
		dbAdapter.addDPInfosListener(this);
	}

	/**
	 * Check if the array of interfaces can be handled by an
	 * 
	 * @param classNames
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean handleNewDevice(Set<String> classNames)
			throws ClassNotFoundException {

		boolean successfulInstallation = false;

		for (String className : classNames) {
			// get dp id related to this interface
			Set<String> dpInfos = new HashSet<String>();
			try {
				Class<?> cl = padmin
						.getExportedPackage(
								className.substring(0,
										className.lastIndexOf(".")))
						.getExportingBundle().loadClass(className);

				if (cl != null && GenericDevice.class.isAssignableFrom(cl)) {
					logger.info(className + " extends GenericDevice");
					dpInfos.add(className);
					logger.info("getting dp id for class : " + className);
					String dpId = dbAdapter.getDeviceAdapterId(dpInfos);
					// check if this dp is already installed
					if (dpId != null) {
						logger.info("related dp id is : " + dpId);
						DeploymentPackage dp = dadmin
								.getDeploymentPackage(dpId);
						if (dp != null) {
							// dp is already installed : do nothing
							logger.info("dp " + dpId + " is already installed.");
						} else {
							// dp is not installed, we should install it
							logger.info(dpId + " is not installed");
							// get url related to this id
							String dpUrl = (String) dbAdapter
									.getDeviceAdapterUrl(dpId);
							logger.info("location of this dp : " + dpUrl);
							URL url = new URL(dpUrl);
							dadmin.installDeploymentPackage(url.openStream());
							successfulInstallation = true;
						}
					} else {
						logger.warn("Could not find any dp to handle "
								+ cl.getName());
					}
				}

			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return successfulInstallation;
	}

	@Override
	public void deviceAdded(GenericDevice genericDevice) {
		devices.put(genericDevice.getSerialNumber(), genericDevice);
		logger.info("device added");
	}

	@Override
	public void deviceRemoved(GenericDevice genericDevice) {
		logger.info("device removed");
	}

	@Override
	public void devicePropertyModified(GenericDevice genericDevice, String s,
			Object oldValue, Object newValue) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void devicePropertyAdded(GenericDevice genericDevice, String s) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void devicePropertyRemoved(GenericDevice genericDevice, String s) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	/**
	 * It is called when a new service of type GenericDevice is registered into
	 * the gateway. (callback method, see metadata.xml).
	 * 
	 * @param detectorRef
	 *            A new GenericDevice (proxy)
	 * @throws ClassNotFoundException
	 */
	@RequiresDevice(id = "pushButtonDevices", type = "bind")
	public void bindPushButtonDevices(PushButton device, Map properties) throws ClassNotFoundException{
		bindManagedDevice(device, properties);
	}

//	@Unbind(id = "GenericDeviceDep")
	@RequiresDevice(id = "pushButtonDevices", type = "unbind")
	public void unbindPushButtonDevice(PushButton device) {
		unbindManagedDevice(device);
	}
	
	@RequiresDevice(id = "motionSensorDevices", type = "bind")
	public void bindMotionSensorDevices(MotionSensor device, Map properties) throws ClassNotFoundException{
		bindManagedDevice(device, properties);
	}

	@RequiresDevice(id = "motionSensorDevices", type = "unbind")
	public void unbindMotionSensorDevice(MotionSensor device) {
		unbindManagedDevice(device);
	}
	
	@RequiresDevice(id="presenceSensorDevices", type="bind")
	public void bindPresenceSensorDevices(PresenceSensor device, Map properties) throws ClassNotFoundException{
		bindManagedDevice(device, properties);
	}
	
	@RequiresDevice(id="presenceSensorDevices", type="unbind")
	public void unbindPresenceSensorDevices(PresenceSensor device){
		unbindManagedDevice(device);
	}
	
	public void bindManagedDevice(GenericDevice device, Map properties) throws ClassNotFoundException{
		String[] classNames = (String[]) properties.get(Constants.OBJECTCLASS);

		// removed GenericDevice and SimulatedDevice from list of classes
		// TODO c'est crade, faut remplacer la chaine SimulatedDevice par autre
		// chose
		Set<String> purifiedClassNames = new HashSet<String>();
		if (classNames != null && classNames.length > 0) {
			for (String className : classNames) {
				if (!className.equals(GenericDevice.class.getName())
						&& !className.contains("Simulated")) {
					purifiedClassNames.add(className);
				}
			}
		}
		logger.info("A new device has been found, id "
				+ device.getSerialNumber());
		device.addListener(this);
		this.deviceAdded(device);
		boolean success = this.handleNewDevice(purifiedClassNames);
		if (!success) {
			uninstalledDevices.put(device.getSerialNumber(),
					purifiedClassNames);
		}
	}
	
	public void unbindManagedDevice(GenericDevice device){
		logger.info("A device has been removed, id "
				+ device.getSerialNumber());
		device.removeListener(this);
		this.deviceRemoved(device);
	}

	@Override
	public void DPInfosAdded(DPInfos addedDP) {
		logger.info("DP INFOS LISTENER : dp added : " + addedDP.getName());

		logger.debug("addedDP classes : " + addedDP.getInterfaces());
		for (Entry<String, Set<String>> entry : uninstalledDevices.entrySet()) {
			logger.debug("uninstalledDevices classes : " + entry.getValue());
			if (addedDP.getInterfaces().containsAll(entry.getValue())) {
				logger.debug("the added dp " + addedDP.getName()
						+ " can handle the device " + entry.getKey());
				try {
					URL url = new URL(addedDP.getUrl());
					dadmin.installDeploymentPackage(url.openStream());
				} catch (Exception e) {
					logger.error("", e);
				}
				break;
			}

		}
	}

	@Override
	public void DPInfosRemoved(DPInfos removedDP) {
		// nothing to do
		logger.info("DP INFOS LISTENER : dp removed : " + removedDP.getName());
	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}
