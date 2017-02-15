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
package fr.liglab.adele.icasa.context.manager.impl.specific;

import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPI;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationConfig;
import fr.liglab.adele.icasa.context.manager.impl.generic.ContextMediationSlice;

import java.util.*;

/**
 * TEMP
 */
final class ContextMediationConfigMap {
    Map<ContextAPI,ContextMediationConfig> contextMediationConfigMap;

    /*TODO : IL MANQUE DES PARTIES DU SIMULATEUR !!! (Person, PresenceSimulatorModel...)*/

    public ContextMediationConfigMap() {
        contextMediationConfigMap = new HashMap<>();
        Set<EnvProxy> envProxies;
        Set<AbstractContextEntities> abstractContextEntities;
        ContextMediationSlice ctxtMedSlice;

        /*TODO : remplacer par un fichier xml ?*/
        ContextMediationConfig ctxtMedBinaryLight = new ContextMediationConfig(ContextAPI.BinaryLight);
        envProxies = new HashSet<>();
        envProxies.add(EnvProxy.SimulatedBinaryLightImpl);
        ctxtMedSlice = new ContextMediationSlice(null, envProxies);
        ctxtMedBinaryLight.addContextMediationSlice(ctxtMedSlice);
        contextMediationConfigMap.put(ctxtMedBinaryLight.getContextAPI(), ctxtMedBinaryLight);

        ContextMediationConfig ctxtMedDimmerLight = new ContextMediationConfig(ContextAPI.DimmerLight);
        envProxies = new HashSet<>();
        envProxies.add(EnvProxy.SimulatedDimmerLightImpl);
        ctxtMedSlice = new ContextMediationSlice(null, envProxies);
        ctxtMedDimmerLight.addContextMediationSlice(ctxtMedSlice);
        contextMediationConfigMap.put(ctxtMedDimmerLight.getContextAPI(), ctxtMedDimmerLight);

        ContextMediationConfig ctxtMedPhotometer = new ContextMediationConfig(ContextAPI.Photometer);
        envProxies = new HashSet<>();
        envProxies.add(EnvProxy.SimulatedPhotometerImpl);
        ctxtMedSlice = new ContextMediationSlice(null, envProxies);
        ctxtMedPhotometer.addContextMediationSlice(ctxtMedSlice);
        contextMediationConfigMap.put(ctxtMedPhotometer.getContextAPI(), ctxtMedPhotometer);

        ContextMediationConfig ctxtMedMomentOfTheDay = new ContextMediationConfig(ContextAPI.MomentOfTheDay);
        abstractContextEntities = new HashSet<>();
        abstractContextEntities.add(AbstractContextEntities.MomentOfTheDaySimulatedImpl);
        ctxtMedSlice = new ContextMediationSlice(abstractContextEntities, null);
        ctxtMedMomentOfTheDay.addContextMediationSlice(ctxtMedSlice);
        contextMediationConfigMap.put(ctxtMedMomentOfTheDay.getContextAPI(), ctxtMedMomentOfTheDay);

        ContextMediationConfig ctxtMedPresenceService = new ContextMediationConfig(ContextAPI.PresenceService);
        abstractContextEntities = new HashSet<>();
        abstractContextEntities.add(AbstractContextEntities.PresenceServiceImpl);
        abstractContextEntities.add(AbstractContextEntities.ZoneImpl);
        envProxies = new HashSet<>();
        envProxies.add(EnvProxy.SimulatedPresenceSensorImpl);
        ctxtMedSlice = new ContextMediationSlice(abstractContextEntities, envProxies);
        ctxtMedPresenceService.addContextMediationSlice(ctxtMedSlice);
        contextMediationConfigMap.put(ctxtMedPresenceService.getContextAPI(), ctxtMedPresenceService);
    }

    public Map<ContextAPI, ContextMediationConfig> getContextMediationConfigMap() {
        return contextMediationConfigMap;
    }
}