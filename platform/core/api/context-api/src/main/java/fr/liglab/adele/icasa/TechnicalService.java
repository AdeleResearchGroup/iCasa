/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa;

import fr.liglab.adele.icasa.location.LocatedDevice;

import java.util.Set;

/**
 *
 */
public interface TechnicalService {

    /**
     * Returns all zone variables computed by this technical service.
     * The variable set must be static.
     *
     * @return all zone variables computed by this technical service.
     */
    Set<Variable> getComputedZoneVariables();

    /**
     * Returns all zone variables that this technical service relies on.
     * If one of these variables is not available on a zone, the zone variable computation may not be performed.
     *
     * @return all zone variables that this technical service relies on.
     */
    Set<Variable> getRequiredZoneVariables();

    /**
     * Returns all global variables that this technical service relies on.
     * If one of these variables is not available, the zone variable computation may not be performed.
     *
     * @return all zone variables that this technical service relies on.
     */
    Set<Variable> getRequiredGlobalVariables();

    /**
     * Returns all global variables computed by this technical service.
     * The variable set must be static.
     *
     * @return all global variables computed by this technical service.
     */
    Set<Variable> getComputedGlobalVariables();

    /**
     * Returns all devices used by it to perform variable computation.
     *
     * @return all devices used by it to perform variable computation.
     */
    Set<LocatedDevice> getUsedDevices();
}
