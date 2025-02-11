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

import fr.liglab.adele.icasa.access.*;
import fr.liglab.adele.icasa.clockservice.Clock;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.util.UUID;

import static org.mockito.Mockito.*;


@RunWith(ChameleonRunner.class)
public class AccessManagerTest {


	@Inject
	public BundleContext context;

    OSGiHelper osgi;

    private Long timeout = 5000L;

    @Inject
    public Clock clock;
	
	@Before
	public void setUp() {
        osgi = new OSGiHelper(context);
	}

	@After
	public void tearDown() {

	}

    /**
     * Test the access right assignment
     */
    @Test
    public void testAccessRightAssignment(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT1" + generateId();
        String deviceID = "deviceT11" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertFalse(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.TOTAL);//It change right access.
        Assert.assertTrue(right.isVisible());
    }

    /**
     * Test the access right assignment
     */
    @Test
    public void testPartialAccessRightAssignment(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT2" + generateId();
        String deviceID = "deviceT2"  + generateId();
        String method = "pingPong" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertFalse(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.PARTIAL);//It change right access.
        Assert.assertTrue(right.isVisible());
        //This should be ok since is in the valid method list for visible.
        Assert.assertTrue(right.hasMethodAccess("getSerialNumber"));
        //It does not have right.
        Assert.assertFalse(right.hasMethodAccess(method));
        service.setMethodAccess(applicationID, deviceID, method, MemberAccessPolicy.READ_ONLY);
        Assert.assertTrue(right.hasMethodAccess(method));
    }

    /**
     * Test the access right assignment
     */
    @Test
    public void testVisibleAccessRightAssignment(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT3" + generateId();
        String deviceID = "deviceT3" + generateId();
        String method1 = "pingPongT3_1" + generateId();
        String method2 = "pingPongT3_2" + generateId();
        String method3 = "pingPongT3_3" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertFalse(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.VISIBLE);//It change right access.
        Assert.assertTrue(right.isVisible());
        service.setMethodAccess(applicationID, deviceID, method1, MemberAccessPolicy.READ_ONLY);
        service.setMethodAccess(applicationID, deviceID, method2, MemberAccessPolicy.READ_ONLY);

        //This should be ok since is in the valid method list for visible.
        Assert.assertTrue(right.hasMethodAccess("getSerialNumber"));
        //It does not have right since the device access is only visible.
        Assert.assertFalse(right.hasMethodAccess(method1));
        Assert.assertFalse(right.hasMethodAccess(method2));
        Assert.assertFalse(right.hasMethodAccess(method3));

        //Change access right to partial,
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.PARTIAL);//It change right access.
        Assert.assertTrue(right.hasMethodAccess(method1));
        Assert.assertTrue(right.hasMethodAccess(method2));
        Assert.assertFalse(right.hasMethodAccess(method3));
    }

    /**
     * Test the method access right assignment
     */
    @Test
    public void testMethodAccessRightAssignment(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT4" + generateId();
        String deviceID = "deviceT4" + generateId();
        String methodName = "getLightT4" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertFalse(right.isVisible());
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.TOTAL);//It change right access.
        service.setMethodAccess(applicationID, deviceID, methodName, MemberAccessPolicy.READ_WRITE);
        Assert.assertTrue(right.isVisible());//access to device
        Assert.assertTrue(right.hasMethodAccess(methodName));//access to method in device
    }

    /**
     * Test the method access right assignment
     */
    @Test
    public void testMethodAccessRight(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT5" + generateId();
        String deviceID = "device1" + generateId();
        String methodName = "getLight" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        Assert.assertNotNull(right);
        Assert.assertFalse(right.isVisible());
        //give access to the method.
        service.setMethodAccess(applicationID, deviceID, methodName, MemberAccessPolicy.READ_WRITE);

        Assert.assertFalse(right.isVisible());
        Assert.assertFalse(right.hasMethodAccess(methodName));//It must be false since there is any access to device
    }

    /**
     * Test the callback listeners.
     */
    @Test
    public void testListenerChangedRight(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT6" + generateId();
        String deviceID = "deviceT6" + generateId();
        String methodName = "getLightT6" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        AccessRightListener mockListener = mock(AccessRightListener.class);
        right.addListener(mockListener);
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.TOTAL);//must call the listeners
        verify(mockListener, times(1)).onAccessRightModified(right);
        verify(mockListener, never()).onMethodAccessRightModified(any(AccessRight.class), any(String.class));
    }

    /**
     * Test the callback listener
     */
    @Test
    public void testListenerChangedMethodRight(){
        AccessManager service  = osgi.waitForService(AccessManager.class, null, timeout);
        String applicationID = "AppT7" + generateId();
        String deviceID = "deviceT7" + generateId();
        String methodName = "getLightT7" + generateId();
        Assert.assertNotNull(service);
        AccessRight right = service.getAccessRight(applicationID, deviceID);
        AccessRightListener mockListener = mock(AccessRightListener.class);
        right.addListener(mockListener);
        service.setDeviceAccess(applicationID, deviceID, DeviceAccessPolicy.TOTAL);//must call the listeners
        service.setMethodAccess(applicationID, deviceID, methodName, MemberAccessPolicy.READ_WRITE);//must call the listeners
        verify(mockListener, times(1)).onAccessRightModified(right);
        verify(mockListener, times(1)).onMethodAccessRightModified(right, methodName);
    }

    public String generateId(){
        return  UUID.randomUUID().toString();
    }
}
