package fr.liglab.adele.icasa.apps.demo.pet.care.context.entities;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.Dispenser;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.WaterDispenser;

@ContextEntity(coreServices = WaterDispenser.class)
public class SimulatedWaterDispenser implements WaterDispenser, Dispenser {

    @ContextEntity.State.Field(service = Dispenser.class, state = Dispenser.ID)
    @SuppressWarnings("unused")
    private String id;

    @ContextEntity.State.Field(service = Dispenser.class, state = Dispenser.OPENED, value="false", directAccess = true)
    @SuppressWarnings("unused")
    private boolean opened;

    @ContextEntity.State.Field(service = Dispenser.class, state = Dispenser.TYPE, value= Dispenser.WATER_TYPE)
    @SuppressWarnings("unused")
    private String dispenserType;

    @ContextEntity.State.Field(service = Dispenser.class, state = Dispenser.FLOW_UNIT, value = WATER_UNIT)
    @SuppressWarnings("unused")
    private String flowUnit;

    @ContextEntity.State.Field(service = Dispenser.class, state = Dispenser.FLOW)
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
