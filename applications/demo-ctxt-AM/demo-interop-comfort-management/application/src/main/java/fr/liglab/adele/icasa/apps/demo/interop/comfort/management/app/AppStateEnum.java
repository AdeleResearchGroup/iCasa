package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.app;

import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextAPIConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum AppStateEnum implements AppStateEventListener{

    INIT (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPIEnum.BinaryLight))))
            {


                /*Init context interactions - is there available shutters*/
        /*Useless - No available shutters*/
                @Override public AppStateEnum onEventShutters(boolean available){
                    return available ? MODE : this;
                }
            },

    MODE (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.BinaryLight,
            ContextAPIEnum.PresenceService,
            ContextAPIEnum.MultiwaySwitch))))
            {
                /*Init context interactions - wait to know which mode to choose*/
                /*HS - Not enough available devices to work*/
            },
    ;

    private final ContextAPIConfig configContextAPI;

    AppStateEnum(ContextAPIConfig c) {
        configContextAPI = c;
    }

    public ContextAPIConfig getConfigContextAPI() {
        return configContextAPI;
    }

    @Override
    public AppStateEnum onEventShutters(boolean available) {
        return available ? this : INIT;
    }
}