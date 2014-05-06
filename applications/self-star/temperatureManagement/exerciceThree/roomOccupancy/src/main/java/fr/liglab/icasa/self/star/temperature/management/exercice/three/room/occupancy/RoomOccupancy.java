package fr.liglab.icasa.self.star.temperature.management.exercice.three.room.occupancy;

/**
 *
 */

public interface RoomOccupancy {

    /**
     * Gets the probability (between 0 and 1) that the given room is occupied
     * at the given moment of the day.
     *
     * @param minuteOfTheDay
     *            a specific time in the day in minute (between 0 (=00:00) and
     *            1439 (=23:59))
     * @param room
     *            the room name where the occupancy value is required.
     * @return the room occupancy is a value between 0 and 1 where 0 indicates
     *         that there the room is always empty and 1 indicates that the room
     *         is always occupied at the given moment of the day.
     */
    public double getRoomOccupancy(String room,int minuteOfTheDay);

    public void addListener(RoomOccupancyListener roomOccupancyListener);

    public void removeListener(RoomOccupancyListener roomOccupancyListener );

}
