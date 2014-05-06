package fr.liglab.adele.icasa.self.star.follow.me.exercice.five.manager;

/**
 *
 */
/**
 * This enum describes the different illuminance goals associated with the
 * manager.
 */
public enum IlluminanceGoal {

    /** The goal associated with soft illuminance. */
    SOFT(1, 500d),
    /** The goal associated with medium illuminance. */
    MEDIUM(2, 2750d),
    /** The goal associated with full illuminance. */
    FULL(3, 4000d);

    /** The number of lights to turn on. */
    private int numberOfLightsToTurnOn;

    /** The number of lights to turn on. */
    private double targetIlluminance;

    /**
     * Gets the number of lights to turn On.
     *
     * @return the number of lights to turn On.
     */
    public int getNumberOfLightsToTurnOn() {
        return numberOfLightsToTurnOn;
    }

    /**
     * Gets the number of lights to turn On.
     *
     * @return the number of lights to turn On.
     */
    public double getTargetIlluminance() {
        return targetIlluminance;
    }

    /**
     * Instantiates a new illuminance goal.
     *
     * @param numberOfLightsToTurnOn
     *            the number of lights to turn on.
     */
    private IlluminanceGoal(int numberOfLightsToTurnOn,double targetIlluminance) {
        this.numberOfLightsToTurnOn = numberOfLightsToTurnOn;
        this.targetIlluminance = targetIlluminance;
    }
}

