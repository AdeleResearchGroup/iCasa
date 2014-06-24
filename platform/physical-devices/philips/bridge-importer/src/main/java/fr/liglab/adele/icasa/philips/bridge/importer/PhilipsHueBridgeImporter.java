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
package fr.liglab.adele.icasa.philips.bridge.importer;

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

import fr.liglab.adele.icasa.philips.bridge.importer.utils.PhilipsHueBridgeImportDeclarationWrapper;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueBridgeImporter extends AbstractImporterComponent {

    private final Logger LOG = LoggerFactory.getLogger(PhilipsHueBridgeImporter.class);

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @Requires(filter = "(factory.name=PhilipsHueBridge)")
    private Factory philipsHueBridgeFactory;

    @ServiceProperty(name = "target", value = "(discovery.philips.bridge.type=*)")
    private String filter;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueBridgeImporter() {

    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        super.start();
        LOG.info("Philips hue Importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");
        cleanup();
        super.stop();
    }

    private void cleanup(){

        for(String id : bridges.keySet())
            try {
                bridges.remove(id).unregister();
            }catch(IllegalStateException e){
                LOG.error("failed unregistering lamp", e);
            }

    }

    @Override
    protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        ComponentInstance instance;

        Hashtable properties = new Hashtable();
        properties.put("philips.device.bridge", pojo.getBridgeObject());
        properties.put("philips.device.bridge.id", pojo.getId());

        try {
            instance = philipsHueBridgeFactory.createComponentInstance(properties);
            ServiceRegistration sr = new IpojoServiceRegistration(
                    instance);
            super.handleImportDeclaration(importDeclaration);

            bridges.put(pojo.getId(),sr);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Proxy instantiation failed",unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Proxy instantiation failed",e);
        } catch (ConfigurationException e) {
            LOG.error("Proxy instantiation failed",e);
        }

    }

    @Override
    protected void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer removal triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        try {
            bridges.remove(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        unhandleImportDeclaration(importDeclaration);
    }


    public String getName() {
        return name;
    }

    class IpojoServiceRegistration implements ServiceRegistration {

        ComponentInstance instance;

        public IpojoServiceRegistration(ComponentInstance instance) {
            super();
            this.instance = instance;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.framework.ServiceRegistration#getReference()
         */
        public ServiceReference getReference() {
            try {
                ServiceReference[] references = instance.getContext()
                        .getServiceReferences(
                                instance.getClass().getCanonicalName(),
                                "(instance.name=" + instance.getInstanceName()
                                        + ")");
                if (references.length > 0)
                    return references[0];
            } catch (InvalidSyntaxException e) {
                LOG.error(" Invalid syntax Exception " , e);
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.osgi.framework.ServiceRegistration#setProperties(java.util.Dictionary
         * )
         */
        public void setProperties(Dictionary properties) {
            instance.reconfigure(properties);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.framework.ServiceRegistration#unregister()
         */
        public void unregister() {
            instance.dispose();
        }

    }
}

