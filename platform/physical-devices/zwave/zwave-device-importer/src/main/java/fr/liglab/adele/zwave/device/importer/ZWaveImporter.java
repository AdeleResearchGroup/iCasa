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
package fr.liglab.adele.zwave.device.importer;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.openhab.binding.zwave.internal.protocol.SerialInterfaceException;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass.ZWaveWakeUpEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import fr.liglab.adele.icasa.device.GenericDevice;


@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class})
public class ZWaveImporter extends AbstractImporterComponent  {

	private static final Logger LOG = LoggerFactory.getLogger(ZWaveImporter.class);

	private final BundleContext context;

    private static final Map<String, ControllerManager> managers = new ConcurrentHashMap<String, ControllerManager>();

 
    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = "target", value = "(&(scope=generic)(protocol=zwave)(port=*))")
    private String filter;

    /**
     * The properties used to match the proxy factory 
     */
    public final static String FACTORY_PROPERTY_MANUFACTURER 	= "zwave.manufacturer";
    public final static String FACTORY_PROPERTY_DEVICE_ID 		= "zwave.device.id";
    public final static String FACTORY_PROPERTY_DEFAULT_PROXY	= "zwave.default.proxy";
    
    /**
     * 
     */
    public final static String PROXY_PROPERTY_CONTROLLER 		= "zwave.controller";
    public final static String PROXY_PROPERTY_NODE 				= "zwave.node";
    public final static String PROXY_PROPERTY_ENDPOINT 			= "zwave.endpoint";


    /**
     * The list of factories
     */
    private Set<ServiceReference<Factory>> factoryDescriptions = new ConcurrentSkipListSet<ServiceReference<Factory>>();
    
    public ZWaveImporter(BundleContext context) {
        this.context = context;
    }
    
    @Bind(id="factories", optional=true, specification=Factory.class, filter="(&("+FACTORY_PROPERTY_MANUFACTURER+"=*)("+FACTORY_PROPERTY_DEVICE_ID+"=*))")
    private void bindFactory(ServiceReference<Factory> factoryDescription) {
    	factoryDescriptions.add(factoryDescription);
    } 

    @Unbind(id="factories")
    private void unbindFactory(ServiceReference<Factory> factoryDescription) {
    	factoryDescriptions.remove(factoryDescription);
    }
 
    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        ZWaveDeclaration declaration = ZWaveDeclaration.from(importDeclaration);

        LOG.info("Importing declaration for zwave device '{}' at port {}",declaration.getId(),declaration.getPort());
        
        ControllerManager manager = managers.get(declaration.getPort());

        if(manager == null) {
            try {
            	manager	= new ControllerManager(declaration.getPort());
            	managers.put(declaration.getPort(),manager);
            	manager.open();
                
            } catch (SerialInterfaceException e) {
                e.printStackTrace();
            }
        }


        handleImportDeclaration(importDeclaration);

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
    	
    	ZWaveDeclaration declaration = ZWaveDeclaration.from(importDeclaration);

        LOG.info("Removing imported declaration for zwave device '{}' at port {}",declaration.getId(),declaration.getPort());

        ControllerManager manager	= managers.remove(declaration.getPort());
        
		if (manager != null) {
			manager.close();
		}
    	
        unhandleImportDeclaration(importDeclaration);
    }
    
    
    public String getName() {
        return name;
    }


    /**
     * This class handles the import for a given controller
     */
    private class ControllerManager implements ZWaveEventListener  {
    
    	private final String			serialPort;
    	private final ZWaveController 	controller;
    	
    	private Map<EndPointIdentifier,ComponentInstance> proxies = new ConcurrentHashMap<EndPointIdentifier,ComponentInstance>();
    	
    	public ControllerManager(String serialPort) throws SerialInterfaceException {
    		
    		this.serialPort		= serialPort;
            this.controller		= new ZWaveController(true,false,serialPort,10000,false);
		}

    	public void open() {
            controller.initialize();
            controller.addEventListener(this);
    	}

    	public void close() {
			controller.close();
			controller.removeEventListener(this);
    	}
    	
    	@Override
    	public void ZWaveIncomingEvent(ZWaveEvent event) {
    		LOG.debug("Zwave event from node "+event.getNodeId()+" on port "+serialPort);
    		
    		EndPointIdentifier endPoint = new EndPointIdentifier(serialPort, event.getNodeId(), event.getEndpoint());
    		  		
			boolean discovered 		= 	( (event instanceof ZWaveNodeStatusEvent) && (! ((ZWaveNodeStatusEvent)event).getState().equals(ZWaveNodeState.ALIVE)) ) ||
    									( (event instanceof ZWaveInclusionEvent) && (! ((ZWaveInclusionEvent)event).getEvent().equals(ZWaveInclusionEvent.Type.IncludeDone)) ) ||
    									( (event instanceof ZWaveWakeUpEvent)) ||
    									( (event instanceof ZWaveCommandClassValueEvent));
    		
    		boolean undiscovered 	= 	( (event instanceof ZWaveNodeStatusEvent) && (! ((ZWaveNodeStatusEvent)event).getState().equals(ZWaveNodeState.ALIVE)) ) ||
										( (event instanceof ZWaveInclusionEvent) && (! ((ZWaveInclusionEvent)event).getEvent().equals(ZWaveInclusionEvent.Type.ExcludeDone)) ) ;
    		
    		if (discovered && ! isManaged(endPoint) ) {
    			createProxy(endPoint);
    		}
    		
    		if (undiscovered && isManaged(endPoint) ) {
    			destroyProxy(endPoint);
    		}
    		
    	}
    	
    	private final boolean isManaged(EndPointIdentifier endPoint) {
    		return proxies.containsKey(endPoint);
    	}
    	
    	private final void createProxy(EndPointIdentifier endPoint) {
    		
    		/*
    		 * verify node is defined in the controller
    		 */
    		ZWaveNode node = controller.getNode(endPoint.nodeId);
    		if (node == null) {
    			LOG.error("Error creating proxy, unknown node "+endPoint.nodeId);
    			return;
    		}
    		
    		/*
    		 * verify end-point is defined in the controller
    		 */
    		if (endPoint.endPointId != 0) {

    			ZWaveMultiInstanceCommandClass multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) node.getCommandClass(CommandClass.MULTI_INSTANCE);
        		if (multiInstanceCommandClass == null) {
        			LOG.error("Error creating proxy, unknown endpoint "+endPoint.endPointId+" for node "+endPoint.nodeId);
        			return;
        		}
        		else if (multiInstanceCommandClass.getVersion() == 2 &&  multiInstanceCommandClass.getEndpoint(endPoint.endPointId) == null) {
        			LOG.error("Error creating proxy, unknown endpoint "+endPoint.endPointId+" for node "+endPoint.nodeId);
        			return;
        		}
    		}
    		
    		/*
    		 * Look for matching proxy
    		 */

    		
    		for (ServiceReference<Factory> factoryDescription : factoryDescriptions) {
				try {

	    			String manufacterer 	= (String) factoryDescription.getProperty(FACTORY_PROPERTY_MANUFACTURER);
	    			String deviceId 		= (String) factoryDescription.getProperty(FACTORY_PROPERTY_DEVICE_ID);
	    			String defaultProxy		= (String) factoryDescription.getProperty(FACTORY_PROPERTY_DEFAULT_PROXY);
	    			
	    			boolean match 			= ( manufacterer != null && Integer.valueOf(manufacterer,16).intValue() == node.getManufacturer()) &&
	    									  ( deviceId != null && Integer.valueOf(deviceId,16).intValue() == node.getDeviceId());
	    			
	    			boolean isDefault		= ( defaultProxy != null && Boolean.valueOf(defaultProxy).booleanValue() && node.getManufacturer() == Integer.MAX_VALUE);

	    			if (match || isDefault) {
						LOG.debug("Zwave creating proxy for end point "+endPoint);
	            		
						String id = "ZWaveDevice["+node.getHomeId()+"."+endPoint.nodeId+"."+endPoint.endPointId+"]";
						
	                    Hashtable<String, Object> configuration = new Hashtable<String, Object>();
	            		
	                    configuration.put( GenericDevice.DEVICE_SERIAL_NUMBER, id);

	                    configuration.put(PROXY_PROPERTY_CONTROLLER, controller);
	                    configuration.put(PROXY_PROPERTY_NODE, endPoint.nodeId);
	                    configuration.put(PROXY_PROPERTY_ENDPOINT, endPoint.endPointId);
	                    
	                    configuration.put(Factory.INSTANCE_NAME_PROPERTY, id);
	            		
	                    Factory factory = context.getService(factoryDescription);
						proxies.put(endPoint,factory.createComponentInstance(configuration));
	    			}
	    			
				} catch (Exception exception) {
					LOG.error("Error in Zwave creating proxy for end point "+endPoint,exception);
				}
			}
    		
    	}
    	
    	private final void destroyProxy(EndPointIdentifier endPoint) {
    		LOG.debug("Zwave destroying proxy for end point "+endPoint);
    		ComponentInstance proxy = proxies.remove(endPoint);
    		if (proxy != null) {
    			proxy.dispose();
    		}
    	}
    	
    }
    
    /**
     * A class to represent a uniquely identify an end-point of a z-wave node 
     */
    private class EndPointIdentifier {
    	
    	public final String 	serialPort;
    	public final int 		nodeId;
    	public final int 		endPointId;
    	
    	private final int 		hash;
    	private final String	label;
    	
    	public EndPointIdentifier(String serialPort, int nodeId, int endPointId) {
    	
    		this.serialPort 	= serialPort;
    		this.nodeId			= nodeId;
    		this.endPointId		= endPointId;
    		
    		hash 				= hash();
    		label				= label();
    	}
    	
    	private int hash() {
    		int hash = 0;
    			
			hash = (hash ^ (serialPort.hashCode() >>> 32));
			hash = (hash ^ (nodeId >>> 32));
			hash = (hash ^ (endPointId >>> 32));
    			
    		return hash;
    	}
    	
    	private String label() {
    		return "node "+nodeId+" end point "+endPointId+ " on port "+serialPort;
    	}
    	
    	@Override
    	public int hashCode() {
    		return hash;
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		
    		if (! (obj instanceof EndPointIdentifier) )
    			return false;
            
        	EndPointIdentifier that = ((EndPointIdentifier)obj);
        	
        	return 	this.serialPort.equals(that.serialPort) &&
        			this.nodeId == that.nodeId &&
        			this.endPointId == that.endPointId;
    	}
    	
    	@Override
    	public String toString() {
    		return label;
    	}
    }
}
