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
package fr.liglab.adele.icasa.context.manager.impl.temp.specific;

/**
 * Created by Eva on 31/01/2017.
 */
public enum AbstractContextEntities {
    PresenceServiceImpl("fr.liglab.adele.icasa.physical.abstraction.impl.PresenceServiceImpl"),
    MomentOfTheDaySimulatedImpl("fr.liglab.adele.icasa.simulator.model.day.part.MomentOfTheDaySimulatedImpl"),
    ZoneImpl("fr.liglab.adele.icasa.location.impl.ZoneImpl");

    private String implemName;

    private AbstractContextEntities(String s) {
        implemName = s;
    }

    public static AbstractContextEntities containsImplem(String implemName){
        for (AbstractContextEntities abstractContextEntities : AbstractContextEntities.values()){
            if(abstractContextEntities.equalsImplem(implemName)){
                return abstractContextEntities;
            }
        }
        return null;
    }

    public boolean equalsImplem(String otherImplemName) {
        return implemName.equals(otherImplemName);
    }
}
