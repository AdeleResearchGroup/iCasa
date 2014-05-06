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
package fr.liglab.adele.icasa.simulator.remote.wisdom.impl;


import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import fr.liglab.adele.icasa.simulator.Person;
import fr.liglab.adele.icasa.simulator.PersonType;
import fr.liglab.adele.icasa.simulator.SimulationManager;
import fr.liglab.adele.icasa.simulator.remote.wisdom.util.IcasaSimulatorJSONUtil;
import fr.liglab.adele.icasa.simulator.remote.wisdom.util.PersonJSON;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;

//import org.wisdom.api.http.Response;

/**
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/persons")
public class PersonREST extends DefaultController {

    @Requires
    private SimulationManager _simulationMgr;



    /**
     * Returns a JSON array containing all persons.
     *
     * @return a JSON array containing all persons.
     */
    public String getPersons() {
        // boolean atLeastOne = false;
        JSONArray currentPersons = new JSONArray();
        for (Person person : _simulationMgr.getPersons()) {
            JSONObject personJSON = IcasaSimulatorJSONUtil.getPersonJSON(person);
            if (personJSON == null)
                continue;

            currentPersons.put(personJSON);
        }

        return currentPersons.toString();
    }


    /**
     * Returns a JSON array containing all person types.
     *
     * @return a JSON array containing all person types.
     */
    public String getPersonTypes() {
        //boolean atLeastOne = false;
        JSONArray currentPersonTypes = new JSONArray();
        for (PersonType personTypeStr : _simulationMgr.getPersonTypes()) {
            JSONObject personType = IcasaSimulatorJSONUtil.getPersonTypeJSON(personTypeStr);
            if (personType == null)
                continue;

            currentPersonTypes.put(personType);
        }

        return currentPersonTypes.toString();
    }


    @Route(method = HttpMethod.GET, uri = "/personTypes")
    public Result personTypes() {
        return ok(getPersonTypes()).as(MimeTypes.JSON);
    }

    @Route(method = HttpMethod.OPTIONS, uri = "/personTypes")
    public Result getPersonTypesOptions() {
        return ok();
    }


    @Route(method = HttpMethod.GET, uri = "/persons")
    public Result persons() {
        return ok(getPersons()).as(MimeTypes.JSON);
    }


    /**
     * Retrieve a device.
     *
     * @param deviceId The ID of the device to retrieve
     * @return The required device,
     * return <code>null<code> if the device does not exist.
     * @throws java.text.ParseException
     */
    @Route(method = HttpMethod.GET, uri = "/person/{personId}")
    public Result person(@Parameter("personId") String deviceId) {
        if (deviceId == null || deviceId.length()<1){
            return ok(getPersons()).as(MimeTypes.JSON);
        }

        Person foundPerson = findPerson(deviceId);
        if (foundPerson == null) {
            return notFound();
        } else {
            JSONObject foundPersonJSON = IcasaSimulatorJSONUtil.getPersonJSON(foundPerson);

            return ok(foundPersonJSON.toString()).as(MimeTypes.JSON);
        }
    }

    private Person findPerson(String personId) {
        Person foundPerson = null;
        for (Person person : _simulationMgr.getPersons()) {
            if (person.getName().equals(personId)) {
                foundPerson = person;
                break;
            }
        }
        return foundPerson;
    }

    /**
     * Create a new person.
     *
     * @return
     */
    @Route(method = HttpMethod.POST, uri = "/person")
    public Result createPerson() {

        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }

        PersonJSON personJSON = PersonJSON.fromString(content);

        _simulationMgr.addPerson(personJSON.getName(), personJSON.getType());
        _simulationMgr.setPersonPosition(personJSON.getName(), new Position(personJSON.getPositionX(), personJSON.getPositionY()));

        Person newPerson = findPerson(personJSON.getName());
        if (newPerson == null)
            return internalServerError();

        JSONObject newPersonJSON = IcasaSimulatorJSONUtil.getPersonJSON(newPerson);

        return ok(newPersonJSON.toString()).as(MimeTypes.JSON);
    }

    /**
     * Update an existing person.
     *
     * @param personId
     *
     * @return
     */
    @Route(method = HttpMethod.PUT, uri = "/person/{personId}")
    public Result updatesPerson(@Parameter("personId") String personId) {

        String content = null;
        try {
            content = IcasaJSONUtil.getContent(context().reader());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        PersonJSON personJSON = PersonJSON.fromString(content);

        Person foundPerson = findPerson(personId);
        if (foundPerson == null)
      	  return notFound();

        if ((personJSON.getPositionX() != null) || (personJSON.getPositionY() != null)) {
            Position personPosition = foundPerson.getCenterAbsolutePosition();
            if (personJSON.getPositionX() == null)
                personJSON.setPositionX(personPosition.x);
            if (personJSON.getPositionY() == null)
                personJSON.setPositionY(personPosition.y);
            _simulationMgr.setPersonPosition(personJSON.getName(), new Position(personJSON.getPositionX(), personJSON.getPositionY()));
        } else if (personJSON.getLocation() != null) {
            _simulationMgr.setPersonZone(personId, personJSON.getLocation());
        }

        
        JSONObject newPersonJSON = IcasaSimulatorJSONUtil.getPersonJSON(findPerson(personId));

        return ok(newPersonJSON.toString()).as(MimeTypes.JSON);
    }

    /**
     * Delete specified person.
     *
     * @param personId person identifier
     * @return ok if person is successful deleted, 404 response if it does not exist.
     */

    @Route(method = HttpMethod.DELETE, uri = "/person/{personId}")
    public Result deletePerson(@Parameter("personId") String personId) {

        Person foundPerson = findPerson(personId);
        if (foundPerson == null)
            return notFound();

        try {
            _simulationMgr.removePerson(foundPerson.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError();
        }

        return ok();
    }
}
