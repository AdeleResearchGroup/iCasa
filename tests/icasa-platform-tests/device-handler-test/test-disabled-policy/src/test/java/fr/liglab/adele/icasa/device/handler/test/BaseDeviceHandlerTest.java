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
package fr.liglab.adele.icasa.device.handler.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;


import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.access.AccessManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.handler.test.mock.devices.BinaryLightMockImpl;
import fr.liglab.adele.icasa.device.handler.test.mock.devices.ThermometerMockImpl;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

/**
 *
 */
@RunWith(ChameleonRunner.class)
public class BaseDeviceHandlerTest {

    @Inject
    public BundleContext context;

    protected DeploymentAdmin dpAdmin;

    protected AccessManager accessManager;
    
    protected ContextManager contextManager;
    
    protected final String TEST_APPLICATION_NAME = "test-handler-app";
    private OSGiHelper osgi;

    @Before
    public void setUp() throws Exception {
        osgi = new OSGiHelper(context);
        dpAdmin = osgi.getServiceObject(DeploymentAdmin.class);
        accessManager = osgi.getServiceObject(AccessManager.class);
        contextManager = osgi.getServiceObject(ContextManager.class);
        //installDeploymentPackage("test-handler-app-dp-1");
    }

    @After
    public void tearDown() {

    }
    


    private Boolean getAccessPolicyPropertyValue() {
        return Boolean.FALSE;
    }

    protected ComponentInstance createComponentInstance(String factoryName) throws Exception {
        Factory factory = osgi.getServiceObject(Factory.class, "(factory.name=" + factoryName + ")");
        assertNotNull(factory);

        ComponentInstance instance = factory.createComponentInstance(null);
        assertNotNull(instance);

        return instance;
    }

    protected GenericDevice createBinaryLigth(String deviceId) {
        BinaryLightMockImpl mockImpl = new BinaryLightMockImpl(deviceId);

        Dictionary properties = new Hashtable();
        properties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);

        context.registerService(new String[] { GenericDevice.class.getName(), BinaryLight.class.getName() }, mockImpl,
                properties);
        
        return mockImpl;
    }

    protected GenericDevice createThermometer(String deviceId) {
        ThermometerMockImpl mockImpl = new ThermometerMockImpl(deviceId);

        Dictionary properties = new Hashtable();
        properties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);

        context.registerService(new String[] { GenericDevice.class.getName(), Thermometer.class.getName() }, mockImpl,
                properties);
        
        return mockImpl;
    }

    private URL getDeploymentPackageArtifactURL(String artifactID) throws MalformedURLException {
        return new URL("mvn:fr.liglab.adele.icasa/" + artifactID + "/" + System.getProperty("applications.test.version")+"/dp");
    }

    protected DeploymentPackage installDeploymentPackage(String dpName) throws DeploymentException, IOException {

        URL url = getDeploymentPackageArtifactURL(dpName);

        return dpAdmin.installDeploymentPackage(url.openStream());
    }

    private void printBundles(String header) {
        Bundle[] bundles = context.getBundles();

        System.out.println("=================== " + header + " =================");
        System.out.println("Total " + bundles.length);
        for (Bundle bundle : bundles) {
            System.out.println(" ---> " + bundle.getSymbolicName());
        }

    }
    


    @Test
    public void simpleTest() {
        // Only created to avoid JUnit error
    }
    
}
