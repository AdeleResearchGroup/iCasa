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
 * TEMP
 * API de contexte, services pouvant être fournis aux applications
 */
public enum ContextAPI {
    BinaryLight("fr.liglab.adele.icasa.device.light.BinaryLight"),
    ColorLight("fr.liglab.adele.icasa.device.light.ColorLight"),
    DimmerLight("fr.liglab.adele.icasa.device.light.DimmerLight"),
    Photometer("fr.liglab.adele.icasa.device.light.Photometer"),
    PresenceService("fr.liglab.adele.icasa.physical.abstraction.PresenceService"),
    MomentOfTheDay("fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay"),
    MultiwaySwitch("fr.liglab.adele.icasa.physical.abstraction.MultiwaySwitch"),

    IOPController("fr.liglab.adele.iop.device.api.IOPController");

    private String interfaceName;

    private ContextAPI(String s) {
        interfaceName = s;
    }

    public static ContextAPI containsInterface(String interfaceName){
        for (ContextAPI contextAPI : ContextAPI.values()){
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
}
