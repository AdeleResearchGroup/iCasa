package fr.liglab.adele.icasa.context.manager.impl.generic.models;

import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.cream.model.introspection.RelationProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.models.CapabilityModelAccess;
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
