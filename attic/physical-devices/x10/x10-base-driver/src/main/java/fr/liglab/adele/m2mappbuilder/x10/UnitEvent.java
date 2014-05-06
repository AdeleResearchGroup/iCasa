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

import java.io.Serializable;


/** UnitEvent an event representing a change in state of an
* x10 hardware or software module.
*
*
*/

public class UnitEvent implements Serializable
{
    
    
    private Command command;
    
    
    /** UnitEvent constructs a UnitEvent based on the specified
    * Command.
    *
    * @param command the command that generated this event.
    *
    */
    
    public UnitEvent(Command command)
    {
        this.command = command;
    }
    
    
    /** getCommand returns the command that generated this event.
    *
    * @return Command the Command that generated this event.
    *
    */
    
    public Command getCommand()
    {
        return(command);
    }
}