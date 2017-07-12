package fr.liglab.adele.icasa.apps.demo.pet.care.context.controllers;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.entities.FeedingRegulatorImpl;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.entities.WateringRegulatorImpl;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.Dispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.FoodDispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.WaterDispenser;
import org.apache.felix.ipojo.annotations.*;

import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class PetRegulatorsControllerImpl implements PetRegulatorsController{

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.internal, requirements = WaterDispenser.class)
    Creator.Entity<WateringRegulatorImpl> wateringRegulatorFactory;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.internal, requirements = FoodDispenser.class)
    Creator.Entity<FeedingRegulatorImpl> feedingRegulatorFactory;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.internal, value = WateringRegulatorImpl.DISPENSER_RELATION, requirements = WaterDispenser.class)
    Creator.Relation<WateringRegulatorImpl, WaterDispenser> attachedWaterDispenser;

    @SuppressWarnings("unused")
    private @Creator.Field(origin = OriginEnum.internal, value = FeedingRegulatorImpl.DISPENSER_RELATION, requirements = FoodDispenser.class)
    Creator.Relation<FeedingRegulatorImpl, FoodDispenser> attachedFoodDispenser;

    @Bind(id = "waterDispenser",specification = WaterDispenser.class, aggregate = true, optional = true)
    @SuppressWarnings("unused")
    public void bindWaterDispenser(WaterDispenser waterDispenser){
        String name = generateEntityName(waterDispenser);
        wateringRegulatorFactory.create(name);
        attachedWaterDispenser.create(name, waterDispenser);
    }

    @Unbind(id = "waterDispenser")
    @SuppressWarnings("unused")
    public void unbindWaterDispenser(WaterDispenser waterDispenser){
        String name = generateEntityName(waterDispenser);
        wateringRegulatorFactory.delete(name);
        attachedWaterDispenser.delete(name,waterDispenser);
    }


    @Bind(id = "foodDispenser",specification = FoodDispenser.class, aggregate = true, optional = true)
    @SuppressWarnings("unused")
    public void bindFoodDispenser(FoodDispenser foodDispenser){
        String name = generateEntityName(foodDispenser);
        feedingRegulatorFactory.create(name);
        attachedFoodDispenser.create(name, foodDispenser);
    }

    @Unbind(id = "foodDispenser")
    @SuppressWarnings("unused")
    public void unbindFoodDispenser(FoodDispenser foodDispenser){
        String name = generateEntityName(foodDispenser);
        feedingRegulatorFactory.delete(name);
        attachedFoodDispenser.delete(name,foodDispenser);
    }

    private String generateEntityName(Dispenser dispenser){
        return dispenser.getId()+".controller";
    }

    @Override
    public Set<String> getWateringRegulators() {
        return wateringRegulatorFactory.getInstances();
    }

    @Override
    public Set<String> getFeedingRegulators() {
        return feedingRegulatorFactory.getInstances();
    }
}
