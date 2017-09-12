package fr.liglab.adele.icasa.apps.demo.temperature.management.app;

import fr.liglab.adele.icasa.apps.demo.global.DemoApp;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(name="DemoTemperatureManagement")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Demo.Temperature.Management.Application", immutable=true)
})

@Instantiate
@CommandProvider(namespace = "demo-temperature-management")
@SuppressWarnings("unused")
public class AppManager implements DemoApp {
    private static final Logger LOG = LoggerFactory.getLogger(AppManager.class);
    public static final String LOG_PREFIX = "TEMPERATURE MANAGEMENT APP - ";

    /*App state*/
    private final static String appName = "demo-temperature-management";
    private static AppStateEnum appState = AppStateEnum.INIT;
    private static AppStateEnum previousAppState = AppStateEnum.INIT;
    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;

    /*Interaction with context*/
    private static boolean registered;

    /*Context state*/
    private boolean configOK = false;
    private boolean thermometersOK = false;
    private boolean heatersOK = false;
    private boolean coolersOK = false;



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
    @Requires(optional = false, specification = Factory.class, filter = "(factory.name=demo-temperature-management)")
    @SuppressWarnings("all")
    private Factory factoryTempMgmt;
    private static ComponentInstance appTempMgmt = null;

    /*Context state check and App state change*/
    @Requires(id="manager", optional = false, specification = ContextDependencyRegistration.class)
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;

    @Requires(id="thermometers",optional = true,specification = Thermometer.class,proxy = false)
    @SuppressWarnings("all")
    private List<Thermometer> thermometers;

    @Requires(id="heaters",optional = true,specification = Heater.class,proxy = false)
    @SuppressWarnings("all")
    private List<Heater> heaters;

    @Requires(id="coolers",optional = true,specification = Cooler.class,proxy = false)
    @SuppressWarnings("all")
    private List<Cooler> coolers;

    @Bind(id="thermometers")
    @SuppressWarnings("unused")
    public synchronized void bindThermometer(){
        thermometersOK = true;
        appState = previousAppState.onConfigCheck(checkConfig());
        appState = previousAppState.onEventThermometers(true);
        appReconfiguration();
    }

    @Unbind(id = "thermometers")
    @SuppressWarnings("unused")
    public synchronized void unbindThermometer(Thermometer thermometer){
        thermometersOK = !(thermometers.isEmpty() || (thermometers.size()==1 && thermometers.contains(thermometer)));
        appState = previousAppState.onConfigCheck(checkConfig());
        if(!thermometersOK){appState = previousAppState.onEventThermometers(false);}
        appReconfiguration();
    }

    @Bind(id="heaters")
    @SuppressWarnings("unused")
    public synchronized void bindHeater(){
        heatersOK = true;
        appState = previousAppState.onConfigCheck(checkConfig());
        appReconfiguration();
    }

    @Unbind(id = "heaters")
    @SuppressWarnings("unused")
    public synchronized void unbindHeater(Heater heater){
        heatersOK = !(heaters.isEmpty() || (heaters.size()==1 && heaters.contains(heater)));
        appState = previousAppState.onConfigCheck(checkConfig());
        appReconfiguration();
    }

    @Bind(id="coolers")
    @SuppressWarnings("unused")
    public synchronized void bindCooler(){
        coolersOK = true;
        appState = previousAppState.onConfigCheck(checkConfig());
        appReconfiguration();
    }

    @Unbind(id = "coolers")
    @SuppressWarnings("unused")
    public synchronized void unbindCooler(Cooler cooler){
        coolersOK = !(coolers.isEmpty() || (coolers.size()==1 && coolers.contains(cooler)));
        appState = previousAppState.onConfigCheck(checkConfig());
        appReconfiguration();
    }

    private boolean checkConfig(){
        configOK = thermometersOK && (coolersOK || heatersOK);
        return configOK;
    }

    /*App state change with context reconfiguration*/
    private void appReconfiguration(){
        /*ToDo lock system doesn't work*/
//        if(isReconfiguring){
//            needToBeReconfigured = true;
//            return;
//        }
//
//        isReconfiguring = true;
        if (appState != previousAppState){
            previousAppState = appState;
            appInternalReconfiguration();
            LOG.info(LOG_PREFIX + "STATE: "+appState.toString());
            contextAPIAppRegistration();
        }
//        isReconfiguring = false;
//
//        if(needToBeReconfigured){
//            needToBeReconfigured = false;
//            appReconfiguration();
//        }
    }

    private void appRegistration(){
        appState = AppStateEnum.INIT;
        previousAppState = AppStateEnum.INIT;
        appInternalReconfiguration();
        contextAPIAppRegistration();
    }

    private void appUnregistration(){
        contextDependencyRegistration.unregisterContextDependencies(appName);
        appState = AppStateEnum.INIT;
        appInternalReconfiguration();
        previousAppState = null;
        LOG.info(LOG_PREFIX + "STATE: "+appState.toString());
    }

    private void contextAPIAppRegistration(){
        if(contextDependencyRegistration !=null&&registered){
            contextDependencyRegistration.registerContextDependencies(appName, appState.getConfigContextAPI());
            LOG.info(LOG_PREFIX + "RECONFIGURATION...");
        }
    }

    private void appInternalReconfiguration(){
        switch(appState){
            case RUNNING:
                appTempMgmt=activateSubComponent(factoryTempMgmt);
                break;
            default:
                disposeSubComponent(appTempMgmt);
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

    /*App command interface*/
    @Command
    @SuppressWarnings("unused")
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
}