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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import fr.liglab.adele.m2mappbuilder.x10.util.ThreadSafeQueue;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;


/** CM17ASerialController is an X10 Controller that bridges x10 hardware
* and software by communicating via a SerialPort with the x10
* "Firecracker" module.  <BR><BR>
*
* This class requires the javax.comm package from Sun Microsystems.
*
*/

public class CM17ASerialController implements Runnable, Controller
{
    
    private final long WAIT_INTERVAL = 1L;
    private final long RESET_INTERVAL = 10L;
    private final long COMMAND_INTERVAL = 1000L;
    
    private final String HEADER = "1101010110101010";
    private final String FOOTER = "10101101";
    
    private final String FIRECRACKER_MAP_FILE = "x10/cm17aCommand.map";
    
    private SerialPort sp;
    private boolean running;
    private UnitEventDispatcher dispatcher;
    private ThreadSafeQueue queue;
    private static final Command STOP = new Command("A1", Command.DIM, 0);
    private Hashtable commandTable;
    
    
    /** CM17ASerialController constructs and starts the Controller on the
    * specified comport.  On a Windows based PC, the comport is of the
    * form "COM1".
    *
    * @param comport the communications port in which the "Firecracker"
    * module is connected.
    * @exception IOException if an error occurs while trying to connect
    * to the specified Communications Port.
    *
    */
    
    public CM17ASerialController(String comport) throws IOException
    {
        try
        {
            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(comport);
            sp = (SerialPort) cpi.open("JavaX10Controller", 10000);
        }
        catch(NoSuchPortException nspe)
        {
            throw new IOException("No Such Port: " + nspe.getMessage());
        }
        catch(PortInUseException piue)
        {
            throw new IOException("Port in use: " + piue.getMessage());
        }
        commandTable = new Hashtable();
        ClassLoader loader = getClass().getClassLoader();
        InputStream commandStream = loader.getResourceAsStream(FIRECRACKER_MAP_FILE);
        if(commandStream == null)
        {
            throw new FileNotFoundException(FIRECRACKER_MAP_FILE);
        }
        else
        {
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(commandStream));
            String nextLine = commandReader.readLine();
            while(nextLine != null)
            {
                String unitCode = nextLine.substring(0, 4).trim();
                String command = nextLine.substring(4, 16).trim();
                String code = nextLine.substring(16).trim();
                
                commandTable.put(unitCode + command, code);
                nextLine = commandReader.readLine();
            }
            commandReader.close();
        }
        
        queue = new ThreadSafeQueue();
        dispatcher = new UnitEventDispatcher();
        new Thread(this).start();
    }
    
    /** addUnitListener registers the UnitListener for events.
    *
    * @param listener the listener to register for events.
    *
    */
    
    public void addUnitListener(UnitListener listener)
    {
        dispatcher.addUnitListener(listener);
    }
    
    
    /** removeUnitListener unregisters the UnitListener for events.
    *
    * @param listener the listener to remove.
    *
    */
    
    public void removeUnitListener(UnitListener listener)
    {
        dispatcher.removeUnitListener(listener);
    }
    
    /** addCommand adds a command to the queue to be dispatched.
    *
    * @param command the Command to be dispatched.
    *
    */
    
    public void addCommand(Command command)
    {
        queue.enqueue(command);
    }
    
    
    /** finalize disconnects the serial port connection and closes
    * the Controller.
    *
    *
    */
    
    protected void finalize()
    {
        addCommand(STOP);
        dispatcher.kill();
    }

    private synchronized void doNotify()
    {
        notifyAll();
    }

    private synchronized void doWait(long millis) throws InterruptedException
    {
        wait(millis);
    }

    /** shutdown tells the controller to finish all commands
    * in the queue and then gracefully disconnects the serial
    * port connection.
    *
    * @param millis the number of milliseconds to wait for a graceful shutdown.
    * @exception OperationTimedOutException thrown if the Controller has not
    * completely shutdown in the amount of time specified.
    * @exception InterruptedException thrown if the thread is unexpectedly interrupted
    *
    */

    public void shutdown(long millis) throws OperationTimedOutException, InterruptedException
    {
        if(running)
        {
            try
            {
                finalize();
                doWait(millis);
                if(running)
                {
                    throw new OperationTimedOutException("Timed out while waiting for CM11ASerialController to shutdown");
                }
            }
            catch(InterruptedException ie)
            {
                if(running)
                {
                    throw ie;
                }
            }
        }
    }

    /** shutdownNow shuts down the controller and closes the serial port immediately.
    * shutdown(long) is the preferred method of shutting down the controller, but this
    * method provides an immediate, unclean, non-graceful means to shut down the controller.
    *
    */

    public void shutdownNow()
    {
        sp.close();
        finalize();
    }
    
    /** run is the thread loop that constantly blocks and writes
    * events to the serial port to the "Firecracker" module.
    *
    *
    */
    
    public void run()
    {
        running = true;
        dispatcher.start();
        resetFirecracker();
        while(running)
        {
            try
            {
                Command nextCommand = (Command) queue.dequeueNextAvailable();
                if(nextCommand == STOP)
                {
                    running = false;
                }
                else
                {
                    char houseCode = nextCommand.getHouseCode();
                    int unitCode = nextCommand.getUnitCode();
                    short function = nextCommand.getFunctionByte();
                    switch(function)
                    {
                        case Command.ON             : toFirecracker((String) commandTable.get(houseCode + "" + unitCode + "ON"));
                        break;
                        case Command.OFF            : toFirecracker((String) commandTable.get(houseCode + "" + unitCode + "OFF"));
                        break;
                        case Command.DIM            :
                        case Command.BRIGHT         :
                        case Command.ALL_LIGHTS_OFF :
                        case Command.ALL_LIGHTS_ON  :
                        case Command.ALL_UNITS_OFF  :
                    }
                    dispatcher.dispatchUnitEvent(new UnitEvent(nextCommand));
                    sleep(COMMAND_INTERVAL);
                }
            }
            catch(InterruptedException ie)
            {
            }
        }
        sp.close();
        doNotify();
    }
    
    private void resetFirecracker()
    {
        sp.setDTR(false);
        sp.setRTS(false);
        sleep(RESET_INTERVAL);
        
        sp.setDTR(true);
        sp.setRTS(true);
        sleep(RESET_INTERVAL);
    }
    
    private void toFirecracker(String transmission)
    {
        transmission = HEADER + transmission + FOOTER;
        int len = transmission.length();
        
        for(int i = 0; i < len; i++)
        {
            if(transmission.charAt(i) == '0')
            {
                sp.setRTS(false);
                sleep(WAIT_INTERVAL);
                sp.setRTS(true);
                sleep(WAIT_INTERVAL);
            }
            else
            {
                sp.setDTR(false);
                sleep(WAIT_INTERVAL);
                sp.setDTR(true);
                sleep(WAIT_INTERVAL);
            }
        }
    }
    
    private void sleep(long interval)
    {
        try
        {
            Thread.sleep(interval);
        }
        catch(InterruptedException ie)
        {
        }
    }
}
