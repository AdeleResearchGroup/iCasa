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
package fr.liglab.adele.zwave.device.importer;


import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.annotations.Invalidate;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;

import fr.liglab.adele.zwave.device.api.ZwaveController;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;

import org.apache.felix.ipojo.Factory;

import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class})
public class ControllerImporter extends AbstractImporterComponent  {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImporter.class);

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(protocol=zwave)(port=*))")
	private String filter;


	private final  	Creator.Entity<? extends ZwaveController> contextCreator;
	
	@Creator.Field(origin = OriginEnum.internal)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.openhab.ControllerImpl> openhabCreator;
	@Creator.Field(origin = OriginEnum.internal)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.ControllerImpl> zwave4jCreator;

	/**
	 * The properties used to match the proxy factory
	 */
	public final static String FACTORY_PROPERTY_MANUFACTURER 	= "zwave.manufacturer";
	public final static String FACTORY_PROPERTY_DEVICE_ID 		= "zwave.device.id";
	public final static String FACTORY_PROPERTY_DEFAULT_PROXY	= "zwave.default.proxy";

	/**
	 *
	 */
	public final static String PROXY_PROPERTY_CONTROLLER 		= "zwave.controller";
	public final static String PROXY_PROPERTY_NODE 				= "zwave.node";
	public final static String PROXY_PROPERTY_ENDPOINT 			= "zwave.endpoint";

	public ControllerImporter(@Property(name="library", mandatory=true, value="openhab") String libraryName) {
		
		ZWaveLibrary library = ZWaveLibrary.valueOf(libraryName);
		
		if 	(library == ZWaveLibrary.openhab) {
			contextCreator	= openhabCreator;
		}
		else if (library == ZWaveLibrary.zwave4j) {
			contextCreator	= zwave4jCreator;
		}
		else {
			throw new IllegalArgumentException("unsupported zwave library "+library);
		}
	}
	
	@Validate
	protected void start() {
		super.start();
	}

	@Invalidate
	protected void stop() {
		super.stop();
	}

	@Override
	protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		ControllerDeclaration declaration = ControllerDeclaration.from(importDeclaration);

		LOG.info("Importing declaration for zwave device '{}' at port {}",declaration.getId(),declaration.getPort());

		Map<String,Object> properties= new HashMap<>();
		properties.put(ContextEntity.State.id(ZwaveController.class,ZwaveController.SERIAL_PORT),declaration.getPort());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.NEIGHBORS),new ArrayList<>());

		contextCreator.create(declaration.getId(),properties);
		handleImportDeclaration(importDeclaration);

	}

	@Override
	protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		ControllerDeclaration declaration = ControllerDeclaration.from(importDeclaration);

		LOG.info("Removing imported declaration for zwave device '{}' at port {}",declaration.getId(),declaration.getPort());

		contextCreator.delete(declaration.getId());
		unhandleImportDeclaration(importDeclaration);
	}


	public String getName() {
		return name;
	}

}
