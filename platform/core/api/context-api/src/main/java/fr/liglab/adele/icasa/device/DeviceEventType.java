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
package fr.liglab.adele.icasa.device;

/**
 * This enum represents the type of the event
 *
 */
public enum DeviceEventType {

    ADDED("added"), REMOVED("removed"), PROP_ADDED("prop-added"), PROP_REMOVED("prop-removed"), PROP_MODIFIED("prop-modified"), DEVICE_EVENT("device-event");

    private String _eventTypeStr;

    private DeviceEventType(String eventTypeStr) {
        _eventTypeStr = eventTypeStr;
    }

    public String toString() {
        return _eventTypeStr;
    }
}
