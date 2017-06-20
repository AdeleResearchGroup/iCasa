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
package fr.liglab.adele.icasa.context.manager.impl.web.administration;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
import fr.liglab.adele.icasa.context.manager.api.generic.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.web.administration.ContextManagerWebMonitoring;
import fr.liglab.adele.icasa.context.manager.api.web.administration.GoalsByAppMonitoring;
import fr.liglab.adele.icasa.context.manager.impl.specific.ContextInternalManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ContextManagerWebMonitoringImpl implements ContextManagerWebMonitoring {
    /*Monitored elements*/
    @Requires(optional = true)
    @SuppressWarnings("all")
    private GoalModelAccess goalModel;

    @Requires(optional = true)
    @SuppressWarnings("all")
    private ContextInternalManager contextInternalManager;

    @Requires(id = "entityProviders", optional = true)
    @SuppressWarnings("all")
    private EntityProvider[] entityProviders;

    @Requires(id = "relationProviders", optional = true)
    @SuppressWarnings("all")
    private RelationProvider[] relationProviders;


    @Override
    public Set<GoalsByAppMonitoring> getGoalsByApp() {
        Set<GoalsByAppMonitoring> result = new HashSet<>();

        try{
            if(goalModel != null){
                for (String app: goalModel.getManagedApps()){
                    GoalsByAppMonitoring goalsByAppMonitoring
                            = new GoalsByAppMonitoring(app, goalModel.getGoalsStateForApp(app));
                    result.add(goalsByAppMonitoring);
                }
            }
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }

        return result;
    }

    private Map<EntityProvider, Set<String>> getCreatorsByEntityProvider(boolean remote){
        Map<EntityProvider, Set<String>> result = new HashMap<>();
        for(EntityProvider entityProvider : entityProviders){
            Set<String> remoteEntities = new HashSet<>();
            for(String providedEntity : entityProvider.getProvidedEntities()){
                if(entityProvider.isRemote(providedEntity) == remote){
                    String creatorName = Util.eCreatorName(entityProvider, providedEntity);
                    remoteEntities.add(creatorName);
                }
            }
            result.put(entityProvider, remoteEntities);
        }
        return result;
    }

    private Map<RelationProvider, Set<String>> getCreatorsByRelationProvider(boolean remote){
        Map<RelationProvider, Set<String>> result = new HashMap<>();
        for(RelationProvider relationProvider : relationProviders){
            Set<String> remoteRelations = new HashSet<>();
            for(String providedRelation : relationProvider.getProvidedRelations()){
                if(relationProvider.isRemote(providedRelation) == remote){
                    String creatorName = Util.eCreatorName(relationProvider, providedRelation);
                    remoteRelations.add(creatorName);
                }
            }
            result.put(relationProvider, remoteRelations);
        }
        return result;
    }

    @Override
    public Map<EntityProvider, Set<String>> getResourceCreatorsByEntityProvider() {
        return getCreatorsByEntityProvider(true);
    }

    @Override
    public Map<RelationProvider, Set<String>> getResourceCreatorsByRelationProvider() {
        return getCreatorsByRelationProvider(true);
    }

    @Override
    public Map<EntityProvider, Set<String>> getAbstractionCreatorsByEntityProvider() {
        return getCreatorsByEntityProvider(false);
    }

    @Override
    public Map<RelationProvider, Set<String>> getAbstractionCreatorsByRelationProvider() {
        return getCreatorsByRelationProvider(false);
    }

    @Override
    public Set<String> getInstancesByCreator(String creator) {
        Set<String> result = new HashSet<>();
        if(contextInternalManager != null){
            result.addAll(contextInternalManager.getInstancesByCreator(creator));
        }
        return result;
    }

    @Override
    public Set<String> getCurrentLookupFilter() {
        Set<String> result = new HashSet<>();
        if(contextInternalManager != null){
            result.addAll(contextInternalManager.getCurrentLookupFilter());
        }
        return result;
    }
}