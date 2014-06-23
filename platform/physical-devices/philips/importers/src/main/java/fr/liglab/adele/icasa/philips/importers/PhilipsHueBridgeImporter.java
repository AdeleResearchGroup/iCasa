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
package fr.liglab.adele.icasa.philips.importers;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.philips.lighting.hue.sdk.bridge.impl.PHBridgeImpl;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import fr.liglab.adele.icasa.philips.importers.utils.PhilipsHueBridgeImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static fr.liglab.adele.icasa.philips.importers.utils.Constants.*;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueBridgeImporter extends AbstractImporterComponent {

    private final Logger LOG = LoggerFactory.getLogger(PhilipsHueBridgeImporter.class);

    private final BundleContext context;

    Timer timer;

    private ServiceReference serviceReference;

    private Map<String,TimerTask> lampDiscoveryTaskMap =new HashMap<String, TimerTask>();
    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.bridge.type=*)")
    private String filter;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueBridgeImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        timer=new Timer();
        LOG.info("Philips hue Importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");

        cleanup();

    }

    private void cleanup(){

        for(TimerTask task : lampDiscoveryTaskMap.values()){
            task.cancel();
        }

        timer.purge();
        timer.cancel();

        for(Map.Entry<String,ServiceRegistration> bridgeEntry:bridges.entrySet()){
            bridges.remove(bridgeEntry.getKey()).unregister();
        }

    }

    @Override
    protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        props.put("bridgeId",pojo.getId());

        ServiceRegistration bridgeService=context.registerService(new String[]{PHBridge.class.getName(),PHBridgeImpl.class.getName()},pojo.getBridgeObject(),props);

        FetchBridgeLampsTask task = new FetchBridgeLampsTask((PHBridgeImpl) pojo.getBridgeObject(),context);

        lampDiscoveryTaskMap.put(pojo.getId(),task);

        timer.schedule(task,0,5000);

        super.handleImportDeclaration(importDeclaration);

        bridges.put(pojo.getId(),bridgeService);

    }

    @Override
    protected void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer removal triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        lampDiscoveryTaskMap.get(pojo.getId()).cancel();

        lampDiscoveryTaskMap.remove(pojo.getId());

        timer.purge();

        try {
            ServiceRegistration sr=bridges.remove(pojo.getId());
            if(sr!=null) {
                sr.unregister();
            };
        }catch(IllegalStateException e){
            LOG.error("failed unregistering bridge", e);
        }

        unhandleImportDeclaration(importDeclaration);
    }


    public String getName() {
        return name;
    }

    class FetchBridgeLampsTask extends TimerTask {

        private final Object lock ;

        private final PHBridge bridge;

        private final DeclarationRegistrationManager declarationManager ;

        public FetchBridgeLampsTask(PHBridge bridge,BundleContext context){
            this.bridge=bridge;
            declarationManager = new DeclarationRegistrationManager(context,ImportDeclaration.class);
            lock = new Object();
        }

        public void run() {
            for(PHLight light:bridge.getResourceCache().getAllLights()){
                if(!light.isReachable()){
                    for(Object obj : declarationManager.getDeclarations()){
                        Declaration declaration = (Declaration) obj;
                        if(declaration.getMetadata().containsValue(light.getIdentifier())){
                            declarationManager.unregisterDeclaration(declaration);
                            break;
                        }
                    }
                }else {

                    // Check if Declaration already registered
                    boolean alreadyRegistered = false;
                    for(Object obj : declarationManager.getDeclarations()){
                        Declaration declaration = (Declaration) obj;
                        if(declaration.getMetadata().containsValue(light.getIdentifier())){
                            alreadyRegistered = true;
                            break;
                        }
                    }

                    if (!alreadyRegistered){
                        Map<String, Object> metadata = new HashMap<String, Object>();

                        metadata.put("id", light.getIdentifier());
                        metadata.put(DISCOVERY_PHILIPS_DEVICE_NAME, light.getModelNumber());
                        metadata.put(DISCOVERY_PHILIPS_DEVICE_TYPE, light.getClass().getName());
                        metadata.put(DISCOVERY_PHILIPS_DEVICE_OBJECT, light);
                        metadata.put(DISCOVERY_PHILIPS_BRIDGE_FILTER,bridge.getResourceCache().getBridgeConfiguration().getIpAddress() );

                        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

                        declarationManager.registerDeclaration(declaration);
                    }
                }
            }
        }

        public boolean cancel() {
            declarationManager.unregisterAll();
            return super.cancel();
        }

    }
}

