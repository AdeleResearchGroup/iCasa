package fr.liglab.adele.icasa.self.star.follow.me.exercice.seven.manager;

/**
 *
 */



/**
 * The Interface FollowMeAdministration allows the administrator to configure
 * its preference regarding the management of the Follow Me application.
 */
public interface FollowMeAdministration {

    /**
     * Sets the illuminance preference. The manager will try to adjust the
     * illuminance in accordance with this goal.
     *
     * @param illuminanceGoal
     *            the new illuminance preference
     */
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal);

    /**
     * Get the current illuminance preference.
     *
     * @return the new illuminance preference
     */
    public IlluminanceGoal getIlluminancePreference();

    /**
     * Get the current illuminance preference.
     *
     * @return the new illuminance preference
     */
    public void exist();

    /**
     * Configure the energy saving goal.
     * @param energyGoal : the targeted energy goal.
     */
    public void setEnergySavingGoal(EnergyGoal energyGoal);

    /**
     * Gets the current energy goal.
     *
     * @return the current energy goal.
     */
    public EnergyGoal getEnergyGoal();

    /**
     * Gets the current energy goal.
     *
     * @return the current energy goal.
     */
    public IlluminanceGoal getIlluminancePreferenceForUser(String UserName);

    public void setIlluminancePreferenceForUser(String UserName,IlluminanceGoal goal);
}
