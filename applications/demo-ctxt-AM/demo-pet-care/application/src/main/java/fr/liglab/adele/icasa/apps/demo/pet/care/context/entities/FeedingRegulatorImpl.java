package fr.liglab.adele.icasa.apps.demo.pet.care.context.entities;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.FeedingRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.dispensers.FoodDispenser;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Timer;
import java.util.TimerTask;

@ContextEntity(coreServices = FeedingRegulator.class)
public class FeedingRegulatorImpl implements FeedingRegulator {

    public final static String DISPENSER_RELATION = "controllingDispenser";
    @ContextEntity.Relation.Field(value = DISPENSER_RELATION, owner = FeedingRegulator.class)
    @Requires(id="dispenser",specification=FoodDispenser.class,optional=true)
    @SuppressWarnings("unused")
    private FoodDispenser foodDispenser;

    private Timer timer = new Timer();
    private final TimerTask timedClose = new TimerTask() {
        @Override
        public void run() {
            if(foodDispenser != null)
                foodDispenser.close();
        }
    };

    @Override
    public void giveFood(int quantityInGrams) {
        try{
            if(foodDispenser == null || quantityInGrams <= 0)
                return;

            int flow = foodDispenser.getFlow();
            if (flow <= 0)
                return;

            long delay = (long) (1000f * (float)quantityInGrams/(float)flow);

            try{
                timer.schedule(timedClose, delay);
                foodDispenser.open();
            } catch (IllegalStateException state){
                state.printStackTrace();
            }
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }
}
