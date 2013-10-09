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
package fr.liglab.adele.icasa.common;

import java.util.List;

/**
 * Allows a component to manage specific state variables (such as Logic Location).
 * It can add new state variable or define specific variables as derived.
 * In this case, it is responsible of value computation.
 * 
 * @author Thomas Leveque
 *
 */
public interface StateVariableExtender {

	/**
	 * Returns list of variables managed by this extender.
	 * 
	 * @return list of variables managed by this extender.
	 */
	public List<StateVariable> getManagedVariables();
}
