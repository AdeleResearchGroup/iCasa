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


import fr.liglab.adele.freshness.utils.Freshness;

public class DeviceFreshnessDemand {
    private String deviceClass;

    private String deviceSerial;

    private String deviceZone;

    private Freshness freshness;

    public Freshness getFreshness() {
        return freshness;
    }

    public void setFreshness(Freshness freshness) {
        this.freshness = freshness;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getDeviceZone() {
        return deviceZone;
    }

    public void setDeviceZone(String deviceZone) {
        this.deviceZone = deviceZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceFreshnessDemand)) return false;

        DeviceFreshnessDemand that = (DeviceFreshnessDemand) o;

        if (deviceClass != null ? !deviceClass.equals(that.deviceClass) : that.deviceClass != null) return false;
        if (deviceSerial != null ? !deviceSerial.equals(that.deviceSerial) : that.deviceSerial != null) return false;
        if (deviceZone != null ? !deviceZone.equals(that.deviceZone) : that.deviceZone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceClass != null ? deviceClass.hashCode() : 0;
        result = 31 * result + (deviceSerial != null ? deviceSerial.hashCode() : 0);
        result = 31 * result + (deviceZone != null ? deviceZone.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeviceFreshnessDemand{" +
                "deviceClass='" + deviceClass + '\'' +
                ", deviceSerial='" + deviceSerial + '\'' +
                ", deviceZone='" + deviceZone + '\'' +
                ", freshness=" + freshness +
                '}';
    }

    public DeviceFreshnessDemand(String deviceClass, String deviceSerial, String deviceZone, Freshness freshness) {
        this.deviceClass = deviceClass;
        this.deviceSerial = deviceSerial;
        this.deviceZone = deviceZone;
        this.freshness = freshness;
    }
}
