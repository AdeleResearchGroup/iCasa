package fr.liglab.adele.icasa.self.star.follow.me.exercice.six.manager;


import fr.liglab.adele.icasa.service.location.PersonLocationService;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.icasa.self.star.follow.me.exercice.six.sum.set.algorithm.FollowMeConfiguration;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashSet;
import java.util.Set;


/**
 *
 */
@Component(name="FollowMeManager")
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class FollowMeManagerImpl implements FollowMeAdministration{

    @Requires
    FollowMeConfiguration followMeConfiguration;


    // You have to create a new dependency :
    @Requires
    private Preferences preferencesService; //...

    //same applied for the person location service :
    @Requires
    private PersonLocationService personLocationService; //...

    /**
     * User preferences for illuminance
     **/
    public static final String USER_PROP_ILLUMINANCE = "illuminance";
    public static final String USER_PROP_ILLUMINANCE_VALUE_SOFT = "SOFT";
    public static final String USER_PROP_ILLUMINANCE_VALUE_MEDIUM = "MEDIUM";
    public static final String USER_PROP_ILLUMINANCE_VALUE_FULL = "FULL";
    public static final String[] LIST_OF_ZONE = {"kitchen","bedroom","bathroom","livingroom"};
    IlluminanceGoal illuminanceGoal;

    EnergyGoal energyGoal;

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");

        illuminanceGoal = IlluminanceGoal.MEDIUM;
        energyGoal= EnergyGoal.LOW;
        applyIlluminanceGoal(illuminanceGoal);
        applyEnergyGoal(energyGoal);
    }

    public void applyIlluminanceGoal(IlluminanceGoal illuminanceGoal) {
        int numberOfLightToTurnOn = illuminanceGoal.getNumberOfLightsToTurnOn();
        double illuminanceTarget = illuminanceGoal.getTargetIlluminance();
        System.out.println("INIT : ILLU " + illuminanceTarget + " MAX " +  numberOfLightToTurnOn);
        followMeConfiguration.setMaximumNumberOfLightsToTurnOn(numberOfLightToTurnOn);
        followMeConfiguration.setTargetedIlluminance(illuminanceTarget);
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
            followMeConfiguration.setTargetedIlluminance(sum/count);
        }else{
            System.out.println(" nobody in the flat " );
        }

    }
}
