package fr.liglab.adele.icasa.layering.services.api;

import java.util.Map;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.location.Zone;

public @ContextService interface ZoneService {

    public final static @State String ZONE_ATTACHED = "zone.attached";
    
    public String getZone();
    
    public static final String RELATION_ATTACHED_TO ="attached.to";

    /**
     * A provider that is able instantiate/destroy a zone service instance
     *
     */
    public interface Provider {
    	
    	public String getEntityName(Zone zone);

		public void create(String instance, Map<String,Object> properties);

		public void delete(String instance);
		
    }

}
