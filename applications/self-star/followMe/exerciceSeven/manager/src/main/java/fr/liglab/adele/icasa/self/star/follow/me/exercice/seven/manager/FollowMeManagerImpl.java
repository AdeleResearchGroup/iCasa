package fr.liglab.adele.icasa.self.star.follow.me.exercice.seven.manager;


import fr.liglab.adele.icasa.service.location.PersonLocationService;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.icasa.self.star.follow.me.exercice.seven.sum.set.algorithm.FollowMeConfiguration;
import fr.liglabl.adele.icasa.self.star.follow.me.exercice.seven.time.MomentOfTheDay;
import fr.liglabl.adele.icasa.self.star.follow.me.exercice.seven.time.MomentOfTheDayListener;
import fr.liglabl.adele.icasa.self.star.follow.me.exercice.seven.time.MomentOfTheDayService;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashSet;
import java.util.Set;


/**
 *
 */
@Component(name="FollowMeManager")
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class FollowMeManagerImpl implements FollowMeAdministration,MomentOfTheDayListener {

    @Requires
    FollowMeConfiguration followMeConfiguration;

    @Requires
    private MomentOfTheDayService momentOfTheDayService;

    // You have to create a new dependency :
    @Requires
    private Preferences preferencesService; //...

    //same applied for the person location service :
    @Requires
    private PersonLocationService personLocationService; //...

    /**
     * The maximum energy consumption allowed in a room in Watt:
     **/
    private double currentFactor = EVENING_ILLUMINANCE_FACTOR;

    /**
     * User preferences for illuminance
     **/
    public static final String USER_PROP_ILLUMINANCE = "illuminance";
    public static final String USER_PROP_ILLUMINANCE_VALUE_SOFT = "SOFT";
    public static final String USER_PROP_ILLUMINANCE_VALUE_MEDIUM = "MEDIUM";
    public static final String USER_PROP_ILLUMINANCE_VALUE_FULL = "FULL";
    public static final String[] LIST_OF_ZONE = {"kitchen","bedroom","bathroom","livingroom"};


    // There is no need of full illuminance in the morning
    private static final double  MORNING_ILLUMINANCE_FACTOR = 0.5;
    // In the afternoon the illuminance can be largely limited
    private static final double  ATERNOON_ILLUMINANCE_FACTOR = 0.2;
    // In the evening, the illuminance should be the best
    private static final double  EVENING_ILLUMINANCE_FACTOR = 1;
    // In the night, there is no need to use the full illuminance
    private static final double  NIGHT_ILLUMINANCE_FACTOR = 0.8;


    IlluminanceGoal illuminanceGoal;

    EnergyGoal energyGoal;

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
        momentOfTheDayService.unregister(this);
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");

        illuminanceGoal = IlluminanceGoal.MEDIUM;
        energyGoal= EnergyGoal.LOW;
        applyIlluminanceGoal(illuminanceGoal);
        applyEnergyGoal(energyGoal);
        momentOfTheDayService.register(this);
    }

    public void applyIlluminanceGoal(IlluminanceGoal illuminanceGoal) {
        int numberOfLightToTurnOn = illuminanceGoal.getNumberOfLightsToTurnOn();
        double illuminanceTarget = illuminanceGoal.getTargetIlluminance();
        System.out.println("INIT : ILLU " + illuminanceTarget*currentFactor + " MAX " +  numberOfLightToTurnOn);
        followMeConfiguration.setMaximumNumberOfLightsToTurnOn(numberOfLightToTurnOn);
        followMeConfiguration.setTargetedIlluminance(illuminanceTarget*currentFactor);
    }

    public void applyEnergyGoal(EnergyGoal energyGoal) {
        double energyInRoom = energyGoal.getMaximumEnergyInRoom();
        followMeConfiguration.setMaximumAllowedEnergyInRoom(energyInRoom);
    }

    @Override
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
        applyIlluminanceGoal(illuminanceGoal);
        this.illuminanceGoal = illuminanceGoal;
    }

    @Override
    public IlluminanceGoal getIlluminancePreference() {
        return illuminanceGoal;
    }

    @Override
    public void exist() {
        System.out.println("Administration Service exist");
    }

    @Override
    public void setEnergySavingGoal(EnergyGoal energyGoal) {
        applyEnergyGoal(energyGoal);
        this.energyGoal = energyGoal;
    }

    @Override
    public EnergyGoal getEnergyGoal() {
        return energyGoal;
    }

    @Override
    public IlluminanceGoal getIlluminancePreferenceForUser(String Name){
        String AliceIlm =  (String) preferencesService.getUserPropertyValue(Name, USER_PROP_ILLUMINANCE);
        if (AliceIlm != null){
            return IlluminanceGoal.valueOf(AliceIlm.toUpperCase());
        }
        else{
            System.out.println("No preference for this user, use global preference");
            return illuminanceGoal;
        }
    }

    @Override
    public void setIlluminancePreferenceForUser(String UserName,IlluminanceGoal goal) {
        preferencesService.setUserPropertyValue(UserName,USER_PROP_ILLUMINANCE,goal.name());
        String locationOfPerson ;
        Set<String> personInTheZone = new HashSet<String>();
        for(String location :LIST_OF_ZONE){
            Set<String> temp = personLocationService.getPersonInZone(location);
            for (String string : temp ) {
                System.out.println(string);
                personInTheZone.add(string);
            }
        }
        System.out.println( (personInTheZone != null) + " &&  " + (!personInTheZone.isEmpty()));
        if ((personInTheZone != null) && (!personInTheZone.isEmpty()) ){
            double sum = 0;
            int count = 0;
            for (String person :  personInTheZone){
                count ++;
                String AliceIlm =  (String) preferencesService.getUserPropertyValue(person, USER_PROP_ILLUMINANCE);
                if (AliceIlm != null){
                    sum += IlluminanceGoal.valueOf(AliceIlm.toUpperCase()).getTargetIlluminance();
                }else{
                    sum += illuminanceGoal.getTargetIlluminance();
                }
            }
            followMeConfiguration.setTargetedIlluminance((sum/count)*currentFactor);
        }else{
            System.out.println(" nobody in the flat " );
        }

    }

    @Override
    public void momentOfTheDayHasChanged(MomentOfTheDay newMomentOfTheDay) {
        if (newMomentOfTheDay == MomentOfTheDay.AFTERNOON){
            currentFactor = ATERNOON_ILLUMINANCE_FACTOR;
        }else if (newMomentOfTheDay == MomentOfTheDay.NIGHT){
            currentFactor = NIGHT_ILLUMINANCE_FACTOR;
        }else if(newMomentOfTheDay == MomentOfTheDay.EVENING){
            currentFactor = EVENING_ILLUMINANCE_FACTOR;
        }else if(newMomentOfTheDay == MomentOfTheDay.MORNING){
            currentFactor = MORNING_ILLUMINANCE_FACTOR;
        }
    }
}
