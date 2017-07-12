package fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators;

import fr.liglab.adele.cream.annotations.ContextService;

public  @ContextService interface WateringRegulator {

    void giveWater(int quantityInMilliliters);
}
