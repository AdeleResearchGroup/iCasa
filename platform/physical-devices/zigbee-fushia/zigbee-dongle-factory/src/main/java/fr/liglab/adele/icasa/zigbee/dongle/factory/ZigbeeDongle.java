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
package fr.liglab.adele.icasa.zigbee.dongle.factory;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.*;
import org.wisdom.akka.AkkaSystemService;

import java.util.concurrent.Callable;

@Component(name = "fr.liglab.adele.icasa.zigbee.dongle.factory.ZigbeeDongleFactory")
public class ZigbeeDongle extends AbstractDiscoveryComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDongle.class);

    @Requires
    private AkkaSystemService akka;

    @ServiceProperty(mandatory = true,name = "zigbee.dongle.id")
    String id;

    @Property(mandatory = true,name = "zigbee.dongle.object")
    Device zigbeeDongle;

    @Property(mandatory = true,name = "zigbee.dongle.vendor.id")
    String idVendor;

    @Property(mandatory = true,name = "zigbee.dongle.product.id")
    String idProduct;

    public ZigbeeDongle(BundleContext context){
        super(context);
    }

    @Validate
    public void start(){
        LOG.info("ZigbeeDongle Start ");

    }

    @Invalidate
    public void stop(){
        LOG.info("ZigbeeDongle Stop ");

    }

    @Override
    public String getName() {
        return null;
    }

    public class DeviceDiscoveryTask implements Callable<Void> {

        public DeviceDiscoveryTask() {

        }


        @Override
        public Void call() {
            int result = LibUsb.init(null);
            if (result != LibUsb.SUCCESS)
            {
                throw new LibUsbException("Unable to initialize libusb", result);
            }

            DeviceHandle handle = LibUsb.openDeviceWithVidPid(null, Short.valueOf(idVendor),Short.valueOf(idProduct));
            if (handle == null)
            {
                throw new LibUsbException("Unable to reach Usb Zigbee Dongle",LibUsb.ERROR_NO_DEVICE);
            }

            DeviceDescriptor descriptor = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(zigbeeDongle, descriptor);
            if (result != LibUsb.SUCCESS){
                LOG.error("Unable to read device descriptor");
                throw new LibUsbException("Unable to read device descriptor",
                        result);
            }


            result = LibUsb.claimInterface(handle,1);
            if (result != LibUsb.SUCCESS)
            {
                throw new LibUsbException("Unable to claim interface", result);
            }

            return null;
        }
    }

}
