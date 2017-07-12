package fr.liglab.adele.icasa.apps.demo.pet.care.context.entities;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.WateringRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.WaterDispenser;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Timer;
import java.util.TimerTask;

@ContextEntity(coreServices = WateringRegulator.class)
public class WateringRegulatorImpl implements WateringRegulator {

    public final static String DISPENSER_RELATION = "controllingDispenser";
    @ContextEntity.Relation.Field(value = DISPENSER_RELATION, owner = WateringRegulator.class)
    @Requires(id="dispenser",specification=WaterDispenser.class,optional=true)
    @SuppressWarnings("unused")
    private WaterDispenser waterDispenser;

    private Timer timer = new Timer();
    private final TimerTask timedClose = new TimerTask() {
        @Override
        public void run() {
            if(waterDispenser != null)
                waterDispenser.close();
        }
    };

    @Override
    public void giveWater(int quantityInMilliliters) {
        try{
            if(waterDispenser == null || quantityInMilliliters <= 0)
                return;

            int flow = waterDispenser.getFlow();
            if (flow <= 0)
                return;

            long delay = (long) (1000f * (float)quantityInMilliliters/(float)flow);

            try{
                timer.schedule(timedClose, delay);
                waterDispenser.open();
            } catch (IllegalStateException state){
                state.printStackTrace();
            }
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }
}