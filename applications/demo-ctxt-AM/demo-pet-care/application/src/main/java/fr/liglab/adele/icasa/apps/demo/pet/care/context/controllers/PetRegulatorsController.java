package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import java.util.Set;

public interface PetRegulatorsController {

    Set<String> getWateringRegulators();

    Set<String> getFeedingRegulators();
}
