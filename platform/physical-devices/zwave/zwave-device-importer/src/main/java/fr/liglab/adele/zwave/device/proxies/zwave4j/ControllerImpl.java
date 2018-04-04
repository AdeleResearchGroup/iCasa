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
package fr.liglab.adele.zwave.device.proxies.zwave4j;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.zwave.device.api.ZwaveController;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.importer.DeviceDeclaration;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryIntrospection;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@ContextEntity(coreServices = { ZwaveController.class, ZwaveDevice.class})
@Provides(specifications = { DiscoveryService.class, DiscoveryIntrospection.class })

public class ControllerImpl extends AbstractDiscoveryComponent implements ZwaveDevice, ZwaveController, NotificationWatcher, ControllerCallback {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImpl.class);

	/**
	 * The zwave4j manager
	 */
	private Manager manager;

	private AtomicBoolean inTransition = new AtomicBoolean(false);

	private ZwaveController.Mode modeRequest = Mode.NORMAL;

	/**
	 * Whether the manager's serial port driver was properly initialized
	 */
	@Controller
	private boolean driverOk = true;

	/**
	 * The configured serial port
	 */
	@ContextEntity.State.Field(service = ZwaveController.class, state = ZwaveController.SERIAL_PORT, directAccess = true)
	private String serialPort;

	@Property(name=fr.liglab.adele.cream.model.ContextEntity.CONTEXT_ENTITY_ID)
	private String contextId;
	/**
	 * The network identifier of the controller
	 */
	@ContextEntity.State.Field(service = ZwaveDevice.class, state = ZwaveDevice.HOME_ID, directAccess=true)
	private int zwaveHomeId;

	@ContextEntity.State.Field(service = ZwaveDevice.class, state = ZwaveDevice.NODE_ID)
	private short zwaveNodeId;

	@ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.NODE_ID)
	Supplier<Short> pullNodeId = () -> {
		return zwaveHomeId != -1 ? manager.getControllerNodeId(zwaveHomeId) : -1;
	};

	@ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.MANUFACTURER_ID)
	private int manufacturerId;

	@ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.MANUFACTURER_ID)
	Supplier<Integer> pullManufactererId = () -> {
		String value = zwaveHomeId != -1 ? manager.getNodeManufacturerId(zwaveHomeId,zwaveNodeId) : null;
		return value != null ? hex(value) : -1;
	};

	@ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_TYPE)
	private int deviceType;

	@ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_TYPE)
	Supplier<Integer> pullDeviceType = () -> {
		String value = zwaveHomeId != -1 ? manager.getNodeProductType(zwaveHomeId,zwaveNodeId) : null;
		return value != null ? hex(value) : -1;
	};

	@ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_ID)
	private int deviceId;

	@ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_ID)
	Supplier<Integer> pullDeviceId =  () -> {
		String value = zwaveHomeId != -1 ? manager.getNodeProductId(zwaveHomeId,zwaveNodeId) : null;
		return value != null ? hex(value) : -1;
	};


	/**
	 * Whether this is the master controller
	 */
	@ContextEntity.State.Field(service = ZwaveController.class, state = ZwaveController.MASTER)
	private boolean master;

	@ContextEntity.State.Pull(service = ZwaveController.class,state = ZwaveController.MASTER)
	Supplier<Boolean> pullMaster=()-> zwaveHomeId != -1 ? manager.isPrimaryController(zwaveHomeId) : false;

	/**
	 * Relation to the neighbor devices
	 */

	@Creator.Field("isZwaveNeighbor")
	Creator.Relation<ZwaveDevice, ZwaveDevice> neighborsRelationCreator;

	/**
	 * The identifiers of neighbor devices
	 */
	@ContextEntity.State.Field(service = ZwaveDevice.class, state = ZwaveDevice.NEIGHBORS)
	private List<Integer> neighbors;

	@ContextEntity.State.Push(service = ZwaveDevice.class, state = ZwaveDevice.NEIGHBORS)
	public List<Integer> pushNeighbors() {
		List<Integer> neighbors = new ArrayList<>();
		for (ZwaveDevice device : neighborDevices) {
			neighbors.add(device.getNodeId());
		}
		return neighbors;
	}

	@ContextEntity.Relation.Field(value = "isZwaveNeighbor", owner = ZwaveDevice.class)
	@Requires(id = "zwavesNeighbors", specification = ZwaveDevice.class, optional = true)
	private List<ZwaveDevice> neighborDevices;

	@Bind(id = "zwavesNeighbors")
	public void bindZDevice(ZwaveDevice device) {
		pushNeighbors();
	}

	@Unbind(id = "zwavesNeighbors")
	public void unbindZDevice(ZwaveDevice device) {
		pushNeighbors();
	}

	/**
	 * The current operation mode
	 */

	private ZwaveController.Mode currentMode;

	/**
	 * State variable to track or request change of mode
	 */
	@ContextEntity.State.Field(service = ZwaveController.class, state = ZwaveController.MODE)
	private ZwaveController.Mode mode;

	@ContextEntity.State.Apply(service = ZwaveController.class,state = ZwaveController.MODE)
	private Consumer<ZwaveController.Mode> requestChangeMode =  (requestedMode) -> requestChangeMode(requestedMode);

	@ContextEntity.State.Push(service = ZwaveController.class, state = ZwaveController.MODE)
	public ZwaveController.Mode changeModeNotification(ZwaveController.Mode newMode) {
		LOG.debug("Zwave mode changed "+newMode);
		return newMode;
	}


	/**
	 * Configuration parameters
	 */
	private final File		configDirectory;
	private final String 	optionsValue;


	/**
	 * Constructor
	 */
	protected ControllerImpl(BundleContext bundleContext) {
		super(bundleContext);

		optionsValue 		= optional(bundleContext.getProperty("zwave4j.options"),"");
		configDirectory		= optional(bundleContext.getProperty("zwave4j.config.directory"),new File("conf","zwave4j"),File::new);

		LOG.debug("Zwave zwave4j : config directory = "+configDirectory+ " options = "+optionsValue);

		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(ZWave4j.class.getClassLoader());
			NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

			Options options = Options.create(configDirectory.getPath(),configDirectory.getPath(), optionsValue);
			options.addOptionBool("Logging", false);
			options.addOptionBool("ConsoleOutput", false);
			options.lock();

		}
		finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}


	}

	private static final <T> T optional(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	private static final <S,T> T optional(S value, T defaultValue, Function<S, T> map) {
		return value != null ? map.apply(value) : defaultValue;
	}

	/**
	 *
	 * SERVICES
	 */
	@Override
	public boolean isMaster() {
		return master;
	}

	public ZwaveController.Mode getMode() {
		return mode;
	}

	public void demandChangeMode(ZwaveController.Mode requestedMode) {
		this.mode = requestedMode;
	}

	@Override
	public List<Integer> getNeighbors() {
		return neighbors;
	}

	@Override
	public int getHomeId() {
		return (int) zwaveHomeId;
	}

	@Override
	public int getNodeId() {
		return zwaveNodeId;
	}

	@Override
	public int getManufacturerId() {
		return manufacturerId;
	}

	@Override
	public int getDeviceId() {
		return deviceId;
	}

	@Override
	public int getDeviceType() {
		return deviceType;
	}

	private ManagerThread launcher;

	/**
	 * LifeCycle
	 */
	@Validate
	protected synchronized void start() {

		zwaveHomeId = -1;

		launcher	= new ManagerThread(serialPort,this);
		launcher.start();

		manager		= launcher.getManager();

		modeRequest = Mode.NORMAL;
		inTransition= new AtomicBoolean(false);
		currentMode = changeModeNotification(ZwaveController.Mode.NORMAL);

		super.start();
	}

	@Invalidate
	protected synchronized void stop() {
		super.stop();

		launcher.invalidate();
		manager = null;

	}

	private static class ManagerThread extends Thread {

		private Manager 					manager;
		private CountDownLatch 				initialization;
		private CountDownLatch 				termination;

		private final String 				serialPort;
		private final NotificationWatcher 	watcher;

		public ManagerThread(String serialPort,NotificationWatcher watcher) {

			super("Zwave4jManagerThread");

			this.manager 			= null;
			this.initialization		= new CountDownLatch(1);
			this.termination		= new CountDownLatch(1);

			this.serialPort			= serialPort;
			this.watcher			= watcher;

			this.setContextClassLoader(ZWave4j.class.getClassLoader());
		}

		public Manager getManager() {
			try {
				initialization.await();
				return manager;
			} catch (InterruptedException e) {
				return null;
			}

		}

		public void invalidate() {
			termination.countDown();
		}

		@Override
		public void run() {

			/*
			 * Initialize manager
			 */

			manager = Manager.create();
	        
	        /*
	         * signal initialization
	         */
			initialization.countDown();
	        
	        /*
	         * Try to access network
	         */
			manager.addWatcher(watcher,null);
			manager.addDriver(serialPort);

	        /*
	         * Wait for termination order
	         */

			try {
				termination.await();
				
				/*
				 * remove listeners
				 */
				manager.removeWatcher(watcher, null);
				manager.removeDriver(serialPort);

				manager	= null;
		        
		        /*
		         * dispose managers
		         */
				Manager.destroy();

			} catch (InterruptedException e) {
			}

		}


	}

	@Override
	public String getName() {
		return "Zwave4jDeviceDiscovery";
	}

	/**
	 * Handle mode change requests by sending the appropriate command to the controller and tracking
	 * progress of the operation.
	 *
	 */


	private void requestChangeMode(ZwaveController.Mode requestedMode) {

		LOG.debug("Zwave demand of change on " + requestedMode );

		if (inTransition.compareAndSet(false,true)) {
			LOG.debug("NOT IN TRANSITION, request mode " + requestedMode + " actual mode " + currentMode);

			switch (currentMode) {

				case NORMAL:
					switch (requestedMode) {
						case EXCLUSION:
							modeRequest = Mode.EXCLUSION;
							manager.beginControllerCommand(zwaveHomeId, ControllerCommand.REMOVE_DEVICE, this, true);
							break;
						case INCLUSION:
							modeRequest = Mode.INCLUSION;
							manager.beginControllerCommand(zwaveHomeId, ControllerCommand.ADD_DEVICE, this, true);
							break;
						case NORMAL:
							LOG.warn(" DO NOTHING ");
							inTransition.set(false);
							break;
					}

					break;

				case INCLUSION:
					switch (requestedMode) {
						case NORMAL:
							LOG.warn(" Demand of cancel actual mode");
							modeRequest = Mode.NORMAL;
							manager.cancelControllerCommand(zwaveHomeId);
							break;
						default:
							LOG.warn(" DO NOTHING ");
							inTransition.set(false);
							break;
					}

					break;

				case EXCLUSION:
					switch (requestedMode) {
						case NORMAL:
							LOG.warn(" Demand of cancel actual mode");
							modeRequest = Mode.NORMAL;
							manager.cancelControllerCommand(zwaveHomeId);
							break;
						default:
							LOG.warn(" DO NOTHING ");
							inTransition.set(false);
							break;
					}
					break;

			}
		}else {
			LOG.warn(" Zwave dongle is on transition, retry later waiting to change for " + modeRequest );
		}
	}

	/**
	 * Tracks mode change progress.
	 *
	 * Inclusion/Exclusion is automatically left when a device is added/removed, or otherwise
	 * a failure is reported by the controller.
	 *
	 * Notice however that report messages from the network are not processed in Inclusion/Exclusion
	 * mode (while the controller is waiting for some user action) , so some form of timeout has to
	 * be handled outside this controller.
	 *
	 */

	@Override
	public void onCallback(ControllerState state, ControllerError err, Object context) {
		LOG.warn(" Callback received  " + currentMode + " state received " + state + " request mode " + modeRequest);
		switch (currentMode) {
			case INCLUSION:
			case EXCLUSION:
				switch (state) {
					case COMPLETED:
					case FAILED:
					case CANCEL:
						LOG.warn(" Back to normal");
						modeRequest = Mode.NORMAL;
						currentMode = changeModeNotification(ZwaveController.Mode.NORMAL);
						inTransition.set(false);
						break;
					default:
						break;
				}
				break;
			case NORMAL:
				switch (state) {
					case COMPLETED:
					case FAILED:
					case WAITING:
						LOG.warn(" Change to " + modeRequest);
						currentMode = changeModeNotification(modeRequest);
						inTransition.set(false);
						break;
					default:
						break;
				}
				break;
			default:
				LOG.error(" Default Case ! WEIRD ! ");
				break;
		}
	}

	private Map<NodeReference,Zwave4jDevice> proxies = new ConcurrentHashMap<>();

	@Requires(id="Zwave4jProxies",specification = ZwaveDevice.class,proxy=false, optional=true)
	@ContextRequirement(spec = Zwave4jDevice.class)
	List<Zwave4jDevice> zwave4jDevices;

	@Bind(id="Zwave4jProxies" )
	private void addProxy(ZwaveDevice proxy, Map<String, Object> properties) {

		int homeId = proxy.getHomeId();;
		int nodeId = proxy.getNodeId();

		((Zwave4jDevice)proxy).initialize(manager);
		proxies.put(new NodeReference(homeId,(short)nodeId), ((Zwave4jDevice)proxy));
	}

	@Unbind(id="Zwave4jProxies")
	private void removeProxy(ZwaveDevice proxy, Map<String, Object> properties) {

		int homeId = proxy.getHomeId();;
		int nodeId = proxy.getNodeId();

		proxies.remove(new NodeReference(homeId, (short)nodeId));

	}

	/**
	 * Handle notifications from the controller to perform device discovery and initialization
	 */
	@Override
	public void onNotification(Notification notification, Object context) {
		switch (notification.getType()) {
			case DRIVER_READY:
				synchronized (this) {
					LOG.debug("Driver ready home id: "+notification.getHomeId());
					zwaveHomeId = ((Long)notification.getHomeId()).intValue();
				}
				break;
			case DRIVER_FAILED:
				LOG.error("Driver failed");
				driverOk = false;
				break;
			case DRIVER_RESET:
				LOG.error("Driver reset");
				break;

			case AWAKE_NODES_QUERIED:
			case ALL_NODES_QUERIED:
			case ALL_NODES_QUERIED_SOME_DEAD:
				LOG.debug("Nodes queried saving configuration");
				manager.writeConfig(zwaveHomeId);
				break;


			case NODE_NEW:
				LOG.debug("Node new node id: "+notification.getNodeId());
				break;
			case NODE_ADDED:
				LOG.debug("Node added node id: "+notification.getNodeId());
				break;
			case NODE_NAMING:
				LOG.debug("Node named node id: "+notification.getNodeId());
				createDeclaration((int)notification.getHomeId(),notification.getNodeId());
				break;

			case ESSENTIAL_NODE_QUERIES_COMPLETE:
				LOG.debug("Node essential queries completed node id: "+notification.getNodeId());
				break;
			case NODE_QUERIES_COMPLETE:
				LOG.debug("Node complete queries completed node id: "+notification.getNodeId());
				manager.writeConfig(zwaveHomeId);
				updateNodeNeighbors((int)notification.getHomeId(),notification.getNodeId());
				break;

			case NODE_REMOVED:
				LOG.debug("Node removed node id: "+notification.getNodeId());
				manager.writeConfig(zwaveHomeId);
				removeDeclaration((int)notification.getHomeId(),notification.getNodeId());
				removeNodeNeighbors((int)notification.getHomeId(),notification.getNodeId());
				break;


			case NOTIFICATION:
			case NODE_EVENT:
			case VALUE_CHANGED:
				LOG.debug("Event "+notification.getType().name()+ " node id "+notification.getNodeId()+ " "+notification.getByte());

				Zwave4jDevice proxy = proxies.get(new NodeReference((int)notification.getHomeId(),notification.getNodeId()));
				if (proxy != null) {
					proxy.notification(manager,notification);
				}
				break;
			//TODO : NODE_EVENT case, inspect what it contains
			default:
				LOG.debug("Event "+notification.getType().name()+ " node id "+notification.getNodeId());
				break;
		}
	}


	private Map<NodeReference,ImportDeclaration> declarations = new ConcurrentHashMap<>();

	private final void createDeclaration(int homeId, short nodeId) {

		ImportDeclaration declaration = ImportDeclarationBuilder.empty()
				.key(DeviceDeclaration.HOME_ID).value(homeId)
				.key(DeviceDeclaration.NODE_ID).value((int)nodeId)
				.key(DeviceDeclaration.DEVICE_MANUFACTURER).value(hex(manager.getNodeManufacturerId(homeId,nodeId)))
				.key(DeviceDeclaration.DEVICE_TYPE).value(hex(manager.getNodeProductType(homeId,nodeId)))
				.key(DeviceDeclaration.DEVICE_ID).value(hex(manager.getNodeProductId(homeId,nodeId)))
				.key("scope").value("generic")
				.key("library").value("zwave4j")
				.build();

		declarations.put(new NodeReference(homeId,nodeId),declaration);
		registerImportDeclaration(declaration);
	}

	private final void removeDeclaration(int homeId, short nodeId) {
		ImportDeclaration declaration = declarations.remove(new NodeReference(homeId,nodeId));
		if (declaration != null) {
			unregisterImportDeclaration(declaration);
		}
	}

	private final void updateNodeNeighbors(int homeId, short nodeId) {

		String nodeContextId = nodeId == zwaveNodeId ? contextId : "ZwaveDevice#"+nodeId;
    	
    	/*
    	 * Calculate the contextIds for the current list of neighbors
    	 */
		Set<String> currentNeighborContextIds	= new HashSet<>();
		List<Relation> relations				= neighborsRelationCreator.getInstancesRelatedTo(nodeContextId);

		for(Relation relation:relations) {
			currentNeighborContextIds.add(relation.getTarget());
		}
        
    	/*
    	 * Calculate the  contextIds for the updated new list of neighbors
    	 */
		Set<String> updatedNeighborContextIds		= new HashSet<>();
		AtomicReference<short[]> neighborNodeIds	= new AtomicReference<>();

		manager.getNodeNeighbors(homeId, nodeId, neighborNodeIds);

		for (int i = 0; i < neighborNodeIds.get().length; i++) {

			short neighborNodeId 		= neighborNodeIds.get()[i];
			String neighborContextId 	= neighborNodeId == zwaveNodeId ? contextId : "ZwaveDevice#"+neighborNodeId;

			updatedNeighborContextIds.add(neighborContextId);
		}
    	
    	/*
    	 * add new neighbors
    	 */
		for (String neighborContextId : updatedNeighborContextIds) {
			if (! currentNeighborContextIds.contains(neighborContextId)) {
				neighborsRelationCreator.create(nodeContextId,neighborContextId);
			}
		}

    	/*
    	 * remove nodes that are no longer neighbors
    	 */
		for (String neighborContextId : currentNeighborContextIds) {
			if (! updatedNeighborContextIds.contains(neighborContextId)) {
				neighborsRelationCreator.delete(nodeContextId,neighborContextId);
			}
		}
	}

	private final void removeNodeNeighbors(int homeId, short nodeId) {

		String removedContextId 	= nodeId == zwaveNodeId ? contextId : "ZwaveDevice#"+nodeId;
		List<Relation> relations	= neighborsRelationCreator.getInstancesRelatedTo(removedContextId);

		for(Relation relation:relations) {
			neighborsRelationCreator.delete(relation.getSource(),relation.getTarget());
			neighborsRelationCreator.delete(relation.getTarget(),relation.getSource());
		}
	}

	private static class NodeReference {
		public final int 	homeId;
		public final short 	nodeId;

		public NodeReference(int homeId, short nodeId) {
			this.homeId = homeId;
			this.nodeId	= nodeId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(homeId,nodeId);
		}

		@Override
		public boolean equals(Object object) {

			if (object instanceof NodeReference) {
				NodeReference that = (NodeReference) object;
				return this.nodeId == that.nodeId && this.homeId == that.homeId;
			}

			return false;
		}
	}

	private final static int hex(String value) {
		return Integer.parseInt(value,16);
	}

}
