package org.ow2.chameleon.fuchsia.examples.xmpp.config;
import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class XMPPLinkerInitializer {

    Instance xmppLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("XMPPLinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(id=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=XMPPImporter)");


    Instance xmppImporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.xmpp.XMPPImporter")
            .named("XMPPImporter");

    Instance fbDisco = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport")
            .named("FilebasedDiscovery");

}
