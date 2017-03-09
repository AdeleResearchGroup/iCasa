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
package fr.liglab.adele.icasa.context.manager.impl.specific;

import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfigs;

import java.util.Map;
import java.util.Set;

/**
 * Created by Eva on 16/02/2017.
 */
public interface ContextInternalManager {
    void configureGoals(Map<String, ContextAPIConfigs> contextGoalMap);

    Runnable getContextResolutionMachine();

    Set<String> getCurrentLookupFilter();
}
