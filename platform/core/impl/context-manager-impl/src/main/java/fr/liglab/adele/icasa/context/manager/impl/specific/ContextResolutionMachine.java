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

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfigs;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationConfig;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.util.*;

/**
 * TEMP
 * Classe qui détermine la meilleure configuration avec les buts et l'environnement actuel
 */
final class ContextResolutionMachine implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ContextResolutionMachine.class);

    private static int i = 0;

    /*Copied state*/
    private Set<ContextAPIConfigs> contextAPIConfigss = new HashSet<>();
    private Set<ContextEntity> contextEntities = new HashSet<>();
    private Set<EntityProvider> entityProviders = new HashSet<>();

    private Map<ContextAPI,ContextMediationConfig> contextMediationConfigMap = new ContextMediationConfigMap().getContextMediationConfigMap();

    @Override
    public void run() {
        /*Attention aux multiples acces*/
        /*TODO Recupérer les info nécessaires du manager*/
        LOG.debug("CONTEXT RESOLUTION MACHINE - Execution " + i++);
        /*TODO UNCOMMENT*/
        resolutionAlgorithm();
    }

    public synchronized void configureState(Map<String, ContextAPIConfigs> contextGoalMap, ContextEntity[] contextEntities, EntityProvider[] entityProviders){
        this.contextAPIConfigss = new HashSet<>(contextGoalMap.values());
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
        /*TODO Sélectionner les configs ?*/
        Set<ContextAPI> goals = new HashSet<>();
        for(ContextAPIConfigs contextAPIConfigs : contextAPIConfigss){
            goals.addAll(contextAPIConfigs.getOptimalConfig());
            LOG.info("GOALS "+ contextAPIConfigs.getOptimalConfig().toString());
        }

        /*Entities à activer*/
        /*TODO AMELIORER*/
        /*TODO vision au runtime des possibilités de services ?*/
        Set<AbstractContextEntities> entityActivationSet = new HashSet<>();
        Set<EnvProxy> proxyActivationSet = new HashSet<>();

//        for(ContextEntity contextEntity : contextEntities){
//            for(String s : contextEntity.getServices()){
//                LOG.info("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
//                /*TODO try/Catch null pointer*/
//                ContextAPI contextAPI = ContextAPI.containsInterface(s);
//                if(contextAPI != null) {
//                    if (goals.contains(contextAPI)) {
//                    /*TODO MODIFIER*/
//                        ContextMediationConfig contextMediationConfig = contextMediationConfigMap.get(contextAPI);
//                        ContextMediationSlice contextMediationSlice = contextMediationConfig.getOptimalContextMediationSlices();
//                        entityActivationSet.addAll(contextMediationSlice.getAbstractContextEntities());
//                        proxyActivationSet.addAll(contextMediationSlice.getEnvProxies());
//                        LOG.info("--> TO ACTIVATE");
//                    }
//                }
//            }
//        }

//        for (ContextAPI contextAPI : ContextAPI.values()){
//            LOG.info("CONTEXT ENTITY "+contextAPI);
//            if (goals.contains(contextAPI)) {
//                    /*TODO MODIFIER -> PROBLEM*/
//                ContextMediationConfig contextMediationConfig = contextMediationConfigMap.get(contextAPI);
//                ContextMediationSlice contextMediationSlice = contextMediationConfig.getOptimalContextMediationSlices();
//                entityActivationSet.addAll(contextMediationSlice.getAbstractContextEntities());
//                proxyActivationSet.addAll(contextMediationSlice.getEnvProxies());
//                LOG.info("--> TO ACTIVATE");
//            }
//        }

        for (ContextAPI contextAPI : goals){
            LOG.info("CONTEXT API TO ACTIVATE: "+contextAPI);
            ContextMediationConfig contextMediationConfig = contextMediationConfigMap.get(contextAPI);
            if(contextMediationConfig !=null){
                ContextMediationSlice contextMediationSlice = contextMediationConfig.getOptimalContextMediationSlices();
                if(contextMediationSlice != null){
                    entityActivationSet.addAll(contextMediationSlice.getAbstractContextEntities());
                    proxyActivationSet.addAll(contextMediationSlice.getEnvProxies());
                    LOG.info("--> IN ACTIVATION LIST - ENTITY: "+ entityActivationSet +" - PROXY: "+ proxyActivationSet);
                }
            }
        }



        /*Activation de la création des entities dans chaque provider*/
        for(EntityProvider entityProvider : entityProviders){
            for(String implementation : entityProvider.getProvidedEntities()){
                /*TODO VERIFIER LA CORRESPONDANCE*/
                AbstractContextEntities abstractContextEntities = AbstractContextEntities.containsImplem(implementation);
                if(abstractContextEntities != null){
                    if(entityActivationSet.contains(abstractContextEntities)){
                        entityProvider.enable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE ENTITY "+implementation);
                    } else {
                        entityProvider.disable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE ENTITY "+implementation);
                    }
                }

                EnvProxy envProxy = EnvProxy.containsImplem(implementation);
                if(envProxy != null){
                    if(proxyActivationSet.contains(envProxy)){
                        entityProvider.enable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" ENABLE PROXY "+implementation);
                    } else {
                        entityProvider.disable(implementation);
                        LOG.info("PROVIDER "+entityProvider.getName()+" DISABLE PROXY "+implementation);
                    }
                }

            }
            /*TODO with proxies*/
        }


        /*Verif config optimale*/
        /*TODO*/
//        Set<String> activatedEntityMap = new HashSet<>();
//        for(ContextEntity contextEntity : contextEntities){
//            for(String s : contextEntity.getServices()){
//                LOG.debug("CONTEXT ENTITY "+contextEntity.toString()+" SERVICE "+s);
//                if(goals.contains(s)){
////                    entityActivationSet.add(contextEntity.getClass().toString());
////                    LOG.debug("CONTEXT ENTITY TO ACTIVATE "+contextEntity.getClass().toString());
//                    entityActivationSet.add(contextEntity.toString());
//                    LOG.debug("--> TO ACTIVATE");
//                }
//            }
//
//        }

    }


}