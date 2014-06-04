package fr.liglab.adele.icasa.zigbee.dongle.importer;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.*;
import org.wisdom.akka.AkkaSystemService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component(name = "fr.liglab.adele.icasa.zigbee.dongle.importer.ZigbeeDongleImporter")
@Instantiate(name = "fr.liglab.adele.icasa.zigbee.dongle.importer.ZigbeeDongleImporter-0")
public class ZigbeeDongleImporter extends AbstractImporterComponent {


    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDongleImporter.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Override
    public String getName() {
        return null;
    }

    public ZigbeeDongleImporter() {
        super();
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

    }



}
