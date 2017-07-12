package fr.liglab.adele.icasa.apps.demo.pet.care.app;

public interface AppStateEventListener {
    /**
     * State machine, state changes according to:
     * - availability of Pet Description
     * - availability of WateringController
     * - availability of FeedingController
     * - chosen mode
     */
    AppStateEnum onEventValidConfiguration(boolean valid);
    AppStateEnum onEventChosenMode(boolean auto);
}
