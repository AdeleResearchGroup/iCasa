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
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;

import java.util.*;

import static fr.liglab.adele.icasa.philips.importers.utils.Constants.*;

@Component
@Provides(specifications = {Scheduled.class})
public class PhilipsHueBridge extends DeclarationRegistrationManager implements Scheduled {

    private final Logger LOG = LoggerFactory.getLogger(PhilipsHueBridge.class);

    private final BundleContext context;

    private ServiceRegistration serviceRegistration;

    @Property(name = "philips.device.bridge")
    private  PHBridge bridge;

    @ServiceProperty(name = "philips.device.bridge.id")
    private String bridgeId;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueBridge(BundleContext context) {
        super(context,ImportDeclaration.class);
        this.context = context;
    }

    @Validate
    public void validate() {
        LOG.info("Philips hue Bridge is up and running");

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("bridgeId",bridgeId);

        serviceRegistration =context.registerService(new String[]{PHBridge.class.getName(),PHBridgeImpl.class.getName()},bridge,props);

    }

    @Invalidate
    public void invalidate() {

        LOG.info("Philips HUE Bridge Stopping....");

        try {
            serviceRegistration.unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering bridge", e);
        }
    }

    public String getName() {
        return name;
    }

    @Every("5s")
    public void run() {
        for(PHLight light:bridge.getResourceCache().getAllLights()){
            if(!light.isReachable()){
                for(Object obj : this.getDeclarations()){
                    Declaration declaration = (Declaration) obj;
                    if(declaration.getMetadata().containsValue(light.getIdentifier())){
                        this.unregisterDeclaration(declaration);
                        break;
                    }
                }
            }else {

                // Check if Declaration already registered
                boolean alreadyRegistered = false;
                for(Object obj : this.getDeclarations()){
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

                    this.registerDeclaration(declaration);
                }
            }
        }
    }
}

