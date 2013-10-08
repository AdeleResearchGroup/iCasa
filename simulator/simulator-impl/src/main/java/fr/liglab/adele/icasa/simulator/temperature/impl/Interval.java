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
package fr.liglab.adele.icasa.simulator.temperature.impl;

/**
 * Represents an integer interval.
 * Min and max numbers must be positive.
 *
 * @author Thomas Leveque
 */
public class Interval {

    public Interval(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int min;
    public int max;

    public int getLength() {
        return max - min;
    }
}
