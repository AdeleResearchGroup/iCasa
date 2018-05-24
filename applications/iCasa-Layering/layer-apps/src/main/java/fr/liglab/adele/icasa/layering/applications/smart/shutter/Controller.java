package fr.liglab.adele.icasa.layering.applications.smart.shutter;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.api.ZoneService;
import fr.liglab.adele.icasa.layering.services.impl.ZoneServiceFunctionalExtension;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.device.light.Photometer;

@ContextEntity(coreServices = ApplicationLayer.class)

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class Controller implements ApplicationLayer {

    @Requires(optional = true, filter = "(locatedobject.object.zone=${zoneservice.zone.attached})", proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    private Photometer photometer;
 
}
