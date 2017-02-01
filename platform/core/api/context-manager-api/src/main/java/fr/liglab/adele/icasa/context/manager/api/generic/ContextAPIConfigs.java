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
package fr.liglab.adele.icasa.context.manager.api.generic;

import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * TEMP
 * Les besoins en API de contexte sont exprimés
 */
public class ContextAPIConfigs {
    /*Liste des configs ordonnées, la première est la config optimale*/
    private List<Set<ContextAPI>> configs;

    public ContextAPIConfigs(Set<ContextAPI> optimalConfig) {
        Objects.requireNonNull(optimalConfig, "optimalConfig parameter must not be null");
        this.configs = new ArrayList<>();
        configs.add(optimalConfig);
    }

    public ContextAPIConfigs(List<Set<ContextAPI>> configs) {
        Objects.requireNonNull(configs, "configs parameter must not be null");
        this.configs = new ArrayList<>(configs);
    }

    public Set<ContextAPI> getOptimalConfig() {
        return getConfig(0);
    }

    public Set<ContextAPI> getConfig(int index) {
        if(index>=0 && index < configs.size()){
            return configs.get(index);
        } else {
            return null;
        }
    }

    public List<Set<ContextAPI>> getConfigList() {
        return configs;
    }

    public boolean setConfigList(List<Set<ContextAPI>> configs) {
        boolean check = true;
        try{
            this.configs = new ArrayList<>(configs);
        } catch (NullPointerException ne){
            check = false;
        }
        return check;
    }

    public boolean addConfigWithLastPriority(Set<ContextAPI> config){
        boolean check = true;
        try{
            configs.add(config);
        } catch (NullPointerException ne){
            check = false;
        }
        return check;
    }

    public boolean addConfigWithFirstPriority(Set<ContextAPI> config){
        boolean check = true;
        try{
            configs.add(0, config);
        } catch (NullPointerException ne){
            check = false;
        }
        return check;
    }
}