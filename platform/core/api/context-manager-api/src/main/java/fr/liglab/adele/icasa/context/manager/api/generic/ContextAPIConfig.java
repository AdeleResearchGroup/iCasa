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

import java.util.*;

/**
 * TEMP
 * Les besoins en API de contexte sont exprimés
 */
public class ContextAPIConfig {
    /*Liste des configs ordonnées, la première est la config optimale*/
    private Set<ContextAPI> config;

    public ContextAPIConfig() {
        this.config = new HashSet<>();
    }

    public ContextAPIConfig(Set<ContextAPI> config) {
        Objects.requireNonNull(config, "config parameter must not be null");
        this.config = new HashSet<>(config);
    }

    public Set<ContextAPI> getConfig() {
        return new HashSet<>(config);
    }

    public boolean resetConfig() {
        config = new HashSet<>();
        return config.isEmpty();
    }

    public boolean setConfig(Set<ContextAPI> config) {
        Objects.requireNonNull(config, "config parameter must not be null");
        this.config = new HashSet<>(config);
        return this.config.containsAll(config);
    }

    public boolean addContextAPI(ContextAPI contextAPI) {
        Objects.requireNonNull(config, "contextAPI parameter must not be null");
        config.add(contextAPI);
        return config.contains(contextAPI);
    }

    public boolean removeContextAPI(ContextAPI contextAPI) {
        Objects.requireNonNull(config, "contextAPI parameter must not be null");
        config.remove(contextAPI);
        return !config.contains(contextAPI);
    }

    public boolean addContextAPIs(Set<ContextAPI> contextAPI) {
        Objects.requireNonNull(config, "contextAPI parameter must not be null");
        config.addAll(contextAPI);
        return config.containsAll(contextAPI);
    }

    public boolean removeContextAPIs(Set<ContextAPI> contextAPI) {
        Objects.requireNonNull(config, "contextAPI parameter must not be null");
        return config.removeAll(contextAPI);
    }

/*POUR LES CONFIG MULTIPLES / ALTERNATIVE (PRIORITE PAR ORDRE)*/
//    public Set<ContextAPI> getOptimalConfig() {
//        return getConfig(0);
//    }
//
//    public Set<ContextAPI> getConfig(int index) {
//        if(index>=0 && index < configs.size()){
//            return configs.get(index);
//        } else {
//            return null;
//        }
//    }
//
//    public List<Set<ContextAPI>> getConfigList() {
//        return configs;
//    }
//
//    public boolean setConfigList(List<Set<ContextAPI>> configs) {
//        boolean check = true;
//        try{
//            this.configs = new ArrayList<>(configs);
//        } catch (NullPointerException ne){
//            check = false;
//        }
//        return check;
//    }
//
//    public boolean addConfigWithLastPriority(Set<ContextAPI> config){
//        boolean check = true;
//        try{
//            configs.add(config);
//        } catch (NullPointerException ne){
//            check = false;
//        }
//        return check;
//    }
//
//    public boolean addConfigWithFirstPriority(Set<ContextAPI> config){
//        boolean check = true;
//        try{
//            configs.add(0, config);
//        } catch (NullPointerException ne){
//            check = false;
//        }
//        return check;
//    }
}