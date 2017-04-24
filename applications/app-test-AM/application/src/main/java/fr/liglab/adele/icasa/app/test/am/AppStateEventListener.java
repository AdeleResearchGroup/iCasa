package fr.liglab.adele.icasa.app.test.am;

import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;

public interface AppStateEventListener {
    /**
     * State machine, state changes according to:
     * - availability of lights
     * - availability of presence sensors
     * - presence of the user
     * - the time of the day
     */
    AppStateEnum onEventLights(boolean available);
    AppStateEnum onEventSwitch(boolean available);
    AppStateEnum onEventPresenceAvailable(boolean available);
    AppStateEnum onEventPresence(boolean presence);

}
