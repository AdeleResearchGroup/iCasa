package fr.liglab.adele.icasa.apps.demo.pet.care.app;

import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextAPIConfig;

import java.util.Arrays;
import java.util.HashSet;

public enum AppStateEnum implements AppStateEventListener{

    INIT (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.PetInfo,
            ContextAPIEnum.WateringRegulator,
            ContextAPIEnum.FeedingRegulator,
            ContextAPIEnum.MomentOfTheDay))))
    {
//        /*Init context interactions - is there available lights*/
//        /*Useless - No available lights*/
//        @Override public AppStateEnum onEventLights(boolean available){
//            return available ? MODE : this;
//        }
        @Override public AppStateEnum onEventValidConfiguration(boolean valid){
            return valid ? (modeAuto ? AUTO : REMOTE) : this;
        }

        @Override public AppStateEnum onEventChosenMode(boolean auto){
            modeAuto = auto;
            return this;
        }
    },

    REMOTE (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.PetInfo,
            ContextAPIEnum.WateringRegulator,
            ContextAPIEnum.FeedingRegulator,
            ContextAPIEnum.MomentOfTheDay))))
    {
        @Override public AppStateEnum onEventChosenMode(boolean auto){
            modeAuto = auto;
            return auto ? AUTO : this;
        }
    },

    AUTO (new ContextAPIConfig(new HashSet<>(Arrays.asList(
            ContextAPIEnum.PetInfo,
            ContextAPIEnum.WateringRegulator,
            ContextAPIEnum.FeedingRegulator,
            ContextAPIEnum.MomentOfTheDay))))
    {
        @Override public AppStateEnum onEventChosenMode(boolean auto){
            modeAuto = auto;
            return auto ? this : REMOTE;
        }
    },
    ;

    private final ContextAPIConfig configContextAPI;

    private static boolean modeAuto = false;

    AppStateEnum(ContextAPIConfig c) {
        configContextAPI = c;
    }

    public ContextAPIConfig getConfigContextAPI() {
        return configContextAPI;
    }


    @Override public AppStateEnum onEventValidConfiguration(boolean valid){
        return valid ? this : INIT;
    }
}