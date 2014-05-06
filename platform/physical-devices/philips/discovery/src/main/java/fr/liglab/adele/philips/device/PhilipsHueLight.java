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
package fr.liglab.adele.philips.device;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.ColorLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Philips Hue light implementation that allows to change its state (color, brightness)
 */
@Component(name = "philipsHueLight")
@Provides(specifications={GenericDevice.class, DimmerLight.class, ColorLight.class,PowerObservable.class})
public class PhilipsHueLight extends AbstractDevice implements
        ColorLight,PowerObservable {

    private final Object m_lock;
    @ServiceProperty(name = GenericDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    @ServiceProperty(name="philips.device.light",mandatory = true)
    private PHLight light;

    @ServiceProperty(name="philips.device.bridge",mandatory = true)
    private PHBridge bridge;

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueLight.class);

    public PhilipsHueLight(){
        super();
        super.setPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME, GenericDevice.LOCATION_UNKNOWN);
        super.setPropertyValue(DimmerLight.DIMMER_LIGHT_MAX_POWER_LEVEL, 8.5d);
        PHLightState lightState = new PHLightState();
        lightState.setOn(false);
        bridge.updateLightState(light, lightState);
        super.setPropertyValue(DimmerLight.DIMMER_LIGHT_POWER_LEVEL,0.0d);
        super.setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL,0.0d );
        float[] xy = PHUtilities.calculateXYFromRGB(Color.RED.getRed(),Color.RED.getGreen(),Color.RED.getBlue(), light.getIdentifier());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        bridge.updateLightState(light, lightState);
        m_lock = new Object();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


    @Override
    public double getPowerLevel() {
        return ((double)(light.getLastKnownLightState().getBrightness())/254.0);
    }

    @Override
    public double setPowerLevel(double level) {
        if (level < 0.0d || level > 1.0d || Double.isNaN(level)){
            throw new IllegalArgumentException("Invalid power level : " + level);
        }

        if (level != 0.0){

            PHLightState lightState = new PHLightState();

            lightState.setOn(true);
            bridge.updateLightState(light, lightState);

            lightState.setBrightness((int)(level*254));
            bridge.updateLightState(light, lightState);
            super.setPropertyValue(DimmerLight.DIMMER_LIGHT_POWER_LEVEL,level);
            getCurrentConsumption();
            return level;
        }
        else{
            PHLightState lightState = new PHLightState();
            lightState.setOn(false);
            bridge.updateLightState(light, lightState);
            super.setPropertyValue(DimmerLight.DIMMER_LIGHT_POWER_LEVEL, 0.0);
            getCurrentConsumption();
            return 0;
        }
    }

    @Override
    public double getMaxPowerLevel() {
        Double maxLevel = (Double) getPropertyValue(BinaryLight.BINARY_LIGHT_MAX_POWER_LEVEL);
        if (maxLevel == null)
            return 0;

        return maxLevel;
    }

    @Override
    public void setColor(Color color) {
        PHLightState lightState = new PHLightState();
        float[] xy = PHUtilities.calculateXYFromRGB(color.getRed(), color.getGreen(), color.getBlue(), light.getIdentifier());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        bridge.updateLightState(light, lightState);
    }

    @Override
    public Color getColor() {
        Color color=new Color(0,0,0);
        return color;
    }

    @Override
    public double getCurrentConsumption() {
        Double maxLevel = (Double) getPropertyValue(DimmerLight.DIMMER_LIGHT_MAX_POWER_LEVEL);
        Double powerLevel = (Double) getPropertyValue(DimmerLight.DIMMER_LIGHT_POWER_LEVEL);
        super.setPropertyValue(PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL,(double)(powerLevel*maxLevel));
        return powerLevel*maxLevel;
    }
}
