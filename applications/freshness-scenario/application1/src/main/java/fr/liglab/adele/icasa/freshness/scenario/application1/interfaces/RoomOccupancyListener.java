package fr.liglab.adele.icasa.freshness.scenario.application1.interfaces;

/**
 *
 */
public interface RoomOccupancyListener {

    /**
     * Notify the listener that roomOccupancy cross down the threshold
     *
     * @param room
     *           room where it happened
     */
    void occupancyCrossDownThreshold(String room);

    /**
     * Notify the listener that roomOccupancy cross up the threshold
     *
     * @param room
     *           room where it happened
     */
    void occupancyCrossUpThreshold(String room);
}