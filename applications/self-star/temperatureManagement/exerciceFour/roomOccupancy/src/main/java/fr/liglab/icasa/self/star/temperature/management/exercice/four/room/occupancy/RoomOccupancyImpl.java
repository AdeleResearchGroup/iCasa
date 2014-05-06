package fr.liglab.icasa.self.star.temperature.management.exercice.four.room.occupancy;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.clock.ClockListener;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.service.location.PersonLocationService;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;

import java.util.*;

/**
 *
 */
@Component(name="RoomOccupancyImpl")
@Instantiate(name="RoomOccupancyImpl-0")
@Provides(specifications = {RoomOccupancy.class,PeriodicRunnable.class})
@CommandProvider(namespace = "roomOccupancy")
public class RoomOccupancyImpl implements RoomOccupancy,PeriodicRunnable,ClockListener {

    private List<String> listOfZone ;

    private List<String> listOfUser ;

    Map<String,Map<String,Map<Integer,Double>>> mapProbaPerRoomPerUser =new HashMap<String, Map<String,Map<Integer,Double>>>();

    Set<RoomOccupancyListener> listenerList = new HashSet<RoomOccupancyListener>();

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the LOCATION property
     */
    public static final int NUMBER_OF_MINUTE = 1439;

    private  double threshold = 0.2;

    @Requires
    private Clock clock;

    @Requires
    private PersonLocationService m_personLocationService;

    private DateTime startDate;

    private int yearOfStart;

    private int dayOfStart;

    private int lastUpdate = -1 ;

    private final Object m_lock ;

    private final Object m_clockLock ;


    RoomOccupancyImpl(){
        m_lock = new Object();
        m_clockLock = new Object();

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
            Map<String,Map<Integer,Double>> tempLocationMap = new HashMap<String, Map<Integer,Double>>();
            for(String location : listOfZone){
                Map<Integer,Double> tempMap = new HashMap<Integer, Double>();
                for(int i = 0 ; i <= NUMBER_OF_MINUTE ; i ++){
                    tempMap.put(i,0.0d);
                }
                tempLocationMap.put(location,tempMap);
            }
            mapProbaPerRoomPerUser.put(user,tempLocationMap);
        }
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
        clock.removeListener(this);
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");
        clock.addListener(this);
        synchronized (m_clockLock){
            startDate = new DateTime(clock.currentTimeMillis());
            yearOfStart = startDate.getYear();
            dayOfStart = startDate.getDayOfYear();
        }
    }

    @Override
    public void addListener(RoomOccupancyListener roomOccupancyListener) {
        listenerList.add(roomOccupancyListener);
    }

    @Override
    public void removeListener(RoomOccupancyListener roomOccupancyListener) {
        listenerList.remove(roomOccupancyListener);
    }

    @Override
    public synchronized void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public synchronized double getRoomOccupancy( String room,int minuteOfTheDay,String user) {
        return mapProbaPerRoomPerUser.get(user).get(room).get(minuteOfTheDay);
    }

    @Override
    public long getPeriod() {
        return 40000;
    }

    @Override
    public String getGroup() {
        return "Room-Occupancy-Thread";
    }

    @Override
    public void run() {
        long updateTime = clock.currentTimeMillis();
        DateTime date = new DateTime(updateTime);
        int minuteOfUpdate = date.getMinuteOfDay();
        synchronized (m_clockLock){
            if(lastUpdate != minuteOfUpdate){
                int dayOfUpdate = date.getDayOfYear();

                int yearOfUpdate = date.getYear();

                synchronized (m_lock){
                    double ponderation = 1 + ((dayOfUpdate - dayOfStart) + 360*(yearOfUpdate-yearOfStart)) ;
                    for(String location : listOfZone){

                        Set<String> userInRoom = m_personLocationService.getPersonInZone(location);

                        for(String user : userInRoom){
                            Map<Integer,Double> tempMap = mapProbaPerRoomPerUser.get(user).get(location);
                            if ( ponderation != 0){
                                double newProba = tempMap.get(minuteOfUpdate)*((ponderation-1)/ponderation) + (1/ponderation);
                                tempMap.put(minuteOfUpdate,newProba);
                            }

                        }

                        for(String user : listOfUser){
                            boolean userPresent = userInRoom.contains(user);
                            if(!userPresent){
                                Map<Integer,Double> tempMap = mapProbaPerRoomPerUser.get(user).get(location);
                                if ( ponderation != 0){
                                    // COMPUTE NEW PROBA
                                    double newProba = tempMap.get(minuteOfUpdate)*((ponderation-1)/ponderation);
                                    tempMap.put(minuteOfUpdate,newProba);

                                }
                                userInRoom.remove(user);
                            }
                        }

                        boolean precedentProbaAboveThreshold = false;
                        double maxPrecedentProba = threshold;
                        String precedentMaxUser = new String();
                        if(!(lastUpdate <0)){
                            for (String user : listOfUser){
                                if(mapProbaPerRoomPerUser.get(user).get(location).get(lastUpdate)>maxPrecedentProba){
                                    precedentProbaAboveThreshold = true;
                                    maxPrecedentProba = mapProbaPerRoomPerUser.get(user).get(location).get(lastUpdate);
                                    precedentMaxUser = new String(user);
                                }
                            }
                        }

                        boolean probaAboveThreshold = false;
                        double maxProba = threshold;
                        String maxUser = new String();
                        for (String user : listOfUser){
                            if(mapProbaPerRoomPerUser.get(user).get(location).get(minuteOfUpdate)>maxProba){
                                probaAboveThreshold = true;
                                maxProba = mapProbaPerRoomPerUser.get(user).get(location).get(minuteOfUpdate);
                                maxUser = new String(user);
                            }
                        }

                        if((!precedentProbaAboveThreshold) && (probaAboveThreshold) ){
                            // Cross up the threshold
                            for(RoomOccupancyListener listener : listenerList){
                                listener.occupancyCrossUpThreshold(location,maxUser);
                            }
                        }else if((precedentProbaAboveThreshold) && (!probaAboveThreshold) ){
                            // cross down
                            for(RoomOccupancyListener listener : listenerList){
                                listener.occupancyCrossDownThreshold(location, maxUser);
                            }
                        }else if((precedentProbaAboveThreshold) && (probaAboveThreshold) ){
                            // Already cross
                            if (maxPrecedentProba > maxProba ){
                                //DO NOTHING
                            }else{
                                for(RoomOccupancyListener listener : listenerList){
                                    listener.occupancyCrossUpThreshold(location,maxUser);
                                }
                            }
                        }else{
                            // DO NOTHING
                        }
                    }
                    lastUpdate = minuteOfUpdate;
                }
            }
        }
    }

    @Override
    public void factorModified(int oldFactor) {

    }

    @Override
    public void startDateModified(long oldStartDate) {
        resetClock();
    }

    @Override
    public void clockPaused() {

    }

    @Override
    public void clockResumed() {

    }

    @Override
    public void clockReset() {
        resetClock();
    }

    private synchronized void resetClock(){
        System.out.println(" Reset Clock ! ");
        synchronized (m_clockLock){
            startDate = new DateTime(clock.currentTimeMillis());
            yearOfStart = startDate.getYear();
            dayOfStart = startDate.getDayOfYear();
            lastUpdate = -1;
            synchronized (m_lock){
                for(String user : listOfUser){
                    Map<String,Map<Integer,Double>> tempLocationMap = new HashMap<String, Map<Integer,Double>>();
                    for(String location : listOfZone){
                        Map<Integer,Double> tempMap = new HashMap<Integer, Double>();
                        for(int i = 0 ; i <= NUMBER_OF_MINUTE ; i ++){
                            tempMap.put(i,0.0d);
                        }
                        tempLocationMap.put(location,tempMap);
                    }
                    mapProbaPerRoomPerUser.put(user,tempLocationMap);
                }
            }
        }
    }


}
