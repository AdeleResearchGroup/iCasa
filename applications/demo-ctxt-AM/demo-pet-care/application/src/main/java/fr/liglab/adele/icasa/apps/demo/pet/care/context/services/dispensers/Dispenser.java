package fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

public @ContextService interface Dispenser {

    @State String ID = "dispenser.id";

    @State String OPENED = "dispenser.opened";

    @State String TYPE = "dispenser.type";
    String FOOD_TYPE = "FOOD";
    String WATER_TYPE = "WATER";

    @State String FLOW_UNIT = "dispenser.flow.unit";
    String FOOD_UNIT = "g/s";
    String WATER_UNIT = "mL/s";

    @State String FLOW = "dispenser.flow";

    String getId();

    void open();

    void close();

    boolean isOpen();

    String getDispenserType();

    String getFlowUnit();

    int getFlow();
}
