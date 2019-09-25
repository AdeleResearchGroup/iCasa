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
package fr.liglab.adele.icasa.remote.context.serialization;

import org.json.JSONException;

import fr.liglab.adele.icasa.ZoneProvider;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import tec.units.ri.unit.Units;

public class SerializedZone extends SerializedItem {

	private static final String ZONE_ID_PROP 			= "zoneId";
	private static final String NAME_PROP 				= "name";
	
	private static final String IS_ROOM_PROP 			= "isRoom";

	private static final String POSITION_LEFTX_PROP 	= "leftX";
	private static final String POSITION_RIGHTX_PROP 	= "rightX";

	private static final String POSITION_TOPY_PROP 		= "topY";
	private static final String POSITION_BOTTOMY_PROP 	= "bottomY";

	private static final String POSITION_TOPZ_PROP		= "topZ";
	private static final String POSITION_BOTTOMZ_PROP 	= "bottomZ";

	private static final String VARIABLE_PROP 			= "variables";


	private SerializedZone() {
		super();
	}

	public SerializedZone(String serialization) throws JSONException {
		super(serialization);
	}

	public SerializedZone(Zone zone) throws JSONException {

		this();
		
		setId(zone.getZoneName());
		setAttribute(NAME_PROP, zone.getZoneName());

		setAttribute(IS_ROOM_PROP, true); // TODO change it when Zone API will be improved

		setAttribute(POSITION_LEFTX_PROP, zone.getLeftTopAbsolutePosition().x);
		setAttribute(POSITION_TOPY_PROP, zone.getLeftTopAbsolutePosition().y);
		setAttribute(POSITION_RIGHTX_PROP, zone.getRightBottomAbsolutePosition().x);
		setAttribute(POSITION_BOTTOMY_PROP, zone.getRightBottomAbsolutePosition().y);

		addProperty(VARIABLE_PROP, Zone.X_LENGHT, zone.getXLength(), Units.METRE);
		addProperty(VARIABLE_PROP, Zone.Y_LENGHT, zone.getYLength(), Units.METRE);
		addProperty(VARIABLE_PROP, Zone.Z_LENGHT, zone.getZLength(), Units.METRE);

	}

	public String getId() {
		return serialization.optString(ZONE_ID_PROP, super.getId());
	}

	public void createZone(ZoneProvider provider) {
		
		int right	= serialization.optInt(POSITION_RIGHTX_PROP, 0);
		int left	= serialization.optInt(POSITION_LEFTX_PROP, 0);
		int width	= right - left;
		
		int top		= serialization.optInt(POSITION_TOPY_PROP, 0);
		int bottom	= serialization.optInt(POSITION_BOTTOMY_PROP, 0);
		int height	= bottom - top;

		int front	= serialization.optInt(POSITION_TOPZ_PROP, 0);
		int back	= serialization.optInt(POSITION_BOTTOMZ_PROP, 0);
		int depth	= front - back;

		provider.createZone(getId(), left, top, back, width, height, depth);

	}
	
	public void updateZone(Zone zone) {

		int right	= serialization.optInt(POSITION_RIGHTX_PROP, 0);
		int left	= serialization.optInt(POSITION_LEFTX_PROP, 0);
		int width	= right - left;
		
		int top		= serialization.optInt(POSITION_TOPY_PROP, 0);
		int bottom	= serialization.optInt(POSITION_BOTTOMY_PROP, 0);
		int height	= bottom - top;

		
		Position position	= new Position(left,top);

		if (!zone.getLeftTopAbsolutePosition().equals(position)) {
			zone.setLeftTopAbsolutePosition(position);
		}
		
		if ((zone.getXLength() != width) || (zone.getYLength() != height)) {
			zone.resize(width, height, 4);
		}

	}
}
