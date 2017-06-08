package fr.liglab.adele.icasa.freshness.scenario.application1.momentday;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    @Override
    public long getPeriod() {
        return 1;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.HOURS;
    }



    @Override
    public void run() {
        // The method run is called on a regular basis

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
