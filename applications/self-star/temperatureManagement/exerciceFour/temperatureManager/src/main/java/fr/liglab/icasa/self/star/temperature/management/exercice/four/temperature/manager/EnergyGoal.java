package fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.manager;

/**
 * Created by aygalinc on 25/04/14.
 */
public enum EnergyGoal {
    LOW(0.1), MEDIUM(0.5), HIGH(1);

    /**
     * The corresponding maximum energy in watt
     */
    private double maximumEnergyInRoom;

    /**
     * get the maximum energy consumption in each room
     *
     * @return the energy in watt
     */
    public double getMaximumEnergyInRoom() {
        return maximumEnergyInRoom;
    }

    private EnergyGoal(double powerInWatt) {
        maximumEnergyInRoom = powerInWatt;
    }
}
