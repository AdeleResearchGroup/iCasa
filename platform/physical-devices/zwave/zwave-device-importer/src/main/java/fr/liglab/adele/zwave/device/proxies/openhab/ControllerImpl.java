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
package fr.liglab.adele.zwave.device.proxies.openhab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.openhab.binding.zwave.internal.protocol.SerialInterfaceException;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeInfoEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;


import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryIntrospection;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.cream.model.Relation;

import fr.liglab.adele.zwave.device.api.ZwaveController;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;
import fr.liglab.adele.zwave.device.importer.DeviceDeclaration;

@ContextEntity(coreServices = {OpenhabController.class,ZwaveController.class,ZwaveDevice.class})
@Provides(specifications = {DiscoveryService.class, DiscoveryIntrospection.class})

public class ControllerImpl extends AbstractDiscoveryComponent implements ZwaveDevice, ZwaveController, OpenhabController {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerImpl.class);

    /**
     * The underlying openhab dependent manager
     */
    private ControllerManager manager;

    /**
     * The scheduler to handle periodic tasks
     */
    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    ManagedScheduledExecutorService scheduler;

    /**
     * The configured serial port
     */
    @ContextEntity.State.Field(service = ZwaveController.class,state = ZwaveController.SERIAL_PORT, directAccess = true)
    private String serialPort;

    /**
     * Whether this is the master controller
     */
    @ContextEntity.State.Field(service = ZwaveController.class,state = ZwaveController.MASTER)
    private boolean master;

    @ContextEntity.State.Pull(service = ZwaveController.class,state = ZwaveController.MASTER)
    Supplier<Boolean> pullMaster=()-> manager.controller.isMasterController();

    /**
     * The network identifier of the controller
     */
    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.HOME_ID)
    private int zwaveHomeId;

    @ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.HOME_ID)
    Supplier<Integer> pullHome=()-> manager.getHomeId();
    
    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.NODE_ID)
    private int zwaveNodeId;

    @ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.NODE_ID)
    Supplier<Integer> pullNode=()-> manager.getNodeId();

    /**
     * Relation to the neighbor devices 
     */
    @Creator.Field(origin = OriginEnum.local, value = "isZwaveNeighbor") 	Creator.Relation<ZwaveDevice,ZwaveDevice> neighborsRelationCreator;
    
    /**
     * The identifiers of neighbor devices
     */
    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.NEIGHBORS)
    private List<Integer> neighbors;

    @ContextEntity.State.Push(service = ZwaveDevice.class,state = ZwaveDevice.NEIGHBORS)
    private List<Integer> pushNeighbors() {
        List<Integer> neighbors = new ArrayList<>();
        for (ZwaveDevice device : neighborDevices){
            neighbors.add(device.getNodeId());
        }
        return neighbors;
    }   
    
    @ContextEntity.Relation.Field(value = "isZwaveNeighbor",owner = ZwaveDevice.class)
    @Requires(id="zwavesNeighbors",specification=ZwaveDevice.class,optional=true)
    private List<ZwaveDevice> neighborDevices;

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_TYPE)
    private Integer deviceType;

    @ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_TYPE)
    Supplier<Integer> pullDeviceType=()-> manager.controller.getDeviceType();

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.MANUFACTURER_ID)
    private Integer manufacturerId;

    @ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.MANUFACTURER_ID)
    Supplier<Integer> pullManufacturerId=()-> manager.controller.getManufactureId();

    @ContextEntity.State.Field(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_ID)
    private Integer deviceId;

    @ContextEntity.State.Pull(service = ZwaveDevice.class,state = ZwaveDevice.DEVICE_ID)
    Supplier<Integer> pullDeviceId=()-> manager.controller.getDeviceId();

    @Bind(id = "zwavesNeighbors")
    private void bindZDevice(ZwaveDevice device){
        pushNeighbors();
    }

    @Unbind(id= "zwavesNeighbors")
    private void unbindZDevice(ZwaveDevice device){
        pushNeighbors();
    }

    /**
     * The current operation mode
     */

    @ContextEntity.State.Field(service = ZwaveController.class,state = ZwaveController.MODE)
    private ZwaveController.Mode mode;
    
    @ContextEntity.State.Apply(service = ZwaveController.class,state = ZwaveController.MODE)
    private Consumer<ZwaveController.Mode> requestChangeMode =  (requestedMode) -> manager.requestChangeMode(requestedMode); 
    
    @ContextEntity.State.Push(service = ZwaveController.class,state = ZwaveController.MODE)
	private ZwaveController.Mode changeModeNotification(ZwaveController.Mode newMode) {
        LOG.debug("Zwave mode changed"+newMode);
		return newMode;
	} 
    

    /**
     * A class to represent a uniquely identify an end-point of a z-wave node
     */
    private class EndPointIdentifier {

        public final int 	homeId;
        public final int	nodeId;

        public EndPointIdentifier(ZWaveNode node) {

            this.homeId 	= node.getHomeId();
            this.nodeId		= node.getNodeId();
        }

        public EndPointIdentifier(int homeId, int nodeId) {
            this.homeId 	= homeId;
            this.nodeId		= nodeId;
        }
            @Override
        public int hashCode() {
            return Objects.hash(homeId,nodeId);
        }

        @Override
        public boolean equals(Object obj) {

            if (! (obj instanceof EndPointIdentifier) )
                return false;

            EndPointIdentifier that = ((EndPointIdentifier)obj);

            return 	this.homeId == that.homeId && this.nodeId == that.nodeId;
        }

        @Override
        public String toString() {
            return "node "+nodeId + " on netowork  "+homeId;
        }
    }

    /**
     * The currently discovered devices in the network
     */
    private Map<EndPointIdentifier,ImportDeclaration> declarationMap = new ConcurrentHashMap<>();
    
    private void registerZwaveDeviceImportDeclaration(EndPointIdentifier id,ImportDeclaration declaration){
        super.registerImportDeclaration(declaration);
        declarationMap.put(id,declaration);
    }

    private void unregisterZwaveDeviceImportDeclaration(EndPointIdentifier id) {
        ImportDeclaration declaration = declarationMap.remove(id);
        if (declaration != null){
            super.unregisterImportDeclaration(declaration);
        }
    }

    /**
     * Constructor
     */
    protected ControllerImpl(BundleContext bundleContext) {
        super(bundleContext);
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
    public synchronized void addEventListener(ZWaveEventListener eventListener) {
        manager.controller.addEventListener(eventListener);
    }

    @Override
    public synchronized void removeEventListener(ZWaveEventListener eventListener) {
        manager.controller.removeEventListener(eventListener);
    }

    @Override
    public List<Integer> getNeighbors() {
        return neighbors;
    }

    @Override
    public int getHomeId() {
        return zwaveHomeId;
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

    
    /**
     * LifeCycle
     */
    @Validate
    protected synchronized void start() {
        super.start();
        try {
            manager	= this.new ControllerManager(serialPort);
            manager.open();
        } catch (SerialInterfaceException e) {
            LOG.error(e.toString());
        }
    }

    @Invalidate
    protected synchronized void stop() {
        super.stop();
        manager.close();
    }

 

    @Override
    public String getName() {
        return "ZwaveOpenhabDeviceDiscovery";
    }

    private ManagedScheduledExecutorService getScheduler() {
        return scheduler;
    }
    
    /**
     * This class handles the import for a given controller
     */
    private class ControllerManager implements ZWaveEventListener,Runnable  {

        private final String			serialPort;

        private final ZWaveController 	controller;

        private final long 	watchDog = 30;

        private final TimeUnit watchDogUnit = TimeUnit.SECONDS;

        private ManagedFutureTask<?> managedFutureTask;

        private Map<EndPointIdentifier,String> proxiesWaiting = new ConcurrentHashMap<>();

        private Map<EndPointIdentifier,String> proxies = new ConcurrentHashMap<>();

        private Set<String> relationProxies = new ConcurrentSkipListSet<>();

        private Map<Integer,Boolean> listeningNode = new HashMap<>();

        private final Object listeningLock = new Object();
        
        private ZwaveController.Mode currentMode;
        

        public ControllerManager(String serialPort) throws SerialInterfaceException {

            this.serialPort		= serialPort;
            this.controller		= new ZWaveController(true,false,serialPort,10000,false);

            /*
             * initialize mode
             */
            currentMode = ControllerImpl.this.changeModeNotification(ZwaveController.Mode.NORMAL);
        }

        public void open() {
            controller.initialize();
            controller.addEventListener(this);
            managedFutureTask =  getScheduler().scheduleAtFixedRate(this,watchDog,watchDog,watchDogUnit);
        }

        public void close() {
            managedFutureTask.cancel(false);
            controller.close();
            controller.removeEventListener(this);

        }

        public int getHomeId() {
        	return controller.getNode(controller.getOwnNodeId()).getHomeId(); 
        }

        public int getNodeId() {
            if (controller.getOwnNodeId() == 0){
                return 1;
            }
            return controller.getOwnNodeId();
        }
        
        public void requestChangeMode(ZwaveController.Mode requestedMode) {
        	
        	/*
        	 * We request the mode change, notice that we don't update the current mode until the controller
        	 * has notified the actual change.
        	 */
            LOG.debug("Zwave mode change requested"+requestedMode);

			switch (currentMode) {

				case NORMAL:
					switch (requestedMode) {
						case EXCLUSION:
							controller.requestRemoveNodesStart();
							break;
		
						case INCLUSION:
							controller.requestAddNodesStart();
							break;
		
						case NORMAL:
							break;
					}
	
					break;
	
				case INCLUSION:
					switch (requestedMode) {
						case NORMAL:
							controller.requestAddNodesStop();
							break;
						default:
							break;
					}
	
					break;
	
				case EXCLUSION:
					switch (requestedMode) {
						case NORMAL:
							controller.requestRemoveNodesStop();
							break;
						default:
							break;
					}
					break;
	
				}
		}
        
    	/**
    	 * Update the current mode, depending on the outcome of the request
    	 */
        private void handleModeChangeEvent(ZWaveInclusionEvent event) {

        	switch (currentMode) {
				case NORMAL:
					switch (event.getEvent()) {
						case IncludeStart:
							currentMode = ControllerImpl.this.changeModeNotification(ZwaveController.Mode.INCLUSION);
							break;
						case ExcludeStart:
							currentMode = ControllerImpl.this.changeModeNotification(ZwaveController.Mode.EXCLUSION);
							break;
						default:
							break;
					}
					break;
					
				case INCLUSION:
					switch (event.getEvent()) {
						case IncludeSlaveFound :
						case IncludeControllerFound :
							break;
						case IncludeDone:
						case IncludeFail:
							currentMode = ControllerImpl.this.changeModeNotification(ZwaveController.Mode.NORMAL);
							break;
						default:
							break;
					}
					break;
					
				case EXCLUSION:
					switch (event.getEvent()) {
						case ExcludeSlaveFound :
                            removeDeclaration(new EndPointIdentifier(zwaveHomeId,event.getNodeId()));
                            break;
						case ExcludeControllerFound :
							break;
						case ExcludeDone:
						case ExcludeFail:
							currentMode = ControllerImpl.this.changeModeNotification(ZwaveController.Mode.NORMAL);
							break;
						default:
							break;
					}
					break;
        	
        	}
        	
        }
        


        @Override
        public void ZWaveIncomingEvent(ZWaveEvent event) {
            LOG.debug("Zwave event from node "+event.getNodeId()+" on port "+serialPort+" :"+event.getClass());

            /*
             * Handle event related to node inclusion/exclusion 
             */
            if (event instanceof ZWaveInclusionEvent) {
            	ZWaveInclusionEvent inclusionEvent = (ZWaveInclusionEvent)event;
            	LOG.debug("Zwave inclusion event type for node "+event.getNodeId()+" type "+inclusionEvent.getEvent());
            	handleModeChangeEvent(inclusionEvent);
            }


            /*
             * Handle discovery 
             */
            boolean discovered 		=  false;	/*( (event instanceof ZWaveNodeStatusEvent) && (! ((ZWaveNodeStatusEvent)event).getState().equals(ZWaveNodeState.ALIVE)) ) ||
                    ( (event instanceof ZWaveInclusionEvent) && (! ((ZWaveInclusionEvent)event).getEvent().equals(ZWaveInclusionEvent.Type.IncludeDone)) ) ||
                    ( (event instanceof ZWaveWakeUpCommandClass.ZWaveWakeUpEvent)) ||
                    ( (event instanceof ZWaveCommandClassValueEvent));*/

           /*( (event instanceof ZWaveNodeStatusEvent) && (! ((ZWaveNodeStatusEvent)event).getState().equals(ZWaveNodeState.ALIVE)) ) ||
                    ( (event instanceof ZWaveInclusionEvent) && (! ((ZWaveInclusionEvent)event).getEvent().equals(ZWaveInclusionEvent.Type.ExcludeDone)) ) ;*/


            ZWaveNode node = controller.getNode(event.getNodeId());
            if (node == null ) {
            	LOG.error("Node not found verify what to do, node id = "+event.getNodeId()+" on port "+serialPort+" :"+event.getClass());
                return;
            }

            if (event instanceof ZWaveCommandClassValueEvent){
                discovered = node.getNodeState().equals(ZWaveNodeState.ALIVE);
            }

            if (event instanceof ZWaveNodeInfoEvent){
                discovered = node.getNodeState().equals(ZWaveNodeState.ALIVE);
            }

            if (event instanceof ZWaveWakeUpCommandClass.ZWaveWakeUpEvent){
                discovered = node.getNodeState().equals(ZWaveNodeState.ALIVE);
            }

            if (event instanceof ZWaveTransactionCompletedEvent){
                if (proxiesWaiting.containsKey(new EndPointIdentifier(node))){
                    discovered = node.getNodeState().equals(ZWaveNodeState.ALIVE);
                }
            }

            synchronized (listeningLock) {
                if (discovered  && node.isListening() && listeningNode.containsKey(node.getNodeId())) {
                    listeningNode.put(node.getNodeId(), true);
                }
            }

            boolean undiscovered 	= node.getNodeState().equals(ZWaveNodeState.DEAD) || node.getNodeState().equals(ZWaveNodeState.FAILED);

            if (discovered && ! isManaged(node) ) {
                createDeclaration(node);
            }

            if (undiscovered && isManaged(node) ) {
                removeDeclaration(node);
            }

            /**
             * Maybe can be compute in a less intensive way ...
             */
            if (discovered) {
                computeRelation();
            }
        }

        private final void computeRelation() {
            /**
             * Relation Management
             */
            for (ZWaveNode node : controller.getNodes()){

            	/*
            	 * remove dangling relations when source node is nop longer managed
            	 */
                if (!isManaged(node)){
                    List<Relation> relations = neighborsRelationCreator.getInstancesRelatedTo("ZwaveDevice#"+node.getNodeId());
                    for(Relation relation:relations) {
                        neighborsRelationCreator.delete(relation.getSource(),relation.getTarget());
                        neighborsRelationCreator.delete(relation.getTarget(),relation.getSource());
                    }
                    
                    continue;
                }

                /*
                 * add relation to neighbors
                 */
            	List<Integer> neighbors = node.getNeighbors();
                if (neighbors == null){
                    continue;
                }
                
                for (Integer neighbor : neighbors){

                    ZWaveNode neighborNode 	= controller.getNode(neighbor);
                    if (( neighborNode == null ) || !isManaged(node)){
                        continue;
                    }
                    if (!isRelationManaged("ZwaveDevice#"+neighbor+"ZwaveDevice#"+node.getNodeId())) {
                        neighborsRelationCreator.create("ZwaveDevice#" + neighbor, "ZwaveDevice#" + node.getNodeId());
                        relationProxies.add("ZwaveDevice#" + neighbor + "ZwaveDevice#" + node.getNodeId());
                    }
                }
            }
        }

        private final boolean isManaged(ZWaveNode node) {
            return proxies.containsKey(new EndPointIdentifier(node));
        }

        private final boolean isRelationManaged(String id) {
            return relationProxies.contains(id);
        }

        private final void createDeclaration(ZWaveNode node) {

        	EndPointIdentifier nodeIdentifier = new EndPointIdentifier(node);
        	
            if (node.getManufacturer() == Integer.MAX_VALUE ||node.getDeviceId()==Integer.MAX_VALUE||node.getDeviceType() == Integer.MAX_VALUE ){
                LOG.warn("Node " + node.getNodeId() + " need to be wake up in order to provide enought information to be handle by an iCasa Proxy ! ");
                if (!proxiesWaiting.containsKey(nodeIdentifier)){
                    controller.requestNodeInfo(node.getNodeId());
                    proxiesWaiting.put(nodeIdentifier,"");
                }
                return;
            }

            ImportDeclaration zigbeeDeclaration = ImportDeclarationBuilder.empty()
                    .key(DeviceDeclaration.DEVICE_MANUFACTURER).value(node.getManufacturer())
                    .key(DeviceDeclaration.DEVICE_TYPE).value(node.getDeviceType())
                    .key(DeviceDeclaration.DEVICE_ID).value(node.getDeviceId())
                    .key(DeviceDeclaration.HOME_ID).value(node.getHomeId())
                    .key(DeviceDeclaration.NODE_ID).value(node.getNodeId())
                    .key("scope").value("generic")
                    .key("library").value("openhab")
                    .build();
            proxies.put(nodeIdentifier,"");

            synchronized (listeningLock) {
                if (node.isListening()) {
                    listeningNode.put(node.getNodeId(), true);
                }
            }

            proxiesWaiting.remove(nodeIdentifier);
            ControllerImpl.this.registerZwaveDeviceImportDeclaration(nodeIdentifier,zigbeeDeclaration);

        }

        private final void removeDeclaration(ZWaveNode node) {
            removeDeclaration(new EndPointIdentifier(node));
        }

        private final void removeDeclaration(EndPointIdentifier nodeIdentifier) {
        	proxies.remove(nodeIdentifier);
            synchronized (listeningLock){
                listeningNode.remove(nodeIdentifier.nodeId);
            }
            
            ControllerImpl.this.unregisterZwaveDeviceImportDeclaration(nodeIdentifier);
            computeRelation();
        }


        @Override
        public final void run() {
            if (mode != Mode.NORMAL){
                return;
            }
            Set<ZWaveNode> nodeToRemove = new HashSet<>();
            synchronized (listeningLock){

                for (Map.Entry<Integer,Boolean> nodeListening:listeningNode.entrySet()){
                    if (nodeListening.getValue().equals(true)){
                        controller.requestNodeInfo(nodeListening.getKey());
                        controller.requestNodeNeighborUpdate(nodeListening.getKey());
                        nodeListening.setValue(false);
                    }
                    else {
                        nodeToRemove.add(controller.getNode(nodeListening.getKey()));
                    }
                }
            }
            
            for (ZWaveNode node : nodeToRemove){
                removeDeclaration(node);
            }
        }
    }

}




