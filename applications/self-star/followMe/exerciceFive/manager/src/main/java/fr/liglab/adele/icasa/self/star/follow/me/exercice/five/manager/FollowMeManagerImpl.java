package fr.liglab.adele.icasa.self.star.follow.me.exercice.five.manager;


import fr.liglab.icasa.self.star.follow.me.exercice.five.sum.set.algorithm.FollowMeConfiguration;
import org.apache.felix.ipojo.annotations.*;

/**
 *
 */
@Component(name="FollowMeManager")
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class FollowMeManagerImpl implements FollowMeAdministration{

    @Requires
    FollowMeConfiguration followMeConfiguration;

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
        double illuminanceTarget = illuminanceGoal.getNumberOfLightsToTurnOn();
        followMeConfiguration.setMaximumNumberOfLightsToTurnOn(numberOfLightToTurnOn);
        followMeConfiguration.setTargetedIlluminance(illuminanceTarget);
    }

    public void applyEnergyGoal(EnergyGoal energyGoal) {
        double energyInRoom = energyGoal.getMaximumEnergyInRoom();
        followMeConfiguration.setMaximumAllowedEnergyInRoom(195);
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
}
