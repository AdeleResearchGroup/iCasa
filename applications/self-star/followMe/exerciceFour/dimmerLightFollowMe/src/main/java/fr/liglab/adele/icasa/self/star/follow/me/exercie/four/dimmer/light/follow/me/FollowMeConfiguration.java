package fr.liglab.adele.icasa.self.star.follow.me.exercie.four.dimmer.light.follow.me;

/**
 * Created by aygalinc on 07/03/14.
 */

/**
 * The FollowMeConfiguration service allows one to configure the Follow Me
 * application.
 */
public interface FollowMeConfiguration {

    /**
     * Gets the maximum number of lights to turn on each time an user is
     * entering a room.
     *
     * @return the maximum number of lights to turn on
     */
    public int getMaximumNumberOfLightsToTurnOn();

    /**
     * Sets the maximum number of lights to turn on each time an user is
     * entering a room.
     *
     * @param maximumNumberOfLightsToTurnOn
     *            the new maximum number of lights to turn on
     */
    public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn);

}
