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
///**
// *
// *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
// *   Group Licensed under a specific end user license agreement;
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
package fr.liglab.adele.icasa.simulator.model.luminosity;


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.icasa.simulator.model.api.LuminosityModel;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {LuminosityModel.class})
public class SimulatedLuminosityModel implements LuminosityModel{

    /**
     * Rought Constant to establish the correspondance between power & illuminance
     */
    public static final double LUMENS_CONSTANT_VALUE = 683.0d;

    // There is no need of full illuminance in the morning
    public static final double  MORNING_EXTERNAL_SOURCE_POWER = 800 ;
    // In the afternoon the illuminance can be largely limited
    public static final double  AFTERNOON_EXTERNAL_SOURCE_POWER = 1600;
    // In the evening, the illuminance should be the best
    public static final double  EVENING_EXTERNAL_SOURCE_POWER = 700;
    // In the night, there is no need to use the full illuminance
    public static final double  NIGHT_EXTERNAL_SOURCE_POWER = 200;

    private final double DEFAUT_VALUE = 100d;

    public static final String RELATION_IS_ATTACHED="illuminance.model.of";

    @ContextEntity.State.Field(service = LuminosityModel.class,state = LuminosityModel.CURRENT_LUMINOSITY)
    public double currentLuminosity;

    @ContextEntity.State.Field(service = LuminosityModel.class,state = LuminosityModel.ZONE_ATTACHED)
    public String zoneName;

    @Validate
    public void start(){

    }

    @Override
    public double getCurrentLuminosity() {
        return currentLuminosity;
    }

    @Requires(specification = MomentOfTheDay.class)
    MomentOfTheDay momentOfTheDay;

    @Requires(specification = BinaryLight.class,filter = "(locatedobject.object.zone=${luminositymodel.zone.attached})",optional = true)
    List<BinaryLight> binaryLights;

    @Requires(specification = DimmerLight.class,filter = "(locatedobject.object.zone=${luminositymodel.zone.attached})",optional = true)
    List<DimmerLight> dimmerLights;

    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=false)
    Zone zone;

    @Bind(id = "zone")
    public void bindZone(Zone zone){
        pushZone(zone.getZoneName());
    }

    @ContextEntity.State.Push(service = LuminosityModel.class,state = LuminosityModel.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    /**
     * Computes and updates the illuminance property value of specified zone .
     * The formula used to compute the illuminance is :
     * Illuminance [cd/m² or lux]=(power[W]*680.0[lumens])/surface[m²]
     *
     */
    @ContextEntity.State.Pull(service = LuminosityModel.class,state = LuminosityModel.CURRENT_LUMINOSITY)
    Supplier<Double> pullLuminosity = () -> {
        double returnedIlluminance = getLightFactor();
        double powerLevelTotal = 0.0;

        for (BinaryLight binaryLight : binaryLights){
            if (binaryLight.getPowerStatus()) {
                if (binaryLight instanceof PowerObservable){
                    powerLevelTotal +=((PowerObservable) binaryLight).getCurrentConsumption();
                }else {
                    powerLevelTotal +=DEFAUT_VALUE;
                }
            } else powerLevelTotal += 0.0d;
        }
        for (DimmerLight dimmerLight : dimmerLights){
            if (dimmerLight.getPowerLevel() != 0.0d) {
                if (dimmerLight instanceof PowerObservable){
                    powerLevelTotal += ((PowerObservable) dimmerLight).getCurrentConsumption();
                }else {
                    powerLevelTotal += dimmerLight.getPowerLevel() * DEFAUT_VALUE;
                }

            }
        }
        return        returnedIlluminance += ( (powerLevelTotal  * LUMENS_CONSTANT_VALUE) / zone.getXLength()*zone.getYLength());
    };

    private double getLightFactor(){
        MomentOfTheDay.PartOfTheDay currentPartOfTheDay = momentOfTheDay.getCurrentPartOfTheDay();
        if (currentPartOfTheDay == null){
            return MORNING_EXTERNAL_SOURCE_POWER;
        }
        switch (currentPartOfTheDay){
            case MORNING: return MORNING_EXTERNAL_SOURCE_POWER;
            case EVENING: return EVENING_EXTERNAL_SOURCE_POWER;
            case AFTERNOON: return AFTERNOON_EXTERNAL_SOURCE_POWER;
            case NIGHT: return NIGHT_EXTERNAL_SOURCE_POWER;
            default: return MORNING_EXTERNAL_SOURCE_POWER;
        }
    }


}
