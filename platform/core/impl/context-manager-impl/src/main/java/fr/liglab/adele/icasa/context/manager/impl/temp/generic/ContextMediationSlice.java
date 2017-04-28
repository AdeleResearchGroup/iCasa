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

import fr.liglab.adele.icasa.context.manager.impl.temp.specific.AbstractContextEntities;
import fr.liglab.adele.icasa.context.manager.impl.temp.specific.EnvProxy;

import java.util.HashSet;
import java.util.Set;

/**
 * TEMP
 * Une configuration complète possible faisant la médiation entre l'environnement et les applications
 */
public class ContextMediationSlice {
    private Set<AbstractContextEntities> abstractContextEntities;

    private Set<EnvProxy> envProxies;

    public ContextMediationSlice(Set<AbstractContextEntities> abstractContextEntities,Set<EnvProxy> envProxies) {
        if(abstractContextEntities !=null){
            this.abstractContextEntities = abstractContextEntities;
        } else {
            this.abstractContextEntities = new HashSet<>();
        }

        if(envProxies != null){
            this.envProxies = envProxies;
        } else {
            this.envProxies = new HashSet<>();
        }
    }

    public Set<AbstractContextEntities> getAbstractContextEntities() {
        return abstractContextEntities;
    }

    public void setAbstractContextEntities(Set<AbstractContextEntities> abstractContextEntities) {
        this.abstractContextEntities = abstractContextEntities;
    }

    public Set<EnvProxy> getEnvProxies() {
        return envProxies;
    }

    public void setEnvProxies(Set<EnvProxy> envProxies) {
        this.envProxies = envProxies;
    }
}
