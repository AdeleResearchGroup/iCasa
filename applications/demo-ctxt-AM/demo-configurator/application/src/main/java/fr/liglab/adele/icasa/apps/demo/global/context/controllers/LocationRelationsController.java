package fr.liglab.adele.icasa.apps.demo.global.context.controllers;

import java.util.Set;

public interface LocationRelationsController {

    Set<String> getLocatedObjectRelations();

    boolean attachLocatedObjectToZone(String zoneId, String locatedObjectId);

    boolean detachLocatedObjectFromZone(String zoneId, String locatedObjectId);

    void removeLocationRelatedToLocatedObject(String locatedObjectId);
}
