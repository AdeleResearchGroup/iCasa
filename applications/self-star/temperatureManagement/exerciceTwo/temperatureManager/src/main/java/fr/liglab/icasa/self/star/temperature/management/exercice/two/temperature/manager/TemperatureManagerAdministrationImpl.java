package fr.liglab.icasa.self.star.temperature.management.exercice.two.temperature.manager;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.icasa.self.star.temperature.management.exercice.two.temperature.controller.TemperatureConfiguration;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
@Component(name="temperatureManager")
@Instantiate(name="temperatureManagerImpl-0")
@Provides(specifications = TemperatureManagerAdministration.class)
public class TemperatureManagerAdministrationImpl implements TemperatureManagerAdministration {

    @Requires
    private Clock clock;

    @Requires
    private TemperatureConfiguration m_configuration;

    private Duration duration = Duration.millis(5*60*1000); //Five minutes

    /**
     * The name of the location for unknown value
     */
    private Map<String,Float> mapTemperatureTarget  ;


    private long lastUpdateHight = 0 ;

    private long lastUpdatelow =0 ;

    @Override
    public synchronized void temperatureIsTooHigh(String roomName) {
        if (mapTemperatureTarget.containsKey(roomName)){
            long time = clock.currentTimeMillis();
            if (time >= lastUpdateHight){
                long periodOfUpdate =  (time - lastUpdateHight);
                Duration durationTemp = Duration.millis(periodOfUpdate);

                System.out.println(" DUR TEMP " + durationTemp.getStandardSeconds());

                System.out.println(" DUR TEMP " + duration.getStandardSeconds());
                if (durationTemp.isLongerThan(duration)){
                    float currentTemp = mapTemperatureTarget.get(roomName);
                    mapTemperatureTarget.put(roomName,(currentTemp + 1));
                    lastUpdateHight = time;
                    m_configuration.setTargetedTemperature(roomName,mapTemperatureTarget.get(roomName));
                    System.out.println(" Preferences are now set to " + (currentTemp + 1) + " for " + roomName);
                }else {
                    System.out.println(" DUR TEMP " + durationTemp.getStandardSeconds());

                    System.out.println(" DUR TEMP " + duration.getStandardSeconds());


                    System.out.println(" Waiting period");
                }
            }else{
                lastUpdateHight = time;
            }
        }else {
            System.out.println(" INVALID ZONE !");
        }
    }

    @Override
    public synchronized void temperatureIsTooLow(String roomName) {
        if (mapTemperatureTarget.containsKey(roomName)){
            long time = clock.currentTimeMillis();
            if (time >= lastUpdatelow){

                long periodOfUpdate =  (time - lastUpdatelow);
                Duration durationTemp = Duration.millis(periodOfUpdate);
                System.out.println(" DUR TEMP " + durationTemp.getStandardSeconds());

                System.out.println(" DUR TEMP " + duration.getStandardSeconds());
                if (durationTemp.isLongerThan(duration)){
                    float currentTemp = mapTemperatureTarget.get(roomName);
                    mapTemperatureTarget.put(roomName,(currentTemp-1));
                    lastUpdateHight = time;
                    m_configuration.setTargetedTemperature(roomName,mapTemperatureTarget.get(roomName));
                    System.out.println(" Preferences are now set to " + (currentTemp-1) + " for " + roomName);
                }else{
                    System.out.println(" Waiting period");
                }
            }else{
                lastUpdatelow = time;
            }
        }else {
            System.out.println(" INVALID ZONE !");
        }
    }

    public TemperatureManagerAdministrationImpl() {



    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");


    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");
        mapTemperatureTarget = new HashMap<String, Float>();

        mapTemperatureTarget.put("kitchen",288.15f);
        mapTemperatureTarget.put("livingroom",291.15f);
        mapTemperatureTarget.put("bedroom",293.15f);
        mapTemperatureTarget.put("bathroom",296.15f);
        for (String stringLocation : mapTemperatureTarget.keySet()){
            m_configuration.setTargetedTemperature(stringLocation,mapTemperatureTarget.get(stringLocation));
        }
    }



}
