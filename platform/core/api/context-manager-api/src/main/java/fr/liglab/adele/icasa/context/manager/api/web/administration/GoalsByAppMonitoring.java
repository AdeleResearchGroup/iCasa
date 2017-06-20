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
package fr.liglab.adele.icasa.context.manager.api.web.administration;

import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;

import java.util.Map;

public class GoalsByAppMonitoring {
    private String app;
    private Map<ContextAPIEnum, Boolean> goals;

    public GoalsByAppMonitoring(String app, Map<ContextAPIEnum, Boolean> goals) {
        this.app = app;
        this.goals = goals;
    }

    public String getApp() {
        return app;
    }

    public Map<ContextAPIEnum, Boolean> getGoals() {
        return goals;
    }
}
