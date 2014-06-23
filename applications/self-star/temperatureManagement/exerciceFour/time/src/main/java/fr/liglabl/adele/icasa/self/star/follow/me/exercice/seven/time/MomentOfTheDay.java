package fr.liglabl.adele.icasa.self.star.follow.me.exercice.seven.time;

public enum MomentOfTheDay {
    MORNING(6), AFTERNOON(12), EVENING(18), NIGHT(22);

    /**
     * Gets the moment of the day corresponding to the hour.
     *
     * @param hour
     *            the given hour
     * @return the corresponding moment of the day
     */
    MomentOfTheDay getCorrespondingMoment(int hour) {
        assert ((0 <= hour) && (hour <= 24));
       /* if ((6 <= hour) &&  (hour< 12)){
            return MomentOfTheDay.MORNING;
        }else if ((6 <= hour) && (hour< 12)){
            return MomentOfTheDay.AFTERNOON;
        }else if ((12 <= hour) && (hour < 18)){
            return MomentOfTheDay.EVENING;
        }else {
            return MomentOfTheDay.NIGHT;
        }*/
        if (MomentOfTheDay.NIGHT.getStartHour() <=hour ){
            return MomentOfTheDay.NIGHT;
        }
        else if (MomentOfTheDay.EVENING.getStartHour() <=hour ){
            return MomentOfTheDay.EVENING;
        }
        else if (MomentOfTheDay.AFTERNOON.getStartHour() <=hour ){
            return MomentOfTheDay.AFTERNOON;
        }
        else if (MomentOfTheDay.MORNING.getStartHour() <=hour ){
            return MomentOfTheDay.MORNING;
        }else{
            return MomentOfTheDay.NIGHT;
        }

    }

    int getStartHour() {
        assert ((0 <= startHour) && (startHour <= 24));
        return startHour;
    }

    /**
     * The hour when the moment start.
     */
    private final int startHour;

    /**
     * Build a new moment of the day :
     *
     * @param startHour
     *            when the moment start.
     */
    MomentOfTheDay(int startHour) {
        assert ((0 <= startHour) && (startHour <= 24));
        this.startHour = startHour;
    }
}