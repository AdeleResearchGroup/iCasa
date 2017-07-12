package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import java.util.Set;

public interface WaterDispenserController {

    Set<String> getWaterDispensers();

    boolean addWaterDispenser(String id, int flow);

    void removeWaterDispenser(String id);
}
