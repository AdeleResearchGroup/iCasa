/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.app.test.am;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.ContextAPIAppRegistration;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MultiwaySwitch;
import fr.liglab.adele.icasa.physical.abstraction.PresenceService;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(name="AppTestAMApplication")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="App.Test.AM.Application", immutable=true)
})

@Instantiate
@CommandProvider(namespace = "app-test")
public class AppManager {

    private static final Logger LOG = LoggerFactory.getLogger(AppManager.class);

    /*Interaction with context*/
    private static boolean registered;
    private static final String appId = AppManager.class.toGenericString();

    /*Contexte state*/
    /*TODO when a presenceService changes, with temporal check and all that stuff*/
//    private static boolean userLeftRoom = false;

    /*App state*/
    private static AppStateEnum appState = AppStateEnum.INIT;
    private static AppStateEnum previousAppState = null;

    @Validate
    @SuppressWarnings("unused")
    public void start() {
        registered = false;
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {
        if(registered){
            appToggleRegistration();
        }
    }

    /*Factories for internal reconfiguration*/
    @Requires(optional = false, specification = Factory.class, filter = "(factory.name=app-test-mgmt-switch)")
    private Factory factorySwitch;
    private static ComponentInstance appLightMgmtSwitch = null;

    @Requires(optional = false, specification = Factory.class, filter = "(factory.name=app-test-mgmt-presence)")
    private Factory factoryPresence;
    private static ComponentInstance appLightMgmtPresence = null;

    /*Context state check and App state change*/
    @Requires(id="manager", optional = true, specification = ContextAPIAppRegistration.class)
    @SuppressWarnings("unused")
    private ContextAPIAppRegistration contextAPIAppRegistration;

//    @Requires(id="lights",optional = true,specification = BinaryLight.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))",proxy = false)
    //TODO
    @Requires(id="lights",optional = true,specification = BinaryLight.class,proxy = false)
    @SuppressWarnings("unused")
    private List<BinaryLight> binaryLights;

    @Requires(id="presence",optional = true,specification = PresenceService.class)
    @SuppressWarnings("unused")
    private List<PresenceService> presenceServices;

    @Requires(id="multiwaySwitch", optional = true, specification = MultiwaySwitch.class)
    @SuppressWarnings("unused")
    private List<MultiwaySwitch> multiwaySwitches;

    /*!TODO ATTENTION AU SYNCHRONIZED*/
    /*TODO ok car pas beaucoup de lignes dans la methode registerGoals*/
    @Bind(id="lights")
    public synchronized void bindBinaryLight(BinaryLight binaryLight){
        appState = previousAppState.onEventLights(true);
        appReconfiguration();
    }


    @Unbind(id = "lights")
    @SuppressWarnings("unused")
    public synchronized void unbindBinaryLight(BinaryLight binaryLight){
        if(binaryLights.size()<=1){
            appState = previousAppState.onEventLights(false);
            appReconfiguration();
        }
    }

    @Bind(id="presence")
    @SuppressWarnings("unused")
    public synchronized void bindPresenceService(PresenceService presenceService){
        appState = previousAppState.onEventPresenceAvailable(true);
        appReconfiguration();
    }

    @Modified(id = "presence")
    @SuppressWarnings("unused")
    public synchronized void modifyPresenceService(PresenceService presenceService){
        /*TODO*/
        //APP STATE CHANGE TO AUTO_ECO (temporisation for state)
    }

    @Unbind(id = "presence")
    @SuppressWarnings("unused")
    public synchronized void unbindPresenceService(PresenceService presenceService){
        if(presenceServices.size()<=1){
            appState = previousAppState.onEventPresenceAvailable(false);
            appReconfiguration();
        }
    }

    @Bind(id="multiwaySwitch")
    @SuppressWarnings("unused")
    public synchronized void bindMultiwaySwitch(MultiwaySwitch multiwaySwitch){
        appState = previousAppState.onEventSwitch(true);
        appReconfiguration();
    }

    @Unbind(id = "multiwaySwitch")
    @SuppressWarnings("unused")
    public synchronized void unbindMultiwaySwitch(MultiwaySwitch multiwaySwitch){
        if(multiwaySwitches.size()<=1){
            appState = previousAppState.onEventSwitch(false);
            appReconfiguration();
        }
    }

    /*App state change with context reconfiguration*/
    private void appReconfiguration(){
        if (appState != previousAppState){
            contextAPIAppRegistration();
            appInternalReconfiguration();
            previousAppState = appState;
            LOG.info("APP STATE: "+appState.toString());
        }
    }
    private void appUnregistration(){
        contextAPIAppRegistration.unregisterContextGoals(appId);
        appState = AppStateEnum.INIT;
        appInternalReconfiguration();
        previousAppState = null;
        LOG.info("APP STATE: "+appState.toString());
    }

    private void contextAPIAppRegistration(){
        if(contextAPIAppRegistration!=null&&registered){
                contextAPIAppRegistration.registerContextGoals(appId, appState.getConfigContextAPI());
                LOG.info("APP RECONFIGURATION...");
        }
    }

    private void appInternalReconfiguration(){
        switch(appState){
            case MANUAL:
                disposeSubComponent(appLightMgmtPresence);
                appLightMgmtSwitch=activateSubComponent(factorySwitch);
                break;
            case AUTO_ACTIVATED:
                disposeSubComponent(appLightMgmtSwitch);
                appLightMgmtPresence=activateSubComponent(factoryPresence);
                break;
            default:
                disposeSubComponent(appLightMgmtSwitch);
                disposeSubComponent(appLightMgmtPresence);
                break;
        }
    }

    private ComponentInstance activateSubComponent(Factory factory){
        ComponentInstance component = null;
        try {
            component = factory.createComponentInstance(null);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return component;
    }

    private void disposeSubComponent(ComponentInstance component){
        if(component != null){
            component.dispose();
        }
    }

    /*Debug interface*/
    @Command
    @SuppressWarnings("unused")
    public void appToggleRegistration() {
        if(contextAPIAppRegistration!=null){
            if(!registered){
                appState = AppStateEnum.INIT;
                registered = true;
                appReconfiguration();
                LOG.info("APP REGISTERED");
            } else {
                appUnregistration();
                registered = false;
                LOG.info("APP UNREGISTERED");
            }
        }
    }

    /*TODO REMOVE (TEMP)*/
    @Requires(id="buttons", optional = true, specification = PushButton.class)
    @SuppressWarnings("unused")
    private List<PushButton> buttons;

    /*TODO REMOVE (TEMP)*/
    @Command
    public void pushButtons(){
        buttons.forEach(PushButton::push);
    }

    @Command
    public void pushButtons(String zone){
        for(PushButton b : buttons){
            if(b instanceof LocatedObject){
                if(zone.equals(((LocatedObject)b).getZone())){
                    b.push();
                    break;
                }
            }
        }
    }
}