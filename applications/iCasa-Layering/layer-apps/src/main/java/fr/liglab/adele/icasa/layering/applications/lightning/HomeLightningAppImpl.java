package fr.liglab.adele.icasa.layering.applications.lightning;



import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.lightning.LightningService;


import java.util.List;



@ContextEntity(coreServices = {ApplicationLayer.class})
public class HomeLightningAppImpl implements ApplicationLayer {

	
    @Requires(id="lightningservices", specification = LightningService.class, optional=false)
    private List<LightningService> lightningServiceLayer;

}
