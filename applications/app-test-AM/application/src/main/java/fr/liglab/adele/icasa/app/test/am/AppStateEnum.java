package fr.liglab.adele.icasa.app.test.am;

import fr.liglab.adele.icasa.context.manager.api.generic.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum AppStateEnum implements AppStateEventListener{

    INIT (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPIEnum.BinaryLight))))
    {
        /*Init context interactions - is there available lights*/
        /*Useless - No available lights*/
        @Override public AppStateEnum onEventLights(boolean available){
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
                @Override public AppStateEnum onEventSwitch(boolean available){
                    return available ? MANUAL : this;
                }

                @Override public AppStateEnum onEventPresenceAvailable(boolean available){
                    return available ? AUTO_ACTIVATED : this;
                }
            },

    MANUAL (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.BinaryLight,
            ContextAPIEnum.PresenceService,
            ContextAPIEnum.MultiwaySwitch))))
    {
        /*Manual - No presence information available - back up state*/
        /*Wait anyway for presence information*/
        @Override public AppStateEnum onEventSwitch(boolean available){
            return available ? MANUAL : this;
        }

        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            return available ? AUTO_ACTIVATED : this;
        }

    },

    AUTO_ECO (new ContextAPIConfig(new HashSet<>(Collections.singletonList(
            ContextAPIEnum.PresenceService))))
    {
        @Override public AppStateEnum onEventLights(boolean available){
            return this;
        }

        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            return available ? this : INIT;
        }

        @Override public AppStateEnum onEventPresence(boolean presence){
            return presence ? INIT : this;
        }
    },

    AUTO_ACTIVATED (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.BinaryLight,
            ContextAPIEnum.PresenceService))))
    {
        @Override public AppStateEnum onEventPresenceAvailable(boolean available){
            return available ? this : MODE;
        }

        @Override public AppStateEnum onEventPresence(boolean presence){
            return  presence ? this : AUTO_ECO;
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
        return available ? this : INIT;
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