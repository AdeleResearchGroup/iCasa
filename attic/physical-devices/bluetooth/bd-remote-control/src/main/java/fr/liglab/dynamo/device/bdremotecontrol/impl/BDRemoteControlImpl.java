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
package fr.liglab.dynamo.device.bdremotecontrol.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.log.LogService;

import com.intel.bluetooth.BlueCoveConfigProperties;

import fr.liglab.dynamo.device.bdremotecontrol.BDRemoteControl;

/**
 * 
 */
@Component(name = "bluetooth.playstation.remote", immediate=true)
@Provides
@Instantiate(name="bluetooth.playstation.remote-1")
public class BDRemoteControlImpl implements fr.liglab.dynamo.device.bdremotecontrol.BDRemoteControl {
	
	class IncomingThread extends Thread {

		private volatile boolean active;

		private final Map<String, BDRemoteControl.Button> buttonMatching = new HashMap<String, BDRemoteControl.Button>() {
			private static final long serialVersionUID = 1L;
			{
				put("a101000000ffffffffffff000", Button.BUTTON_RELEASED);
				put("a10100000000ffffffffff010", Button.BUTTON_1);
				put("a10100000001ffffffffff010", Button.BUTTON_2);
				put("a10100000002ffffffffff010", Button.BUTTON_3);
				put("a10100000003ffffffffff010", Button.BUTTON_4);
				put("a10100000004ffffffffff010", Button.BUTTON_5);
				put("a10100000005ffffffffff010", Button.BUTTON_6);
				put("a10100000006ffffffffff010", Button.BUTTON_7);
				put("a10100000007ffffffffff010", Button.BUTTON_8);
				put("a10100000008ffffffffff010", Button.BUTTON_9);
				put("a10100000009ffffffffff010", Button.BUTTON_0);
				put("a10100000016ffffffffff010", Button.EJECT_KEY);
				put("a10100000064ffffffffff010", Button.AUDIO_KEY);
				put("a10100000065ffffffffff010", Button.ANGLE_KEY);
				put("a10100000063ffffffffff010", Button.SUBTITLE_KEY);
				put("a1010000000fffffffffff010", Button.CLEAR_KEY);
				put("a10100000028ffffffffff010", Button.TIME_KEY);
				put("a10100000081ffffffffff010", Button.RED_KEY);
				put("a10100000082ffffffffff010", Button.GREEN_KEY);
				put("a10100000080ffffffffff010", Button.BLUE_KEY);
				put("a10100000083ffffffffff010", Button.YELLOW_KEY);
				put("a10100000070ffffffffff010", Button.DISPLAY_KEY);
				put("a1010000001affffffffff010", Button.TOP_MENU_KEY);
				put("a10100000040ffffffffff010", Button.POP_UP_MENU_KEY);
				put("a1010000000effffffffff010", Button.RETURN_KEY);
				put("a1010010005cffffffffff010", Button.TRIANGLE_OPTIONS_KEY);
				put("a1010020005dffffffffff010", Button.CIRCLE_BACK_KEY);
				put("a1010080005fffffffffff010", Button.SQUARE_VIEW_KEY);
				put("a1010040005effffffffff010", Button.X_KEY);
				put("a101003000ffffffffffff010", Button.TRIANGLE_AND_CIRCLE_KEYS);
				put("a101009000ffffffffffff010", Button.TRIANGLE_AND_SQUARE_KEYS);
				put("a101005000ffffffffffff010", Button.TRIANGLE_AND_X_KEYS);
				put("a10100a000ffffffffffff010", Button.CIRCLE_AND_SQUARE_KEYS);
				put("a101006000ffffffffffff010", Button.CIRCLE_AND_X_KEYS);
				put("a10100c000ffffffffffff010", Button.SQUARE_AND_X_KEYS);
				put("a1010004005affffffffff010", Button.L1_KEY);
				put("a10100010058ffffffffff010", Button.L2_KEY);
				put("a10102000051ffffffffff010", Button.L3_KEY);
				put("a1010008005bffffffffff010", Button.R1_KEY);
				put("a10100020059ffffffffff010", Button.R2_KEY);
				put("a10104000052ffffffffff010", Button.R3_KEY);
				put("a10101000050ffffffffff010", Button.SELECT_KEY);
				put("a10108000053ffffffffff010", Button.START_KEY);
				put("a10100000143ffffffffff010", Button.PS_KEY);
				put("a10100000033ffffffffff010", Button.REWIND_KEY);
				put("a10100000034ffffffffff010", Button.FAST_FORWARD_KEY);
				put("a10100000030ffffffffff010", Button.PREVIOUS_KEY);
				put("a10100000031ffffffffff010", Button.NEXT_KEY);
				put("a10100000060ffffffffff010", Button.SLOW_REWIND_KEY);
				put("a10100000061ffffffffff010", Button.SLOW_FORWARD_KEY);
				put("a10100000032ffffffffff010", Button.PLAY_KEY);
				put("a10100000039ffffffffff010", Button.PAUSE_KEY);
				put("a10100000038ffffffffff010", Button.STOP_KEY);
				put("a10110000054ffffffffff010", Button.UP_ARROW);
				put("a10180000057ffffffffff010", Button.LEFT_ARROW);
				put("a10120000055ffffffffff010", Button.RIGHT_ARROW);
				put("a10140000056ffffffffff010", Button.DOWN_ARROW);
				put("a101900000ffffffffffff010", Button.UP_AND_LEFT_DIAGONAL);
				put("a101300000ffffffffffff010", Button.UP_AND_RIGHT_DIAGONAL);
				put("a101c00000ffffffffffff010", Button.DOWN_AND_LEFT_DIAGONAL);
				put("a101600000ffffffffffff010", Button.DOWN_AND_RIGHT_DIAGONAL);
				put("a1010000080bffffffffff010", Button.ENTER_KEY);
				put("a101000000ffffffffffff010", Button.MULTIPLE_KEYS);
			}
		};

		private final L2CAPConnection incoming;

		protected IncomingThread(int source, String btaddress) throws IOException, InterruptedException {
			super("out:" + btaddress);

			// see http://code.google.com/p/bluecove/issues/detail?id=64
			System.setProperty(BlueCoveConfigProperties.PROPERTY_JSR_82_PSM_MINIMUM_OFF, "true");
			System.setProperty(BlueCoveConfigProperties.PROPERTY_STACK_FIRST, "widcomm");
			System.setProperty(BlueCoveConfigProperties.PROPERTY_STACK, "widcomm");
			System.loadLibrary("bluecove");

			String l2cap = "btl2cap://" + btaddress + ":13;authenticate=false;encrypt=false;master=false";

			logger.log(LogService.LOG_DEBUG, "Bluetooth address: " + btaddress);
			logger.log(LogService.LOG_DEBUG, "Opening incoming connection: " + l2cap);

			incoming = (L2CAPConnection) Connector.open(l2cap, Connector.WRITE, true);

			logger.log(LogService.LOG_DEBUG, "Incoming connection is " + incoming.toString());

			active = true;
		}

		public void disconnect() {
			active = false;
		}

		@Override
		public void run() {
			while (active) {
				byte[] buf = new byte[1024];
				int length = 0;
				try {
					length = incoming.receive(buf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					active = false;
				}
				/*
				StringBuffer sb = new StringBuffer();
				logger.log(LogService.LOG_DEBUG, "received:");
				for (int i = 0; i < length; i++) {
					String hex = Integer.toHexString(buf[i] & 0xff);
					sb.append(hex.length() == 1 ? "0x0" : "0x").append(hex).append(" ");
					if ((i + 1) % 8 == 0) {
						logger.log(LogService.LOG_DEBUG, sb.toString());
						sb.delete(0, sb.length());
					}
				}

				if (sb.length() > 0) {
					logger.log(LogService.LOG_DEBUG, sb.toString());
				}*/

				StringBuffer s = new StringBuffer();
				for (int i = 0; i < length; i++) {
					s.append(String.format("%02x", buf[i]));
				}

				// do not take the last number in account, it seems to not be constant
				String incomingString = s.substring(0, s.length() - 1);
				Button b = buttonMatching.get(incomingString);
				if (b == null) {
					logger.log(LogService.LOG_WARNING, "The button with number '" + incomingString + "' is not recognized (usually because several buttons are pressed in the same time).");
				} else {
					// FIXME: to be complete
					switch (b) {
					case CIRCLE_BACK_KEY:
						eventSender = sensorCircle;
						break;

					case DOWN_ARROW:
						eventSender = sensorDown;
						break;

					case LEFT_ARROW:
						eventSender = sensorLeft;
						break;

					case RIGHT_ARROW:
						eventSender = sensorRight;
						break;

					case UP_ARROW:
						eventSender = sensorUp;
						break;

					case ENTER_KEY:
						eventSender = sensorEnter;
						break;

					case PLAY_KEY:
						eventSender = sensorPlay;
						break;

					case SELECT_KEY:
						eventSender = sensorSelect;
						break;

					case SQUARE_VIEW_KEY:
						eventSender = sensorSquare;
						break;

					case START_KEY:
						eventSender = sensorStart;
						break;

					case TRIANGLE_OPTIONS_KEY:
						eventSender = sensorTriangle;
						break;

					case X_KEY:
						eventSender = sensorX;
						break;

					default:
						break;
					}
				}
			}
		}
	}

	/**
	 * Sender that sends events when it is assigned.
	 */
	@SuppressWarnings("unused")
	private Object eventSender;
	private IncomingThread incomingThread;
	
	@Requires(optional=false)
	private LogService logger;
	private boolean sensorCircle;
	private boolean sensorDown;
	private boolean sensorEnter;
	private boolean sensorLeft;
	private boolean sensorPlay;
	private boolean sensorRight;
	private boolean sensorSelect;
	private boolean sensorSquare;
	private boolean sensorStart;
	private boolean sensorTriangle;
	private boolean sensorUp;
	private boolean sensorX;
	
	public BDRemoteControlImpl() {
		// do nothing
		@SuppressWarnings("unused")
		String btAddress = null;
	}

	/**
	 * Called by iPOJO when the instance becomes valid.
	 */
	@Validate
	private void start() {
		logger.log(LogService.LOG_DEBUG, "BD Remote control component instance is starting.");
		String btAddress = null;

		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DiscoveryAgent discoveryAgent = localDevice.getDiscoveryAgent();

		RemoteDevice[] cachedDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED);
		if (cachedDevices != null) {
			int s = cachedDevices.length;
			for (int i = 0; i < s; i++) {
				logger.log(LogService.LOG_DEBUG, "Cached device" + i + ":"
						+ cachedDevices[i].getBluetoothAddress() + ", "
						+ cachedDevices[i]);
				btAddress = cachedDevices[i].getBluetoothAddress();
			}
		}
		RemoteDevice[] preknownDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
		if (preknownDevices != null) {
			int s = preknownDevices.length;
			for (int i = 0; i < s; i++) {
				logger.log(LogService.LOG_DEBUG, "Preknown device" + i + ":"
						+ preknownDevices[i].getBluetoothAddress() + ", "
						+ preknownDevices[i]);
			}
		}

		try {
			incomingThread = new IncomingThread(1, btAddress);
			incomingThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Called by iPOJO when the instance becomes invalid.
	 */
	@Invalidate
	private void stop() {
		logger.log(LogService.LOG_DEBUG, "BD Remote control component instance is stopping.");
		if (incomingThread != null) {
			incomingThread.disconnect();
			incomingThread = null;
		}
	}
}
