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
package fr.liglab.adele.icasa.gateway.box.impl;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.box.Box;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;
import java.util.UUID;


@Instantiate(name = "gateway-box-device-0")
@Component(name = "gateway-box-device")
public class GatewayBoxImpl extends AbstractDevice implements Box {

    public static final String SERIAL_NUMBER_PREFIX = "gateway-";

    @Requires
    private ContextManager _contextMgr;

    @Requires
    private Preferences _preferences;

    private String serialNumber;

    private BundleContext _context;

    private ServiceRegistration _serviceReg;

    public GatewayBoxImpl(BundleContext context) {
        super();
        _context = context;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Validate
    private void start() {
        Object gatewayId = _preferences.getGlobalPropertyValue(ContextManager.GATEWAY_ID_PROP_NAME);
        if ((gatewayId != null) && (gatewayId instanceof String)) {
             serialNumber = SERIAL_NUMBER_PREFIX + gatewayId;
        } else {
            String generatedGatewayId = UUID.randomUUID().toString();
            _preferences.setGlobalPropertyValue(ContextManager.GATEWAY_ID_PROP_NAME, generatedGatewayId);

            serialNumber = SERIAL_NUMBER_PREFIX + generatedGatewayId;
        }

        String[] providedServiceClasses = {GenericDevice.class.getName(), Box.class.getName()};
        Hashtable<String, Object> serviceProps = new Hashtable<String, Object>();
        serviceProps.put(GenericDevice.DEVICE_SERIAL_NUMBER, serialNumber);
        _serviceReg = _context.registerService(providedServiceClasses, this, serviceProps);
    }

    @Invalidate
    private void stop() {
        if (_serviceReg != null) {
            _serviceReg.unregister();
            _serviceReg = null;
        }
    }
}