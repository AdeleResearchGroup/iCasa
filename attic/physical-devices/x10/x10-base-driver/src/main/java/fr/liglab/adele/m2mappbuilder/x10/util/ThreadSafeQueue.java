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
package fr.liglab.adele.m2mappbuilder.x10.util;
import java.util.LinkedList;


/** ThreadSafeQueue this is an implementation of a First In First Out (FIFO)
* data structure.  This class uses synchronization to provide thread safety
* for adding and removing objects to and from the queue.  All of the methods
* in this class are synchronized, therefore temporary thread blocking might
* occur when calling any of these methods.
*
*
*/

public class ThreadSafeQueue
{
    
    
    /** queue LinkedList the list of objects in the queue
    *
    */
    
    LinkedList queue;
    
    
    /** ThreadSafeQueue constructs an empty ThreadSafeQueue
    *
    *
    */
    
    public ThreadSafeQueue()
    {
        queue = new LinkedList();
    }
    
    
    /** enqueue adds the specified object to the end of the queue
    *
    * @param element the object to be added to the end of the queue
    *
    */
    
    public synchronized void enqueue(Object element)
    {
        queue.add(element);
        try
        {
            notifyAll();
        }
        catch(IllegalMonitorStateException imse)
        {
        }
    }
    
    
    /** dequeueNextAvailable blocks until the next object becomes available
    * to the queue.  If the queue is not empty, the first object is removed
    * from the queue and returned without blocking.
    *
    * @return Object - the next available object in the queue.
    * @exception InterruptedException if the blocked thread is interrupted
    * before an object becomes available.
    *
    */
    
    public synchronized Object dequeueNextAvailable() throws InterruptedException
    {
        while(queue.size() < 1)
        {
            wait();
        }
        Object element = queue.getFirst();
        queue.removeFirst();
        return(element);
    }
    
    
    /** dequeue removes and returns the first object in the queue without
    * blocking.  If the queue is empty, null is returned.
    *
    * @return Object - the next object in the queue or null if the queue is
    * empty.
    *
    */
    
    public synchronized Object dequeue()
    {
        if(queue.size() < 1)
        {
            return(null);
        }
        else
        {
            Object element = queue.getFirst();
            queue.removeFirst();
            return(element);
        }
    }
    
    
    /** peek returns the next available object in the queue without
    * physically removing the object from the queue.
    *
    * @return Object the next available object in the queue or null
    * if the queue is empty.
    *
    */
    
    public synchronized Object peek()
    {
        if(queue.size() < 1)
        {
            return(null);
        }
        else
        {
            return(queue.getFirst());
        }
    }
    
    
    /** dequeue removes the specified object from the queue
    * if and only if the specified object is the first object
    * in the queue.
    *
    * @param toDequeue the object to dequeue
    *
    */
    
    public synchronized void dequeue(Object toDequeue)
    {
        if(queue.size() > 0)
        {
            if(queue.getFirst() == toDequeue)
            {
                queue.removeFirst();
            }
        }
    }
    
    
    /** empty completely removes all objects that are currently in the
    * queue.
    *
    *
    */
    
    public synchronized void empty()
    {
        while(dequeue() != null);
    }
}