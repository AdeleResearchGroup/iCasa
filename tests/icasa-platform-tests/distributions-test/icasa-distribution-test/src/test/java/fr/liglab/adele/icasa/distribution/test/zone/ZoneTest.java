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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;


@RunWith(ChameleonRunner.class)
public class ZoneTest {
	
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
	
	/**
	 * Test the creation of a new zone.
	 */
	@Test
	public void creationZoneTest(){
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(0,0);
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		//Test the zone and its Id. 
		Assert.assertNotNull(zone_0);
		Assert.assertEquals(zone_id_0, zone_0.getId());
        icasa.removeAllZones();
	}
	
	/**
	 * Test the creation of a new zone with an existent ID.
	 */
	@Test
	public void creationZoneFailDueToExistentTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		Position positionZone_0 = new Position(0,0);

		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, 5);
        try {
            Zone zone_1 = icasa.createZone(zone_id_0, positionZone_0, 10);
            fail("Creation of existing zone should throw an exception");
        } catch (IllegalArgumentException e) {
            // passed
        }
        icasa.removeAllZones();
	}
	
	/**
	 * Test the events for not available zones. 
	 */
	@Test
	public void eventsForRemovedZoneFailTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		String variable = "variable";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(0,0);
		//create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		Assert.assertNull(listener.getListenZone());//There is any zone added.
		//Add zone listener.
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//There is any added zone.
		//remove zone.
		icasa.removeZone(zone_id_0);//We remove the zone
		//Test that it is correctly removed.
		Assert.assertNull(icasa.getZone(zone_id_0)); //Zone should not be in ContextManager.
		zone_0.addVariable(variable);
		Assert.assertNull(listener.getListenVariable());
        icasa.removeAllZones();
	}
	/**
	 * Test the the listener event is called when adding a zone.
	 */
	@Test
	public void addZoneTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(0,0);
		//Add zone listener.
		ZoneTestListener listener = new ZoneTestListener();
		icasa.addListener(listener);
		
		Assert.assertNull(listener.getListenZone());//There is any zone added.
		
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		
		Assert.assertNotNull(listener.getListenZone());//There is an added zone.
		Assert.assertEquals(zone_0, listener.getListenZone());//added zone is the same as the added.
        icasa.removeAllZones();
	}
	/**
	 * Test the remove zone and the associated listener.
	 */
	@Test
	public void removeZoneTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(0,0);
		//create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		Assert.assertNull(listener.getListenZone());//There is any zone added.
		//Add zone listener.
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//There is any added zone.
		//remove zone.
		icasa.removeZone(zone_id_0);//We remove the zone
		//Test that it is correctly removed.
		Assert.assertNull(icasa.getZone(zone_id_0)); //Zone should not be in ContextManager.
		Assert.assertEquals(zone_0, listener.getListenZone());//removed zone is the same as the added.
		Assert.assertEquals(listener.getListenZone(), zone_0);
        icasa.removeAllZones();
	}
	
	/**
	 * Test the correct behavior when moving zones.
	 */
	@Test
	public void moveZoneTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(5,5);
		Position newPosition = new Position(10,10);
		//Create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//There is any zone in the listener.
		//move zone.
		try {
			icasa.moveZone(zone_id_0, 10, 10, 0);
		} catch (Exception e) {
			Assert.fail("Error when moving zone");
		}
		Assert.assertEquals(zone_0, listener.getListenZone());//moved zone is the same as the added.
		Assert.assertEquals(newPosition, listener.getListenZone().getLeftTopRelativePosition());//moved zone is the same as the added.
		Assert.assertNotSame(positionZone_0,listener.getListenZone().getCenterAbsolutePosition());//Old position is not the initial position
        icasa.removeAllZones();
	}

	/**
	 * Test the correct behavior of zones when moving.
	 */
	@Test
	public void resizeZoneTest(){
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		int zone_0_scope = 5;//size of the squared zone is 10
		Position positionZone_0 = new Position(5,5,5);
		Position newPositionAfterMove = new Position(10,10,10);
		//Create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		Assert.assertEquals(10,zone_0.getYLength());
		Assert.assertEquals(10,zone_0.getXLength());
        Assert.assertEquals(10,zone_0.getZLength());
		//Add listener after creating to only receive resize event.
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//Any event received.
		//Test center positions
		Assert.assertEquals(zone_0.getCenterAbsolutePosition(), positionZone_0);
		//Resizes zone.
		try {
			icasa.resizeZone(zone_id_0, 20, 20, 20);
		} catch (Exception e) {
			Assert.fail("Error when resizing zone");
		}
		Assert.assertEquals(zone_0.getCenterAbsolutePosition(), newPositionAfterMove);
		//The resize event has been received.
		Assert.assertEquals(zone_0, listener.getListenZone());
		//Test the new zone size.
		Assert.assertEquals(20,zone_0.getYLength());
		Assert.assertEquals(20,zone_0.getXLength());
        icasa.removeAllZones();
		//Test the new zone center position.
		
	}

	/**
	 * Test the fail when resizing zone when does not fit in parent zone
	 */
	@Test
	public void addZoneParentTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		String zone_parent_id_0 = "myParentZone-0";
		//Create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, 77,387, Zone.DEFAULT_Z_BOTTOM,49,49, Zone.DEFAULT_Z_LENGTH);
		//Add listener after creating to only receive resize event.
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//Any event received.
		//Add parent zone.
		icasa.createZone(zone_parent_id_0, 55,370,Zone.DEFAULT_Z_BOTTOM, 259, 210, Zone.DEFAULT_Z_LENGTH);
		try {
			icasa.setParentZone(zone_id_0, zone_parent_id_0);
		} catch (Exception e1) {
			Assert.fail("Unable to set parent in zone");
		}
		icasa.removeAllZones();
	}
	
	/**
	 * Test the fail when resizing zone when does not fit in parent zone
	 */
	@Test
	public void resizeZoneInParentFailTest() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);

		String zone_id_0 = "myZone-0";
		String zone_parent_id_0 = "myParentZone-0";
		int zone_0_scope = 2;//size of the squared zone is 4
		int zone_parent_0_scope = 4;//size of the squared zone is 8
		Position positionZone_0 = new Position(5,5);
		Position positionParentZone_0 = new Position(5,5);
		//Create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		Assert.assertEquals(zone_0_scope*2,zone_0.getYLength());
		Assert.assertEquals(zone_0_scope*2,zone_0.getXLength());
		//Add listener after creating to only receive resize event.
		icasa.addListener(listener);
		Assert.assertNull(listener.getListenZone());//Any event received.
		//Test center positions
		Assert.assertEquals(zone_0.getCenterAbsolutePosition(), positionZone_0);
		//Add parent zone.
		icasa.createZone(zone_parent_id_0, positionParentZone_0, zone_parent_0_scope);
		try {
			icasa.setParentZone(zone_id_0, zone_parent_id_0);
		} catch (Exception e1) {
			Assert.fail("Unable to set parent in zone");
		}
		//Resizes zone.
		try {
			icasa.resizeZone(zone_id_0, 20, 20, Zone.DEFAULT_Z_LENGTH);
			Assert.fail("must throw an exception, child does not fit in parent zone");
		} catch (Exception e) {
			
		}
        icasa.removeAllZones();
	}
	/**
	 * Test the zone variables and its values.
	 */
	@Test
	public void testZoneAddVariables() {
        ContextManager icasa = helper.getServiceObject(ContextManager.class);
        assertNotNull(icasa);
		String zone_id_0 = "myZone-0";
		String zone_variable = "variable-0";
		int zone_0_scope = 5;
		Position positionZone_0 = new Position(0,0);
		//create zone
		ZoneTestListener listener = new ZoneTestListener();
		Zone zone_0 = icasa.createZone(zone_id_0, positionZone_0, zone_0_scope);
		//Add Zone Listener.
		icasa.addListener(listener);
		//Add variable to the zone.
		icasa.addZoneVariable(zone_id_0, zone_variable);
		//Test the variable in the listener.
		Assert.assertEquals(zone_variable,listener.getListenVariable());
		//Test the variable in the zone.
		Assert.assertTrue(zone_0.getVariableNames().contains(zone_variable));
		//Test the variables in the context.
		Assert.assertTrue(icasa.getZoneVariables(zone_id_0).contains(zone_variable));
        icasa.removeAllZones();
	}

}
