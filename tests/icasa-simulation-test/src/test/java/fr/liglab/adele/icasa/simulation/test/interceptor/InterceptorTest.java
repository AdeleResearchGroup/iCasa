package fr.liglab.adele.icasa.simulation.test.interceptor;

import java.util.Hashtable;

import javax.inject.Inject;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.api.Dependency;
import org.apache.felix.ipojo.api.PrimitiveComponentType;
import org.apache.felix.ipojo.api.Service;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.simulation.test.util.IPojoApiBaseTest;
import fr.liglab.adele.icasa.simulator.SimulationManager;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class InterceptorTest extends IPojoApiBaseTest {

	@Inject
	public BundleContext context;

	@Inject
	private SimulationManager simulationMgr;
	
	private BinaryLightConsumer consumerApp;

	@Before
	public void setUp() {
		waitForStability(context);
	}

	@After
	public void tearDown() {
		try {
			simulationMgr.resetContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void contextAddedToServiceReference() {
		createInstanceConsumer("(location=kitchen)");
		prepareSimulationEnvironment();
		LocatedDevice lDevice = simulationMgr.createDevice("iCASA.BinaryLight", "12345", new Hashtable<String, Object>());
		simulationMgr.moveDeviceIntoZone(lDevice.getSerialNumber(), "kitchen");
		
		consumerApp = (BinaryLightConsumer) getService(context, BinaryLightConsumer.class);
				
		Assert.assertNotNull(consumerApp);
		Assert.assertEquals(consumerApp.getDeviceId(), lDevice.getSerialNumber());		
	}
	
	@Test
	public void serviceReferenceWithOtherContext() {
		createInstanceConsumer("(location=kitchen)");
		prepareSimulationEnvironment();
		LocatedDevice lDevice = simulationMgr.createDevice("iCASA.BinaryLight", "12345", new Hashtable<String, Object>());
		simulationMgr.moveDeviceIntoZone(lDevice.getSerialNumber(), "livingroom");
		
		consumerApp = (BinaryLightConsumer) getService(context, BinaryLightConsumer.class);				
		Assert.assertNull(consumerApp);
	}	
	
	
	@Test
	public void contextChangedToServiceReference() {
		createInstanceConsumer("(location=kitchen)");
		prepareSimulationEnvironment();
		LocatedDevice lDevice = simulationMgr.createDevice("iCASA.BinaryLight", "12345", new Hashtable<String, Object>());
		simulationMgr.moveDeviceIntoZone(lDevice.getSerialNumber(), "kitchen");
		
		consumerApp = (BinaryLightConsumer) getService(context, BinaryLightConsumer.class);
				
		Assert.assertNotNull(consumerApp);
		Assert.assertEquals(consumerApp.getDeviceId(), lDevice.getSerialNumber());		
		
		simulationMgr.moveDeviceIntoZone(lDevice.getSerialNumber(), "livingroom");
		consumerApp = (BinaryLightConsumer) getService(context, BinaryLightConsumer.class);
		Assert.assertNull(consumerApp);
		
	}
	

	private void createInstanceConsumer(String filter) {
		PrimitiveComponentType appCT = new PrimitiveComponentType().setBundleContext(context)
		      .setClassName(BinaryLightConsumerImpl.class.getName())
		      .addDependency(new Dependency().setField("m_device").setFilter(filter)).addService(new Service());
		try {
			appCT.createInstance("Test-Consumer");
		} catch (UnacceptableConfiguration e) {
			e.printStackTrace();
		} catch (MissingHandlerException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void prepareSimulationEnvironment() {
		simulationMgr.createZone("kitchen", new Position(0, 0), 50);
		simulationMgr.createZone("bathroom", new Position(0, 100), 50);
		simulationMgr.createZone("livingroom", new Position(0, 200), 50);
		simulationMgr.createZone("bedroom", new Position(0, 300), 50);
	}
}
