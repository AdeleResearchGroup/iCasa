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
package fr.liglab.adele.icasa.common.impl;

/**
 * Util methods to compare objects.
 *
 *
 */
public class ComparisonUtil {

	/**
	 * Returns true if the specified objects are equal.
	 * This method allows arguments to be null.
	 * 
	 * @param obj1 an object
	 * @param obj2 an object
	 * @return true if the specified objects are equal.
	 */
	public static boolean same(Object obj1, Object obj2) {
		return ((obj1 == null) && (obj2 == null)) || ((obj1 != null) && obj1.equals(obj2));
	}
}
