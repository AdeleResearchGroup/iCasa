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

/**
 * TEMP
 * Query RoSe -> proxy
 * -> Exprimer des besoins en services ?
 */
public enum EnvProxy {
    /*Simulated Environment*/
    SimulatedBinaryLightImpl("fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedBinaryLightImpl"),
    SimulatedDimmerLightImpl("fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedDimmerLightImpl"),
    SimulatedPhotometerImpl("fr.liglab.adele.icasa.simulator.device.light.impl.SimulatedPhotometerImpl"),
    SimulatedPresenceSensorImpl("fr.liglab.adele.icasa.simulator.device.presence.impl.SimulatedPresenceSensorImpl");

    private String implemName;

    private EnvProxy(String s) {
        implemName = s;
    }

    public static EnvProxy containsImplem(String implemName){
        for (EnvProxy envProxy : EnvProxy.values()){
            if(envProxy.equalsImplem(implemName)){
                return envProxy;
            }
        }
        return null;
    }

    public boolean equalsImplem(String otherImplemName) {
        return implemName.equals(otherImplemName);
    }
}
