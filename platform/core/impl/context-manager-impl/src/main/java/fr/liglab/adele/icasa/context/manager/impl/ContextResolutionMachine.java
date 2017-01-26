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

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.ContextGoal;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TEMP
 * Classe qui détermine la meilleure configuration avec les buts et l'environnement actuel
 */
final class ContextResolutionMachine implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ContextResolutionMachine.class);

    private static int i = 0;

    /*Copied state*/
    private Set<ContextGoal> contextGoals;
    private Set<ContextEntity> contextEntities;
    private Set<EntityProvider> entityProviders;
    private Set<RelationProvider> relationProviders;

    @Override
    public void run() {
        /*TODO Hard coder ???*/
        /*Attention aux multiples acces*/
        /*TODO Recupérer les info nécessaires du manager*/
        LOG.info("Execution " + i++);
        resolutionAlgorithm();
    }

    protected synchronized void configureState(Map<String, ContextGoal> contextGoalMap, ContextEntity[] contextEntities, EntityProvider[] entityProviders, RelationProvider[]relationProviders){
        this.contextGoals = new HashSet<>(contextGoalMap.values());
        this.contextEntities = new HashSet<>(Arrays.asList(contextEntities));
        this.entityProviders = new HashSet<>(Arrays.asList(entityProviders));
        this.relationProviders = new HashSet<>(Arrays.asList(relationProviders));
    }

    private synchronized void resolutionAlgorithm(){

        /*TODO Algorithme naze, à repenser*/

        /*Services disponibles, avec les entités de contexte correspondantes*/
        Map<String, Set<ContextEntity>> contextEntityByService = new HashMap<>();
        for(ContextEntity contextEntity : contextEntities){
            Set<String> services = new HashSet<>();
            services.addAll(contextEntity.getServices());

            for (String service : services){
                Set<ContextEntity> contextEntitySet = contextEntityByService.get(service);

                if(contextEntitySet == null){
                    contextEntitySet = new HashSet<>();
                }
                contextEntitySet.add(contextEntity);
                contextEntityByService.put(service, contextEntitySet);
            }
        }

        /*App, Config optimale*/
        Set<String> goals = new HashSet<>();
        for(ContextGoal contextGoal : contextGoals){
            goals.addAll(contextGoal.getOptimalConfig());
        }

//        for(String goal : goals){
////            if(services.contains(goal))
//            //TODO Continuer
//        }

    }
}