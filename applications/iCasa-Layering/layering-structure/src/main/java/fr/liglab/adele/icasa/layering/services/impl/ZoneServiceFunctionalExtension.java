package fr.liglab.adele.icasa.layering.services.impl;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import fr.liglab.adele.icasa.layering.services.api.ZoneService;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;

@FunctionalExtender(contextServices = ZoneService.class)
public class ZoneServiceFunctionalExtension implements ZoneService {

    @ContextEntity.State.Field(service = ZoneService.class,state = ZoneService.ZONE_ATTACHED, value = LocatedObject.LOCATION_UNKNOWN)
    private String zoneName;

    @ContextEntity.State.Push(service = ZoneService.class, state = ZoneService.ZONE_ATTACHED)
    public String updatedZone(Zone zone) {
        return zone != null ? zone.getZoneName() : LocatedObject.LOCATION_UNKNOWN; 
    }
    
	@Override
	public String getZone() {
		return zoneName;
	}
	
    @ContextEntity.Relation.Field(owner=ZoneService.class,value=ZoneService.RELATION_ATTACHED_TO)
	@Requires(id="relatedZone", optional=false, proxy = false)
    private Zone relatedZone;

	@Bind(id="relatedZone")
	protected void bindRelatedZone(Zone zone) {
		updatedZone(zone);
	}
	
	@Unbind(id="relatedZone")
	protected void unbindRelatedZone(Zone zone) {
		updatedZone(null);
	}

	@Modified(id="relatedZone")
	protected void modifieddRelatedZone(Zone zone) {
		updatedZone(zone);
	}

}
