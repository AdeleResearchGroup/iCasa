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
package fr.liglab.adele.icasa.location;

/**
 * This class represents a point in a coordinate system.
 *
 * 
 */
public final class Position {

	public final int x;
	public final int y;
    public final int z;
    public final static int DEFAULT_Z = 0;

	/**
	 * Default constructor
	 * 
	 * @param x
	 * @param y
	 */
	public Position(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
        this.z = z;
	}
    public Position(final int x, final int y) {
        this(x,y,DEFAULT_Z);
    }

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z +")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
        result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
        if (z != other.z)
            return false;
		return true;
	}

	/**
	 * Clones this point into a new object
	 */
	public Position clone() {
		return new Position(this.x, this.y, this.z);
	}

}
