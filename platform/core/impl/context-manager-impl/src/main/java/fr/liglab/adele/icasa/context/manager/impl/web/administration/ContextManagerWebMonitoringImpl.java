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

import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.Util;
import fr.liglab.adele.icasa.context.manager.api.generic.models.CapabilityModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.ExternalFilterModelAccess;
import fr.liglab.adele.icasa.context.manager.api.generic.models.goals.GoalModelAccess;
import fr.liglab.adele.icasa.context.manager.api.web.administration.ContextManagerWebMonitoring;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.*;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ContextManagerWebMonitoringImpl implements ContextManagerWebMonitoring {
    /*Monitored elements*/

    @Requires(optional = true)
    @SuppressWarnings("all")
    private CapabilityModelAccess capabilityModel;

    @Requires(id = "entityProviders", optional = true)
    @SuppressWarnings("all")
    private EntityProvider[] entityProviders;

    @Requires(id = "relationProviders", optional = true)
    @SuppressWarnings("all")
    private RelationProvider[] relationProviders;

    /*ToDo*/
    private Map<EntityProvider, Set<String>> getCreatorsByEntityProvider(OriginEnum originEnum){
        Map<EntityProvider, Set<String>> result = new HashMap<>();
        for(EntityProvider entityProvider : entityProviders){
            Set<String> remoteEntities = new HashSet<>();
            for(String providedEntity : entityProvider.getProvidedEntities()){
                if(originEnum.equals(entityProvider.getOrigin(providedEntity))){
                    String creatorName = Util.eCreatorName(entityProvider, providedEntity);
                    remoteEntities.add(creatorName);
                }
            }
            result.put(entityProvider, remoteEntities);
        }
        return Collections.unmodifiableMap(result);
    }

    /*ToDo*/
    private Map<RelationProvider, Set<String>> getCreatorsByRelationProvider(OriginEnum originEnum){
        Map<RelationProvider, Set<String>> result = new HashMap<>();
        for(RelationProvider relationProvider : relationProviders){
            Set<String> remoteRelations = new HashSet<>();
            for(String providedRelation : relationProvider.getProvidedRelations()){
                if(originEnum.equals(relationProvider.getOrigin(providedRelation))){
                    String creatorName = Util.eCreatorName(relationProvider, providedRelation);
                    remoteRelations.add(creatorName);
                }
            }
            result.put(relationProvider, remoteRelations);
        }
        return Collections.unmodifiableMap(result);
    }

    /*ToDo*/
    @Override
    public Map<EntityProvider, Set<String>> getResourceCreatorsByEntityProvider() {
//        Map<EntityProvider, Set<String>> result = new HashMap<>();
//        result.putAll(getCreatorsByEntityProvider(OriginEnum.local));
//        result.putAll(getCreatorsByEntityProvider(OriginEnum.remote));
//        return result;
        return getCreatorsByEntityProvider(OriginEnum.local);
    }

    /*ToDo*/
    @Override
    public Map<RelationProvider, Set<String>> getResourceCreatorsByRelationProvider() {
//        Map<RelationProvider, Set<String>> result = new HashMap<>();
//        result.putAll(getCreatorsByRelationProvider(OriginEnum.local));
//        result.putAll(getCreatorsByRelationProvider(OriginEnum.remote));
//        return result;
        return getCreatorsByRelationProvider(OriginEnum.local);
    }

    /*ToDo*/
    @Override
    public Map<EntityProvider, Set<String>> getAbstractionCreatorsByEntityProvider() {
        return getCreatorsByEntityProvider(OriginEnum.internal);
    }

    /*ToDo*/
    @Override
    public Map<RelationProvider, Set<String>> getAbstractionCreatorsByRelationProvider() {
        return getCreatorsByRelationProvider(OriginEnum.internal);
    }

    @Override
    public Set<String> getInstancesByCreator(String creator) {
        Set<String> result = new HashSet<>();
        if(capabilityModel != null){
            result.addAll(capabilityModel.getInstancesByCreator(creator));
        }
        return result;
    }
}