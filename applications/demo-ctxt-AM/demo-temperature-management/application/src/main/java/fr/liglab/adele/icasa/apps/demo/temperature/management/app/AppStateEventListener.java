package fr.liglab.adele.icasa.apps.demo.temperature.management.app;

public interface AppStateEventListener {
    /**
     * State machine, state changes according to:
     * - availability of thermometers
     * - config is good enough
     */
    /*TODO Temperature locale vs. distante*/
    AppStateEnum onEventThermometers(boolean available);
    AppStateEnum onConfigCheck(boolean ok);
}