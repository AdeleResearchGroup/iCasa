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
package fr.liglab.adele.habits.monitoring.event.webservice.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.liglab.adele.habits.monitoring.event.webservice.api.ProcessEventException;
import fr.liglab.adele.habits.monitoring.event.webservice.api.ProcessEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * 
 */
public class RestProcessEventService implements ProcessEventService {

	private static final Logger logger = LoggerFactory
			.getLogger(RestProcessEventService.class);

	/**
	 * Web service url.
	 */
	private String url;

	/**
	 * Call the RestFul WebService with the given event data values
	 * 
	 * @param sensorId
	 *            sensor providing the event
	 * @param patientId
	 *            concerned patient id
	 * @param eventType
	 *            type of event(location, electric,...)
	 * @param dateTime
	 *            date and time when occurred the event
	 * @param reliability
	 *            reliability of the measure
	 * @param value
	 *            corresponding value
	 * @return result
	 */
	public synchronized boolean processEventData(final String sensorId,
			final String patientId, final String eventType,
			final Date dateTime, final float reliability, final String value)
			throws ProcessEventException {

		// log received data
		logger.info("==================================================");
		logger.info("Patient ID --->" + patientId);
		logger.info("Sensor ID --->" + sensorId);
		logger.info("Date  --->" + formatDateTime(dateTime));
		logger.info("Localisation  --->" + value);
		logger.info("==================================================");

		Runnable runWSCall = new Runnable() {

			public void run() {
				if (sensorId == null || sensorId.length() == 0) {
					throw new IllegalArgumentException(
							"sensor id is null or empty");
				}
				if (patientId == null || patientId.length() == 0) {
					throw new IllegalArgumentException(
							"patient id is null or empty");
				}
				if (eventType == null || eventType.length() == 0) {
					throw new IllegalArgumentException(
							"event type is null or empty");
				}
				if (dateTime == null) {
					throw new IllegalArgumentException("date time is null");
				}
				if (value == null || value.length() == 0) {
					throw new IllegalArgumentException(
							"event value is null or empty");
				}
				try {
					Client client = Client.create();
					WebResource webResource = client.resource(url + eventType);
					String xmlData = "<locationEvent><patientId>" + patientId
							+ "</patientId><reliability>" + reliability
							+ "</reliability><sensorId>" + sensorId
							+ "</sensorId><timeStamp>"
							+ formatDateTime(dateTime)
							+ "</timeStamp><location>" + value
							+ "</location><x>0.0</x><y>0.0</y></locationEvent>";
					ClientResponse response = (ClientResponse) webResource
							.type(MediaType.APPLICATION_XML).put(
									ClientResponse.class, xmlData);

					if (response.getStatus() == Response.Status.OK
							.getStatusCode()) {
						// return true;
					} else {
						throw new ProcessEventException(
								"Failed to proceed event data, HTTP error code : "
										+ response.getStatus());
					}
				} catch (Exception ex) {
					logger.error("An error occured when proceeding event data : "
							+ ex.toString());
					// return false;
				}

			}
		};

		Thread exeThread = new Thread(runWSCall);
		exeThread.start();

		return true;

	}

	/**
	 * Format the date in a structure described by WebService
	 * 
	 * @param date
	 *            the event date
	 * @return the formatted date
	 */
	private String formatDateTime(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		String s = formatter.format(date);
		// String formattedDate =
		// s+"T"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
		return s;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
