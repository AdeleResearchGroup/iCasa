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
package fr.liglab.adele.icasa.physical.abstraction;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

public @ContextService interface MultiwaySwitch {

    public @State static final String GLOBAL_SWITCH_STATE="global.switch.state";

    public @State static final String ZONE_ATTACHED="zone.attached";

    public SwitchState stateInZone();

    public String switchStateOf();

    public enum SwitchState {
        ON(1), OFF(0), NOT_MEASURED(-1);

        private final int state;

        private SwitchState(int state) {
            assert ((1 >= state) && (state >= -1));
            this.state = state;
        }
    }
}
