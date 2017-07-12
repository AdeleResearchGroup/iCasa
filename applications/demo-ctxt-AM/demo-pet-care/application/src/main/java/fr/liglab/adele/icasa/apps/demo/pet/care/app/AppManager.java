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
package fr.liglab.adele.icasa.apps.demo.pet.care.app;

import fr.liglab.adele.icasa.apps.demo.global.DemoApp;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.PetInfo;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component(name="DemoPetCare")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Demo.AppPetCare", immutable=true)
})

@Instantiate
@SuppressWarnings("unused")
//public class AppManager implements DemoApp {
public class AppManager implements DemoApp, PetCareAppManagerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(AppManager.class);
    public static final String LOG_PREFIX = "PET CARE APP - ";

    /*App state*/
    private final static String appName = "demo-pet-care";
    private static AppStateEnum appState = AppStateEnum.INIT;
    private static AppStateEnum previousAppState = AppStateEnum.INIT;

    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;

    /*Interaction with context*/
    private static boolean registered;

    /*Contexte state*/
    private static boolean validConfiguration = false;

    /*App mode*/
    private static boolean autoMode = false;
    /*Context availability*/
    private boolean contextValidity = false;


    /*AppManager component life cycle methods*/
    @Validate
    @SuppressWarnings("unused")
    public void start() {
        registered = false;
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {
        if(registered){
            toggleRegistration();
        }
    }

    /*Factories for app internal reconfiguration*/
    @Requires(optional = false, specification = Factory.class, filter = "(factory.name=demo-pet-care-component)")
    @SuppressWarnings("all")
    private Factory factoryPetCare;
    private static ComponentInstance appPetCare = null;

    /*Context state check and App state change*/
    @Requires(id="manager", optional = false, specification = ContextDependencyRegistration.class)
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;


    @Requires(id="pets",optional = true, specification = PetInfo.class, proxy = false)
    @SuppressWarnings("all")
    private List<PetInfo> petInfoList;

    @Requires(id="momentOfTheDay",optional = true, specification = MomentOfTheDay.class, proxy = false)
    @SuppressWarnings("unused")
    private MomentOfTheDay momentOfTheDay;


    @Bind(id="pets")
    @SuppressWarnings("unused")
    public synchronized void bindPetInfo(){
        contextValidity = checkContextConfigurationValidity(null);
        appConfigurationUpdate();
    }

    @Modified(id = "pets")
    @SuppressWarnings("unused")
    public synchronized void modifiedPetInfo(){
        contextValidity = checkContextConfigurationValidity(null);
        appConfigurationUpdate();
    }

    @Unbind(id = "pets")
    @SuppressWarnings("unused")
    public synchronized void unbindPetInfo(PetInfo petInfo){
        contextValidity = checkContextConfigurationValidity(petInfo);
        appConfigurationUpdate();
    }

    @Bind(id = "momentOfTheDay")
    @SuppressWarnings("unused")
    public synchronized void bindMomentOfTheDay(){
        appConfigurationUpdate();
    }

    @Unbind(id = "momentOfTheDay")
    @SuppressWarnings("unused")
    public synchronized void unbindMomentOfTheDay(){
        appConfigurationUpdate();
    }

    private void appConfigurationUpdate() {
        appState = previousAppState.onEventValidConfiguration(contextValidity && (momentOfTheDay != null));
        appReconfiguration();
    }

    /*ToDo do NOT work - (because unbinding item is still in the list*/
    private boolean checkContextConfigurationValidity(PetInfo toIgnore){

        if(petInfoList == null || petInfoList.isEmpty())
            return false;

        List<PetInfo> petInfoList = new ArrayList<>(this.petInfoList);
        if(toIgnore != null){
            petInfoList.remove(toIgnore);
        }

        for(PetInfo petInfo : petInfoList){
            if(!petInfo.getPetCareInfoValidity())
                return false;
            if(petInfo.getWateringRegulator() == null)
                return false;
            if(petInfo.getFeedingRegulator() == null)
                return false;
        }
        return true;
    }

    /*App state change with context reconfiguration*/
    private void appReconfiguration(){
        if (appState != previousAppState){
            previousAppState = appState;
            appInternalReconfiguration();
            LOG.info(LOG_PREFIX + "STATE: " + appState.toString());
            contextAPIAppRegistration();
        }
    }

    private void appRegistration(){
        appState = AppStateEnum.INIT;
        previousAppState = AppStateEnum.INIT;
        appInternalReconfiguration();
        contextAPIAppRegistration();
    }

    private void appUnregistration(){
        contextDependencyRegistration.unregisterContextDependencies(appName);
//        previousAppState = AppStateEnum.INIT;
        appState = AppStateEnum.INIT;
        appInternalReconfiguration();
    }

    private void contextAPIAppRegistration(){
        if(contextDependencyRegistration!=null && registered){
            contextDependencyRegistration.registerContextDependencies(appName, appState.getConfigContextAPI());
            LOG.info(LOG_PREFIX + "RECONFIGURATION...");
        }
    }

    private void appInternalReconfiguration(){
        switch(appState){
            case INIT:
                disposeSubComponent(appPetCare);
                break;
            default:
                if(appPetCare == null)
                    appPetCare=activateSubComponent(factoryPetCare);
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

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public boolean getRegistrationState() {
        return registered;
    }

    /*App interface*/
    @Override
    public boolean toggleRegistration() {
        if(contextDependencyRegistration !=null){
            if(!registered){
                registered = true;
                appRegistration();
                LOG.info(LOG_PREFIX + "REGISTERED");
            } else {
                appUnregistration();
                registered = false;
                LOG.info(LOG_PREFIX + "UNREGISTERED");
            }
        }
        return registered;
    }

    @Override
    public String getState() {
        return appState.name();
    }

    @Override
    public void petCareModeToggle(){
        if(autoMode){
            LOG.info(LOG_PREFIX + "AUTO MODE");
        } else {
            LOG.info(LOG_PREFIX + "REMOTE MODE");
        }
        appState = previousAppState.onEventChosenMode(autoMode);
        autoMode = !autoMode;
        appReconfiguration();
    }
}