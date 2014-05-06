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
package fr.liglab.adele.icasa.remote.wisdom.util;

import fr.liglab.adele.icasa.location.Zone;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO
 *
 */
public class ZoneJSON {

	public static final String IS_ROOM_PROP = "isRoom";
	public static final String VARIABLE_PROP = "variables";
	public static final String POSITION_TOPY_PROP = "topY";
	public static final String POSITION_LEFTX_PROP = "leftX";
	public static final String POSITION_BOTTOMY_PROP = "bottomY";
	public static final String POSITION_RIGHTX_PROP = "rightX";
    public static final String POSITION_BOTTOMZ_PROP = "bottomZ";
    public static final String POSITION_TOPZ_PROP = "topZ";
	public static final String NAME_PROP = "name";
	public static final String TYPE_PROP = "type";
	public static final String ZONE_ID_PROP = "zoneId";
	public static final String ID_PROP = "id";

	private String name;
	private String id;
	private Integer leftX;
	private Integer topY;
	private Integer rigthX;
	private Integer bottomY;
    private Integer bottomZ;
    private Integer topZ;

	public static ZoneJSON fromString(String jsonStr) {
		ZoneJSON zone = null;
		JSONObject json = null;
		try {
			json = new JSONObject(jsonStr);
			zone = new ZoneJSON();
			if (json.has(ID_PROP)) {
				zone.setId(json.getString(ID_PROP));
			} else if (json.has(ZONE_ID_PROP)) {
				zone.setId(json.getString(ZONE_ID_PROP));
			}
			;
			if (json.has(NAME_PROP)){
				zone.setName(json.getString(NAME_PROP));
            }
			if (json.has(POSITION_LEFTX_PROP)){
				zone.setLeftX(json.getInt(POSITION_LEFTX_PROP));
            }
			if (json.has(POSITION_TOPY_PROP)){
				zone.setTopY(json.getInt(POSITION_TOPY_PROP));
            }
			if (json.has(POSITION_RIGHTX_PROP)){
				zone.setRigthX(json.getInt(POSITION_RIGHTX_PROP));
            }
			if (json.has(POSITION_BOTTOMY_PROP)){
				zone.setBottomY(json.getInt(POSITION_BOTTOMY_PROP));
            }
            if (json.has(POSITION_BOTTOMZ_PROP)){
                zone.setBottomZ(json.getInt(POSITION_BOTTOMZ_PROP));
            } else {
                zone.setBottomZ(Zone.DEFAULT_Z_BOTTOM);
            }
            if (json.has(POSITION_TOPZ_PROP)){
                zone.setTopZ(json.getInt(POSITION_TOPZ_PROP));
            } else {
                zone.setTopZ(zone.getBottomZ() + Zone.DEFAULT_Z_LENGTH);
            }

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return zone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the leftX
	 */
	public Integer getLeftX() {
		return leftX;
	}

	/**
	 * @param leftX
	 *           the leftX to set
	 */
	public void setLeftX(Integer leftX) {
		this.leftX = leftX;
	}

	/**
	 * @return the topY
	 */
	public Integer getTopY() {
		return topY;
	}

	/**
	 * @param topY
	 *           the topY to set
	 */
	public void setTopY(Integer topY) {
		this.topY = topY;
	}

	/**
	 * @return the rigthX
	 */
	public Integer getRigthX() {
		return rigthX;
	}

	/**
	 * @param rigthX
	 *           the rigthX to set
	 */
	public void setRigthX(Integer rigthX) {
		this.rigthX = rigthX;
	}

	/**
	 * @return the bottomY
	 */
	public Integer getBottomY() {
		return bottomY;
	}

	/**
	 * @param bottomY
	 *           the bottomY to set
	 */
	public void setBottomY(Integer bottomY) {
		this.bottomY = bottomY;
	}

    public Integer getBottomZ() {
        return bottomZ;
    }

    public void setBottomZ(Integer bottomZ) {
        this.bottomZ = bottomZ;
    }

    public Integer getTopZ() {
        return topZ;
    }

    public void setTopZ(Integer topZ) {
        this.topZ = topZ;
    }
}
