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


import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import fr.liglab.adele.icasa.simulator.person.Person;
import fr.liglab.adele.icasa.simulator.person.PersonProvider;
import fr.liglab.adele.icasa.simulator.person.PersonType;
import fr.liglab.adele.icasa.simulator.remote.wisdom.util.IcasaSimulatorJSONUtil;
import fr.liglab.adele.icasa.simulator.remote.wisdom.util.PersonJSON;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;
import java.util.List;


/**
 *
 */
@Component
@Provides
@Instantiate
@Path("/icasa/persons")
public class PersonREST extends DefaultController {

    @Requires(specification = Person.class, optional = true,proxy = false)
    List<Person> persons;

    @Requires(specification = PersonProvider.class)
    PersonProvider personsProvider;


    /**
     * Returns a JSON array containing all persons.
     *
     * @return a JSON array containing all persons.
     */
    public String getPersons() {
        // boolean atLeastOne = false;
        JSONArray currentPersons = new JSONArray();
        for (Person person : persons) {
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

        for (PersonType personTypeStr : PersonType.values()) {
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

    @Route(method = HttpMethod.GET, uri = "/persons")
    public Result persons() {
        return ok(getPersons()).as(MimeTypes.JSON);
    }


    /**
     * Retrieve a person.
     *
     * @param personName The name of the device to retrieve
     * @return The required device,
     * return <code>null<code> if the device does not exist.
     * @throws java.text.ParseException
     */
    @Route(method = HttpMethod.GET, uri = "/person/{personName}")
    public Result person(@Parameter("personName") String personName) {
        if (personName == null || personName.length()<1){
            return ok(getPersons()).as(MimeTypes.JSON);
        }

        Person foundPerson = findPerson(personName);
        if (foundPerson == null) {
            return notFound();
        } else {
            JSONObject foundPersonJSON = IcasaSimulatorJSONUtil.getPersonJSON(foundPerson);

            return ok(foundPersonJSON.toString()).as(MimeTypes.JSON);
        }
    }

    private Person findPerson(String personName) {
        Person foundPerson = null;
        for (Person person : persons) {
            if (person.getName().equals(personName)) {
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
            content = IcasaJSONUtil.content(context());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }

        PersonJSON personJSON = PersonJSON.fromString(content);

        personsProvider.createPerson(personJSON.getName(), personJSON.getType());

        return ok();
    }

    /**
     * Update an existing person.
     *
     * @return
     */
    @Route(method = HttpMethod.PUT, uri = "/person/{personName}")
    public Result updatesPerson() {

        String content = null;
        try {
            content = IcasaJSONUtil.content(context());
        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError();
        }
        PersonJSON personJSON = PersonJSON.fromString(content);

        Person foundPerson = findPerson(personJSON.getName());
        if (foundPerson == null)
      	  return notFound();

        if (! (foundPerson instanceof LocatedObject)){
            return forbidden();
        }
        LocatedObject locatedPerson = (LocatedObject) foundPerson;
        if ((personJSON.getPositionX() != null) && (personJSON.getPositionY() != null)) {
            locatedPerson.setPosition(new Position(personJSON.getPositionX(), personJSON.getPositionY()));
        }

        return ok();
    }

    /**
     * Delete specified person.
     *
     * @param personName person identifier
     * @return ok if person is successful deleted, 404 response if it does not exist.
     */

    @Route(method = HttpMethod.DELETE, uri = "/person/{personName}")
    public Result deletePerson(@Parameter("personName") String personName) {

        Person foundPerson = findPerson(personName);
        if (foundPerson == null)
            return notFound();

        try {
            personsProvider.removePerson(foundPerson.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError();
        }

        return ok();
    }

}
