package fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.controller;

/**
 *
 */
/**
 * The TemperatureConfiguration service allows one to configure the temperature
 * controller.
 */
public interface TemperatureConfiguration {

    /**
     * Configure the controller to reach a given temperature in Kelvin in a
     * given room.
     *
     * @param targetedRoom
     *            the targeted room name
     * @param temperature
     *            the temperature in Kelvin (>=0)
     */
    public void setTargetedTemperature(String targetedRoom, float temperature);

    /**
     * Gets the targetted temperature of a given room.
     *
     * @param room
     *            the room name
     * @return the temperature in Kelvin
     */
    public float getTargetedTemperature(String room);

    /**
     * Turn on the temperature management in the given room
     *
     * @param room
     *            the given room
     */
    public void turnOn(String room);

    /**
     * Turn off the temperature management in the given room
     *
     * @param room
     *            the given room
     */
    public void turnOff(String room);

    /**
     * Sets the maximum allowed energy consumption in Watts in each room
     *
     * @param maximumEnergy
     *            the maximum allowed energy consumption in Watts in each room
     */
    public void setMaximumAllowedEnergyInRoom(double maximumEnergy);



}