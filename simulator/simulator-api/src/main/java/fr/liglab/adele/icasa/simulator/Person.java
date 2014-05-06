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
package fr.liglab.adele.icasa.simulator;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.simulator.listener.PersonListener;

/**
 * This interface represents a person in the icasa simulated Environment
 *
 */
public interface Person extends LocatedObject {

	public static final int DEFAULT_WIDTH = 50;

	public static final int DEFAULT_HEIGHT = 50;

	/**
	 * Gets the person name.
	 * 
	 * @return The person name.
	 */
	public String getName();

	/**
	 * Sets the person name.
	 * 
	 * @param name the person new name.
	 */
	public void setName(String name);

	/**
	 * Gets the person type.
	 * 
	 * @return The person type.
	 */
	public PersonType getPersonType();

	/**
	 * Sets the person type.
	 * 
	 * @param personType the new person type.
	 */
	public void setPersonType(PersonType personType);

	/**
	 * Gets the person logical location.
	 * 
	 * @return the person location (zone name) or null.
	 */
	public String getLocation();

	/**
	 * Adds a listener to the person.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addListener(final PersonListener listener);

	/**
	 * Removes a listener from the person.
	 * 
	 * @param listener the listener to be removed.
	 */
	public void removeListener(final PersonListener listener);

}
