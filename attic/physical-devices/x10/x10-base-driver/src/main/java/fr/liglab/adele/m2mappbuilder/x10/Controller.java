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


/** Controller is implemented by any class that can act as an entry
* point for controlling x10 devices.  A Controller must be able to
* distribute added Commands to ALL registered x10 hardware and software
* modules.  A Controller must also handle the addition and removal of
* UnitListeners.
*
*
*/

public interface Controller
{
    
    
    /** addUnitListener registers the specified UnitListener to recieve
    * ALL events that occur, whether initiated by hardware or software
    * control modules.
    *
    * @param listener the object to recieve UnitEvent objects.
    * @see x10.UnitEvent
    */
    
    public void addUnitListener(UnitListener listener);
    
    
    /** removeUnitListener unregisters the specified UnitListener.
    * If the specified UnitListener isn't registered, then it is
    * ignored.
    *
    * @param listener the listener to unregister.
    *
    */
    
    public void removeUnitListener(UnitListener listener);
    
    
    /** addCommand adds a Command to the queue to be dispatched to
    * all hardware and software x10 modules.
    *
    * @param command the Command to be queued.
    *
    */
    
    public void addCommand(Command command);
}