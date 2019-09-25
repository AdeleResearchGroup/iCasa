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
package fr.liglab.adele.icasa.remote.context.serialization;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class SerializedItem {

    protected final JSONObject  serialization;

	protected SerializedItem(String  serialization) throws JSONException {
		this.serialization = new JSONObject(serialization);
	}

	protected SerializedItem() {
		this.serialization = new JSONObject();
	}

	public JSONObject toJson() {
		return serialization;
	}

	protected static final String ID_PROP 		= "id";
    protected static final String UNKNOWN_ID  	= "<UNKNOWN-ID>";

	public String getId() {
		return serialization.optString(ID_PROP, UNKNOWN_ID);	
	}

	protected void setId(String id) throws JSONException {
		setAttribute(SerializedItem.ID_PROP, id);
	}

	protected void setAttribute(String key, Object value) throws JSONException {
		serialization.putOnce(key,value);
	}

    protected static final String NOT_AVAILABLE  	= "N/A";

	protected <Q extends Quantity<Q>> void  addProperty(String propertySet, String name, Object value, Unit<Q> unit) throws JSONException {
        
    	JSONObject property = new JSONObject();
        
    	property.put("name", name);
    	property.put("value", serialize(value));
    	property.put("unit", serialize(unit));
        
    	serialization.append(propertySet,property);
    }

    private static Object serialize(Object value) {
        
    	if (value instanceof Double) {
            return serialize((Double)value);
        }

        if (value instanceof Float) {
            return serialize((Float)value);
        }

        if (value instanceof Unit) {
            return serialize((Unit<?>)value);
        }

        return value;
    }
    
    private static Object serialize(Float value) {
        
    	if (Float.isInfinite(value) || Float.isNaN(value)) {
            return String.valueOf(value);
        }
        
        return value;
    }

    private static Object serialize(Double value) {
        
    	if (Double.isInfinite(value) || Double.isNaN(value)) {
            return String.valueOf(value);
        }
        
        return value;
    }

    private static Object serialize(Unit<?> value) {
        return value != null ? value.getSymbol() : NOT_AVAILABLE;
    }


}
