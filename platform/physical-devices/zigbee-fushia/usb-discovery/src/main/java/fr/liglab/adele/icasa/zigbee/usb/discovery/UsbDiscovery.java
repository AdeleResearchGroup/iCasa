package fr.liglab.adele.icasa.zigbee.usb.discovery;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
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

@Component(name = "fr.liglab.adele.icasa.zigbee.usb.discovery.UsbDiscovery")
@Instantiate(name = "fr.liglab.adele.icasa.zigbee.usb.discovery.UsbDiscovery-0")
public class UsbDiscovery extends AbstractDiscoveryComponent {


    private static final Logger LOG = LoggerFactory.getLogger(UsbDiscovery.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Requires
    AkkaSystemService akka;

    private final EventHandlingThread eventHandlingThread = new EventHandlingThread();

    HotplugCallbackHandle callbackHandle = new HotplugCallbackHandle();

    public UsbDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public String getName() {
        return null;
    }

    /**
     * This task is charged of turn off the light.
     */
    public class EventHandlingThread implements Callable<Void> {

        /** If thread should abort. */
        private volatile boolean abort;

        /**
         * Aborts the event handling thread.
         */
        public void abort()
        {
            this.abort = true;
        }

        public EventHandlingThread() {

        }


        @Override
        public Void call() {
            while (!this.abort)
            {
                int result = LibUsb.handleEventsTimeout(null, 1000000);
                if (result != LibUsb.SUCCESS){
                    LOG.error("Unable to handle events");
                    throw new LibUsbException("Unable to handle events", result);
                }
            }
            return null;
        }
    }

    @Validate
    public void start(){
        LOG.info("USB discovery Start ");
        int result = LibUsb.init(null);
        if (result != LibUsb.SUCCESS) {
            LOG.error("LibUsb init fail ");
            throw new LibUsbException("Unable to initialize libusb.", result);
        }
        if(LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)){
            LibUsb.hotplugRegisterCallback(null, LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED |LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                    LibUsb.HOTPLUG_ENUMERATE,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    new UsbCallback(this),
                    null,
                    callbackHandle);

            akka.dispatch(eventHandlingThread, akka.fromThread());

        } else{
            LOG.info("Don't have Hotplug");
        }
    }

    @Invalidate
    public void stop(){
        LOG.info("USB discovery Stop ");
        eventHandlingThread.abort();
        LibUsb.hotplugDeregisterCallback(null, callbackHandle);
        LibUsb.exit(null);
    }

    private class UsbCallback implements HotplugCallback{

        private final UsbDiscovery m_parent;

        private final Object m_lock;

        public UsbCallback(UsbDiscovery parent) {
            this.m_parent = parent;
            m_lock = new Object();
        }
        public int processEvent(org.usb4java.Context context, Device device, int event, Object o) {
            LOG.info("USB device event detected ");
            DeviceDescriptor descriptor = new DeviceDescriptor();
            int result = LibUsb.getDeviceDescriptor(device, descriptor);
            if (result != LibUsb.SUCCESS){
                LOG.error("Unable to read device descriptor");
                throw new LibUsbException("Unable to read device descriptor",
                        result);
            }
            if(event == LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED){
                LOG.info("Device connect, id: " +  descriptor.idVendor() +":" +descriptor.idProduct());
                ImportDeclaration UsbImportDeclaration = generateImportDeclaration(device,descriptor);
                synchronized (m_lock){
                    m_parent.registerImportDeclaration(UsbImportDeclaration);
                }
            }else{

                String idCurrentDevice= descriptor.idVendor() +":" +descriptor.idProduct();
                LOG.info(idCurrentDevice);

                synchronized (m_lock){
                    LOG.info("Device disconnet, id: " +  descriptor.idVendor() +":" +descriptor.idProduct());
                    ImportDeclaration importDeclarationsToUnregister = null;
                    for(ImportDeclaration declaration : m_parent.getImportDeclarations()){;
                        if ( ((String)declaration.getMetadata().get("usb.discovery.idVendor")).equals(idCurrentDevice) ){
                            importDeclarationsToUnregister = declaration;
                            break;
                        }
                    }
                    if(importDeclarationsToUnregister != null ){
                        m_parent.unregisterImportDeclaration(importDeclarationsToUnregister);
                    }
                }
            }

            return 0;
        }

        private ImportDeclaration generateImportDeclaration(Device device,DeviceDescriptor descriptor) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("usb.discovery.id",Short.toString(descriptor.idVendor())+":"+Short.toString(descriptor.idProduct()));
            metadata.put("usb.discovery.idVendor",Short.toString(descriptor.idVendor()));
            metadata.put("usb.discovery.idProduct", Short.toString(descriptor.idProduct()));
            metadata.put("usb.discovery.device.object", device);

            return ImportDeclarationBuilder.fromMetadata(metadata).build();
        }

    }

}
