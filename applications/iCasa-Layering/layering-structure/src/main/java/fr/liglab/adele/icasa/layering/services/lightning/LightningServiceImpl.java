package fr.liglab.adele.icasa.layering.services.lightning;

import java.util.List;
import java.util.function.Supplier;

import org.apache.felix.ipojo.annotations.*;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.api.ZoneService;
import fr.liglab.adele.icasa.layering.services.impl.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.icasa.device.light.BinaryLight;



@ContextEntity(coreServices = {LightningService.class, ServiceLayer.class,})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class LightningServiceImpl implements LightningService, ServiceLayer {
	
	
    private static final Integer MIN_QOS = 34;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @Override
    public String getServiceName() {
        return name;
    }

    //requirements
    @Requires(optional = true, filter = "(locatedobject.object.zone=${zoneservice.zone.attached})",proxy = false, specification = BinaryLight.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<BinaryLight> binaryLights;

    @Requires(id = "MoD", optional=false)
    MomentOfTheDay momentOfTheDay;

    @Modified(id = "MoD")
    protected void momentOfDayUpdated() {
    	
    	System.out.println("Lightning service trigered "+momentOfTheDay.getCurrentPartOfTheDay());
    	
    	if (momentOfTheDay.getCurrentPartOfTheDay() == MomentOfTheDay.PartOfTheDay.EVENING) {
    		for (BinaryLight binaryLight : binaryLights) {
        		binaryLight.turnOn();
    		}
    	}
    	else {
    		for (BinaryLight binaryLight : binaryLights) {
        		binaryLight.turnOff();
    		}
    	}
    }

   
    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    @Override
    public int getServiceQoS() {
        return AppQoS;
    }

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQoS = ()-> {

    	int currentQoS = 0;
    			
        if(binaryLights.size()>0) {
        	currentQoS+=35;
        }

    	return currentQoS;
    };
    
    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

}
