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
package fr.liglab.adele.icasa.service.zone.size.calculator;

/**
 * The Interface of service ZoneSize provides information on the size of the
 * different Zones of the iCASA simulator
 *
 */
public interface ZoneSizeCalculator {

    public static final String X_SCALE_FACTOR_PROP_NAME = "x-scale-factor";
    public static final String Y_SCALE_FACTOR_PROP_NAME = "y-scale-factor";
    public static final String Z_SCALE_FACTOR_PROP_NAME = "z-scale-factor";

	/**
	 * Gets the X size in meter of the given zone.
	 * 
	 * @param zoneId
	 *            unique id of the given zone
	 * @return the x size in meter
	 */
	public float getXInMeter(String zoneId);

    /**
     * Returns scale factor from pixels to meters on X axis.
     *
     * @return scale factor on X axis.
     */
    public float getXScaleFactor();

	/**
	 * Gets the Y size in meter of the given zone.
	 * 
	 * @param zoneId
	 *            unique id of the given zone
	 * @return the y size in meter
	 */
	public float getYInMeter(String zoneId);

    /**
     * Returns scale factor from pixels to meters on Y axis.
     *
     * @return scale factor on Y axis.
     */
    public float getYScaleFactor();

    /**
     * Gets the Z size in meter of the given zone.
     *
     * @param zoneId
     *            unique id of the given zone
     * @return the z size in meter
     */
    public float getZInMeter(String zoneId);

    /**
     * Returns scale factor from pixels to meters on Z axis.
     *
     * @return scale factor on Z axis.
     */
    public float getZScaleFactor();

	/**
	 * Gets the surface in meter square.
	 * 
	 * @param zoneId
	 *            unique id of the given zone
	 * @return the surface in meter square
	 */
	public float getSurfaceInMeterSquare(String zoneId);

}