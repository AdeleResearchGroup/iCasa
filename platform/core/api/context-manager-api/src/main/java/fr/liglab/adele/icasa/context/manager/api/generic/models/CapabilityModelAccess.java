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
package fr.liglab.adele.icasa.context.manager.api.generic.models;

import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface CapabilityModelAccess {

    /*TODO MODIFY*/
    Set<EntityProvider> getEntityProviders();

    Set<String> getInstancesByCreator(String creator);

    Map<String, Set<String>> getEntityCreatorsByService();

    Map<String, Set<String>> getEntityCreatorsRequirements();

    Map<String, EntityProvider> getEntityProviderByCreatorName();

    Map<EntityProvider, Set<String>> getEntitiesByProvider();

    Map<EntityProvider, Set<String>> getEntitiesByProvider(OriginEnum originEnum);

    Set<RelationProvider> getRelationProviders();

    Map<String, Set<String>> getRelationCreatorsByService();

    Map<String, Set<String>> getRelationCreatorsRequirements();

    Map<String, RelationProvider> getRelationProviderByCreatorName();

    Map<RelationProvider, Set<String>> getRelationsByProvider();

    Map<RelationProvider, Set<String>> getRelationsByProvider(OriginEnum originEnum);
}
