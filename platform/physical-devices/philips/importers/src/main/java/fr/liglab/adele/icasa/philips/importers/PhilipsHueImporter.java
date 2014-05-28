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

import fr.liglab.adele.icasa.device.GenericDevice;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import fr.liglab.adele.icasa.philips.importers.utils.PhilipsHueImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueImporter.class);

    private final BundleContext context;

    private ServiceReference serviceReference;

    @Requires(filter = "(factory.name=philipsHueLight)")
    private Factory philipsHueLightFactory;

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();


    @ServiceProperty(name = "target", value = "(discovery.philips.device.name=*)")
    private String filter;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        LOG.info("Philips hue Importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");

        cleanup();

    }

    private void cleanup(){

        for(Map.Entry<String,ServiceRegistration> lampEntry:lamps.entrySet()){
            lamps.remove(lampEntry.getKey()).unregister();
        }
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue importer triggered");

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        ComponentInstance instance;



        LOG.debug("Creating proxy for the light " + pojo.getLightId());

        Hashtable properties = new Hashtable();
        properties.put("philips.device.light", pojo.getObject());
        //properties.put("bridge.filter",pojo.getBridgeID());
        //properties.put("PHBridge.filter","(bridgeId="+pojo.getBridgeID()+")");
        Hashtable filters = new Hashtable();
        filters.put("PHBridge","(bridgeId="+pojo.getBridgeID()+")");
        properties.put("requires.filters",filters);
        properties.put(GenericDevice.DEVICE_SERIAL_NUMBER,pojo.getLightId());

        try {
           instance = philipsHueLightFactory.createComponentInstance(properties);
           ServiceRegistration sr = new IpojoServiceRegistration(
                    instance);
            super.handleImportDeclaration(importDeclaration);

            lamps.put(pojo.getUniqueId(),sr);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Proxy instantiation failed",unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Proxy instantiation failed",e);
        } catch (ConfigurationException e) {
            LOG.error("Proxy instantiation failed",e);
        }



    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        try {
            lamps.remove(pojo.getUniqueId()).unregister();
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

