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

import java.util.ArrayList;
import java.util.List;

public class ApplicationFreshnessDemand {

    private String appBundle;

    public String getAppBundle() {
        return appBundle;
    }

    public void setAppBundle(String appBundle) {
        this.appBundle = appBundle;
    }

    private List<DeviceFreshnessDemand> devices;

    public List<DeviceFreshnessDemand> getDevices() {
        return devices;
    }

    public ApplicationFreshnessDemand(String appBundle, List<DeviceFreshnessDemand> devices) {
        this.appBundle = appBundle;
        this.devices = devices;
    }

    public ApplicationFreshnessDemand(String appBundle) {
        this.appBundle = appBundle;
        this.devices = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationFreshnessDemand)) return false;

        ApplicationFreshnessDemand that = (ApplicationFreshnessDemand) o;

        if (appBundle != null ? !appBundle.equals(that.appBundle) : that.appBundle != null) return false;
        if (devices != null ? !devices.equals(that.devices) : that.devices != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appBundle != null ? appBundle.hashCode() : 0;
        result = 31 * result + (devices != null ? devices.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationFreshnessDemand{" +
                "appBundle='" + appBundle + '\'' +
                ", devices=" + devices +
                '}';
    }

    public void addDeviceDemand(DeviceFreshnessDemand demand) {
        devices.add(demand);
    }
}
