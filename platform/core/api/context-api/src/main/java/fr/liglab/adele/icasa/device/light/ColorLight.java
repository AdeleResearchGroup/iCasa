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
package fr.liglab.adele.icasa.device.light;

import java.awt.*;

/**
 * Created by jnascimento on 21/03/14.
 * Represents a light device in which its color can be changed
 */
public interface ColorLight extends BinaryLight,DimmerLight{

    /**
     * Changes the colorlight
     * @param color
     */
    public void setColor(Color color);

    /**
     * Fetches last color state
     * @return color
     */
    public Color getColor();

}