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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "Caching-Demand-Impl")
@Provides
@Instantiate(name = "Caching-Demand-Impl-0")
public class FreshnessTrackerImpl implements FreshnessTracker, ApplicationTracker, PeriodicRunnable {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".caching");

    @Requires
    ApplicationManager manager;

    private final Object m_lock = new Object();

    public FreshnessTrackerImpl() {
        System.out.println("Caching demands: verifying application status");
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
        System.out.println("Caching demands: add application - " + app.getName() + " size: " + manager.getApplications().size());
        computeDemands(manager.getApplications());
    }

    //listener que importa
    @Override
    public void removeApplication(Application app) {
        System.out.println("Caching demands: rem application - " + app.getName() + " size: " + manager.getApplications().size());
        computeDemands(manager.getApplications());
    }

    @Override
    public void computeDemands(List<Application> applications) {

        //identify when an application is activate/deactivated
        List<Application> activeApplications = applications.stream()
                .filter(app -> app.getState().equals(ApplicationState.STARTED))
                .collect(Collectors.toList());

        System.out.println(FreshnessMapping.applicationToDevices);

    }

    @Override
    public Map<String, Map<String, Long>> demands() {
        return FreshnessMapping.applicationToDevices;
    }

    @Override
    public long getPeriod() {
        return 10;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.SECONDS;
    }

    /**
     * From time to time, check whether an application was enabled or disabled
     * TODO: a better solution would be tracking application status changes in an event listener
     */
    @Override
    public void run() {
        System.out.println("Caching demands: verifying application status");
        computeDemands(manager.getApplications());
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
