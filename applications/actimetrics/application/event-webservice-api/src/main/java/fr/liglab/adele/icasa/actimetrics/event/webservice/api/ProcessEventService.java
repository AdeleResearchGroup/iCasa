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
package fr.liglab.adele.icasa.actimetrics.event.webservice.api;

import java.util.Date;

/**
 * 
 */
public interface ProcessEventService {

	/**
	 * Call the RestFul WebService with the given event data values
	 * 
	 * @param sensorId
	 *           sensor providing the event
	 * @param patientId
	 *           concerned patient id
	 * @param eventType
	 *           type of event(location, electric,...)
	 * @param dateTime
	 *           date and time when occurred the event
	 * @param reliability
	 *           reliability of the measure
	 * @param value
	 *           corresponding value
	 * @return result
	 */
	public boolean processEventData(String sensorId, String patientId, String eventType, Date dateTime,
	      float reliability, String value) throws ProcessEventException;
}
