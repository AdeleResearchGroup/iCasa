/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.application.impl;

import fr.liglab.adele.freshness.utils.Freshness;
import fr.liglab.adele.freshness.utils.FreshnessMapping;
import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.application.*;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component(name = "Freshness-Demand-Impl")
@Provides
@Instantiate(name = "Freshness-Demand-Impl-0")
public class FreshnessTrackerImpl implements FreshnessTracker, ApplicationTracker { //, PeriodicRunnable {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".freshness");

    @Requires
    ApplicationManager manager;

    @Requires(specification = BinaryLight.class, optional = true, proxy = false)
    List<BinaryLight> binaryLights;

    @Requires(specification = DimmerLight.class, optional = true, proxy = false)
    List<DimmerLight> dimmerLights;

    @Requires(specification = PresenceSensor.class, optional = true, proxy = false)
    List<PresenceSensor> presenceSensors;

    @Requires(specification = MotionSensor.class, optional = true, proxy = false)
    List<MotionSensor> motionSensors;

    @Requires(specification = PushButton.class, optional = true, proxy = false)
    List<PushButton> pushButtons;

    @Requires(specification = Photometer.class, optional = true, proxy = false)
    List<Photometer> photometers;

    @Requires(specification = Thermometer.class, optional = true, proxy = false)
    List<Thermometer> thermometers;

    @Requires(specification = Cooler.class, optional = true, proxy = false)
    List<Cooler> coolers;

    @Requires(specification = Heater.class, optional = true, proxy = false)
    List<Heater> heaters;

    private BundleContext _context;

    private final Object m_lock = new Object();

    private List<ApplicationFreshnessDemand> appDemands;
    private List<DeviceFreshnessDemand> deviceDemands;

    public FreshnessTrackerImpl(BundleContext context) {
        _context = context;
        appDemands = new ArrayList<>();
        deviceDemands = new ArrayList<>();
    }

    /**
     * Component Lifecycle Method
     */
    @Invalidate
    public void stop() {
        System.out.println("Component freshnessDemands is stopping... ");
    }

    /**
     * Component Lifecycle Method
     */
    @Validate
    public void start() {
        System.out.println("Component freshnessDemands is starting...");
        manager.addApplicationListener(this);
        computeDemands();
    }

    private void computeDemands() {

        synchronized (m_lock) {
            appDemands = handlerToContextDemands();
            deviceDemands.clear();

            List<GenericDevice> devices = new ArrayList<>();
            devices.addAll(binaryLights);
            devices.addAll(dimmerLights);
            devices.addAll(presenceSensors);
            devices.addAll(motionSensors);
            devices.addAll(pushButtons);
            devices.addAll(photometers);
            devices.addAll(thermometers);
            devices.addAll(coolers);
            devices.addAll(heaters);

            for (ApplicationFreshnessDemand app : appDemands) {

                for (GenericDevice d : devices) {
                    String serialNumber = d.getSerialNumber();
                    if (serialNumber == null)
                        continue;

                    Class dClass = null;
                    if (d instanceof Heater)
                        dClass = Heater.class;
                    if (d instanceof Cooler)
                        dClass = Cooler.class;
                    if (d instanceof Thermometer)
                        dClass = Thermometer.class;
                    if (d instanceof BinaryLight)
                        dClass = BinaryLight.class;
                    if (d instanceof DimmerLight)
                        dClass = DimmerLight.class;
                    if (d instanceof PresenceSensor)
                        dClass = PresenceSensor.class;
                    if (d instanceof MotionSensor)
                        dClass = MotionSensor.class;
                    if (d instanceof Photometer)
                        dClass = Photometer.class;
                    if (d instanceof PushButton)
                        dClass = PushButton.class;

                    ServiceReference<?>[] serviceReferences = new ServiceReference<?>[0];
                    try {
                        //available properties: [batteryobservable.batteryLevel, context.entity.id, factory.context.entity.spec, factory.name, genericdevice.device.serialNumber, instance.name, locatedobject.object.position.x, locatedobject.object.position.y, locatedobject.object.zone, objectClass, service.bundleid, service.id, service.scope, simulateddevice.simulated.device.type]
                        serviceReferences = _context.getServiceReferences(dClass.getCanonicalName(), "(genericdevice.device.serialNumber=" + serialNumber + ")");
                    } catch (InvalidSyntaxException e) {
                        e.printStackTrace();
                    }
                    if (serviceReferences == null)
                        continue;

                    ServiceReference scr = serviceReferences[0];

                    //in case there were the freshness annotation
                    //the relation is all declared: app-device-freshness
                    boolean used = false;
                    for (Bundle b : scr.getUsingBundles()) {
                        if (app.hasBundle(b.getSymbolicName())) {
                            Map<String, Freshness> appFreshnessMap = null;
                            for (String appBund : app.getAppBundles()) {
                                if (getAllApplicationDemandsMapped().get(appBund) != null)
                                    appFreshnessMap = getAllApplicationDemandsMapped().get(appBund);
                            }

                            if (appFreshnessMap != null) {
                                Freshness deviceFreshness = appFreshnessMap.get(dClass.getCanonicalName());
                                if (deviceFreshness != null) {
                                    //its a relation between device and app and the annotation is available
                                    DeviceFreshnessDemand deviceDemand = new DeviceFreshnessDemand(dClass.getCanonicalName(),
                                            scr.getProperty("genericdevice.device.serialNumber").toString(),
                                            scr.getProperty("locatedobject.object.zone").toString(),
                                            deviceFreshness);

                                    addDeviceDemandToList(deviceDemand);
                                    app.addDeviceDemand(deviceDemand);
                                    used = true;
                                    break;
                                }
                            }

                            //its a relation between device and app, but has no annotations, freshness is 0
                            DeviceFreshnessDemand deviceDemand = new DeviceFreshnessDemand(dClass.getCanonicalName(),
                                    scr.getProperty("genericdevice.device.serialNumber").toString(),
                                    scr.getProperty("locatedobject.object.zone").toString(),
                                    new Freshness(0l, TimeUnit.SECONDS));

                            addDeviceDemandToList(deviceDemand);
                            app.addDeviceDemand(deviceDemand);
                            used = true;
                            break;
                        }
                    }
                    //device is not used by any application
                    if (!used) {
                        addDeviceDemandToList(new DeviceFreshnessDemand(dClass.getCanonicalName(),
                                scr.getProperty("genericdevice.device.serialNumber").toString(),
                                scr.getProperty("locatedobject.object.zone").toString(),
                                null));
                    }
                }
            }
        }
    }

    private void addDeviceDemandToList(DeviceFreshnessDemand deviceDemand) {
        if (!deviceDemands.contains(deviceDemand)) {
            deviceDemands.add(deviceDemand);
        } else {
            //if already exists a device demand, updates with the most critical freshness
            DeviceFreshnessDemand freshnessDemand = deviceDemands.get(deviceDemands.indexOf(deviceDemand));
            if (freshnessDemand.getFreshness() == null)
                freshnessDemand.setFreshness(deviceDemand.getFreshness());
            else if (deviceDemand.getFreshness() == null)
                return;
            else {
                if (freshnessDemand.getFreshness().compareTo(deviceDemand.getFreshness()) > 0) {
                    freshnessDemand.setFreshness(deviceDemand.getFreshness());
                }
            }
        }
    }

    private List<ApplicationFreshnessDemand> handlerToContextDemands() {

        List<ApplicationFreshnessDemand> appDevicesDemands = new ArrayList<>();
        for (String appBundle : getAllApplicationDemandsMapped().keySet()) {

            Application application = manager.getApplicationOfBundle(appBundle);
            ApplicationFreshnessDemand appDemand = null;
            if (application == null)
                appDemand = new ApplicationFreshnessDemand(appBundle);
            else appDemand = new ApplicationFreshnessDemand(application.getBundles());

            if (appDevicesDemands.contains(appDemand))
                appDemand = appDevicesDemands.get(appDevicesDemands.indexOf(appDemand));

            for (String device : getAllApplicationDemandsMapped().get(appBundle).keySet()) {
                //check if the application is indeed using the device
                try {

                    ServiceReference<?>[] serviceReferences = _context.getServiceReferences(device, "(objectClass=*)");
                    if (serviceReferences == null)
                        continue;

                    //it gives all the services (devices) which implements a class
                    for (ServiceReference scr : serviceReferences) {

                        //it gives all the applications which uses the devices
                        boolean uses = false;
                        for (Bundle b : scr.getUsingBundles()) {
                            if (b.getSymbolicName().equals(appBundle)) {
                                uses = true;
                                break;
                            }
                        }
                        //there is no device from this type injected into the application
                        if (!uses)
                            continue;

                        //it is just for device proxies
                        //it does not work for presenceService for example, because it is not a genericDevice, is a grouped class of devices (presenceSensor)
                        if (scr.getProperty("genericdevice.device.serialNumber") == null)
                            continue;

                        getAllApplicationDemandsMapped().get(appBundle).get(device);

                        //available properties: [batteryobservable.batteryLevel, context.entity.id, factory.context.entity.spec, factory.name, genericdevice.device.serialNumber, instance.name, locatedobject.object.position.x, locatedobject.object.position.y, locatedobject.object.zone, objectClass, service.bundleid, service.id, service.scope, simulateddevice.simulated.device.type]
                        DeviceFreshnessDemand deviceDemand = new DeviceFreshnessDemand(device,
                                scr.getProperty("genericdevice.device.serialNumber").toString(),
                                scr.getProperty("locatedobject.object.zone").toString(),
                                getAllApplicationDemandsMapped().get(appBundle).get(device));

                        appDemand.addDeviceDemand(deviceDemand);
                    }
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }
            }
            appDevicesDemands.add(appDemand);
        }


        //getting applications which has no freshness annotations
        //and removing apps that are not active
        for (Application application : manager.getApplications()) {

            //TODO the state of an app is not working
//            if (application.getState().equals(ApplicationState.STOPED)) {
//                System.out.println("removed due to state: " + application.getName() + application.getState());
//                appDevicesDemands.remove(new ApplicationFreshnessDemand(application.getId()));
//                continue;
//            }

            ApplicationFreshnessDemand appDemand = new ApplicationFreshnessDemand(application.getBundles());
            if (!appDevicesDemands.contains(appDemand)) {
                appDevicesDemands.add(appDemand);
            }
        }

        return appDevicesDemands;
    }

    //not tested
    @Override
    public ApplicationFreshnessDemand getApplicationDemand(String appIdOrBundle) {
//        if (appDemands.isEmpty())
            computeDemands();

        for (ApplicationFreshnessDemand appDemand : appDemands) {
            if (appDemand.hasBundle(appIdOrBundle))
                return appDemand;
        }
        return null;
    }

    @Override
    public List<ApplicationFreshnessDemand> getApplicationDemands() {
        computeDemands();
        return appDemands;
    }

    @Override
    public DeviceFreshnessDemand getDeviceDemand(String deviceSerialNumber) {
//        if (deviceDemands.isEmpty())
            computeDemands();

        for (DeviceFreshnessDemand deviceDemand : deviceDemands) {
            if (deviceDemand.getDeviceSerial().equals(deviceSerialNumber)) return deviceDemand;
        }
        return null;
    }

    @Override
    public List<DeviceFreshnessDemand> getDeviceDemands() {
        computeDemands();
        return deviceDemands;
    }

    //not working state
    //also additional bundles are not always conveyed as applications
//    private List<Application> getActiveApplications() {
//        //identify when an application is activate/deactivated
//        List<Application> activeApplications = manager.getApplications().stream()
//                .filter(app -> app.getState().equals(ApplicationState.STARTED))
//                .collect(Collectors.toList());
//
//        //TODO not returning the active applications
//        return manager.getApplications();
//    }

    @Override
    public Map<String, Map<String, Freshness>> getAllApplicationDemandsMapped() {
        return FreshnessMapping.applicationToDevices;
    }

//    @Override
//    public long getPeriod() {
//        return 1;
//    }
//
//    @Override
//    public TimeUnit getUnit() {
//        return TimeUnit.MINUTES;
//    }
//
//    /**
//     * From time to time, check whether an application was enabled or disabled
//     * TODO: a better solution would be tracking application status changes in an event listener
//     */
//    @Override
//    public void run() {
//        System.out.println("Caching appsToDevices: verifying application status");
//        computeDemands();
//    }

    /*
     * Listeners to important changes in the context
     */


    @Override
    public void addApplication(Application app) {
        System.out.println("Freshness appsToDevices: add application - " + app.getName() + " size: " + manager.getApplications().size());
        computeDemands();
    }

    @Override
    public void removeApplication(Application app) {
//        System.out.println("Caching appsToDevices: rem application - " + app.getName() + " size: " + manager.getApplications().size());

        for (Bundle bundle : app.getBundles()) {
            cleanBundleMapped(bundle.getSymbolicName());
        }
        computeDemands();
    }

    @Override
    public void deploymentPackageAdded(Application app, String symbolicName) {
//        System.out.println("Freshness appsToDevices: add package - " + app.getName() + " size: " + manager.getApplications().size());
//        computeDemands();
    }

    @Override
    public void deploymentPackageRemoved(Application app, String symbolicName) {
//        cleanBundleMapped(symbolicName);
//        computeDemands();
    }

    @Override
    public void bundleAdded(Application app, String symbolicName) {
//        System.out.println("Freshness appsToDevices: add bundle - " + app.getName() + " size: " + manager.getApplications().size());
//        computeDemands();
    }

    @Override
    public void bundleRemoved(Application app, String symbolicName) {
//        cleanBundleMapped(symbolicName);
//        computeDemands();
    }

    private void cleanBundleMapped(String bundleSymbolicName) {
        FreshnessMapping.applicationToDevices.remove(bundleSymbolicName);
    }
}
