package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.cream.model.Relation;
import fr.liglab.adele.icasa.apps.demo.global.Util;
import fr.liglab.adele.icasa.apps.demo.pet.care.app.AppManager;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.entities.Pet;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.entities.SimulatedFoodDispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.entities.SimulatedWaterDispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.PetInfo;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.FoodDispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.WaterDispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.FeedingRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.WateringRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.util.PetCareInfo;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class PetCareControllerImpl implements PetController, WaterDispenserController, FoodDispenserController, PetRelationsController {

    private final static Logger LOG = LoggerFactory.getLogger(PetCareControllerImpl.class);

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local) Creator.Entity<Pet> petCreator;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local, value = Pet.RELATION_FEED_BY)
    Creator.Relation<Pet,FeedingRegulator> feedByRelationCreator;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local, value = Pet.RELATION_WATERED_BY)
    Creator.Relation<Pet,WateringRegulator> wateredByRelationCreator;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local) Creator.Entity<SimulatedWaterDispenser> waterDispenserCreator;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.local) Creator.Entity<SimulatedFoodDispenser> foodDispenserCreator;

    @Requires(id = "pets", specification = Pet.class, optional = true)
    @SuppressWarnings("all")
    List<Pet> pets;

    @Requires(id = "feedingRegulators", specification = FeedingRegulator.class, optional = true)
    @SuppressWarnings("all")
    List<FeedingRegulator> feedingRegulators;

    @Requires(id = "wateringRegulators", specification = WateringRegulator.class, optional = true)
    @SuppressWarnings("all")
    List<WateringRegulator> wateringRegulators;


    @Override
    public Set<String> getPets() {
        return petCreator.getInstances();
    }

    @Override
    public boolean addPet(String petName) {

        if (Util.checkIfItExists(petCreator, petName))
            return false;

        Map<String, Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(PetInfo.class,PetInfo.NAME),petName);

        petCreator.create(petName, propertiesInit);
        LOG.info(AppManager.LOG_PREFIX + "Welcome to " + petName + "! :)");
        return true;
    }

    @Override
    public boolean setPetInfo(String petName, PetCareInfo petCareInfo) {

        if(!Util.checkIfItExists(petCreator, petName) || petCareInfo == null)
            return false;

        Pet pet = petCreator.getInstance(petName);
        if(pet == null)
            return false;

        LOG.info(AppManager.LOG_PREFIX + "Update info of " + petName);
        return pet.setPetCareInfo(petCareInfo);
    }

    @Override
    public void removePet(String petName) {
        if (Util.checkIfItExists(petCreator, petName)){
            petCreator.delete(petName);
        }

        LOG.info(AppManager.LOG_PREFIX + "Goodbye " + petName + " :(");
    }

    @Override
    public Set<String> getWaterDispensers() {
        return waterDispenserCreator.getInstances();
    }

    @Override
    public boolean addWaterDispenser(String id, int flow) {
        if (Util.checkIfItExists(waterDispenserCreator, id))
            return false;

        Map<String, Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(WaterDispenser.class,WaterDispenser.ID),id);
        propertiesInit.put(ContextEntity.State.id(WaterDispenser.class,WaterDispenser.FLOW),flow);

        waterDispenserCreator.create(id, propertiesInit);
        LOG.info(AppManager.LOG_PREFIX + "Water dispenser added: " + id);
        return true;
    }

    @Override
    public void removeWaterDispenser(String id) {
        if (Util.checkIfItExists(waterDispenserCreator, id))
            waterDispenserCreator.delete(id);
        LOG.info(AppManager.LOG_PREFIX + "Water dispenser removed: " + id);
    }

    @Override
    public Set<String> getFoodDispensers() {
        return foodDispenserCreator.getInstances();
    }

    @Override
    public boolean addFoodDispenser(String id, int flow) {
        if (Util.checkIfItExists(foodDispenserCreator, id))
            return false;

        Map<String, Object> propertiesInit = new HashMap<>();
        propertiesInit.put(ContextEntity.State.id(FoodDispenser.class,FoodDispenser.ID),id);
        propertiesInit.put(ContextEntity.State.id(FoodDispenser.class,FoodDispenser.FLOW),flow);

        foodDispenserCreator.create(id, propertiesInit);
        LOG.info(AppManager.LOG_PREFIX + "Food dispenser added: " + id);
        return true;
    }

    @Override
    public void removeFoodDispenser(String id) {
        if (Util.checkIfItExists(foodDispenserCreator, id))
            foodDispenserCreator.delete(id);
        LOG.info(AppManager.LOG_PREFIX + "Food dispenser removed: " + id);
    }

    @Override
    public Set<String> getPetWateringRegulatorRelations() {
        return wateredByRelationCreator.getInstances();
    }

    @Override
    public boolean attachWateringRegulatorToPet(String petName, String wateringRegulatorId) {
        if(!Util.checkIfItExists(petCreator, petName))
            return false;
        Pet pet = petCreator.getInstance(petName);
        if(pet == null)
            return false;

        wateredByRelationCreator.create(pet, wateringRegulatorId);
        LOG.info(AppManager.LOG_PREFIX + "Attach water dispenser to pet: " + petName + " - " + wateringRegulatorId);
        return true;
    }

    @Override
    public boolean detachWateringRegulatorFromPet(String petName) {
        if(!Util.checkIfItExists(petCreator, petName))
            return false;

        Pet pet = petCreator.getInstance(petName);
        if(pet == null)
            return false;
        WateringRegulator wateringRegulator = pet.getWateringRegulator();
        if(wateringRegulator == null)
            return false;

        wateredByRelationCreator.delete(pet, wateringRegulator);
        LOG.info(AppManager.LOG_PREFIX + "Detach water dispenser to pet: " + petName);
        return true;
    }

    @Override
    public Set<String> getPetFeedingRegulatorRelations() {
        return feedByRelationCreator.getInstances();
    }

    @Override
    public boolean attachFeedingRegulatorToPet(String petName, String feedingRegulatorId) {
        if(!Util.checkIfItExists(petCreator, petName))
            return false;
        Pet pet = petCreator.getInstance(petName);
        if(pet == null)
            return false;

        feedByRelationCreator.create(pet, feedingRegulatorId);
        LOG.info(AppManager.LOG_PREFIX + "Attach food dispenser to pet: " + petName + " - " + foodDispenserCreator);
        return true;
    }

    @Override
    public boolean detachFeedingRegulatorFromPet(String petName) {
        if(!Util.checkIfItExists(petCreator, petName))
            return false;

        Pet pet = petCreator.getInstance(petName);
        if(pet == null)
            return false;
        FeedingRegulator feedingRegulator = pet.getFeedingRegulator();
        if(feedingRegulator == null)
            return false;

        feedByRelationCreator.delete(pet, feedingRegulator);
        LOG.info(AppManager.LOG_PREFIX + "Detach food dispenser from pet: " + petName);
        return true;
    }

    @Unbind(id = "pets")
    public synchronized void unbindPet(Pet pet){
        feedByRelationCreator.delete(pet, pet.getFeedingRegulator());
        wateredByRelationCreator.delete(pet, pet.getWateringRegulator());
    }

    @Unbind(id = "feedingRegulators")
    public synchronized void unbindFeedingRegulator(FeedingRegulator feedingRegulator){
        List<Relation> relations = feedByRelationCreator.getInstancesRelatedTo(feedingRegulator.toString());

        for (Relation relation:relations){
            String source = relation.getSource();
            String end = relation.getTarget();
            feedByRelationCreator.delete(source,end);
        }
    }

    @Unbind(id = "wateringRegulators")
    public synchronized void unbindWateringRegulator(WateringRegulator wateringRegulator){
        List<Relation> relations = wateredByRelationCreator.getInstancesRelatedTo(wateringRegulator.toString());

        for (Relation relation:relations){
            String source = relation.getSource();
            String end = relation.getTarget();
            wateredByRelationCreator.delete(source,end);
        }
    }
}
