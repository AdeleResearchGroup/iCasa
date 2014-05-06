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
package fr.liglab.adele.m2mappbuilder.x10;


/** UnitListener is the interface implemented by objects that wish
* to recieve UnitEvents.  This interface is typically implemented
* by software-based x10 modules.  A registered UnitListener will
* recieve ALL events for ALL housecodes and unitcodes.  It is up
* to the implementing class to filter out the events for specific
* housecodes and unitcodes if desired.
*
*
*/

public interface UnitListener
{
    
    
    /** allUnitsOff is called when an x10 All Units Off event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void allUnitsOff(UnitEvent event);
    
    
    /** allLightsOff is called when an x10 All Lights Off event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void allLightsOff(UnitEvent event);
    
    
    /** allLightsOn is called when an x10 All Lights On event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void allLightsOn(UnitEvent event);
    
    
    /** unitOn is called when an x10 On event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void unitOn(UnitEvent event);
    
    
    /** unitOff is called when an x10 Off event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void unitOff(UnitEvent event);
    
    
    /** unitDim is called when an x10 Dim event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void unitDim(UnitEvent event);
    
    
    /** unitBright is called when an x10 Bright event occurs.
    *
    * @param event the UnitEvent that is dispatched.
    *
    */
    
    public void unitBright(UnitEvent event);
}