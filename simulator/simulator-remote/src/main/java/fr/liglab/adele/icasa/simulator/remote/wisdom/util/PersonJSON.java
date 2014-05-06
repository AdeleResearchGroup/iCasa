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
package fr.liglab.adele.icasa.simulator.remote.wisdom.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO
 *
 */
public class PersonJSON {

    public static final String POSITION_Y_PROP = "positionY";
    public static final String POSITION_X_PROP = "positionX";
    public static final String LOCATION_PROP = "location";
    public static final String NAME_PROP = "name";
    public static final String TYPE_PROP = "type";
    public static final String PERSON_ID_PROP = "personId";
    public static final String ID_PROP = "id";

    private String name;
    private String type;
    private String id;
    private String location;
    private Integer positionX;
    private Integer positionY;

    public PersonJSON() {
        //do nothing
    }

    public static PersonJSON fromString(String jsonStr) {
        PersonJSON person = null;
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            person = new PersonJSON();
            if (json.has(ID_PROP)) {
                person.setId(json.getString(ID_PROP));
            } else if (json.has(PERSON_ID_PROP)) {
                person.setId(json.getString(PERSON_ID_PROP));
            };
            if (json.has(NAME_PROP))
                person.setName(json.getString(NAME_PROP));
            if (json.has(TYPE_PROP))
                person.setType(json.getString(TYPE_PROP));
            if (json.has(LOCATION_PROP))
                person.setLocation(json.getString(LOCATION_PROP));
            if (json.has(POSITION_X_PROP))
                person.setPositionX(json.getInt(POSITION_X_PROP));
            if (json.has(POSITION_Y_PROP))
                person.setPositionY(json.getInt(POSITION_Y_PROP));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return person;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getPositionX() {
        return positionX;
    }

    public void setPositionX(Integer positionX) {
        this.positionX = positionX;
    }

    public Integer getPositionY() {
        return positionY;
    }

    public void setPositionY(Integer positionY) {
        this.positionY = positionY;
    }

    public JSONObject toJSONObject() {
        JSONObject personJSON = null;
        try {
            personJSON = new JSONObject();
            personJSON.putOnce("id", getName());
            personJSON.putOnce("name", getName());
            personJSON.put("positionX", getPositionX());
            personJSON.put("positionY", getPositionY());
            personJSON.putOnce("location", getLocation());
        } catch (JSONException e) {
            e.printStackTrace();
            personJSON = null;
        }

        return personJSON;
    }
}
