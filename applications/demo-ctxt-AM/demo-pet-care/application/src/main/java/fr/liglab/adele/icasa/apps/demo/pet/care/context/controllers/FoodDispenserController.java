package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import java.util.Set;

public interface FoodDispenserController {

    Set<String> getFoodDispensers();

    boolean addFoodDispenser(String id, int flow);

    void removeFoodDispenser(String id);
}
