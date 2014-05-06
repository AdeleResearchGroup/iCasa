package fr.liglab.icasa.self.star.temperature.management.exercice.three.temperature.manager;

/**
 *
 */
/**
 * This interface allows to configure the temperature manager responsible for
 * configuring the temperature controller.
 */
public interface TemperatureManagerAdministration {

    /**
     * This method is called every time a user think the temperature is too high
     * in a given room.
     *
     * @param roomName
     *            the room where the temperature should be reconfigured
     */
    public void temperatureIsTooHigh(String roomName);

    /**
     * This method is called every time a user think the temperature is too high
     * in a given room.
     *
     * @param roomName
     *            the room where the temperature should be reconfigured
     */
    public void temperatureIsTooLow(String roomName);

    /**
     * Enable the energy saving mode.
     */
    public void turnOnEnergySavingMode();

    /**
     * Disable the energy saving mode.
     */
    public void turnOffEnergySavingMode();

    /**
     * Checks if the energy saving mode is enabled.
     *
     * @return true, if the energy saving mode is enabled
     */
    public boolean isPowerSavingEnabled();


    /**
     * Set the maximum energy goal
     *
     */
    public void setTemperatureEnergyGoal(EnergyGoal goal);

    /**
     * Get the maximum energy goal
     *
     * @return The energy goal Policy
     */
    public EnergyGoal getTemperatureEnergyGoal();

    /**
     * Get the room occupancy
     *
     * @return The probability of the occupance of the room
     */
    public double getRoomOccupancy(String room,int minute);


}
