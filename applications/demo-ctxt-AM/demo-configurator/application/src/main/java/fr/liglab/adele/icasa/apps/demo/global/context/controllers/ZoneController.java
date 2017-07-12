package fr.liglab.adele.icasa.apps.demo.global.context.controllers;

import java.util.Set;

public interface ZoneController {

    Set<String> getZones();

    boolean addZone(String id);

    void removeZone(String id);
}
