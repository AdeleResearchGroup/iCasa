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
package fr.liglab.adele.icasa.simulator;

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.Variable;

import java.util.Set;

/**
 *  A service providing this service contract will be used to compute simulated physical properties.
 *
 */
public interface PhysicalModel {

    /**
     * Returns all zone variables computed by this physical model.
     * The variable set must be static.
     *
     * @return all zone variables computed by this physical model.
     */
    Set<Variable> getComputedZoneVariables();

    /**
     * Returns all zone variables that this physical model relies on.
     * If one of these variables is not available on a zone, the zone variable computation may not be performed.
     *
     * @return all zone variables that this physical model relies on.
     */
    Set<Variable> getRequiredZoneVariables();

    /**
     * Returns all devices used by it to perform variable computation.
     *
     * @return all devices used by it to perform variable computation.
     */
    Set<LocatedDevice> getUsedDevices();
}
