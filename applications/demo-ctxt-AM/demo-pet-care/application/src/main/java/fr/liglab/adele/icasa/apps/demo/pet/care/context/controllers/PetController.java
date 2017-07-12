package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import fr.liglab.adele.icasa.apps.demo.pet.care.context.util.PetCareInfo;

import java.util.Set;

public interface PetController {

    Set<String> getPets();

    boolean addPet(String petName);

    boolean setPetInfo(String petName, PetCareInfo petCareInfo);

    void removePet(String petName);
}
