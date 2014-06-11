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


import fr.liglab.adele.icasa.zigbee.dongle.driver.api.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component(name = "fr.liglab.adele.icasa.zigbee.dongle.factory.ZigbeeDongleFactory")
@Provides(specifications = {DiscoveryService.class,ZigbeeModuleDriver.class})
public class ZigbeeDongle extends AbstractDiscoveryComponent implements ZigbeeSerialPortListener, ZigbeeModuleDriver,ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(ZigbeeDongle.class);

    private final Object m_listenerLock;

    private final Map<ZigbeeDeviceListener,String > listeners = new Hashtable<ZigbeeDeviceListener, String>();

    private final ServiceTracker m_devicetracker;

    @Requires
    private ZigbeeDriver driver;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    private BundleContext m_context;

    public ZigbeeDongle(BundleContext context){
        super(context);
        m_listenerLock = new Object();
        m_context = context;
        m_devicetracker = new ServiceTracker(m_context, DeviceInfo.class.getName(),this);

    }

    @Validate
    public void start(){

        LOG.info("ZigbeeDongle Start ");
        driver.addListener(this);
        m_devicetracker.open();
    }

    @Invalidate
    public void stop(){
        LOG.info("ZigbeeDongle Stop ");
        synchronized (m_listenerLock){
            listeners.clear();
        }
        m_devicetracker.close();
        driver.removeListener(this);

    }



    @Override
    public Data getData(String moduleAddress) {
        return driver.getData(moduleAddress);
    }

    @Override
    public void setData(String moduleAddress, String dataToSet) {
        LOG.debug("sending request response to device : " + moduleAddress + " with value : " + dataToSet);
        driver.setData(moduleAddress, dataToSet);
    }

    @Override
    public void addListener(ZigbeeDeviceListener listener,String moduleAdress) {
        synchronized (m_listenerLock){
            this.listeners.put(listener,moduleAdress);
        }
    }

    @Override
    public void removeListener(ZigbeeDeviceListener listener) {
        synchronized (m_listenerLock){
            this.listeners.remove(listener);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void createImportDeclaration(DeviceInfo info){
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("zigbee.module.adress",info.getModuleAddress());
        metadata.put("zigbee.module.serial.number",computeSerialNumber(info.getModuleAddress()));
        metadata.put("zigbee.device.type.code", info.getTypeCode().toString());
        ImportDeclaration declarationToRegister = ImportDeclarationBuilder.fromMetadata(metadata).build();
        this.registerImportDeclaration(declarationToRegister);
    }

    public void removeImportDeclaration(DeviceInfo info){
        ImportDeclaration declarationToRemove = null;
        for(ImportDeclaration declaration : this.getImportDeclarations()){
            if (declaration.getMetadata().get("zigbee.module.adress").equals(info.getModuleAddress())){
                declarationToRemove = declaration;
                break;
            }
        }
        if (declarationToRemove != null){
            this.unregisterImportDeclaration(declarationToRemove);
        }
    }


    private String computeSerialNumber(String moduleAddress){
        return "zigbee#"+moduleAddress;
    }

    @Override
    public void notifyBatteryLevelChange(DeviceInfo info){
        synchronized (m_listenerLock){
            for (ZigbeeDeviceListener deviceListener : listeners.keySet()) {
                if (listeners.get(deviceListener).equals(info.getModuleAddress())){
                    try {
                        deviceListener.deviceBatteryLevelChanged(info.getBatteryLevel());
                    } catch (Exception e) {
                        LOG.error("could not notify tracker about battery level change",e);
                    }
                }
            }
        }
    }

    @Override
    public void notifyDataChange(DeviceInfo info) {
        for (ZigbeeDeviceListener deviceListener : listeners.keySet()) {
            try {
                deviceListener.deviceDataChanged(info.getDeviceData().getData());
            } catch (Exception e) {
                LOG.error("could not notify tracker about data change for device "+ info.getModuleAddress(), e);
            }
        }
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        DeviceInfo deviceInfo = (DeviceInfo) m_context.getService(serviceReference);
        createImportDeclaration(deviceInfo);
        return deviceInfo;
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        DeviceInfo deviceInfo = (DeviceInfo) m_context.getService(serviceReference);
        notifyDataChange(deviceInfo);
        notifyBatteryLevelChange(deviceInfo);
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        DeviceInfo deviceInfo = (DeviceInfo) m_context.getService(serviceReference);
        removeImportDeclaration(deviceInfo);

    }
}
