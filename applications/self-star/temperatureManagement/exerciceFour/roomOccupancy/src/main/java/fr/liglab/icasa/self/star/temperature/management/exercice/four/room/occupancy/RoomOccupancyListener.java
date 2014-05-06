package fr.liglab.icasa.self.star.temperature.management.exercice.four.room.occupancy;

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
    void occupancyCrossDownThreshold(String room,String user);

    /**
     * Notify the listener that roomOccupancy cross up the threshold
     *
     * @param room
     *           room where it happened
     */
    void occupancyCrossUpThreshold(String room,String user);
}