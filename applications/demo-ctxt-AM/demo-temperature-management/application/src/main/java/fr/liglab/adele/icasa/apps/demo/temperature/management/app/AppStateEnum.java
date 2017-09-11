package fr.liglab.adele.icasa.apps.demo.temperature.management.app;

import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextAPIConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum AppStateEnum implements AppStateEventListener{

    INIT (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPIEnum.Thermometer))))
            {
                /*Init context interactions - is there available heaters*/
                /*Useless - No available thermometers*/
                @Override public AppStateEnum onEventThermometers(boolean available){
                    return available ? PENDING : this;
                }
            },
    PENDING (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.Heater,
            ContextAPIEnum.Cooler,
            ContextAPIEnum.Thermometer))))
            {
                /*Init context interactions - wait to know which mode to choose*/
                /*HS - Config not good enough to work*/
            },
    RUNNING (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.Heater,
            ContextAPIEnum.Cooler,
            ContextAPIEnum.Thermometer))))
            {
                /*Running mode*/
                public AppStateEnum onConfigCheck(boolean ok){
                    return ok ? this : PENDING;
                }
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
    public AppStateEnum onEventThermometers(boolean available) {
        return available ? this : INIT;
    }

    @Override
    public AppStateEnum onConfigCheck(boolean ok){
        return ok ? RUNNING : this;
    }
}