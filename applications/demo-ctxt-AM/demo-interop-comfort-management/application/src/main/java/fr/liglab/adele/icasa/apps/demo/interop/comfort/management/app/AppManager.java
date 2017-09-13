package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.app;

import fr.liglab.adele.icasa.apps.demo.global.DemoApp;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.config.ContextAPIEnum;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.models.goals.ContextDependencyRegistration;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

@Component(name="DemoLuminosityManagement")

@Provides(properties= {
        @StaticServiceProperty(name="icasa.application", type="String", value="Demo.Interop.Comfort.Management.Application", immutable=true)
})

@Instantiate
@CommandProvider(namespace = "demo-interop-comfort-management")
@SuppressWarnings("unused")
public class AppManager implements DemoApp {

    /*APP INFO*/
    private static final Logger LOG = LoggerFactory.getLogger(AppManager.class);
    private static final String LOG_PREFIX = "INTEROP COMFORT MANAGEMENT APP - ";
    private static final String appName = "demo-interop-comfort-management";
    private static String appState = "DEACTIVATED";

    /*Interaction with context*/
    private static boolean registered;
    private static ContextAPIConfig goals = new ContextAPIConfig(new HashSet<>(Arrays.asList(
        ContextAPIEnum.Photometer,
        ContextAPIEnum.Shutter,
        ContextAPIEnum.Thermometer,
        ContextAPIEnum.Cooler
    )));

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
    @Requires(optional = false, specification = Factory.class, filter = "(factory.name=demo-interop-comfort-management)")
    @SuppressWarnings("all")
    private Factory factoryComfortMgmt;
    private static ComponentInstance appComfortMgmt = null;


    /*Context state check and App state change*/
    @Requires(id="manager", optional = false, specification = ContextDependencyRegistration.class)
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;

    private void appRegistration(){
        contextAPIAppRegistration();
        appComfortMgmt = activateSubComponent(factoryComfortMgmt);
    }

    private void appUnregistration(){
        disposeSubComponent(appComfortMgmt);
        contextDependencyRegistration.unregisterContextDependencies(appName);
        appState = "DEACTIVATED";
        LOG.info(LOG_PREFIX + "STATE: "+ appState);
    }

    private void contextAPIAppRegistration(){
        if(contextDependencyRegistration !=null&&registered){
            contextDependencyRegistration.registerContextDependencies(appName, goals);
            appState = "ACTIVATED";
            LOG.info(LOG_PREFIX + "STATE: "+ appState);
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
        return appState;
    }
}