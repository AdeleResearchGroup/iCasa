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
package fr.liglab.adele.icasa.distribution.test.zone;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.util.ZoneTracker;
import fr.liglab.adele.icasa.location.util.ZoneTrackerCustomizer;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 *
 */
@RunWith(ChameleonRunner.class)
public class ZoneTrackerTest {

	@Inject
	public BundleContext context;

    OSGiHelper helper;

	@Before
	public void setUp() {
		helper = new OSGiHelper(context);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testTrackerZoneAddedRemoved() {
        ContextManager contextMgr = helper.getServiceObject(ContextManager.class);

        Assert.assertNotNull(contextMgr);

		ZoneTracker tracker = new ZoneTracker(context, null);
		tracker.open();
		
		// No tracked zones 
		Assert.assertEquals(0, tracker.size());

		// zone-1 added to the context
		contextMgr.createZone("zone-1", 10, 10, 10, 10, 10, 10);
		
		// One tracked zone
		Assert.assertEquals(1, tracker.size());
		
		// zone-1 removed from context
		contextMgr.removeZone("zone-1");

		// no tracked zones
		Assert.assertEquals(0, tracker.size());
        contextMgr.removeAllZones();

	}



	@Test
	public void testZoneTrackerWithVariable() {
        ContextManager contextMgr = helper.getServiceObject(ContextManager.class);
		Assert.assertNotNull(contextMgr);

		String variableName = "needed-variable";

		ZoneTracker tracker = new ZoneTracker(context, null, variableName);
		tracker.open();
		Assert.assertEquals(0, tracker.size());

		// create zone.
		Zone zone = contextMgr.createZone("zone-1", 10, 10, 10, 10, 10, 10);
		// check size in context and in tracker.
		Assert.assertEquals(contextMgr.getZones().size(), 1);// One zone in context.
		Assert.assertEquals(tracker.size(), 0);// zone does not have needed variable.
		// add a variable
		contextMgr.addZoneVariable(zone.getId(), variableName);
		// now the tracker must have the zone
		Assert.assertEquals(1, tracker.size());// One tracked zone with the variable
		
		// removing variable from zone
		zone.removeVariable(variableName);
		// now the tracker must have 0 zones
		Assert.assertEquals(tracker.size(), 0);// 0 tracked zones 'cause the needed variable has been removed.
        contextMgr.removeAllZones();
	}

	
	@Test
	public void testZonesCreationThenTracker() {
        ContextManager contextMgr = helper.getServiceObject(ContextManager.class);
		Assert.assertNotNull(contextMgr);

		contextMgr.createZone("zone-1", 10, 10, 10, 10, 10, 10);
		contextMgr.createZone("zone-2", 10, 10, 10, 10, 10, 10);
		
		ZoneTracker tracker = new ZoneTracker(context, null);
		tracker.open();
		Assert.assertEquals(2, tracker.size());


		contextMgr.removeZone("zone-1");
		Assert.assertEquals(1, tracker.size());
        contextMgr.removeAllZones();
	}

	@Test
	public void testZoneTrackerWithCustomizer() {
        ContextManager contextMgr = helper.getServiceObject(ContextManager.class);
		Assert.assertNotNull(contextMgr);

		ZoneTrackerCustomizer customizer = mock(ZoneTrackerCustomizer.class);
		

		
		
		when(customizer.addingZone(any(Zone.class))).thenReturn(true);
		
		ZoneTracker tracker = new ZoneTracker(context, customizer);
		tracker.open();
		Assert.assertEquals(0, tracker.size());
		
		

		Zone zone1 = contextMgr.createZone("zone-1", 10, 10, 10, 10, 10, 10);
		Assert.assertEquals(1, tracker.size());
		Assert.assertEquals(contextMgr.getZones().size(), tracker.size());
		
		
		verify(customizer, times(1)).addedZone(zone1);		
		verify(customizer, times(1)).addingZone(zone1);
				
		
		Zone zone2 = contextMgr.createZone("zone-2", 10, 10, 10, 10, 10, 10);
		Assert.assertEquals(tracker.size(), 2);
		Assert.assertEquals(tracker.size(), contextMgr.getZones().size());
		
		verify(customizer, times(1)).addingZone(zone2);
		verify(customizer, times(1)).addedZone(zone2);
		
		contextMgr.removeZone(zone1.getId());
		
		verify(customizer, times(1)).removedZone(zone1);
		
		contextMgr.removeZone(zone2.getId());
		
		verify(customizer, times(1)).removedZone(zone2);
        contextMgr.removeAllZones();
		
	}
	
	@Test
	public void testZoneTrackerWithCustomizerAndVariables() {
		ContextManager contextMgr = helper.getServiceObject(ContextManager.class);
		Assert.assertNotNull(contextMgr);

		ZoneTrackerCustomizer customizer = mock(ZoneTrackerCustomizer.class);
		
		String variableName = "needed-variable";
		
		
		when(customizer.addingZone(any(Zone.class))).thenReturn(true);
		
		ZoneTracker tracker = new ZoneTracker(context, customizer, variableName);
		tracker.open();
		Assert.assertEquals(0, tracker.size());
		
		

		Zone zone1 = contextMgr.createZone("zone-1", 10, 10, 10, 10, 10, 10);
		Assert.assertEquals(0, tracker.size());

		contextMgr.addZoneVariable(zone1.getId(), variableName);
		
		Assert.assertEquals(1, tracker.size());
		
		
		verify(customizer, times(1)).addedZone(zone1);		
		verify(customizer, times(1)).addingZone(zone1);
				
		zone1.setVariableValue(variableName, "test-value");
		verify(customizer, times(1)).modifiedZone(zone1, variableName, null, "test-value");
		
		
		try {
	      contextMgr.moveZone(zone1.getId(), 20, 20, 20);
	      
	      verify(customizer, times(1)).movedZone(zone1, new Position(10, 10, 10), new Position(20, 20, 20));
	      
      } catch (Exception e) {
	      e.printStackTrace();
      }

        // removing variable from zone
        zone1.removeVariable(variableName);
        // now the tracker must have 0 zones
        Assert.assertEquals(tracker.size(), 0);// 0 tracked zones 'cause the needed variable has been removed.

		verify(customizer, times(1)).removedZone(zone1);
        contextMgr.removeAllZones();
		
	}
	

	
}