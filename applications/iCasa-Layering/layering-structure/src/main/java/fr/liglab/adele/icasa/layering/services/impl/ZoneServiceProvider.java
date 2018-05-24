package fr.liglab.adele.icasa.layering.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.api.ZoneService;

import fr.liglab.adele.icasa.layering.services.lightning.LightningServiceImpl;
import fr.liglab.adele.icasa.location.Zone;


@Component(immediate=true, publicFactory = false)
@Instantiate

public class ZoneServiceProvider {

    private final List<ZoneService.Provider> delegates ;
    
    public ZoneServiceProvider() {
    	delegates = new ArrayList<>();
    	delegates.add(new Lightning());
	}

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
