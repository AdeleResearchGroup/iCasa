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
/**
 *
 */
package fr.liglab.adele.icasa.device.zigbee.driver.impl;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.device.zigbee.driver.serial.SerialPortHandler;
import fr.liglab.adele.icasa.device.zigbee.driver.serial.model.ResponseType;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.Data;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.DeviceInfo;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeDriver;
import fr.liglab.adele.icasa.zigbee.dongle.driver.api.ZigbeeSerialPortListener;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Implementation class for the Zigbee Driver interface.
 *
 */
@Component(name = "zigbee.driver.impl")
@Instantiate
@Provides(specifications = { ZigbeeDriver.class })
public class ZigbeeDriverImpl implements ZigbeeDriver {

    private SerialPortHandler handler;

    @Requires
    private AkkaSystemService akka;

    private final ListeningTask listeningTask;

    private final Object m_zigbeeSerialListenerLock;

    private final BundleContext m_context;

    private final Set<ZigbeeSerialPortListener> zigbeeSerialPortListenerSet = new HashSet<ZigbeeSerialPortListener>();

    private static final Logger LOG = LoggerFactory.getLogger(Constants.ICASA_LOG_DEVICE + ".zigbee");

    private static final String SERIAL_PORT_PROPERTY = "zigbee.driver.port";

    private final Object m_serivceRegistrationLock;

    private final Map<String,ServiceRegistration> serviceRegistrationMap = new HashMap<String,ServiceRegistration>();

    @Property(name = SERIAL_PORT_PROPERTY,mandatory = true)
    private String port;

    @Property(name = "baud.rate",mandatory = true)
    private int baud;

    public ZigbeeDriverImpl(BundleContext context)  {
        handler = new SerialPortHandler(this);
        listeningTask = new ListeningTask();
        m_zigbeeSerialListenerLock = new Object();
        m_serivceRegistrationLock = new Object();
        this.m_context = context;

    }

    @Validate
    private void start() {
        LOG.info(" Driver start");
        akka.dispatch(listeningTask, akka.fromThread());
    }

    @Invalidate
    private void stop() {
        LOG.info(" Driver stop");
        handler.stopListening();
    }


    private class ListeningTask implements Callable<Void>{

        public ListeningTask(){ 
        }

        @Override
        public Void call() throws Exception {
            LOG.info(" Start listenning with port " + port + " speed " + baud );
            try {
                handler.startListening(port, baud);
            } catch (Exception e) {
                LOG.error("Unable to connect into port: " + port);
            }finally {
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.liglab.adele.habits.monitoring.zigbee.driver.ZigbeeDriver#getData(
     * java.lang.String)
     */
    @Override
    public Data getData(String moduleAddress) {
        DeviceInfo deviceInfo = handler.getDeviceInfo(moduleAddress);
        if (deviceInfo == null)
            return null;

        return deviceInfo.getDeviceData();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.liglab.adele.habits.monitoring.zigbee.driver.ZigbeeDriver#setData(
     * String moduleAddress, String dataToSet)
     */
    @Override
    public void setData(String moduleAddress, String dataToSet) {
        LOG.debug("sending request response to device : " + moduleAddress + " with value : " + dataToSet);
        handler.write(ResponseType.REQUEST, moduleAddress, dataToSet);
    }

    @Override
    public void addListener(ZigbeeSerialPortListener listener) {
       synchronized (m_zigbeeSerialListenerLock){
           zigbeeSerialPortListenerSet.add(listener);
       }
    }

    @Override
    public void removeListener(ZigbeeSerialPortListener listener) {
        synchronized (m_zigbeeSerialListenerLock){
            zigbeeSerialPortListenerSet.remove(listener);
        }
    }

    public void deviceAdded(DeviceInfo info){
        Hashtable properties = new Hashtable();
        properties.put("device.adress", info.getModuleAddress());
        properties.put("device.data",info.getDeviceData().getData());
        properties.put("device.battery",info.getBatteryLevel());
        ServiceRegistration sr = m_context.registerService(DeviceInfo.class,info,properties);
       synchronized (m_serivceRegistrationLock){
           serviceRegistrationMap.put(info.getModuleAddress(),sr);
       }
    }

    public void deviceRemoved(DeviceInfo info){
        synchronized (m_serivceRegistrationLock){
            serviceRegistrationMap.remove(info.getModuleAddress()).unregister();
        }
    }

    public void updateDeviceBattery(DeviceInfo info){
        ServiceRegistration sr = serviceRegistrationMap.get(info.getModuleAddress());
        Hashtable properties = new Hashtable();
        properties.put("device.battery",info.getBatteryLevel());
        sr.setProperties(properties);
    }

    public void updateData(DeviceInfo info){
        ServiceRegistration sr = serviceRegistrationMap.get(info.getModuleAddress());
        Hashtable properties = new Hashtable();
        properties.put("device.data",info.getDeviceData().getData());
        sr.setProperties(properties);
    }
}
