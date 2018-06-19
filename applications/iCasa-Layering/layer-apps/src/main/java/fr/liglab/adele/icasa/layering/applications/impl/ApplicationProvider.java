package fr.liglab.adele.icasa.layering.applications.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;

import fr.liglab.adele.icasa.layering.services.api.ZoneService;
import fr.liglab.adele.icasa.device.light.Photometer;

import fr.liglab.adele.icasa.layering.applications.lightning.HomeLightningAppImpl;
import fr.liglab.adele.icasa.layering.applications.smart.shutter.Controller;

@Component(immediate = true,publicFactory = false)
@Provides(specifications={PeriodicRunnable.class})

@Instantiate

public class ApplicationProvider implements PeriodicRunnable {

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    private final List<ZoneService.Provider> zoneServiceCreators;

    @Creator.Field Creator.Entity<HomeLightningAppImpl> lightningApplicationCreator;

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

    public ApplicationProvider() {
    	zoneServiceCreators = new ArrayList<>();
    	zoneServiceCreators.add(new SmartShutter());
	}

    @Validate
    public void start(){
    	lightningApplicationCreator.create("LightApp");
    }

    @Invalidate
    public void stop(){
    	lightningApplicationCreator.delete("LightApp");
    };



    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {
    	
    	for (ZoneService.Provider delegate : zoneServiceCreators) {

    		String instance = delegate.getEntityName(zone);
    		
    		Map<String,Object> properties = new HashMap<>();

    		delegate.create(instance,properties);
            attacher.create(instance,zone);
		}
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone) {
    	
    	for (ZoneService.Provider delegate : zoneServiceCreators) {

    		String instance = delegate.getEntityName(zone);
    		
    		delegate.delete(instance);
            attacher.delete(instance,zone);
		}
    }

 
	@Override
	public long getPeriod() {
		return 15;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.MINUTES;
	}

    private boolean hasRequestedLookup	= false;
    private boolean hasPhotometer 		= false;

    
    @Bind(id="photometer", optional=true, aggregate=true, proxy=false)
    public void addPhotometer(Photometer photometer) {
    	this.hasPhotometer = true;
    }
    
    @Unbind(id="photometer")
    public void unbind() {
    	this.hasPhotometer = false;
    }

    @Requires(optional = false)
    private IOPLookupService lookup;

	@Override
	public void run() {
		System.out.println("15 minutes se sont ecoulés");
		
		if (!hasRequestedLookup && !hasPhotometer ) {
			System.out.println("je vais faire un lookup vers IOP");
			lookup.consider(new String[] {Photometer.class.getCanonicalName()}, Collections.emptyMap());
			hasRequestedLookup = true;
		}
		
	}

	private static final boolean isRemote(ServiceReference<Photometer> reference) {
		String[] services = (String[]) reference.getProperty(Constants.OBJECTCLASS);
		for (String service : services) {
			if (service.contains(IOPService.class.getCanonicalName())) {
				return true;
			}
		}
		
		return false;
	}

    public static class Ranking implements Comparator<ServiceReference<Photometer>> {

		@Override
		public int compare(ServiceReference<Photometer> first, ServiceReference<Photometer> second) {
			
			boolean firstIsRemote 	= isRemote(first);
			boolean secondIsRemote 	= isRemote(second);
			
			return 	firstIsRemote && !secondIsRemote ? +1 :
		    		!firstIsRemote && secondIsRemote ? -1 :
		    		first.compareTo(second);
			
		}
    	
    }


}
