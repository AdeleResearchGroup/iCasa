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
package fr.liglab.adele.icasa.context.model.example;

import fr.liglab.adele.icasa.context.handler.creator.entity.EntityCreator;
import fr.liglab.adele.icasa.context.handler.creator.entity.Creator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component(immediate = true)
@Provides
@Instantiate(name = "LocationManage-0")
public class LocationManagerImpl implements LocationManager {

	@EntityCreator(entity= ZoneImpl.class)
	Creator creator;

	@Override
	public void createZone(String id, int leftX, int topY, int bottomZ, int width, int height, int depth) {
		Map propertiesInit = new HashMap<>();
		propertiesInit.put(Zone.ZONE_NAME,id);
		propertiesInit.put(Zone.ZONE_X,leftX);
		propertiesInit.put(Zone.ZONE_Y,topY);
		propertiesInit.put(Zone.ZONE_Z,bottomZ);
		propertiesInit.put(Zone.ZONE_X_LENGHT,width);
		propertiesInit.put(Zone.ZONE_Y_LENGHT,height);
		propertiesInit.put(Zone.ZONE_Z_LENGHT,depth);
		creator.createEntity(id,propertiesInit);
	}


	@Override
	public void removeZone(String id) {
		creator.deleteEntity(id);
	}

	@Override
	public void moveZone(String id, int leftX, int topY, int bottomZ) throws Exception {

	}

	@Override
	public void resizeZone(String id, int width, int height, int depth) throws Exception {

	}

	@Override
	public void removeAllZones() {
		creator.deleteAllEntities();
	}

	@Override
	public Set<String> getZoneIds() {
		return null;
	}


}