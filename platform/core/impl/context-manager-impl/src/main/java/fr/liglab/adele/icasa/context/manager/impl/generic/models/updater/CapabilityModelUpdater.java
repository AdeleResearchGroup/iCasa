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
package fr.liglab.adele.icasa.context.manager.impl.generic.models.updater;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.impl.generic.models.api.CapabilityModelUpdate;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class CapabilityModelUpdater {

    /*Capability model(providers and creators)*/
    @Requires
    @SuppressWarnings("unused")
    private CapabilityModelUpdate capabilityModelUpdate;

    /*Managed elements*/
    @Requires(id = "entityProviders", optional = true)
    @SuppressWarnings("all")
    private EntityProvider[] entityProviders;

    @Requires(optional = true)
    @SuppressWarnings("all")
    private RelationProvider[] relationProviders;


    @Bind(id = "entityProviders")
    @SuppressWarnings("unused")
    private void bindEntityProvider(EntityProvider entityProvider){
        capabilityModelUpdate.addEntityProvider(entityProvider);
    }

    @Modified(id = "entityProviders")
    @SuppressWarnings("unused")
    private void modifyEntityProvider(EntityProvider entityProvider){
        capabilityModelUpdate.modifyEntityProvider(entityProvider);
    }

    @Unbind(id = "entityProviders")
    @SuppressWarnings("unused")
    private void unbindEntityProvider(EntityProvider entityProvider){
        capabilityModelUpdate.removeEntityProvider(entityProvider);
    }

    @Bind(id = "relationProviders")
    @SuppressWarnings("unused")
    private void bindRelationProvider(RelationProvider relationProvider){
        capabilityModelUpdate.addRelationProvider(relationProvider);
    }

    @Modified(id = "relationProviders")
    @SuppressWarnings("unused")
    private void modifyRelationProvider(RelationProvider relationProvider){
        capabilityModelUpdate.modifyRelationProvider(relationProvider);
    }

    @Unbind(id = "relationProviders")
    @SuppressWarnings("unused")
    private void unbindRelationProvider(RelationProvider relationProvider){
        capabilityModelUpdate.removeRelationProvider(relationProvider);
    }
}
