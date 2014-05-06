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

/**
 * This class represents a person type
 *
 * 
 */
public class PersonType {

	private String name;

	/**
	 * Default constructor
	 * @param name person type name
	 */
	public PersonType(String name) {
		this.name = name;
	}

	/**
	 * Gets the person type name (id)
	 * 
	 * @return person type name
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PersonType that = (PersonType) o;

		if (!name.equals(that.name))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
