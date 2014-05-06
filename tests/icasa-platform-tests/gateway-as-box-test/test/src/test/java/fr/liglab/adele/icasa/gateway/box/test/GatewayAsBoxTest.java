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
package fr.liglab.adele.icasa.gateway.box.test;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.box.Box;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.runner.test.utils.TestUtils;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;

import static org.junit.Assert.*;


/**
 * Tests of gateway reification as a box.
 *
 */
@RunWith(ChameleonRunner.class)
public class GatewayAsBoxTest {

    @Inject
    public BundleContext context;

    public Preferences _preferences;

    OSGiHelper helper;
    
    protected ContextManager _contextMgr;

    @Before
    public void setUp() throws Exception {
        helper = new OSGiHelper(context);
        // should wait for these services
        _contextMgr = (ContextManager) waitForService(context, ContextManager.class);
        _preferences = (Preferences) waitForService(context, Preferences.class);
    }

    @After
    public void tearDown() {
        _contextMgr = null;
        _preferences = null;
    }


    @Test
    public void boxExistsTest() {
        Box box = (Box) waitForService(context, Box.class);
        assertNotNull(box);

        String boxId = box.getSerialNumber();
        assertNotNull(boxId);
        assertFalse(boxId.isEmpty());
        assertTrue(boxId.startsWith("gateway-"));
    }

    @Test
    public void boxIdDefinedByGlobalPropTest() {
        Box box = (Box) waitForService(context, Box.class);
        assertNotNull(box);

        String globalPropValue = "myBoxId";
        _preferences.setGlobalPropertyValue(ContextManager.GATEWAY_ID_PROP_NAME, globalPropValue);

        // need to restart boxcomponent
        try {
            Bundle boxCompBundle = null;
            for (Bundle bundle : context.getBundles()) {
                if ("gateway.as.box".equals(bundle.getSymbolicName())) {
                    boxCompBundle = bundle;
                    break;
                }
            }
            if (boxCompBundle == null)
                fail("cannot find gateway as box bundle.");
            boxCompBundle.stop();
            boxCompBundle.start();
        } catch (BundleException e) {
            e.printStackTrace();
        }

        box = (Box) waitForService(context, Box.class);
        assertNotNull(box);

        String boxId = box.getSerialNumber();
        assertEquals("gateway-" + globalPropValue, boxId);
    }

    public Object waitForService(BundleContext context, Class clazz) {
        TestUtils.testConditionWithTimeout(new ServiceExistsCondition(context, clazz), 10000, 20);

        return helper.getServiceObject(clazz);
    }
    
}
