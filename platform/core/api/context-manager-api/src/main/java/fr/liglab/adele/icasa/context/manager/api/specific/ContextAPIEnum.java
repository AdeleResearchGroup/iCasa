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
package fr.liglab.adele.icasa.context.manager.api.specific;

/**
 * Context API, services that can be provided to applications
 * Can be generated from the different existing context entities in the distribution
 */
public enum ContextAPIEnum {
    BinaryLight("fr.liglab.adele.icasa.device.light.BinaryLight"),
    ColorLight("fr.liglab.adele.icasa.device.light.ColorLight"),
    DimmerLight("fr.liglab.adele.icasa.device.light.DimmerLight"),
    Photometer("fr.liglab.adele.icasa.device.light.Photometer"),
    PresenceService("fr.liglab.adele.icasa.physical.abstraction.PresenceService"),
    MomentOfTheDay("fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay"),
    MultiwaySwitch("fr.liglab.adele.icasa.physical.abstraction.MultiwaySwitch"),

    IOPController("fr.liglab.adele.iop.device.api.IOPController");

    private String interfaceName;

    private ContextAPIEnum(String s) {
        interfaceName = s;
    }

    public static ContextAPIEnum containsInterface(String interfaceName){
        for (ContextAPIEnum contextAPI : ContextAPIEnum.values()){
            if(contextAPI.equalsInterface(interfaceName)){
                return contextAPI;
            }
        }
        return null;
    }

    public String getInterfaceName(){
        return interfaceName;
    }

    public boolean equalsInterface(String otherInterface) {
        return interfaceName.equals(otherInterface);
    }

    @Override
    public String toString() {
        return "CtxtAPI{" + interfaceName + '}';
    }
}