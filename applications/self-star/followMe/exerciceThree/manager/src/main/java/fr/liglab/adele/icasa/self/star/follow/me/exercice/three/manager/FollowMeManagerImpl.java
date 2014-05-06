package fr.liglab.adele.icasa.self.star.follow.me.exercice.three.manager;



import fr.liglab.adele.icasa.self.star.follow.me.exercice.three.dimmer.light.follow.me.FollowMeConfiguration;
import org.apache.felix.ipojo.annotations.*;

/**
 *
 */
@Component(name="FollowMeManager")
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class FollowMeManagerImpl implements FollowMeAdministration{

    @Requires(id="FollowMeConfiguration", optional=false)
    FollowMeConfiguration followMeConfiguration;

    IlluminanceGoal illuminanceGoal;

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
    }

    public void applyGoal(IlluminanceGoal illuminanceGoal) {
            int numberOfLightToTurnOn = illuminanceGoal.getNumberOfLightsToTurnOn();
            followMeConfiguration.setMaximumNumberOfLightsToTurnOn(numberOfLightToTurnOn);
    }

    @Override
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
        applyGoal(illuminanceGoal);
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
}
