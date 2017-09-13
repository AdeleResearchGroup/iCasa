package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.app;

import fr.liglab.adele.icasa.apps.demo.global.DemoApp;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name="DemoLuminosityManagement")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Demo.Luminosity.Management.Application", immutable=true)
})

@Instantiate
@CommandProvider(namespace = "demo-luminosity-management")
@SuppressWarnings("unused")
public class AppManager implements DemoApp {
    private static final Logger LOG = LoggerFactory.getLogger(AppManager.class);
    public static final String LOG_PREFIX = "LUMINOSITY MANAGEMENT APP - ";

    /*App state*/
    /*ToDo*/
    private final static String appName = "demo-luminosity-management";
    private static AppStateEnum appState = AppStateEnum.INIT;
    private static AppStateEnum previousAppState = AppStateEnum.INIT;
    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;

    /*Interaction with context*/
    private static boolean registered;

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
    //

    /*Context state check and App state change*/
    @Requires(id="manager", optional = false, specification = ContextDependencyRegistration.class)
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;

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
        //
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
