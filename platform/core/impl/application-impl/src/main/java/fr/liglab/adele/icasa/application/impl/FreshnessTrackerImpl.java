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

import fr.liglab.adele.freshness.utils.FreshnessMapping;
import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.application.*;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component(name = "Caching-Demand-Impl")
@Provides
@Instantiate(name = "Caching-Demand-Impl-0")
public class FreshnessTrackerImpl implements FreshnessTracker, ApplicationTracker, PeriodicRunnable {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".caching");

    @Requires
    ApplicationManager manager;

    private final Object m_lock = new Object();

    private Map<String, Long> demands;

    public FreshnessTrackerImpl() {
        System.out.println("Caching appsToDevices: verifying application status");

        demands = new HashMap<>();
        for (Application app : manager.getApplications()) {
            System.out.println(app.getName() + ":" + app.getState());
        }
    }

    /**
     * Component Lifecycle Method
     */
    @Invalidate
    public void stop() {
        System.out.println("Component cachingDemands is stopping... " + manager.getApplications().size());
    }

    /**
     * Component Lifecycle Method
     */
    @Validate
    public void start() {
        System.out.println("Component cachingDemands is starting..." + manager.getApplications().size());
        manager.addApplicationListener(this);
    }

    //listener que importa
    @Override
    public void addApplication(Application app) {
        System.out.println("Caching appsToDevices: add application - " + app.getName() + " size: " + manager.getApplications().size());
        computeDemands();
    }

    //listener que importa
    @Override
    public void removeApplication(Application app) {
        System.out.println("Caching appsToDevices: rem application - " + app.getName() + " size: " + manager.getApplications().size());

        FreshnessMapping.applicationToDevices.keySet()
                .stream().filter(bundleApp -> manager.getApplicationOfBundle(bundleApp).getId().equals(app.getId()))
                .forEach(FreshnessMapping.applicationToDevices::remove);

        computeDemands();
    }

    private void computeDemands() {

        synchronized (m_lock) {
//TODO applications sometimes are load but not listed as applications,
// such apps are load into pom.xml and not via bundle in applications folder
//            demands = new HashMap<>();
//            for (Application app : getActiveApplications()) {
//
//                Map<String, Long> appDemands = getApplicationDemands(app.getId());
//                for (String device : appDemands.keySet()) {
//                    Long deviceDemand = appDemands.get(device);
//
//                    //if device not mapped, create an entry
//                    if (demands.get(device) == null) {
//                        demands.put(device, deviceDemand);
//                    } else {
//
//                        //if already exists a device demand, updates with the most critical freshness
//                        if (demands.get(device) > deviceDemand) {
//                            demands.put(device, deviceDemand);
//                        }
//                    }
//                }
//            }

            demands = new HashMap<>();
            for (String app : getAllApplicationDemands().keySet()) {

                Map<String, Long> appDemands = getApplicationDemands(app);
                for (String device : appDemands.keySet()) {
                    Long deviceDemand = appDemands.get(device);

                    //if device not mapped, create an entry
                    if (demands.get(device) == null) {
                        demands.put(device, deviceDemand);
                    } else {

                        //if already exists a device demand, updates with the most critical freshness
                        if (demands.get(device) > deviceDemand) {
                            demands.put(device, deviceDemand);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Long> getDemands() {
        computeDemands();
        return demands;
    }


    //not tested
    private List<Application> getActiveApplications() {
        //identify when an application is activate/deactivated
        List<Application> activeApplications = manager.getApplications().stream()
                .filter(app -> app.getState().equals(ApplicationState.STARTED))
                .collect(Collectors.toList());

        //TODO not return the active applicaitons
        return manager.getApplications();
    }


    //not tested
    @Override
    public Map<String, Long> getApplicationDemands(String appIdOrBundle) {

        //searching for bundle
        if (FreshnessMapping.applicationToDevices.get(appIdOrBundle) != null)
            return FreshnessMapping.applicationToDevices.get(appIdOrBundle);

        //searching for app id
        for (String bundleApp : FreshnessMapping.applicationToDevices.keySet()) {
            if (manager.getApplicationOfBundle(bundleApp).getId().equals(appIdOrBundle)) {
                return FreshnessMapping.applicationToDevices.get(bundleApp);
            }
        }
        return null;
    }

    public Map<String, Map<String, Long>> getAllApplicationDemands() {
        return FreshnessMapping.applicationToDevices;
    }

    @Override
    public long getPeriod() {
        return 10;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.MINUTES;
    }

    /**
     * From time to time, check whether an application was enabled or disabled
     * TODO: a better solution would be tracking application status changes in an event listener
     */
    @Override
    public void run() {
        System.out.println("Caching appsToDevices: verifying application status");
        computeDemands();
    }

    @Override
    public void deploymentPackageAdded(Application app, String symbolicName) {

    }

    @Override
    public void deploymentPackageRemoved(Application app, String symbolicName) {

    }

    @Override
    public void bundleAdded(Application app, String symbolicName) {

    }

    @Override
    public void bundleRemoved(Application app, String symbolicName) {

    }
}
