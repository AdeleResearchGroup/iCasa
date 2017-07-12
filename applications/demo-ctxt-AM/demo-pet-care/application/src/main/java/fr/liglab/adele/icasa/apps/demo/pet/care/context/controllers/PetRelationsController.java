package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import java.util.Set;

public interface PetRelationsController {

    Set<String> getPetWateringRegulatorRelations();

    boolean attachWateringRegulatorToPet(String petName, String wateringRegulatorId);

    boolean detachWateringRegulatorFromPet(String petName);


    Set<String> getPetFeedingRegulatorRelations();

    boolean attachFeedingRegulatorToPet(String petName, String feedingRegulatorId);

    boolean detachFeedingRegulatorFromPet(String petName);
}
