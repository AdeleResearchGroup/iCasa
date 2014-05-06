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
package fr.liglab.adele.m2mappbuilder.x10.presence.sensor;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import fr.liglab.adele.icasa.environment.SimulatedDevice;
import fr.liglab.adele.icasa.environment.SimulatedEnvironment;
import fr.liglab.adele.m2mappbuilder.x10.Command;
import fr.liglab.adele.m2mappbuilder.x10.Controller;
import fr.liglab.adele.m2mappbuilder.x10.UnitEvent;
import fr.liglab.adele.m2mappbuilder.x10.UnitListener;

/**
 * 
 */
@Component(name = "x10.presence.sensor", immediate=true)
@Provides
public class X10PresenceSensorImpl extends AbstractDevice implements
		PresenceSensor, SimulatedDevice, X10PresenceSensor {

	@ServiceProperty(name = PresenceSensor.DEVICE_SERIAL_NUMBER, value="x10.A1")
	private String m_serialNumber;

	@ServiceProperty(name = "state", value = "deactivated")
	private String state;

	@ServiceProperty(name = "fault", value = "no")
	private String fault;
	
	@Property(name="x10.address", value = "A1")
	private String m_address;

	private boolean m_presenceDetected = false;

	private volatile SimulatedEnvironment m_env;

	private BundleContext m_context;

	private final ServiceTracker m_controllerTracker;

	private DetectUnavailableSensorThread m_thread;

	private Long m_lastTimeStamp = null;

	private class DetectUnavailableSensorThread extends Thread {

		private boolean m_stoped = false;

		@Override
		public void run() {
			while (!m_stoped) {
				
				if (m_lastTimeStamp == null) {
					updatePresenceDetectedFlag(true); // because appears when detect presence
				} else {
					long timestamp = m_lastTimeStamp;
					long diffTS = System.currentTimeMillis() - timestamp;
					updatePresenceDetectedFlag(!(diffTS > 5000));
				}

				synchronized (this) {
					try {
						wait(3000);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		}

	}

	public X10PresenceSensorImpl(BundleContext context) {
		m_context = context;
		
		m_controllerTracker = new ServiceTracker(m_context,
				Controller.class.getName(), new ServiceTrackerCustomizer() {

					private Controller _controller;
					private UnitListener _listener;

					public Object addingService(ServiceReference reference) {
						_controller = (Controller) m_context
								.getService(reference);

						_listener = new UnitListener() {

							public void allUnitsOff(UnitEvent event) {
								// do nothing
							}

							public void allLightsOff(UnitEvent event) {
								// do nothing
							}

							public void allLightsOn(UnitEvent event) {
								// do nothing
							}

							public void unitOn(UnitEvent event) {
								Command command = event.getCommand();

								// event from presence sensor
								// TODO check that one command parameter = 1126
								if (command.getFunction() != Short
										.valueOf("1634"))
									return;
								if (command.getFunctionByte() != Byte
										.valueOf("2"))
									return;
								if (command.getLevel() != 0)
									return;

								String deviceAddress = "" + command.getHouseCode() + command.getUnitCode();
								if (!m_address.equals(deviceAddress))
									return;
								
								m_lastTimeStamp = System.currentTimeMillis();
							}

							public void unitOff(UnitEvent event) {
								// do nothing
							}

							public void unitDim(UnitEvent event) {
								// do nothing
							}

							public void unitBright(UnitEvent event) {
								// do nothing
							}

						};
						_controller.addUnitListener(_listener);

						return _controller;
					}

					public void modifiedService(ServiceReference reference,
							Object service) {
						// do nothing
					}

					public void removedService(ServiceReference reference,
							Object service) {

						_controller.removeUnitListener(_listener);

						m_context.ungetService(reference);
					}
				});
	}
	
	private void updatePresenceDetectedFlag(boolean presenceDetected) {
		boolean oldPresenceDetected = m_presenceDetected;
		
		m_presenceDetected = presenceDetected;
		
		if (oldPresenceDetected != m_presenceDetected)
			notifyListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.m2mappbuilder.device.generic.MedicalGenericDevice#getState()
	 */
	public String getState() {
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.m2mappbuilder.device.generic.MedicalGenericDevice#setState(java.lang.String
	 * )
	 */
	public void setState(String state) {
		this.state = state;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.m2mappbuilder.device.generic.MedicalGenericDevice#getFault()
	 */
	public String getFault() {
		return fault;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.liglab.adele.m2mappbuilder.device.generic.MedicalGenericDevice#setFault(java.lang.String
	 * )
	 */
	public void setFault(String fault) {
		this.fault = fault;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.device.GenericDevice#getSerialNumber()
	 */
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public String getLocation() {
		return getEnvironmentId();
	}

	@Override
	public boolean getSensedPresence() {
		return m_presenceDetected;
	}

	@Override
	public synchronized void bindSimulatedEnvironment(
			SimulatedEnvironment environment) {
		m_env = environment;
	}

	@Override
	public synchronized String getEnvironmentId() {
		return m_env != null ? m_env.getEnvironmentId() : null;
	}

	@Override
	public synchronized void unbindSimulatedEnvironment(
			SimulatedEnvironment environment) {
		m_env = null;
	}

	@Validate
	public void start() {
		m_thread = new DetectUnavailableSensorThread();
		m_thread.start();

		m_controllerTracker.open();
	}

	@Invalidate
	public void stop() {
		if (m_thread != null) {
			m_thread.m_stoped = true;
			m_thread = null;
		}

		m_controllerTracker.close();
	}
	
	private String getSerialNumber(char houseCode, int unitCode) {
		return getSerialNumber("" + houseCode + unitCode);
	}

	private String getSerialNumber(String address) {
		return "X10." + address;
	}
}
