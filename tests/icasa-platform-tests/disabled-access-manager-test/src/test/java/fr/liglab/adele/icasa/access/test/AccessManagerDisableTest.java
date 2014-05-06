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
package fr.liglab.adele.icasa.access.test;

import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.access.DeviceAccessPolicy;
import fr.liglab.adele.icasa.access.MemberAccessPolicy;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.util.UUID;

/**
 *
 */
@RunWith(ChameleonRunner.class)
public class AccessManagerDisableTest {

    @Inject
    public BundleContext context;

    OSGiHelper helper;

    @Before
    public void setup(){
        helper = new OSGiHelper(context);
    }

    @Test
    public void testAccessRightAssignment(){
        AccessManager service = helper.getServiceObject(AccessManager.class);
        String applicationID = "AppT1" + generateId();
        String deviceID = "deviceT11" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertTrue(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.TOTAL);//It change right access.
        Assert.assertTrue(right.isVisible()); // It should be visible 'cause the access right is disabled.
    }

    /**
     * Test the access right assignment
     */
    @Test
    public void testPartialAccessRightAssignment(){
        AccessManager service = helper.getServiceObject(AccessManager.class);
        String applicationID = "AppT2" + generateId();
        String deviceID = "deviceT2"  + generateId();
        String method = "pingPong" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertTrue(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.PARTIAL);//It change right access.
        Assert.assertTrue(right.isVisible());
        //It should be visible 'cause the access right is disabled.
        Assert.assertTrue(right.hasMethodAccess("getSerialNumber"));
        //It should be visible 'cause the access right is disabled.
        Assert.assertTrue(right.hasMethodAccess(method));
        service.setMethodAccess(applicationID, deviceID, method, MemberAccessPolicy.READ_ONLY);
        Assert.assertTrue(right.hasMethodAccess(method));
    }

    @Test
    public void testVisibleAccessRightAssignment(){
        AccessManager service = helper.getServiceObject(AccessManager.class);;
        String applicationID = "AppT3" + generateId();
        String deviceID = "deviceT3" + generateId();
        String method1 = "pingPongT3_1" + generateId();
        String method2 = "pingPongT3_2" + generateId();
        String method3 = "pingPongT3_3" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertTrue(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.VISIBLE);//It change right access.
        Assert.assertTrue(right.isVisible());
        service.setMethodAccess(applicationID, deviceID, method1, MemberAccessPolicy.READ_ONLY);
        service.setMethodAccess(applicationID, deviceID, method2, MemberAccessPolicy.READ_ONLY);

        //This should be ok since is in the valid method list for visible.
        Assert.assertTrue(right.hasMethodAccess("getSerialNumber"));
        //It should be visible 'cause the access right is disabled.
        Assert.assertTrue(right.hasMethodAccess(method1));
        Assert.assertTrue(right.hasMethodAccess(method2));
        Assert.assertTrue(right.hasMethodAccess(method3));

        //Change access right to partial,
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.PARTIAL);//It change right access.
        Assert.assertTrue(right.hasMethodAccess(method1));
        Assert.assertTrue(right.hasMethodAccess(method2));
        Assert.assertTrue(right.hasMethodAccess(method3));
    }

    public String generateId(){
        return  UUID.randomUUID().toString();
    }

}
