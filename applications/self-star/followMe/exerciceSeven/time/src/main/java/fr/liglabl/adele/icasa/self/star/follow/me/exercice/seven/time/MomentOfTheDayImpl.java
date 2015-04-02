package fr.liglabl.adele.icasa.self.star.follow.me.exercice.seven.time;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "follow.me.time")
@Provides(specifications = {PeriodicRunnable.class, MomentOfTheDayService.class} )
public class MomentOfTheDayImpl implements MomentOfTheDayService, PeriodicRunnable {

    /**
     * The current moment of the day :
     **/
    MomentOfTheDay currentMomentOfTheDay = MomentOfTheDay.MORNING;

    /**
     * The current moment of the day :
     **/
    Set<MomentOfTheDayListener> listeners = new HashSet<MomentOfTheDayListener>();

    @Requires
    Clock clock;

    // Implementation of the MomentOfTheDayService ....
    @Override
    public MomentOfTheDay getMomentOfTheDay(){
        return currentMomentOfTheDay;
    }

    @Override
    public void register(MomentOfTheDayListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(MomentOfTheDayListener listener) {
        listeners.remove(listener);
    }

    // Implementation ot the PeriodicRunnable ...

    public long getPeriod(){
        // The service will be periodically called every hour.
        return 3600 * 1000 ;
    }

    public String getGroup(){
        return "default"; // you don't need to understand this part.
    }


    @Override
    public void run() {
        // The method run is called on a regular basis
        System.out.println(" EXEC ");
        // TODO : do something to check the current time of the day and see if
        // it has changed
        DateTime dateTimeEli =new DateTime(clock.currentTimeMillis());
        int hour = dateTimeEli.getHourOfDay();
        MomentOfTheDay temp = currentMomentOfTheDay;
        currentMomentOfTheDay = temp.getCorrespondingMoment(hour);
        if (currentMomentOfTheDay != temp ){
            for(MomentOfTheDayListener listener : listeners){
                listener.momentOfTheDayHasChanged(currentMomentOfTheDay);
            }
        }
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component TIME is stopping...");
    }

    /** Component Lifecycle Method */

    @Validate
    public void start() {
        System.out.println("Component TIME is starting...");
    }



}
