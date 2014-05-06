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
package fr.liglab.adele.icasa.device.button.simulated.impl;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.button.simulated.SimulatedPushButton;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;
import java.util.List;

/**
 *
 */
@Component(name = "iCasa.PushButton")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedPushButtonImpl extends AbstractDevice implements SimulatedPushButton, SimulatedDevice {

	@ServiceProperty(name = PushButton.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;

	@Requires
	private Clock clock;

    private PushAndHoldTask task = new PushAndHoldTask();

    ServiceRegistration registrationTask;

    BundleContext bundleContext;

	/**
	 * Influence zone corresponding to the zone with highest level where the
	 * device is located
	 */
	private volatile Zone m_zone;

	public SimulatedPushButtonImpl(BundleContext context) {
		super();
        this.bundleContext = context;
        super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(PUSH_AND_HOLD, false);
	}

	@Override
	public void enterInZones(List<Zone> zones) {
		if (!zones.isEmpty()) {
			m_zone = zones.get(0);
			setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, m_zone.getId());
		}
	}

	@Override
	public void leavingZones(List<Zone> zones) {
		m_zone = null;
		setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

    @Override
    public boolean pushAndHold(long l) {
        if(isPushed()){
            return false;
        }
        push();
        long currentTime = clock.currentTimeMillis();
        task.setExecutionDate(currentTime + l);
        registrationTask = bundleContext.registerService(ScheduledRunnable.class.getName(), task, new Hashtable());
        return true;
    }

    @Override
    public void pushAndRelease() {
       push();
       release();
    }

    @Override
    public boolean isPushed() {
        Boolean status = (Boolean) getPropertyValue(PUSH_AND_HOLD);
        if (status==null)
            return false;
        return status;
    }



    private void push() {
        setPropertyValue(PUSH_AND_HOLD, true);
    }

    private void release() {
        setPropertyValue(PUSH_AND_HOLD, false);
    }

    /**
     * This task is charged of release the button.
     */
    private class PushAndHoldTask implements ScheduledRunnable {

        private long executionDate = 0;

        public void setExecutionDate(long executionDate) {
            this.executionDate = executionDate;
        }

        @Override
        public long getExecutionDate() {
            return executionDate;
        }

        @Override
        public String getGroup() {
            return null;
        }

        @Override
        public void run() {
            registrationTask.unregister();//remove service.
            release();
        }
    }

}
