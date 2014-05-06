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
/**
 * 
 */
package fr.liglab.adele.icasa.rfid.reader.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.usb.basedriver.bridge.driver.UsbBridgeDrivers;
import org.apache.felix.usb.basedriver.descriptor.UsbDevice;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.rfid.reader.RFIDDeviceReader;

/**
 *
 */
@Component(name="TikitagRFIDReader")
@Provides(properties = {
        @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION)})
public class RFIDDeviceReaderImpl extends AbstractDevice implements RFIDDeviceReader, PresenceSensor, GenericDevice {

   @ServiceProperty(name = PresenceSensor.DEVICE_SERIAL_NUMBER, mandatory = true)
   private String m_serialNumber;
   
   @Requires
	private UsbDevice myDevice; //TODO manage multiple usb devices !!!
	
   @Requires
	private UsbBridgeDrivers myBridgeDriver;
   
   /**
    * Buffer of read Tags
    */
   private List<String> buffer;
   
   @ServiceProperty(name = "presenceSensor.sensedPresence", value = "false")
   private volatile boolean m_currentPresence;
   
	private Thread uniqueThread;
	
	private volatile boolean polling = false;

	private static final long DEFAULT_POLL_INTERVAL = 800;

	
	/** the buffer of read tags */
	protected List<String> readingBuffer = new ArrayList<String>();

    public RFIDDeviceReaderImpl() {
        setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, false);
    }


	/* (non-Javadoc)
	 * @see fr.liglab.adele.icasa.device.GenericDevice#getSerialNumber()
	 */
	public String getSerialNumber() {
		return m_serialNumber;
	}


	/* (non-Javadoc)
	 * @see fr.liglab.adele.m2mappbuilder.rfid.reader.RFIDDeviceReader#getTags()
	 */
	public List getTags() {
		synchronized (buffer) {
			List newList = new ArrayList(buffer);
			buffer.clear();
			return newList;
      }
	}

	public void environmentPropertyChanged(String arg0, Double arg1, Double arg2) {
	   // do nothing
   }

	@Override
	public boolean getSensedPresence() {
		return m_currentPresence;
	}

	@Invalidate
	protected void stop() {
		stopPooling();
	}

	@Validate
	protected void start() {
		buffer = new ArrayList<String>();
		startPooling();
	}
	
	private void startPooling() {
		final TikitagDescriptor descriptor = new TikitagDescriptor("Tikitag Descriptor", myDevice, myBridgeDriver);
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				while (polling) {
					descriptor.open();
					String tag = descriptor.getTag();
					synchronized (buffer) {
						if (tag!=null)
							buffer.add(tag);			
               }
					//List tags = getTags();
					final boolean oldPresence = m_currentPresence;
					m_currentPresence = (tag!=null);
	            if (oldPresence != m_currentPresence) {
                    setPropertyValue(PRESENCE_SENSOR_SENSED_PRESENCE, m_currentPresence);
	            }
					try {
						Thread.sleep(DEFAULT_POLL_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		};
		
		polling = true;
		uniqueThread= new Thread(runnable, "Tikitag Thread");
		uniqueThread.start();
	}

	
	private void stopPooling() {
		polling = false;
		uniqueThread = null;
	}


}
