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
package fr.liglab.adele.icasa.application;

import org.osgi.framework.Bundle;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationFreshnessDemand {

    private Set<String> appBundles;

    public ApplicationFreshnessDemand(Set<Bundle> bundles) {
        appBundles = new HashSet<>();
        appBundles.addAll(bundles.stream().map(Bundle::getSymbolicName).collect(Collectors.toList()));
        this.devices = new HashSet<>();
    }

    public ApplicationFreshnessDemand(String bundle) {
        appBundles = new HashSet<>();
        appBundles.add(bundle);
        this.devices = new HashSet<>();
    }

    public ApplicationFreshnessDemand(Set<String> appBundles, Set<DeviceFreshnessDemand> devices) {
        this.appBundles = appBundles;
        this.devices = devices;
    }

    public Set<String> getAppBundles() {
        return appBundles;
    }

    public void setAppBundles(Set<String> appBundles) {
        this.appBundles = appBundles;
    }

    private Set<DeviceFreshnessDemand> devices;

    public Set<DeviceFreshnessDemand> getDevices() {
        return devices;
    }

    public boolean hasBundle(String bundle){
        for(String appBundle : appBundles){
            if(appBundle.equals(bundle))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationFreshnessDemand)) return false;

        ApplicationFreshnessDemand that = (ApplicationFreshnessDemand) o;

        if (appBundles != null ? !appBundles.equals(that.appBundles) : that.appBundles != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return appBundles != null ? appBundles.hashCode() : 0;
    }

    public void addDeviceDemand(DeviceFreshnessDemand demand) {
        devices.add(demand);
    }
}
