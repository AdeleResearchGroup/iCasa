package fr.liglab.adele.icasa.apps.demo.pet.care.context.entities;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.Dispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.FoodDispenser;

@ContextEntity(coreServices = FoodDispenser.class)
public class SimulatedFoodDispenser implements FoodDispenser, Dispenser {

    @State.Field(service = Dispenser.class, state = Dispenser.ID)
    @SuppressWarnings("unused")
    private String id;

    @State.Field(service = Dispenser.class, state = Dispenser.OPENED, value="false", directAccess = true)
    @SuppressWarnings("unused")
    private boolean opened;

    @State.Field(service = Dispenser.class, state = Dispenser.TYPE, value= Dispenser.FOOD_TYPE)
    @SuppressWarnings("unused")
    private String dispenserType;

    @State.Field(service = Dispenser.class, state = Dispenser.FLOW_UNIT, value = FOOD_UNIT)
    @SuppressWarnings("unused")
    private String flowUnit;

    @State.Field(service = Dispenser.class, state = Dispenser.FLOW)
    @SuppressWarnings("unused")
    private int flow;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void open() {
        opened = true;
    }

    @Override
    public void close() {
        opened = false;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    @Override
    public String getDispenserType() {
        return dispenserType;
    }

    @Override
    public String getFlowUnit() {
        return flowUnit;
    }

    @Override
    public int getFlow() {
        return flow;
    }
}
