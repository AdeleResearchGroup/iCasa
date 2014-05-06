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

import fr.liglab.adele.icasa.Variable;

import java.util.List;
import java.util.Set;

/**
 * This interface represents a Zone (a rectangular surface) into the iCasa platform. Zones can be embeded, a zone can
 * have several children zones. Each children zone must fit into its parent zone.
 *
 * 
 */
public interface Zone extends LocatedObject {

    static final int DEFAULT_Z_LENGTH = 100;

    static final int DEFAULT_Z_BOTTOM = 0;


    /**
	 * Returns the zone id.
	 * 
	 * @return zone id.
	 */
	public String getId();

	/**
	 * Gets the (x,y) point in the left-top corner of the zone. This point is relative to its parent zone. If there is no
	 * parent, this method returns the same point of <code>getLeftTopAbsolutePosition</code> method.
	 * 
	 * @return the relative (x,y) point in the left-top corner of the zone
	 */
	public Position getLeftTopRelativePosition();

	/**
	 * Gets the absolute (x,y) point in the left-top corner of the zone.
	 * 
	 * @return the absolute (x,y) point in the left-top corner of the zone.
	 */
	public Position getLeftTopAbsolutePosition();

	/**
	 * Gets the (x,y) point in the right-bottom corner of the zone. This point is relative to its parent zone. If there
	 * is no parent, this method returns the same point of <code>getRightBottomAbsolutePosition</code> method.
	 * 
	 * @return Gets the relative (x,y) point in the right-bottom corner of the zone.
	 */
	public Position getRightBottomRelativePosition();

	/**
	 * Gets the absolute (x,y) point in the right-bottom corner of the zone.
	 * 
	 * @return the absolute (x,y) point in the right-bottom corner of the zone.
	 */
	public Position getRightBottomAbsolutePosition();

	/**
	 * Sets the relative (x,y) point in the left-top corner of the zone.
	 * 
	 * @param leftTopPosition The new left-top corner of the zone.
	 * @throws Exception When the zone does not fit its parent zone.
	 */
	public void setLeftTopRelativePosition(Position leftTopPosition) throws Exception;

	/**
	 * Gets the zone X length (width)
	 * 
	 * @return the zone width
	 */
	public int getXLength();

	/**
	 * Sets the zone X length .
	 * 
	 * @param length the new zone X length.
	 * @throws Exception When the zone does not fit its parent zone.
	 */
	public void setXLength(int length) throws Exception;

	/**
	 * Gets the zone Y length.
	 * 
	 * @return the zone Y length.
	 */
	public int getYLength();

	/**
	 * Sets the zone Y length.
	 * 
	 * @param length the new zone Y length.
	 * @throws Exception When the zone does not fit its parent zone.
	 */
	public void setYLength(int length) throws Exception;
    /**
     * Gets the zone Z length.
     *
     * @return the zone Y length.
     */
    public int getZLength();

    /**
     * Sets the zone Y length.
     *
     * @param length the new zone Z length.
     * @throws Exception When the zone does not fit its parent zone.
     */
    public void setZLength(int length) throws Exception;

	/**
	 * Returns true if a object is geographically contained into the zone.
	 * 
	 * @param object a located object
	 * @return true if a object is geographically contained into the zone, false if it is not.
	 */
	public boolean contains(LocatedObject object);

	/**
	 * Returns true if a point is geographically contained into the zone.
	 * 
	 * @param position a point
	 * @return true if a point is geographically contained into the zone, false if it is not.
	 */
	public boolean contains(Position position);

	/**
	 * Returns relative position of specified object from top left corner of this zone.
	 * 
	 * @param object a located object
	 * @return relative position of specified object from top left corner of this zone.
	 */
	public Position getRelativePosition(LocatedObject object);

	/**
	 * Adds a child zone to this zone. The child zone must fit this zone.
	 * 
	 * @param child
	 * @return
	 */
	public boolean addZone(Zone child);

	/**
	 * Determines if a Zone fits (don't exceed limits) of this zone/
	 * 
	 * @param aZone a zone
	 * @return true if aZone fits into this zone, false otherwise.
	 */
	public boolean fits(Zone aZone);

	/**
	 * Determines the deepth of this zone
	 * 
	 * @return
	 */
	public int getLayer();

	/**
	 * Gets the list of children zones
	 * 
	 * @return
	 */
	public List<Zone> getChildren();

	/**
	 * Sets the parent Zone
	 * 
	 * @param parent
	 */
	public void setParent(Zone parent);

	/**
	 * Gets the parent Zone
	 * 
	 * @return
	 */
	public Zone getParent();

	/**
	 * Adds a listener to this zone.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addListener(final ZoneListener listener);

	/**
	 * Removes a listener from the zone.
	 * 
	 * @param listener the listener to be removed.
	 */
	public void removeListener(final ZoneListener listener);

	/**
	 * Modifies if this zone will use the variable of its parent zone.
	 * 
	 * @param useParentVariables true if the zone must use its parent variables.
	 */
	public void setUseParentVariables(boolean useParentVariables);

	/**
	 * Determines if this zone use the variable of its parent zone.
	 * 
	 * @return
	 */
	public boolean getUseParentVariables();

	/**
	 * Gets the value from a zone variable.
	 * 
	 * @param name The name of variable.
	 * @return the value of variable.
	 */
	public Object getVariableValue(String name);

	/**
	 * Sets specified variable value. If the property does not exist, adds it.
	 * 
	 * @param name variable name to set
	 * @param newValue property value to set
	 */
	public void setVariableValue(String name, Object newValue);

	/**
	 * Adds a variable to this zone.
	 * 
	 * @param name The variable name.
	 */
	public void addVariable(String name);

	/**
	 * Removes a variable from this zone.
	 * 
	 * @param name The variable name.
	 */
	public void removeVariable(String name);

	/**
	 * Gets the list of variable names
	 * 
	 * @return the list of variable names
	 */
	public Set<String> getVariableNames();

	/**
	 * Gets the list of variable
	 * 
	 * @return the list of variable
	 */
	public Set<Variable> getVariables();

	/**
	 * Resizes the zone
	 * 
	 * @param newWidth The new width (X length value)
	 * @param newHeight The new Height (Y length value)
     * @param newDepth The new Depth (Z length value)
	 * @throws Exception When the zone does not fit its parent zone.
	 */
	public void resize(int newWidth, int newHeight, int newDepth) throws Exception;

}
