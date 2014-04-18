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
package fr.liglab.adele.icasa.actimetrics.event.webservice.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import fr.liglab.adele.icasa.actimetrics.event.webservice.api.ProcessEventException;
import fr.liglab.adele.icasa.actimetrics.event.webservice.api.ProcessEventService;
import fr.liglab.adele.icasa.service.scheduler.ScheduledRunnable;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author Aygalinc Colin
 *
 */
@Component(name = "ActimetryRestServiceClient")
@Instantiate
@Provides(specifications = ProcessEventService.class)
public class RestProcessEventService implements ProcessEventService {

    private static final Logger logger = LoggerFactory
            .getLogger(RestProcessEventService.class);

    /**
     * Web service url.
     */
    @Property
    private String url;

    private Map<SendTask,ServiceRegistration> serviceRegistrationSet = new HashMap<SendTask, ServiceRegistration>();

    private final BundleContext bundleContext;

    private final Object m_lock;
    public RestProcessEventService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        m_lock = new Object();
    }

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
                                                 final Date dateTime, final float reliability, final String value) {

        // log received data
        logger.info("SEND TO " + url);
        logger.info("==================================================");
        logger.info("Patient ID --->" + patientId);
        logger.info("Sensor ID --->" + sensorId);
        logger.info("Date  --->" + formatDateTime(dateTime));
        logger.info("Localisation  --->" + value);
        logger.info("==================================================");

        SendTask sendTask = new SendTask(sensorId,patientId,eventType,dateTime,reliability,value);
        sendTask.setExecutionDate(dateTime.getTime());
        ServiceRegistration computeTempTaskSRef = bundleContext.registerService(ScheduledRunnable.class.getName(), sendTask,new Hashtable());
        synchronized (m_lock){
            serviceRegistrationSet.put(sendTask, computeTempTaskSRef);
        }
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


    /**
     * This task is charged of turn off the light.
     */
    public class SendTask implements ScheduledRunnable {

        private long executionDate ;

        private String groupName = "Actimetrics-Sender";

        final String sensorId;
        final String patientId;
        final String eventType;
        final Date dateTime;
        final float reliability;
        final String value;

        public SendTask(String sensorId, String patientId, String eventType, Date dateTime, float reliability, String value) {
            this.sensorId = sensorId;
            this.patientId = patientId;
            this.eventType = eventType;
            this.dateTime = dateTime;
            this.reliability = reliability;
            this.value = value;
        }


        public void setExecutionDate(long executionDate) {
            this.executionDate = executionDate;
        }

        @Override
        public long getExecutionDate() {
            return executionDate;
        }

        @Override
        public String getGroup() {
            return groupName;
        }

        @Override
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
                    logger.error(" Failed to proceed event data, HTTP error code  " + response.getClientResponseStatus().getReasonPhrase() );
                    throw new ProcessEventException(
                            "Failed to proceed event data, HTTP error code : "
                                    + response.getStatus());
                }
            } catch (Exception ex) {
                logger.error("An error occured when proceeding event data : "
                        + ex.toString());
                // return false;
            }
            synchronized (m_lock){
                serviceRegistrationSet.remove(this);
            }
        }
    }
}
