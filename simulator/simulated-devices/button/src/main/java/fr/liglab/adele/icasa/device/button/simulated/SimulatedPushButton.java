/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.device.button.simulated;

import fr.liglab.adele.icasa.device.button.PushButton;

/**
 * User: garciai@imag.fr
 * Date: 9/2/13
 * Time: 2:24 PM
 */
public interface SimulatedPushButton extends PushButton{
    /**
     * Push the button and hold for a timeout.
     * @param timeout, the time the button remains pressed.
     * @return <code>true</code> when the instruction is taken into account.
     * <code>false</code> if not, For example, button is already pushed.
     */
    boolean pushAndHold(long timeout);

    /**
     * Push the button and release it.
     */
    void pushAndRelease();
}
