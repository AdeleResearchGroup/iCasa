/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.intrusion.alarm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.conflict.exception.ConflictManagementException;
import fr.liglab.adele.conflict.ipojo.annotation.ConflictManagement;
import fr.liglab.adele.conflict.service.ConflictManagerService;
import fr.liglab.adele.icasa.alarm.AlarmService;
import fr.liglab.adele.icasa.device.doorWindow.DoorWindowSensor;
import fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.notification.NotificationService;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component(name="IntrusionAlarmApplication")

@Provides(specifications = {PeriodicRunnable.class},properties= {
		@StaticServiceProperty(name="icasa.application", type="String", value="Intrusion.Alarm.Application", immutable=true)
})


@Instantiate
public class IntrusionAlarmApplication implements PeriodicRunnable {

	private static final Logger m_logger = LoggerFactory.getLogger(IntrusionAlarmApplication.class);

	@ConflictManagement
	private ConflictManagerService conflictManager;

	private boolean intrusion = false;

	private boolean state = true;

	@Requires
	NotificationService notificationService;

	@Requires
	AlarmService alarmService;

	@Requires(id="lights",optional = true,specification = BinaryLight.class)
	private List<BinaryLight> binaryLights;

	@Requires(id="MotionSensors",optional=true,specification = MotionSensor.class)
	private List<MotionSensor> motionSensors;

	@Requires(id="PresenceSensors",optional=true,specification = PresenceSensor.class)
	private List<PresenceSensor> presenceSensors;

	@Requires(id="DoorWindowSensors",optional=true,specification = DoorWindowSensor.class)
	private List<DoorWindowSensor> doorWindowSensors;

	/** Component Lifecycle Method */
	@Invalidate
	public void stop() {
		m_logger.info(" Intrusion Alarm Application is stopping ... ");

	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		m_logger.info(" Intrusion Alarm Application is starting ... ");
	}

	@Bind(id="MotionSensors")
	public void bindMotionSensor(MotionSensor motionSensor){


	}

	@Unbind(id="MotionSensors")
	public void unbindMotionSensor(MotionSensor motionSensor){

	}

	@Bind(id="PresenceSensors")
	public void bindPresenceSensor(PresenceSensor presenceSensor){

	}	

	@Modified(id="PresenceSensors")
	public void modifiedPresenceSensor(PresenceSensor presenceSensor){

		}

	@Unbind(id="PresenceSensors")
	public void unbindPresenceSensor(PresenceSensor presenceSensor){ 
	}

	@Bind(id="DoorWindowSensors")
	public void bindDoorWindowSensor(DoorWindowSensor doorWindowSensor){

	}

	@Modified(id="DoorWindowSensors")
	public void modifiedDoorWindowSensor(DoorWindowSensor doorWindowSensor) {
	

	}

	@Unbind(id="DoorWindowSensors")
	public void unbindDoorWindowSensor(DoorWindowSensor doorWindowSensor){

		
	}

	@Bind(id="lights")
	public void bindBinaryLight(BinaryLight binaryLight){

	}

	@Modified(id="lights")
	public void modifiedBinaryLight(BinaryLight binaryLight){

	}

	@Unbind(id="lights")
	public void unbindBinaryLight(BinaryLight binaryLight){
		m_logger.info(" UNBIND Intrusion Alarm Application  ... ");
		

	}

	@ConflictManagement
	public void conflictCallbackMethod(){
		m_logger.info("CALLBACK:BinaryLight cannot be used to alert the user that Intrusion is detected !  ");
		notificationService.sendNotification("[iCasa] Intrusion Alarm ", " BinaryLight cannot be used to alert the user");
	}

	public boolean detectIntrusion() {
		for(DoorWindowSensor doorWindowSensor : doorWindowSensors){
			if( doorWindowSensor.isOpened()){
				return true;
			}
		}
		for(PresenceSensor presenceSensor: presenceSensors){
			if( presenceSensor.getSensedPresence()){
				return true;
			}
		}

		return false;
	}
	@Override
	public void run() {
		if (detectIntrusion()) {
			try{
				m_logger.info("Intrusion is detected !  ");
			
			//notificationService.sendNotification("[ICASA] INTRUSION Alert", " intrusion is detected.");
			//alarmService.fireAlarm();
			
			if (state){
				for(BinaryLight binaryLight : binaryLights) {
					binaryLight.turnOn();
				}
				state = false;
			}
			else{
				for(BinaryLight binaryLight : binaryLights) {
					binaryLight.turnOff();
				}
				state = true;
				
			}
			}catch(Exception e){
				if(e instanceof ConflictManagementException) {
					m_logger.info("ConflictManagementException !  ");
				}
			}
		}   

	}

	@Override
	public long getPeriod() {
		return 20;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.SECONDS;
	}



}
