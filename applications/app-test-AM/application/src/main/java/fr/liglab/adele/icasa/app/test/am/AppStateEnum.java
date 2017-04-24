package fr.liglab.adele.icasa.app.test.am;

import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum AppStateEnum implements AppStateEventListener{

    INIT (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPI.BinaryLight))))
    {
        /*Init context interactions - is there available lights*/
        /*Useless - No available lights*/
        @Override public AppStateEnum onEventLights(boolean available){
            if(available)
                return MODE;
            else
                return this;
        }
    },

    MODE (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPI.BinaryLight,
            ContextAPI.PresenceService,
            ContextAPI.MultiwaySwitch))))
            {
                /*Init context interactions - wait to know which mode to choose*/
                /*HS - Not enough available devices to work*/
                @Override public AppStateEnum onEventSwitch(boolean available){
                    if(available)
                        return MANUAL;
                    else
                        return this;
                }

                @Override public AppStateEnum onEventPresenceAvailable(boolean available){
                    if(available)
                        return AUTO_ACTIVATED;
                    else
                        return this;
                }
            },

    MANUAL (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPI.BinaryLight,
            ContextAPI.PresenceService,
            ContextAPI.MultiwaySwitch))))
    {
        /*Manual - No presence information available - back up state*/
        /*Wait anyway for presence information*/
        @Override public AppStateEnum onEventSwitch(boolean available){
            if(available)
                return MANUAL;
            else
                return this;
        }

        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            if(available)
                return AUTO_ACTIVATED;
            else
                return this;
        }

    },

    AUTO_ECO (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPI.PresenceService))))
    {
        @Override public AppStateEnum onEventLights(boolean available){
            return this;
        }

        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            if(available)
                return this;
            else
                return INIT;
        }

        @Override public AppStateEnum onEventPresence(boolean presence){
            if(presence)
                return INIT;
            else
                return this;
        }
    },

    AUTO_ACTIVATED (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPI.BinaryLight,
            ContextAPI.PresenceService))))
    {
        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            if(available)
                return this;
            else
                return MODE;
        }

        @Override public AppStateEnum onEventPresence(boolean presence){
            if(presence)
                return this;
            else
                return AUTO_ECO;
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

    @Override public AppStateEnum onEventLights(boolean available){
        if(available)
            return this;
        else
            return INIT;
    }

    @Override public AppStateEnum onEventSwitch(boolean available){
        return this;
    }

    @Override public AppStateEnum onEventPresenceAvailable(boolean available){
        return this;
    }

    @Override public AppStateEnum onEventPresence(boolean presence){
        return this;
    }
}