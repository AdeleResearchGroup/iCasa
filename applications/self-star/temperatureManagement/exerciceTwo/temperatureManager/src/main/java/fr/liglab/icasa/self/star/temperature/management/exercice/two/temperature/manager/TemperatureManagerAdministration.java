package fr.liglab.icasa.self.star.temperature.management.exercice.two.temperature.manager;

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
}
