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

package fr.liglab.adele.icasa.context.manager.impl;
import fr.liglab.adele.icasa.context.manager.api.ContextGoal;
import fr.liglab.adele.icasa.context.manager.api.ContextGoalRegistration;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
@Component(publicFactory = false)
@Provides
@Instantiate
public class ContextManager implements ContextGoalRegistration, Runnable {

    /*Runnable parameters*/
    private static Thread thread = null;
    private static boolean end = true;
    private static final int DELAY = 10 * 1000; //milliseconds

    private Map<String, ContextGoal> contextGoalMap = new HashMap<>();

    @Validate
    public void start(){
        thread = new Thread(this);
        end = false;
        thread.start();
    }

    @Invalidate
    public void stop(){
        end = true;
    }

    @Override
    public boolean registerContextGoals(String appId, ContextGoal contextGoal) {
        contextGoalMap.put(appId,contextGoal);
        thread.interrupt(); //refresh
        return true;
    }

    @Override
    public ContextGoal getRegisteredContextGoals(String appId) {
        return contextGoalMap.get(appId);
    }

    @Override
    public boolean unregisterContextGoals(String appId) {
        contextGoalMap.remove(appId);
        return true;
    }


    @Override
    public void run() {
        while (!end) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ie) {
                /* will recheck quit */
            } finally {
                /*TODO do something*/
            }
        }
    }
}