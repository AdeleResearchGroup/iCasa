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
package fr.liglab.adele.icasa.fire.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import fr.liglab.adele.conflict.ipojo.annotation.ConflictManagement;
import fr.liglab.adele.conflict.service.ConflictManagerService;
import fr.liglab.adele.icasa.alarm.AlarmService;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.notification.NotificationService;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component(name="FireAlarmApplication")

@Provides(specifications = {PeriodicRunnable.class}, properties= {
		@StaticServiceProperty(name="icasa.application", type="String", value="Fire.Alarm.Application", immutable=true) 
})


@Instantiate(name = "FireAlarmApplicationImpl-0")
@CommandProvider(namespace = "GasAlarm")
public class FireAlarmApplication implements PeriodicRunnable  {

	private static final Logger m_logger = LoggerFactory.getLogger(FireAlarmApplication.class);

	private double _CO2Threshold = 3.8;
	private int nbre = 0;
	private long sommeDelta= 0;

	private final Object m_lock;
	private boolean state = true;

	@ConflictManagement
	private ConflictManagerService conflictManager;

	@Requires
	NotificationService notificationService;

	@Requires
	AlarmService alarmService;

	@Requires(id="lights",optional = true,specification = BinaryLight.class)
	private List<BinaryLight> binaryLights;

	@Requires(id="carbonDioxydeSensors", optional=true, specification = CarbonDioxydeSensor.class)
	private List<CarbonDioxydeSensor> carbonDioxydeSensors;

	private Map<BinaryLight,String> services = new HashMap<>();

	public FireAlarmApplication() {
		m_lock = new Object();
	}

	/** Component Lifecycle Method */
	@Invalidate
	public void stop() {
		m_logger.info(" Fire Alarm Application is stopping ... ");

	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		m_logger.info(" Fire Alarm Application is starting ... ");
	}

	@Bind(id="carbonDioxydeSensors")
	public void bindCarbonDioxydeSensor(CarbonDioxydeSensor carbonDioxydeSensor){

	}

	@Unbind(id="carbonDioxydeSensors")
	public void unbindCarbonDioxydeSensor(CarbonDioxydeSensor carbonDioxydeSensor){

	}

	@Bind(id="lights")
	public void bindBinaryLight(BinaryLight binaryLight, Map properties){
		services.put(binaryLight,(properties.get("service.id")).toString());

	}

	@Modified(id="lights")
	public void modifiedBinaryLight(BinaryLight binaryLight){

	}


	@Unbind(id="lights")
	public void unbindBinaryLight(BinaryLight binaryLight){
		binaryLight.turnOff();

	}

	public boolean checkCo2() {
		for(CarbonDioxydeSensor sensor : carbonDioxydeSensors){
			synchronized (m_lock){
				if(sensor.getCO2Concentration() > _CO2Threshold ){
					return true;
				}
			}
		}
		return false;

	}

	long start = System.currentTimeMillis();

	@Override
	public void run() {	
		if (checkCo2()) {
			m_logger.info("CO2 is too hight !  ");
			notificationService.sendNotification("[ICASA] CO2 Alert", " CO2 is too hight in the house.");
			alarmService.fireAlarm();
			if (state == true) {
			for(BinaryLight binaryLight : binaryLights) {
				conflictManager.lockService(services.get(binaryLight));
				binaryLight.turnOn();
			}
			state = false;
			}
			else {
				for(BinaryLight binaryLight : binaryLights) {
				conflictManager.lockService(services.get(binaryLight));
					binaryLight.turnOff();
				}
				state = true;
			}
		}   

		else {
			for(BinaryLight binaryLight : binaryLights) {
				conflictManager.unlockService(services.get(binaryLight));

			}
		}
		long fin = System.currentTimeMillis();
		long delta = fin - start;
		nbre++;
		System.out.println( " delta " + delta + ""+ "nbre de fois " + nbre);
		sommeDelta = sommeDelta+ delta;

		System.out.println( " Somme de delta " + sommeDelta );
	}

	@Override
	public long getPeriod() {
		return 1;

	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.SECONDS;

	}

	@Command
	public  void setGasLimit(double value) {
		synchronized (m_lock){
			_CO2Threshold = value;
			m_logger.info(" New Threshold gas value " + _CO2Threshold);
		}

	}

}
