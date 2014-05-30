package fr.liglab.adele.habits.monitoring.db.manager.fileinstall.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

import fr.liglab.adele.habits.monitoring.autonomic.manager.listeners.DPInfos;
import fr.liglab.adele.habits.monitoring.db.manager.fileinstall.DBFileParser;

@RunWith(JUnit4.class)
public class DBFileParserTest {

	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testXmlConfigFileLoading() throws IOException, SAXException, ParserConfigurationException{
		
		URL url = this.getClass().getResource("/config.hma");
		Assert.assertNotNull(url);
		HashSet<DPInfos> nodes = (HashSet<DPInfos>) DBFileParser.parse(new File(url.getFile()));
		Assert.assertEquals(2, nodes.size());
		
		DPInfos dp1 = new DPInfos();
		dp1.setName("presence-sensor");
		dp1.setUrl("http://www.test.com");
		dp1.getInterfaces().add("fr.liglab.adele.icasa.device.presence.PresenceSensor");
		DPInfos dp2 = new DPInfos();
		dp2.setName("dimming-light");
		dp2.setUrl("http://www.light.com");
		dp2.getInterfaces().add("fr.liglab.adele.icasa.simulator.SimulatedDevice");
		
		Assert.assertEquals(true, nodes.contains(dp1));
		Assert.assertEquals(true, nodes.contains(dp2));
	}
}
