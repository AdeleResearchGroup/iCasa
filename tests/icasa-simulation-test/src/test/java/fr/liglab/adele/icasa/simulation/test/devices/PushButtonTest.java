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
package fr.liglab.adele.icasa.simulation.test.devices;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.button.simulated.SimulatedPushButton;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.LocatedDeviceListener;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.runner.test.utils.Condition;
import org.ow2.chameleon.runner.test.utils.TestUtils;

import javax.inject.Inject;
import java.util.Hashtable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
@RunWith(ChameleonRunner.class)
public class PushButtonTest  {
    @Inject
    BundleContext context;

    @Inject
    private SimulationManager simulationMgr;

    @Inject
    private Clock clock;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        try {
            simulationMgr.resetContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validPushButton(){
        createPushButton("pushButton-1");
    }

    @Test
    public void pushAndHoldTest(){
        long holdTime = 100;
        clock.resume();
        LocatedDevice device = createPushButton("pushButton-2");
        SimulatedPushButton button = (SimulatedPushButton) device.getDeviceObject();

        Assert.assertFalse(button.isPushed()); //Is not pushed.
        long initTime = clock.currentTimeMillis();

        button.pushAndHold(holdTime); //push and hold.

        Assert.assertTrue(button.isPushed());//It must be pushed
        TestUtils.testConditionWithTimeout(new TestButtonCondition(button, false));
        long lapsedTime = clock.currentTimeMillis();

        Assert.assertFalse(button.isPushed()); //Is not pushed.
        Assert.assertTrue(lapsedTime >= initTime + holdTime);//It has passed at least initTime + holdTime
    }

    @Test
    public void pushAndReleaseListenerInDeviceTest(){
        LocatedDevice device = createPushButton("pushButton-3");
        SimulatedPushButton button = (SimulatedPushButton) device.getDeviceObject();
        DeviceListener<PushButton> listener = mock(DeviceListener.class);
        button.addListener(listener);
        button.pushAndRelease();
        //It has switched from false to true, and then to false.
        verify(listener, times(2)).devicePropertyModified(any(PushButton.class), anyString(), anyBoolean(), anyBoolean());
        verify(listener, times(1)).devicePropertyModified(button, PushButton.PUSH_AND_HOLD, Boolean.FALSE, Boolean.TRUE);
        verify(listener, times(1)).devicePropertyModified(button, PushButton.PUSH_AND_HOLD, Boolean.TRUE, Boolean.FALSE);
        Assert.assertFalse(button.isPushed()); //Is not pushed.
    }

    @Test
    public void pushAndReleaseListenerInLocatedDeviceTest(){
        LocatedDevice device = createPushButton("pushButton-4");
        SimulatedPushButton button = (SimulatedPushButton) device.getDeviceObject();
        LocatedDeviceListener listener = mock(LocatedDeviceListener.class);
        device.addListener(listener);
        button.pushAndRelease();
        //It has switched from false to true, and then to false.
        verify(listener, times(2)).devicePropertyModified(any(LocatedDevice.class), anyString(), anyBoolean(), anyBoolean());
        verify(listener, times(1)).devicePropertyModified(device, PushButton.PUSH_AND_HOLD, Boolean.FALSE, Boolean.TRUE);
        verify(listener, times(1)).devicePropertyModified(device, PushButton.PUSH_AND_HOLD, Boolean.TRUE, Boolean.FALSE);
        Assert.assertFalse(button.isPushed()); //Is not pushed.
    }

    private LocatedDevice createPushButton(String id){
        LocatedDevice device = simulationMgr.createDevice("iCasa.PushButton", id, new Hashtable());
        return device;
    }

    class TestButtonCondition implements Condition {

        private boolean value;
        private SimulatedPushButton button;

        public TestButtonCondition(SimulatedPushButton btn, boolean value) {
            button = btn;
            this.value = value;
        }

        public boolean isChecked() {
            return (button.isPushed() == value);
        }

        public String getDescription() {
            return "Expected " + value + " in push button";
        }

    }

}
