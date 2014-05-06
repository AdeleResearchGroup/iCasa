package fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.manager;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.moment.of.the.day.MomentOfTheDay;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.moment.of.the.day.MomentOfTheDayListener;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.moment.of.the.day.MomentOfTheDayService;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.room.occupancy.RoomOccupancy;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.room.occupancy.RoomOccupancyListener;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.controller.TemperatureConfiguration;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
@Component(name="temperatureManager")
@Instantiate(name="temperatureManagerImpl-0")
@Provides(specifications = TemperatureManagerAdministration.class)
public class TemperatureManagerAdministrationImpl implements TemperatureManagerAdministration,RoomOccupancyListener,MomentOfTheDayListener {

    @Requires
    private Clock clock;

    @Requires
    private TemperatureConfiguration m_configuration;

    @Requires
    private RoomOccupancy m_roomOccupancy;

    @Requires
    private MomentOfTheDayService m_momentOfTheDay;


    private static final Duration duration = Duration.millis(5*60*1000); //Five minutes

    private Map<String, Map<String,Float>> mapTemperatureTarget  = new HashMap<String,  Map<String,Float>>() ;

    private Map<String,Float> mapOfPreferenceUse  = new HashMap<String,Float>() ;

    private Map<String,Long> mapOfUpdate  = new HashMap<String,Long>() ;

    private List<String> listOfUser ;

    private List<String> listOfZone ;

    private  boolean energySavingMode = false;

    private double occupancyThreshold = 0.2 ;

    private float momentOfTheDayFactor ;

    private EnergyGoal energyGoal = EnergyGoal.HIGH ;

    private MomentOfTheDay momentOfTheDay ;

    public TemperatureManagerAdministrationImpl() {

        m_configuration.setMaximumAllowedEnergyInRoom(energyGoal.getMaximumEnergyInRoom());

         /*
        *Init
        */
        listOfZone = new ArrayList<String>();
        listOfZone.add("kitchen");
        listOfZone.add("bedroom");
        listOfZone.add("livingroom");
        listOfZone.add("bathroom");

        listOfUser = new ArrayList<String>();
        listOfUser.add("Aurelie");
        listOfUser.add("Paul");
        listOfUser.add("Pierre");
        listOfUser.add("Lea");

        for(String user : listOfUser){
            Map<String,Float> temp = new HashMap<String, Float>();
            for(String location : listOfZone){
                if (location.equals("kitchen")){
                    temp.put(location,288.15f);
                }else if (location.equals("livingroom")){
                    temp.put(location,291.15f);
                }else if(location.equals("bedroom")){
                    temp.put(location,293.15f);
                }else if (location.equals("bathroom")){
                    temp.put(location,296.15f);
                }
            }
            mapTemperatureTarget.put(user,temp);
        }
        for(String zone : listOfZone){
            mapOfUpdate.put(zone,(long)0);
        }

        for (String stringLocation : mapTemperatureTarget.get("Aurelie").keySet()){
            m_configuration.setTargetedTemperature(stringLocation,mapTemperatureTarget.get("Aurelie").get(stringLocation));
            m_configuration.turnOn(stringLocation);
        }
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
        m_roomOccupancy.removeListener(this);
        m_momentOfTheDay.unregister(this);
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");

        m_roomOccupancy.setThreshold(this.occupancyThreshold);
        m_roomOccupancy.addListener(this);
        m_momentOfTheDay.register(this);
        momentOfTheDay = m_momentOfTheDay.getMomentOfTheDay();
        if(momentOfTheDay == MomentOfTheDay.AFTERNOON ){
            momentOfTheDayFactor = (float)-1;
        }else if (momentOfTheDay == MomentOfTheDay.MORNING){
            momentOfTheDayFactor = (float)-0.5;
        }else if (momentOfTheDay == MomentOfTheDay.NIGHT){
            momentOfTheDayFactor = (float) 1;
        }else if (momentOfTheDay == MomentOfTheDay.EVENING){
            momentOfTheDayFactor = (float)0.5;
        }
    }

    @Override
    public synchronized void temperatureIsTooHigh(String roomName,String user) {
        if (listOfZone.contains(roomName)){
            if(listOfUser.contains(user)){
                long time = clock.currentTimeMillis();
                long lastUpdate = mapOfUpdate.get(roomName);

                if (time >= mapOfUpdate.get(roomName)){
                    long periodOfUpdate =  (time - lastUpdate);
                    Duration durationTemp = Duration.millis(periodOfUpdate);

                    if (durationTemp.isLongerThan(duration)){

                        float currentUserTemp = mapTemperatureTarget.get(user).get(roomName);

                        m_configuration.turnOn(roomName);
                        if(mapOfPreferenceUse.get(roomName) < currentUserTemp){
                            mapOfPreferenceUse.put(roomName,currentUserTemp);
                            setTargetedTemperature(roomName);
                        }else {
                            float temperature = mapOfPreferenceUse.get(roomName);
                            mapOfPreferenceUse.put(roomName,temperature+1);
                            mapTemperatureTarget.get(user).put(roomName,temperature+1);
                            setTargetedTemperature(roomName);
                        }
                    }else {
                        System.out.println(" Waiting period");
                    }
                }
            }else{
                System.out.println(" INVALID USER !");
            }
        }else {
            System.out.println(" INVALID ZONE !");
        }
    }


    @Override
    public synchronized void temperatureIsTooLow(String roomName,String user) {
        if (listOfZone.contains(roomName)){
            if(listOfUser.contains(user)){
                long time = clock.currentTimeMillis();
                long lastUpdate = mapOfUpdate.get(roomName);

                if (time >= mapOfUpdate.get(roomName)){
                    long periodOfUpdate =  (time - lastUpdate);
                    Duration durationTemp = Duration.millis(periodOfUpdate);

                    if (durationTemp.isLongerThan(duration)){

                        float currentUserTemp = mapTemperatureTarget.get(user).get(roomName);

                        m_configuration.turnOn(roomName);
                        if(mapOfPreferenceUse.get(roomName) > currentUserTemp){
                            mapOfPreferenceUse.put(roomName,currentUserTemp);
                            setTargetedTemperature(roomName);
                        }else {
                            float temperature = mapOfPreferenceUse.get(roomName);
                            mapOfPreferenceUse.put(roomName,temperature-1);
                            mapTemperatureTarget.get(user).put(roomName,temperature-1);
                            setTargetedTemperature(roomName);
                        }
                    }else {
                        System.out.println(" Waiting period");
                    }
                }
            }else{
                System.out.println(" INVALID USER !");
            }
        }else {
            System.out.println(" INVALID ZONE !");
        }
    }

    @Override
    public synchronized void turnOnEnergySavingMode() {
        for(String zoneId : listOfZone){
            long time = clock.currentTimeMillis();
            DateTime date = new DateTime(time);
            for(String user : listOfUser){
                if (m_roomOccupancy.getRoomOccupancy(zoneId,date.getMinuteOfDay(),user) > occupancyThreshold ){
                    m_configuration.turnOn(zoneId);
                }
            }
        }
        energySavingMode = true;
    }

    @Override
    public synchronized void turnOffEnergySavingMode() {
        for(String zoneId : listOfZone){
            m_configuration.turnOff(zoneId);
        }
        energySavingMode = false;
    }

    @Override
    public synchronized boolean isPowerSavingEnabled() {
        return energySavingMode;
    }

    @Override
    public synchronized void setTemperatureEnergyGoal(EnergyGoal goal) {
        m_configuration.setMaximumAllowedEnergyInRoom(goal.getMaximumEnergyInRoom());
    }

    @Override
    public synchronized EnergyGoal getTemperatureEnergyGoal() {
        return energyGoal;
    }

    @Override
    public synchronized double getRoomOccupancy(String room, int minute,String user) {
        return m_roomOccupancy.getRoomOccupancy(room,minute,user);
    }

    @Override
    public synchronized void occupancyCrossDownThreshold(String room,String user) {
        if (energySavingMode){
            m_configuration.turnOff(room);
        }
    }

    @Override
    public synchronized void occupancyCrossUpThreshold(String room,String user) {
        if (energySavingMode){
            long time = clock.currentTimeMillis();
            long lastUpdate = mapOfUpdate.get(room);
            if (time >= mapOfUpdate.get(room)){
                long periodOfUpdate =  (time - lastUpdate);
                Duration durationTemp = Duration.millis(periodOfUpdate);
                if (durationTemp.isLongerThan(duration)){
                    mapOfPreferenceUse.put(room,mapTemperatureTarget.get(user).get(room));
                    setTargetedTemperature(room);
                }
            }
            m_configuration.turnOn(room);
        }
    }

    @Override
    public synchronized void momentOfTheDayHasChanged(MomentOfTheDay newMomentOfTheDay) {
        if(newMomentOfTheDay == MomentOfTheDay.AFTERNOON ){
            momentOfTheDayFactor = (float)-1;
        }else if (newMomentOfTheDay == MomentOfTheDay.MORNING){
            momentOfTheDayFactor = (float)-0.5;
        }else if (newMomentOfTheDay == MomentOfTheDay.NIGHT){
            momentOfTheDayFactor = (float) 1;
        }else if (newMomentOfTheDay == MomentOfTheDay.EVENING){
            momentOfTheDayFactor = (float)0.5;
        }
        momentOfTheDay = newMomentOfTheDay;
        setTargetedTemperature();
    }

    private  void setTargetedTemperature(String roomName) {
        long time = clock.currentTimeMillis();
        mapOfUpdate.put(roomName,time);
        m_configuration.setTargetedTemperature(roomName,mapOfPreferenceUse.get(roomName) + momentOfTheDayFactor);
    }

    private void setTargetedTemperature() {
        for(String zone : listOfZone){
            long time = clock.currentTimeMillis();
            mapOfUpdate.put(zone,time);
            m_configuration.setTargetedTemperature(zone,mapOfPreferenceUse.get(zone) + momentOfTheDayFactor);
        }
    }

}
