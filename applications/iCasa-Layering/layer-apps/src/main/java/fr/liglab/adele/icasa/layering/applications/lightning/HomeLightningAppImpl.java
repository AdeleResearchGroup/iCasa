package fr.liglab.adele.icasa.layering.applications.lightning;



import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.api.ZoneService;

import fr.liglab.adele.icasa.layering.services.lightning.LightningService;
import fr.liglab.adele.icasa.layering.services.lightning.LightningServiceImpl;

import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay.PartOfTheDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications= {HomeLightningAppImpl.class})

public class HomeLightningAppImpl implements ApplicationLayer {



    @Requires(id="lightningservices", specification = LightningService.class, optional=true)
    @ContextRequirement(spec = {ZoneService.class})
    
    private List<LightningService> lightningServiceLayer;

    public HomeLightningAppImpl() {
    	delegates = new ArrayList<>();
    	delegates.add(new Lightning());
	}

    @ServiceProperty(name = "osgi.command.scope", value = "home-lightning")
    String commandScope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {"schedule"};

    @Descriptor("Schedule lightning in a given zone and period")
    public void schedule(@Descriptor("zone") String zone, @Descriptor("period") String periodName) {
    	
    	System.out.println("Enabling schedule at : "+periodName+" for zone "+zone);
    	
    	PartOfTheDay period = periodName.equals("NONE") ? null : PartOfTheDay.valueOf(periodName);
    	
        for (LightningService service: lightningServiceLayer) {
        	if ("ALL".equalsIgnoreCase(zone) || ((ZoneService) service).getZone().equals(zone)) {
            	service.setSchedule(period);
        	}
        }
        
    }

    private final List<ZoneService.Provider> delegates ;

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {
    	
    	for (ZoneService.Provider delegate : delegates) {

    		String instance = delegate.getEntityName(zone);
    		
    		Map<String,Object> properties = new HashMap<>();
        	properties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME), instance);

        	delegate.create(instance,properties);
            attacher.create(instance,zone);
		}
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone) {
    	
    	for (ZoneService.Provider delegate : delegates) {

    		String instance = delegate.getEntityName(zone);
    		
    		delegate.delete(instance);
            attacher.delete(instance,zone);
		}
    }

    private @Creator.Field Creator.Entity<LightningServiceImpl>	lightningCreator;

	private class Lightning implements ZoneService.Provider {

		public String getEntityName(Zone zone) {
    		return zone.getZoneName()+".lightning";
    	}

		@Override
		public void create(String instance, Map<String, Object> properties) {
			lightningCreator.create(instance,properties);			
		}

		@Override
		public void delete(String instance) {
			lightningCreator.delete(instance);
		}

	}
 

}
