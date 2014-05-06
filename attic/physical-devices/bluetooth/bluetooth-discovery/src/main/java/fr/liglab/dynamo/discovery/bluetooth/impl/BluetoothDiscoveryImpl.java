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
package fr.liglab.dynamo.discovery.bluetooth.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.rose.disco.RemoteEntityDescriptionManager;

import fr.liglab.dynamo.discovery.bluetooth.BluetoothDiscovery;
//import fr.liglab.dynamo.repositories.bundle.BundleRepositoryManager;

/**
 * This class is a discovery service for the Bluetooth protocol.
 * FIXME: service that disappears are not tracked for now
 *
 * 
 */
public class BluetoothDiscoveryImpl implements BluetoothDiscovery {
	private class DiscoveryThread extends Thread {
		private boolean end = false;

		synchronized public void inquiryIsFinished() {
			this.notify();
		}

		@Override
		synchronized public void run() {
			while (!end) {
				try {
					// start a bluetooth inquiry
					BluetoothDiscoveryImpl.this.discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);
				} catch (BluetoothStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		synchronized public void stopDiscovery() {
			end = true;
			this.notify();
		}
	}

	/**
	 * This thread enables to start the Bluetooth discovery. If the start fails,
	 * this thread try again, until the discovery thread is created of the
	 * <code>end</code> method is called.
	 *
	 * 
	 */
	private class StarterThread extends Thread {
		private static final long PAUSE_DURATION = 5000;
		private boolean end = false;

		/**
		 * Stops the thread.
		 */
		public void end() {
			end = true;
		}

		@Override
		synchronized public void run() {
			boolean error;
			do {
				error = false;
				try {
					LocalDevice localDevice = LocalDevice.getLocalDevice();
					// display local device address and name
					logger.log(LogService.LOG_DEBUG, "Bluetooth local device, address: '"
							+ localDevice.getBluetoothAddress()
							+ "' friendly name: '"
							+ localDevice.getFriendlyName()
							+ "'.");
					// find devices
					discoveryAgent = localDevice.getDiscoveryAgent();
					logger.log(LogService.LOG_DEBUG, "Starting device inquiry.");

					// start thread that discover Bluetooth devices
					discoveryThread = new DiscoveryThread();
					discoveryThread.start();
				} catch (BluetoothStateException ex) {
					error = true;
					logger.log(LogService.LOG_WARNING, "Unable to find a Bluetooth connection.");
					try {
						Thread.sleep(PAUSE_DURATION);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (!end && error);
		}
	}

	/**
	 * Check if the specified Bluetooth address has already been discovered.
	 * 
	 * @param devices collection of already tracked Bluetooth devices
	 * @param bluetoothAddress Bluetooth address eventually already tracked
	 * @return true if the Bluetooth address has already been discovered, false
	 * otherwise
	 */
	private static boolean isAlreadyTracked(Collection<TrackedBluetoothDevice> devices, String bluetoothAddress) {
		for (TrackedBluetoothDevice device : devices) {
			for (String address : device.getDiscoveredAddresses().keySet()) {
				if (address.equals(bluetoothAddress)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * OSGi bundle context, given to the constructor by iPOJO.
	 */
	private final BundleContext bundleContext;

	/**
	 * Bundle repository manager service, injected by iPOJO.
	 */
//	private BundleRepositoryManager bundleRepositoryManager;
	private DiscoveryAgent discoveryAgent;

	private DiscoveryThread discoveryThread;

	protected final DiscoveryListener listener = new DiscoveryListener() {
		@Override
		public void deviceDiscovered(RemoteDevice device, DeviceClass clazz) {
			String deviceBluetoothAddress = device.getBluetoothAddress();
			// check if the device has already been discovered
			if (isAlreadyTracked(trackedBluetoothDevice, deviceBluetoothAddress)) {
				logger.log(LogService.LOG_INFO, "The Bluetooth device with address '" + device.getBluetoothAddress() + "' is discovered, but it has already been discovered.");
				return;
			}

			String friendlyName = "";
			try {
				friendlyName = device.getFriendlyName(false);
			} catch (IOException e) {
				logger.log(LogService.LOG_WARNING, "Can not get remote service name of '" + device.getBluetoothAddress() + "'.");
			}
			logger.log(LogService.LOG_DEBUG, "bluetooth discovery find a device: "
					+ friendlyName + " - "
					+ device.getBluetoothAddress() + " - "
					+ clazz.getMajorDeviceClass() + ":"
					+ clazz.getMinorDeviceClass() + " - "
					+ clazz.getServiceClasses());

			for (TrackedBluetoothDevice s : trackedBluetoothDevice) {
				try {
					if (friendlyName.startsWith(s.getFriendlyName())) {
						logger.log(LogService.LOG_DEBUG, friendlyName + " Bluetooth device has been discovered.");

						// load the bundle in the OSGi environment
//						try {
////							bundleRepositoryManager.loadBundle(s.getBundleFilename());
//						} catch (BundleException e) {
//							logger.log(LogService.LOG_ERROR, "Unable to start the bundle with the filename '" + s.getBundleFilename() + "'.", e);
//							e.printStackTrace();
//							return;
//						}

						// build the mandatory data to build the description manager
						Map<String, Object> description = s.getConfiguration();
						description.put("ipojo.factory.name", s.getIpojoFactoryName());
						// informs Rose that the Generic Importer has to be used 
						String[] roseConfiguration = { "ipojo.factory" };

						// build the manager
						RemoteEntityDescriptionManager manager = new RemoteEntityDescriptionManager.Builder(new String[0], roseConfiguration).properties(description).id(deviceBluetoothAddress).build();
						manager.register(bundleContext);

						// keep the description manager and the Bluetooth address
						Map<String, RemoteEntityDescriptionManager> discoveredId = s.getDiscoveredAddresses();
						discoveredId.put(deviceBluetoothAddress, manager);
						s.setDiscoveredAddresses(discoveredId);

						break;
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void inquiryCompleted(int arg0) {
			// this method can be called when the component is invalid
			if (BluetoothDiscoveryImpl.this.discoveryThread != null) {
				BluetoothDiscoveryImpl.this.discoveryThread.inquiryIsFinished();
			}
		}

		@Override
		public void servicesDiscovered(int arg0, ServiceRecord[] serviceRecord) {
			// TODO Auto-generated method stub
			logger.log(LogService.LOG_DEBUG, "Bluetooth services discovered: '" + serviceRecord + "'.");
		}

		@Override
		public void serviceSearchCompleted(int arg0, int arg1) {
			// TODO Auto-generated method stub
			logger.log(LogService.LOG_DEBUG, "Bluetooth service search completed.");
		}
	};

	private LogService logger; // log service, injected by iPOJO

	private StarterThread starterThread;

	/**
	 * Bluetooth services to track.
	 * FIXME: the access and modifications of this list must be synchronized
	 */
	private final List<TrackedBluetoothDevice> trackedBluetoothDevice;

	/**
	 * Called by iPOJO.
	 */
	public BluetoothDiscoveryImpl(BundleContext bundleContext) {
		trackedBluetoothDevice = new ArrayList<TrackedBluetoothDevice>();
		this.bundleContext = bundleContext;
	}

	/**
	 * Called by iPOJO when the instance becomes valid.
	 */
	@SuppressWarnings("unused")
	private void start() {
		starterThread = new StarterThread();
		starterThread.start();
	}

	/**
	 * Called by iPOJO when the instance becomes invalid.
	 */
	@SuppressWarnings("unused")
	private void stop() {
		if (discoveryAgent != null) {
			discoveryAgent.cancelInquiry(listener);
			discoveryAgent = null;
		}
		// unregister all Bluetooth service from Rose, unload the bundles
		for (TrackedBluetoothDevice s : trackedBluetoothDevice) {
			for (RemoteEntityDescriptionManager manager : s.getDiscoveredAddresses().values()) {
				manager.unRegister();
//				try {
////					bundleRepositoryManager.unloadBundle(s.getBundleFilename());
//				} catch (BundleException e) {
//					logger.log(LogService.LOG_WARNING, "Unable to unload the bundle named '" + s.getBundleFilename() + "'.");
//					e.printStackTrace();
//				}
			}
		}
		trackedBluetoothDevice.clear();

		// stop the starter thread
		starterThread.end();
		starterThread = null;

		// stop the discovery thread
		if (discoveryThread != null) {
			discoveryThread.stopDiscovery();
			discoveryThread = null;
		}
	}

	@Override
	public void trackBluetoothDevice(String friendlyName, String ipojoComponentName, String bundleFilename, Map<String, Object> configuration) {
		// check if the bluetooth device is already tracked
		for (TrackedBluetoothDevice s : trackedBluetoothDevice) {
			if (s.getFriendlyName().equals(friendlyName)) {
				throw new IllegalArgumentException();
			}
		}
		logger.log(LogService.LOG_DEBUG, "Bluetooth discovery service is starting to track '" + friendlyName + "'.");

		if (discoveryAgent != null) {
			// TODO: have a list that keeps track of the discovered bluetooth device
			// or start the discovery again. See also
			// discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED),
			// discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
			RemoteDevice[] cachedDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED);
			if (cachedDevices != null) {
				int s = cachedDevices.length;
				for (int i = 0; i < s; i++) {
					logger.log(LogService.LOG_DEBUG, "Cached device" + i + ":"
							+ cachedDevices[i].getBluetoothAddress() + ", "
							+ cachedDevices[i]);
				}
			}
			RemoteDevice[] preknownDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
			if (preknownDevices != null) {
				int s = preknownDevices.length;
				for (int i = 0; i < s; i++) {
					logger.log(LogService.LOG_DEBUG, "Cached device" + i + ":"
							+ preknownDevices[i].getBluetoothAddress() + ", "
							+ preknownDevices[i] + ".");
				}
			}
			trackedBluetoothDevice.add(new TrackedBluetoothDevice(friendlyName, ipojoComponentName, bundleFilename, configuration));
			try {
				discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);
			} catch (BluetoothStateException bse) {
				// Display error message
				logger.log(LogService.LOG_WARNING, "Bluetooth inquiry failed.");
				return;
			}
		}
	}

	@Override
	public void untrackBluetoothDevice(String friendlyName) {
		// check if the bluetooth device is really tracked
		boolean found = false;
		for (TrackedBluetoothDevice s : trackedBluetoothDevice) {
			if (s.getFriendlyName().equals(friendlyName)) {
				// notify ROSE that the service becomes untracked
				for (RemoteEntityDescriptionManager manager : s.getDiscoveredAddresses().values()) {
					// unregister the device
					manager.unRegister();

					// unload the bundle from the OSGi environment
//					try {
//						bundleRepositoryManager.unloadBundle(s.getBundleFilename());
//					} catch (BundleException e) {
//						logger.log(LogService.LOG_WARNING, "Unable to unload the bundle named '" + s.getBundleFilename() + "'.");
//						e.printStackTrace();
//					}
				}
				trackedBluetoothDevice.remove(s);
				found = true;
				break;
			}
		}
		logger.log(LogService.LOG_DEBUG, "Bluetooth discovery service is stopping to track '" + friendlyName + "'.");
		if (!found) {
			logger.log(LogService.LOG_ERROR, "The bluetooth device with name '" + friendlyName + "' has not been added.");
			throw new IllegalArgumentException();
		}
	}
}
