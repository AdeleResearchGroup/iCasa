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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import fr.liglab.adele.m2mappbuilder.x10.util.ThreadSafeQueue;
import fr.liglab.adele.m2mappbuilder.x10.util.LogHandler;


/** UnitEventDispatcher is a threaded dispatcher that calls each
* registered UnitListener when an event comes in.  This object
* is single-threaded, so all UnitListeners are notified by the
* same thread.  This is important to note so that UnitListeners
* do not "hog" the thread.  UnitListeners should quickly return
* from handling events as to not delay subsequent listeners
* from receiving the event. <BR><BR>
*
* This class can be reused by other Controller implementations.
 *
*/

public class UnitEventDispatcher extends Thread
{
    
    private LinkedList listeners;
    private ThreadSafeQueue eventQueue;
    private boolean running;
    private static final UnitEvent STOP = new UnitEvent(null);
    
    
    /** UnitEventDispatcher constructs an unstarted dispatcher.
    *
    *
    */
    
    public UnitEventDispatcher()
    {
        listeners = new LinkedList();
        eventQueue = new ThreadSafeQueue();
    }
    
    
    /** run blocks until an event comes in, and then dispatches it to
    * all registered listeners.
    *
    *
    */
    
    public void run()
    {
        running = true;
        while(running)
        {
            try
            {
                UnitEvent nextEvent = (UnitEvent) eventQueue.dequeueNextAvailable();
                if(nextEvent != STOP)
                {
                	List currentListeners = null;
                    synchronized (listeners) {
                    	currentListeners = new ArrayList(listeners);
                    }
                	ListIterator iterator = currentListeners.listIterator(0);
                    while(iterator.hasNext())
                    {
                        UnitListener nextListener = (UnitListener) iterator.next();
                        switch(nextEvent.getCommand().getFunctionByte())
                        {
                            case Command.ALL_UNITS_OFF : nextListener.allUnitsOff(nextEvent);
                            break;
                            case Command.ALL_LIGHTS_ON : nextListener.allLightsOn(nextEvent);
                            break;
                            case Command.ALL_LIGHTS_OFF : nextListener.allLightsOff(nextEvent);
                            break;
                            case Command.DIM : nextListener.unitDim(nextEvent);
                            break;
                            case Command.BRIGHT : nextListener.unitBright(nextEvent);
                            break;
                            case Command.ON : nextListener.unitOn(nextEvent);
                            break;
                            case Command.OFF : nextListener.unitOff(nextEvent);
                            break;
                        }
                    }
                }
            }
            catch(InterruptedException ie)
            {
                LogHandler.logException(ie, 1);
            }
        }
    }
    
    
    /** dispatchUnitEvent adds the specified UnitEvent to the queue
    * of events to be dispatched.
    *
    * @param event the UnitEvent to be dispatched
    *
    */
    
    public void dispatchUnitEvent(UnitEvent event)
    {
        eventQueue.enqueue(event);
    }
    
    
    /** addUnitListener adds the specified UnitListener to the list
    * of listeners that are notified by the dispatcher.
    *
    * @param listener the listener to add.
    *
    */
    
    public void addUnitListener(UnitListener listener)
    {
        if(!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }
    
    
    /** removeUnitListener removes the specified listener.
    * This cuts the specified listener off from receiving
    * any subsequent events.
    *
    * @param listener the listener to be removed.
    *
    */
    
    public void removeUnitListener(UnitListener listener)
    {
        listeners.remove(listener);
    }
    
    
    /** kill terminates the dispatcher thread.  This
    * typically occurs when the system is shutting down.
    *
    *
    */
    
    public void kill()
    {
        running = false;
        dispatchUnitEvent(STOP);
    }
}