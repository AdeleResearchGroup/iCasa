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

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.api.web.administration.ContextManagerWebMonitoring;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.Map;
import java.util.Set;


@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class WebController extends DefaultController {

    @Requires
    Json json;

    @Requires(optional = true)
    private ContextManagerWebMonitoring contextManagerWebMonitoring;

    @Route(method = HttpMethod.GET, uri = "/manager/goals")
    public Result getGoals() {
        ObjectNode result = json.newObject();

        if(contextManagerWebMonitoring != null){
            for(Map.Entry<String,ContextAPIConfig> appGoal: contextManagerWebMonitoring.getGoalsByApp().entrySet()){
                Set<ContextAPIEnum> g = appGoal.getValue().getConfig();
                result.put(appGoal.getKey(), g.toString());
            }
        }

        return ok(result);
    }

    @Route(method = HttpMethod.GET, uri = "/manager/resources")
    public Result getResources() {
        ObjectNode result = json.newObject();
        ObjectNode entityProviders = json.newObject();
        ObjectNode relationProviders = json.newObject();

        if(contextManagerWebMonitoring != null){
            for(Map.Entry<EntityProvider, Set<String>> entityProviderSetEntry: contextManagerWebMonitoring.getResourceCreatorsByEntityProvider().entrySet()){
                ObjectNode entities = json.newObject();
                for(String creatorName : entityProviderSetEntry.getValue()){
                    entities.put(creatorName, contextManagerWebMonitoring.getInstancesByCreator(creatorName).toString());
                }
                if(entities.size()!=0){
                    entityProviders.put(entityProviderSetEntry.getKey().getName(),entities);
                }
            }
        }
        result.put("Entities", entityProviders);

        if(contextManagerWebMonitoring != null){
            for(Map.Entry<RelationProvider, Set<String>> relationProviderSetEntry: contextManagerWebMonitoring.getResourceCreatorsByRelationProvider().entrySet()){
                ObjectNode relations = json.newObject();
                for(String creator : relationProviderSetEntry.getValue()){
                    relations.put(creator, contextManagerWebMonitoring.getInstancesByCreator(creator).toString());
                }
                if(relations.size()!=0){
                    relationProviders.put(relationProviderSetEntry.getKey().getName(),relations);
                }
            }
        }
        result.put("Relations", relationProviders);

        return ok(result);
    }

    @Route(method = HttpMethod.GET, uri = "/manager/abstractions")
    public Result getAbstractions() {
        ObjectNode result = json.newObject();
        ObjectNode entityProviders = json.newObject();
        ObjectNode relationProviders = json.newObject();

        if(contextManagerWebMonitoring != null){
            for(Map.Entry<EntityProvider, Set<String>> entityProviderSetEntry: contextManagerWebMonitoring.getAbstractionCreatorsByEntityProvider().entrySet()){
                ObjectNode entities = json.newObject();
                for(String creator : entityProviderSetEntry.getValue()){
                    entities.put(creator, contextManagerWebMonitoring.getInstancesByCreator(creator).toString());
                }
                if(entities.size()!=0){
                    entityProviders.put(entityProviderSetEntry.getKey().getName(),entities);
                }
            }
        }
        result.put("Entities:", entityProviders);

        if(contextManagerWebMonitoring != null){
            for(Map.Entry<RelationProvider, Set<String>> relationProviderSetEntry: contextManagerWebMonitoring.getAbstractionCreatorsByRelationProvider().entrySet()){
                ObjectNode relations = json.newObject();
                for(String creator : relationProviderSetEntry.getValue()){
                    relations.put(creator, contextManagerWebMonitoring.getInstancesByCreator(creator).toString());
                }
                if(relations.size()!=0){
                    relationProviders.put(relationProviderSetEntry.getKey().getName(),relations);
                }
            }
        }
        result.put("Relations:", relationProviders);

        return ok(result);
    }


    @Route(method = HttpMethod.GET, uri = "/manager/filters")
    public Result getEntityProviders() {
        ObjectNode result = json.newObject();

        if(contextManagerWebMonitoring != null){
            for(String filter: contextManagerWebMonitoring.getCurrentLookupFilter()){
                result.put(filter, true);
            }
        }

        return ok(result);
    }
}
