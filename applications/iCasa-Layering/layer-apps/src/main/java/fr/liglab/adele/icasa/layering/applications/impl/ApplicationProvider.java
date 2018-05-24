package fr.liglab.adele.icasa.layering.applications.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.*;

import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.layering.services.api.ZoneService;

import fr.liglab.adele.icasa.layering.applications.lightning.HomeLightningAppImpl;
import fr.liglab.adele.icasa.layering.applications.smart.shutter.Controller;

@Component(immediate = true,publicFactory = false)
@Instantiate
public class ApplicationProvider {


    private final List<ZoneService.Provider> delegates;

    @Creator.Field Creator.Entity<HomeLightningAppImpl> lightningApplicationCreator;

    public ApplicationProvider() {
    	delegates = new ArrayList<>();
    	delegates.add(new SmartShutter());
	}

    @Validate
    public void start(){
    	lightningApplicationCreator.create("LightApp");
    }

    @Invalidate
    public void stop(){
    	lightningApplicationCreator.delete("LightApp");
    };


    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {
    	
    	for (ZoneService.Provider delegate : delegates) {

    		String instance = delegate.getEntityName(zone);
    		
    		Map<String,Object> properties = new HashMap<>();

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

    private @Creator.Field Creator.Entity<Controller> smartShutterCreator;

    private class SmartShutter implements ZoneService.Provider {

		@Override
		public String getEntityName(Zone zone) {
		   	return "SmartShutter."+zone.getZoneName();
		}

		@Override
		public void create(String instance, Map<String, Object> properties) {
			smartShutterCreator.create(instance,properties);
		}

		@Override
		public void delete(String instance) {
			smartShutterCreator.delete(instance);
		}
    	
    }

}
