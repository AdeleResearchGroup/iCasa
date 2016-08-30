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
package fr.liglab.adele.zwave.device.proxies.openhab;

import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.LocatedObjectBehaviorProvider;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;

import fr.liglab.adele.zwave.device.proxies.ZwaveDeviceBehaviorProvider;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.ArrayList;
import java.util.List;

@ContextEntity(services = {})
@Behavior(id="ZwaveBehavior",spec = ZwaveDevice.class,implem = ZwaveDeviceBehaviorProvider.class)
@Behavior(id="LocatedBehavior",spec = LocatedObject.class,implem = LocatedObjectBehaviorProvider.class)
public class FibaroSmokeSensor {

}