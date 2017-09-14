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
package fr.liglab.adele.icasa.context.manager.impl.models.data;

import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.security.Camera;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ExternalInteractionAuthorization {
    public static final Set<String> SERVICES_AUTHORIZED_FOR_IMPORTATION = new HashSet<>(Arrays.asList(
            PresenceSensor.class.getName(),
            Thermometer.class.getName(),
            Photometer.class.getName(),
            Camera.class.getName()
    ));

    public static final Set<String> SERVICES_AUTHORIZED_FOR_EXPORTATION = new HashSet<>(Arrays.asList(
            Thermometer.class.getName(),
            Photometer.class.getName()
    ));
}