package fr.liglab.adele.icasa.layering.applications.smart.shutter;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.BindingPolicy;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.api.ZoneService;
import fr.liglab.adele.icasa.layering.services.impl.ZoneServiceFunctionalExtension;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;
import tec.units.ri.unit.Units;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.Photometer;

@ContextEntity(coreServices = {ApplicationLayer.class,PeriodicRunnable.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class Controller implements ApplicationLayer, PeriodicRunnable {

	private static final String isRemoteService = "(objectClass=fr.liglab.adele.iop.device.api.IOPService)";

	@Requires(id="photometer",
    				optional = true, proxy = false, 
    				policy = BindingPolicy.DYNAMIC_PRIORITY, comparator = Controller.Ranking.class,
    				filter = "(| (locatedobject.object.zone=${zoneservice.zone.attached}) "+isRemoteService+")" )
    
    @ContextRequirement(spec = {LocatedObject.class})
    private Photometer photometer;
 
    @Requires(id="shutters" , optional = true, filter = "(locatedobject.object.zone=${zoneservice.zone.attached})", proxy = false, specification= WindowShutter.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<WindowShutter> shutters;

    private boolean hasRequestedLookup;
    private boolean hasPhotometer;
    
    @Requires(optional = false)
    private IOPLookupService lookup;
    
    public Controller() {
		this.hasPhotometer 		= false;
		this.hasRequestedLookup = false;
	}
    
    @Bind(id="photometer")
    public void addPhotometer() {
    	this.hasPhotometer = true;
    }
    
    @Unbind(id="photometer")
    public void unbind() {
    	this.hasPhotometer = false;
    }
    
    @Modified(id="photometer")
    public void modified() {
    	if (photometer != null) {
    		double shutterLevel = photometer.getIlluminance().to(Units.LUX).getValue().doubleValue() >= 1600 ? 0d : 1d;
    		
    		for (WindowShutter	shutter : shutters) {
				shutter.setShutterLevel(shutterLevel);
			}
    	}
    }

	@Override
	public void run() {
		if (!hasRequestedLookup && !hasPhotometer ) {
			System.out.println("je vais faire un lookup vers IOP");
			lookup.consider(new String[] {Photometer.class.getCanonicalName()}, Collections.emptyMap());
			hasRequestedLookup = true;
		}
		
		if (hasPhotometer && photometer instanceof IOPService) {
			System.out.println(photometer.getSerialNumber()+" "+photometer.getIlluminance()); 
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
