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
package fr.liglab.adele.zwave.device.api;

import fr.liglab.adele.icasa.context.model.annotations.ContextService;
import fr.liglab.adele.icasa.context.model.annotations.State;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;

/**
 * Created by aygalinc on 19/04/16.
 */
public @ContextService interface ZwaveControllerICasa extends ZwaveRepeater{

    public static @State  String MASTER="Zwave.Master";

    public static @State  String CONTROLLER="Zwave.Controller";

    public boolean isMaster();

    public void addEventListener(ZWaveEventListener eventListener);

    public void removeEventListener(ZWaveEventListener eventListener);
}
