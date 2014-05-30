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
package fr.liglab.adele.habits.monitoring.measure.generator;

import java.util.Date;

import fr.liglab.adele.habits.monitoring.event.webservice.api.ProcessEventService;
import fr.liglab.adele.habits.monitoring.event.webservice.api.ProcessEventException;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;

public class MeasureSender implements ISender {

	private ProcessEventService eventService;
	
	public boolean send(Data data) {
		if (data != null) {
			Measure measure = (Measure) data.getContent();
			String sensorId = measure.getDeviceId();
			if (sensorId.equals("X10.A1") || sensorId.equals("uuid:7cec7632-1dd2-11b2-9de3-48e5e124c101"))
				return false;
			
			Date date = new Date(measure.getTimestamp());
			try {
	         eventService.processEventData("shake", measure.getPatientId(), "location", date, measure.getReliability(), measure.getLocalisation());
         } catch (ProcessEventException e) {
	         e.printStackTrace();
         }
			return true;
		}
		return false;
	}

}
