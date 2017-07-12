package fr.liglab.adele.icasa.apps.demo.pet.care.context.services;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.FeedingRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.WateringRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.util.PetCareInfo;

public @ContextService interface PetInfo {
    @State String NAME = "pet.name";

    @State String VALIDITY = "pet.info.validity";

    @State String CARE_INFO = "pet.care.info";

    String getPetName();

    boolean getPetCareInfoValidity();

    PetCareInfo getPetCareInfo();

    boolean setPetCareInfo(PetCareInfo petCareInfo);

    WateringRegulator getWateringRegulator();

    FeedingRegulator getFeedingRegulator();
}
