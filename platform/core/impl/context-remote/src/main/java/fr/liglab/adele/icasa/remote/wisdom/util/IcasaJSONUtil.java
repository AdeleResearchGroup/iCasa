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
package fr.liglab.adele.icasa.remote.wisdom.util;


import java.io.BufferedReader;
import java.io.IOException;

import org.wisdom.api.http.Context;

import org.json.JSONException;
import org.json.JSONObject;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.remote.context.impl.ClockREST;
import fr.liglab.adele.icasa.clockservice.util.DateTextUtil;


public class IcasaJSONUtil {

    public static String content(Context context) throws IOException {
        
    	StringBuilder content = new StringBuilder();
        
        try {
        	
        	BufferedReader reader = context.reader();
        	String line = null;
        	
            while ( (line = reader.readLine()) != null) {
                content.append(line);
            }
            
        } catch (IOException e) {
        }
        
        return content.toString();
    }
    

    public static JSONObject serialize(String person) throws JSONException {
       	JSONObject result = new JSONObject();
       	
       	result.putOnce("id", person);
       	result.putOnce("name", person);

		return result;
    }

  
    public static JSONObject serialize(Clock clock) throws JSONException {
    	
    	JSONObject result = new JSONObject();
    	
    	result.putOnce("id", ClockREST.DEFAULT_INSTANCE_NAME); //TODO should be changed to manage multiple clocks
    	result.putOnce("factor", clock.getFactor());
    	result.putOnce("pause", clock.isPaused());
    	
    	long start	= clock.getStartDate();
    	long now	= clock.currentTimeMillis();
    	
    	result.putOnce("startDateStr", DateTextUtil.getTextDate(start));
    	result.putOnce("startDate", start);
    	
    	result.putOnce("currentDateStr", DateTextUtil.getTextDate(now));
    	result.putOnce("currentTime", now);
    	
    	return result;
    }


}
