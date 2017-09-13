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
package fr.liglab.adele.icasa.commands.impl;

import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.ScriptLanguage;
import fr.liglab.adele.icasa.commands.Signature;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.button.PushButton;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.motion.MotionSensor;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;


/**
 *
 * Sets the fault state of device to "Yes"
 *
 *
 */
@Component(name = "MoveDeviceCommandNew")
@Provides
@Instantiate(name = "move-device-command")
public class MoveDeviceCommand extends AbstractCommand {

    @Requires(specification = BinaryLight.class,optional = true,proxy = false)
    List<BinaryLight> binaryLights;

    @Requires(specification = DimmerLight.class,optional = true,proxy = false)
    List<DimmerLight> dimmerLights;

    @Requires(specification = PresenceSensor.class,optional = true,proxy = false)
    List<PresenceSensor> presenceSensors;

    @Requires(specification = MotionSensor.class,optional = true,proxy = false)
    List<MotionSensor> motionSensors;

    @Requires(specification = PushButton.class,optional = true,proxy = false)
    List<PushButton> pushButtons;

    @Requires(specification = Photometer.class, optional = true,proxy = false)
    List<Photometer> photometers;

    @Requires(specification = Thermometer.class,optional = true,proxy = false)
    List<Thermometer> thermometers;

    @Requires(specification = Cooler.class,optional = true,proxy = false)
    List<Cooler> coolers;

    @Requires(specification = Heater.class,optional = true,proxy = false)
    List<Heater> heaters;

    private static Signature MOVE = new Signature(new String[]{ScriptLanguage.DEVICE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y} );
    private static Signature MOVE_WZ = new Signature(new String[]{ScriptLanguage.DEVICE_ID, ScriptLanguage.LEFT_X, ScriptLanguage.TOP_Y, ScriptLanguage.BOTTOM_Z } );

    public MoveDeviceCommand(){
        addSignature(MOVE);
        addSignature(MOVE_WZ);
    }

    @Override
    public Object execute(InputStream in, PrintStream out,JSONObject param, Signature signature) throws Exception {
        String deviceId = param.getString(ScriptLanguage.DEVICE_ID);

        LocatedObject deviceToMove = findDevice(deviceId);
        if (deviceToMove == null) {
            throw new IllegalArgumentException("Device ("+ deviceId +") does not exist");
        }

        int newX = param.getInt(ScriptLanguage.LEFT_X);
        int newY = param.getInt(ScriptLanguage.TOP_Y);
        deviceToMove.setPosition(new Position(newX,newY));

        return null;
    }

    private LocatedObject findDevice(String deviceSerialNumber){
        for (Heater heater:heaters){
            if (heater instanceof LocatedObject && deviceSerialNumber.equals(heater.getSerialNumber()))return (LocatedObject) heater;
        }
        for (DimmerLight light:dimmerLights){
            if (light instanceof LocatedObject &&deviceSerialNumber.equals(light.getSerialNumber()))return (LocatedObject)light;
        }
        for (Cooler cooler:coolers){
            if (cooler instanceof LocatedObject &&deviceSerialNumber.equals(cooler.getSerialNumber()))return (LocatedObject)cooler;
        }
        for (Thermometer thermometer:thermometers){
            if (thermometer instanceof LocatedObject &&deviceSerialNumber.equals(thermometer.getSerialNumber()))return (LocatedObject)thermometer;
        }
        for (BinaryLight binaryLight:binaryLights){
            if (binaryLight instanceof LocatedObject && deviceSerialNumber.equals(binaryLight.getSerialNumber()))return (LocatedObject)binaryLight;
        }
        for (PresenceSensor presenceSensor:presenceSensors){
            if (presenceSensor instanceof LocatedObject && deviceSerialNumber.equals(presenceSensor.getSerialNumber()))return (LocatedObject)presenceSensor;
        }
        for (MotionSensor motionSensor:motionSensors){
            if (motionSensor instanceof LocatedObject && deviceSerialNumber.equals(motionSensor.getSerialNumber()))return (LocatedObject)motionSensor;
        }
        for (PushButton pushButton:pushButtons){
            if (pushButton instanceof LocatedObject && deviceSerialNumber.equals(pushButton.getSerialNumber()))return (LocatedObject)pushButton;
        }
        for (Photometer photometer : photometers){
            if (photometer instanceof LocatedObject && deviceSerialNumber.equals(photometer.getSerialNumber()))return (LocatedObject)photometer;
        }
        return null;
    }

    /**
     * Get the name of the  Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "move-device";
    }

    @Override
    public String getDescription(){
        return "Move a device to new X,Y positions.\n\t" + super.getDescription();
    }

}
