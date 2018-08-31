package fr.liglab.adele.icasa.layering.services.root;

import java.util.List;

import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.RootService;
import fr.liglab.adele.icasa.location.Zone;

@ContextEntity(coreServices = RootService.class)
public class RootServiceImpl implements RootService {

    @ContextEntity.State.Field(service = RootService.class,state = RootService.CURRENT_STATE, value="init")
    public String status;


    @Requires(optional=true, proxy=false, specification=Zone.class)
    private List<Zone> zones;
}
