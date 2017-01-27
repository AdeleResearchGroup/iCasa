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
package fr.liglab.adele.icasa.context.manager.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * TEMP
 * Les besoins en API de contexte sont exprimés
 */
public class ContextGoal {
    /*Liste des configs ordonnées, la première est la config optimale*/
    private List<Set<String>> configs;

    public ContextGoal(Set<String> optimalConfig) {
        Objects.requireNonNull(optimalConfig, "optimalConfig parameter must not be null");
        this.configs = new ArrayList<>();
        configs.add(optimalConfig);
    }

    public ContextGoal(List<Set<String>> configs) {
        Objects.requireNonNull(configs, "configs parameter must not be null");
        this.configs = new ArrayList<>(configs);
    }

    public Set<String> getOptimalConfig() {
        return getConfig(0);
    }

    public Set<String> getConfig(int index) {
        if(index>=0 && index < configs.size()){
            return configs.get(index);
        } else {
            return null;
        }
    }

    public List<Set<String>> getConfigList() {
        return configs;
    }

    public boolean setConfigList(List<Set<String>> configs) {
        boolean check = checkConfigList(configs);
        if(check){this.configs = new ArrayList<>(configs);}
        return check;
    }

    public boolean addConfigWithLastPriority(Set<String> config){
        if(config != null){
            return configs.add(config);
        } else {
            return false;
        }
    }

    public boolean addConfigWithFirstPriority(Set<String> config){
        /*TODO améliorer*/
        if(config != null){
            configs.add(0, config);
            return true;
        } else {
            return false;
        }
    }

    private boolean checkConfigList(List<Set<String>> configList){
        for(Set<String> config : configList){
            if(!checkConfig(config)){return false;}
        }
        return true;
    }

    private boolean checkConfig(Set<String> config){
        boolean check = true;
        /*Check if all the interfaces exist*/
        try {
            for(String contextInterface : config){
               Class.forName(contextInterface);
            }
        } catch (ClassNotFoundException e) {
            check = false;
        }
        return check;
    }
}