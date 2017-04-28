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
package fr.liglab.adele.icasa.context.manager.impl.temp.generic;

import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMP
 * Différentes configurations possibles du contexte pour fournir une API de contexte
 */
public class ContextMediationConfig {
    private ContextAPI contextAPI;

    private List<ContextMediationSlice> contextMediationSlices;

    public ContextMediationConfig(ContextAPI contextAPI) {
        this.contextAPI = contextAPI;
        contextMediationSlices = new ArrayList<>();
    }

    public ContextMediationConfig(ContextAPI contextAPI, List<ContextMediationSlice> contextMediationSlices) {
        this(contextAPI);
        setContextMediationSlices(contextMediationSlices);
    }

    public ContextAPI getContextAPI() {
        return contextAPI;
    }

    public List<ContextMediationSlice> getContextMediationSlices() {
        return contextMediationSlices;
    }

    public ContextMediationSlice getOptimalContextMediationSlices() {
        return contextMediationSlices.get(0);
    }

    public void setContextMediationSlices(List<ContextMediationSlice> contextMediationSlices) {
        this.contextMediationSlices = contextMediationSlices;
    }

    public void addContextMediationSlice(ContextMediationSlice contextMediationSlice) {
        contextMediationSlices.add(contextMediationSlice);
    }
}