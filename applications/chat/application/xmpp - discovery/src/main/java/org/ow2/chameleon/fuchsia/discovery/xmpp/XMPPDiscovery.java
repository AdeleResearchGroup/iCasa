package org.ow2.chameleon.fuchsia.discovery.xmpp;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery UPnP
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

import fr.liglab.adele.icasa.jabber.chat.config.configInt.ChatConfig;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
@Component
@Instantiate
@Provides(specifications = {DiscoveryService.class})
public class XMPPDiscovery extends AbstractDiscoveryComponent {

    @Requires(id="config",optional = true)
    private ChatConfig chatConfig;

    private static final Logger LOG = LoggerFactory.getLogger(XMPPDiscovery.class);

    private final Map<String, ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();

    @ServiceProperty(name = "instance.name")
    private String name;

    public XMPPDiscovery(BundleContext bundleContext) {
        super(bundleContext);
        LOG.debug("xmpp discovery: loading..");
    }

    @Validate
    public void start() {
        LOG.debug("xmpp discovery: up and running.");
    }

    @Invalidate
    public void stop() {
        super.stop();
        importDeclarations.clear();
        LOG.debug("xmpp discovery: stopped.");
    }

    public String getName() {
        return name;
    }

    @Bind(id="config")
    public Object addingService() {

        ServiceReference<ChatConfig> reference=getBundleContext().getServiceReference(ChatConfig.class);
        LOG.info(reference.getProperty(ChatConfig.USER_PROPERTY).toString());
        String user = reference.getProperty(ChatConfig.USER_PROPERTY).toString();
        LOG.info(user);
        String pwd = reference.getProperty(ChatConfig.PASSWORD_PROPERTY).toString();
        LOG.info(pwd);
        String host = reference.getProperty(ChatConfig.HOST_PROPERTY).toString();
        LOG.info(host);
       // int port = parseInt((String) reference.getProperty(ChatConfig.PORT_PROPERTY));
        String service = reference.getProperty(ChatConfig.SERVICE_PROPERTY).toString();
        LOG.info(service);

        createImportationDeclaration(user, pwd, host, service, reference);

        return getBundleContext().getService(reference);
    }

    @Unbind(id="config")
    public void removedService() {

        ServiceReference<ChatConfig> reference=getBundleContext().getServiceReference(ChatConfig.class);
        String protocol = (String) reference.getProperty(chatConfig.PROTOCOL_SERVICE);

        ImportDeclaration importDeclaration = importDeclarations.get(protocol);

        unregisterImportDeclaration(importDeclaration);

    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return Collections.unmodifiableSet(new HashSet<ImportDeclaration>(importDeclarations.values()));
    }

    /**
     * Create an import declaration and delegates its registration for an upper class.
     */
    public synchronized void createImportationDeclaration(String user, String pwd, String host, String service, ServiceReference reference) {

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("protocol", "XMPP");
        metadata.put("username", user);
        metadata.put("password", pwd);
        metadata.put("host", host);
        //metadata.put("port", port);
        metadata.put("service", service);
        metadata.put("scope", "jabber");

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        importDeclarations.put("XMPP", declaration);

        registerImportDeclaration(declaration);
    }

}