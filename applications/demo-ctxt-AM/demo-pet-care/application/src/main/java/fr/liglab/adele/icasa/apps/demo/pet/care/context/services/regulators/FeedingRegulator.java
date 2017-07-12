package fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators;

import fr.liglab.adele.cream.annotations.ContextService;

public  @ContextService interface FeedingRegulator {

    void giveFood(int quantityInGrams);
}
