package fr.liglab.adele.icasa.layering.services.location;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

public @ContextService interface ZoneService {

    public final static @State String ZONE_ATTACHED = "zone.attached";
    
    public String getZone();
    
    public static final String RELATION_ATTACHED_TO ="attached.to";

	public static final String OBJECTS_IN_ZONE = "(locatedobject.object.zone=${zoneservice.zone.attached})";
	
}
