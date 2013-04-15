/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa-Simulator/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.simulator.listener.util;

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;

public class BaseZoneListener implements ZoneListener {

	@Override
	public void zoneVariableAdded(Zone zone, String variableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneVariableRemoved(Zone zone, String variableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneVariableModified(Zone zone, String variableName, Object oldValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneAdded(Zone zone) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneRemoved(Zone zone) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneMoved(Zone zone, Position oldPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneResized(Zone zone) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoneParentModified(Zone zone, Zone oldParentZone) {
		// TODO Auto-generated method stub

	}

    @Override
    public void deviceAttached(Zone zone, LocatedDevice locatedDevice) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deviceDetached(Zone zone, LocatedDevice locatedDevice) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
