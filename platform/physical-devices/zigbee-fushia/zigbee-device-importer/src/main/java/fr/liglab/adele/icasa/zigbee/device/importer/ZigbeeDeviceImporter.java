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
package fr.liglab.adele.icasa.zigbee.device.importer;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.TypeCode;
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

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component(name = "fr.liglab.adele.icasa.zigbee.device.importer.ZigbeeDeviceImporter")
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class ZigbeeDeviceImporter extends AbstractImporterComponent {


    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDeviceImporter.class);

    private final Map<String,ServiceRegistration> zigbeeDevices = new HashMap<String, ServiceRegistration>();


    @Requires(filter = "(factory.name=zigbeePhotometer)")
    private Factory photometerFactory;

    @Requires(filter = "(factory.name=zigbeeBinaryLight)")
    private Factory binaryLightFactory;

    @Requires(filter = "(factory.name=zigbeeMotionSensor)")
    private Factory motionSensorFactory;

    @Requires(filter = "(factory.name=zigbeePowerSwitch)")
    private Factory powerSwitchFactory;

    @Requires(filter = "(factory.name=zigbeePushButton)")
    private Factory pushButtonFactory;

    @Requires(filter = "(factory.name=zigbeeThermometer)")
    private Factory thermometerFactory;

    @ServiceProperty(name = "target", value = "(zigbee.module.serial.number=*)")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public ZigbeeDeviceImporter() {
        super();
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        ComponentInstance instance;
        try {

            Factory factory = null;


            String deviceType = (String) importDeclaration.getMetadata().get("zigbee.device.type.code");
            String moduleAddress = (String) importDeclaration.getMetadata().get("zigbee.module.adress");
            String serialNumber = (String) importDeclaration.getMetadata().get("zigbee.module.serial.number");
            LOG.info("Importer trigged with module address : " + moduleAddress + " device Type " + deviceType + " Serial Number " + serialNumber);

            if (TypeCode.A001.toString().equals(deviceType)) {
                factory = binaryLightFactory;
            } else if (TypeCode.C004.toString().equals(deviceType)) {
                factory = photometerFactory;
            } else if (TypeCode.C001.toString().equals(deviceType)) {
                factory = pushButtonFactory;
            } else if (TypeCode.C002.toString().equals(deviceType)) {
                factory = powerSwitchFactory;
            } else if (TypeCode.C003.toString().equals(deviceType)) {
                factory = motionSensorFactory;
            } else if (TypeCode.C005.toString().equals(deviceType)) {
                factory = thermometerFactory;
            } else {
                // device type not supported
                LOG.warn("Device type not supported");
                return ;
            }

            Hashtable properties = new Hashtable();
            properties.put("zigbee.moduleAddress", moduleAddress);
            properties.put(GenericDevice.DEVICE_SERIAL_NUMBER, serialNumber);

            instance = factory.createComponentInstance(properties);
            ServiceRegistration sr = new IpojoServiceRegistration(instance);
            zigbeeDevices.put(serialNumber,sr);
            super.handleImportDeclaration(importDeclaration);
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
        String serialNumber = (String) importDeclaration.getMetadata().get("zigbee.module.serial.number");
        LOG.info(" denyImportDelcaration " + serialNumber);
        try {
            zigbeeDevices.remove(serialNumber).unregister();
        }catch(IllegalStateException e){
            LOG.error("Failed to unregister zigbee device " + serialNumber, e);
        }
        super.unhandleImportDeclaration(importDeclaration);

    }


    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void start() {
        LOG.info("ZigbeeDeviceImporter is up and running");
    }

    @Invalidate
    public void stop() {
        LOG.info("ZigbeeDeviceImporter is stopping");
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
