package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.app;

public interface AppStateEventListener {
    /**
     * State machine, state changes according to:
     * - ???
     */
    AppStateEnum onEventShutters(boolean available);
}
