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
    private Set<ContextGoal> contextGoals = new HashSet<>();
    private Set<ContextEntity> contextEntities = new HashSet<>();
    private Set<EntityProvider> entityProviders = new HashSet<>();

    @Override
    public void run() {
        /*TODO Hard coder ???*/
        /*Attention aux multiples acces*/
        /*TODO Recupérer les info nécessaires du manager*/
        LOG.debug("CONTEXT RESOLUTION MACHINE - Execution " + i++);
        /*TODO UNCOMMENT*/
        resolutionAlgorithm();
    }

    protected synchronized void configureState(Map<String, ContextGoal> contextGoalMap, ContextEntity[] contextEntities, EntityProvider[] entityProviders){
        this.contextGoals = new HashSet<>(contextGoalMap.values());
        try{
            this.contextEntities = new HashSet<>(Arrays.asList(contextEntities));
        } catch (NullPointerException ne){
            this.contextEntities = new HashSet<>();
        }
        try{
            this.entityProviders = new HashSet<>(Arrays.asList(entityProviders));
        } catch (NullPointerException ne){
            this.entityProviders = new HashSet<>();
        }
    }

    private synchronized void resolutionAlgorithm(){

        /*TODO Algorithme naze, à repenser*/

        /*App, set de services en config optimale*/
        Set<String> goals = new HashSet<>();
        for(ContextGoal contextGoal : contextGoals){
            goals.addAll(contextGoal.getOptimalConfig());
            LOG.debug("GOALS "+contextGoal.getOptimalConfig().toString());
        }

        /*Entities à activer*/
        /*TODO vision au runtime des possibilités de services ?*/
        /*TODO MODIFIER : NE FONCTIONNE PAS (normal, ça ne prend en compte que ce qui est actif*/
        Set<String> entityActivationMap = new HashSet<>();
        for(ContextEntity contextEntity : contextEntities){
            for(String s : contextEntity.getServices()){
                LOG.debug("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
                if(goals.contains(s)){
                    /*TODO MODIFIER*/
//                    entityActivationMap.add(contextEntity.getClass().toString());
//                    LOG.debug("CONTEXT ENTITY TO ACTIVATE "+contextEntity.getClass().toString());
                    entityActivationMap.add(contextEntity.toString());
                    LOG.debug("--> TO ACTIVATE");
                }
            }

        }

        /*Activation de la création des entities dans chaque provider*/
        for(EntityProvider entityProvider : entityProviders){
            for(String providedEntity : entityProvider.getProvidedEntities()){
                /*TODO VERIFIER LA CORRESPONDANCE*/
                if(entityActivationMap.contains(providedEntity)){
                    entityProvider.enable(providedEntity);
                    LOG.debug("PROVIDER "+entityProvider.getName()+" ENABLE "+providedEntity);
                } else {
                    entityProvider.disable(providedEntity);
                    LOG.debug("PROVIDER "+entityProvider.getName()+" DISABLE "+providedEntity);
                }
            }
        }


        /*Verif config optimale*/
        /*TODO*/
//        Set<String> activatedEntityMap = new HashSet<>();
//        for(ContextEntity contextEntity : contextEntities){
//            for(String s : contextEntity.getServices()){
//                LOG.debug("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
//                if(goals.contains(s)){
//                    /*TODO MODIFIER*/
////                    entityActivationMap.add(contextEntity.getClass().toString());
////                    LOG.debug("CONTEXT ENTITY TO ACTIVATE "+contextEntity.getClass().toString());
//                    entityActivationMap.add(contextEntity.toString());
//                    LOG.debug("--> TO ACTIVATE");
//                }
//            }
//
//        }

    }


}