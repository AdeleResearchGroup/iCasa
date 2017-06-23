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

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.zwave.device.api.ZwaveDevice;



import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
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
public class DeviceImporter extends AbstractImporterComponent  {

	public enum SupportedDeviceType {

		AeonMULTISENSOR{

			public String getDescription() {
				return "Aeon Multi Sensor";
			};

			public boolean matches(DeviceDeclaration importDeclaration) {

				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();

				return (manufactererId == 0x0086) &&
						(deviceType == 0x0002) &&
						(deviceId == 0x004A);

			}
		},
		AeonSMARTENERGYILLUMINATOR {

			public String getDescription() {
				return "Aeon Smart Energy Illuminator";
			};

			public boolean matches(DeviceDeclaration importDeclaration) {

				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();

				return (manufactererId == 0x0086) &&
						(deviceType == 0x0003) &&
						(deviceId == 0x0008);

			}
		},
		AeonDSD37 {

			public String getDescription() {
				return "Aeon Repeater Slave";
			};

			public boolean matches(DeviceDeclaration importDeclaration) {

				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();

				return (manufactererId == 0x0086) &&
						(deviceType == 0x0004) &&
						(deviceId == 0x0025);

			}
		},
		FibaroGSS001 {

			public String getDescription() {
				return "Fibaro Smoke Sensor";
			};

			public boolean matches(DeviceDeclaration importDeclaration) {

				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();

				return (manufactererId == 0x10F) &&
						(deviceType == 0x0C02) &&
						(deviceId == 0x1002);

			}
		},

		FibaroGFMS001 {
			
			public String getDescription() {
				return "Fibaro Multi Sensor";
			};
			
			public boolean matches(DeviceDeclaration importDeclaration) {
				
				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();
				
				return (manufactererId == 0x10F) &&
					   (deviceType == 0x0800 || deviceType == 0x0801) &&
					   (deviceId == 0x1001 || deviceId == 0x2001 || (deviceId == 0x3001 && deviceType == 0x0800));
				
			}
		},
		
		FibaroFGWPE101 {

			public String getDescription() {
				return "Fibaro Wall Plug";
			};
			
			public boolean matches(DeviceDeclaration importDeclaration) {
				
				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();
				
				return (manufactererId == 0x10F) &&
					   (deviceType == 0x0600) &&
					   (deviceId == 0x1000);
				
			}
			
		},
		
		FibaroFGK101 {
			
			public String getDescription() {
				return "Fibaro Door Window Sensor";
			};
			
			public boolean matches(DeviceDeclaration importDeclaration) {

				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();
				
				return (manufactererId == 0x10F) &&
					   (deviceType == 0x0700) &&
					   (deviceId == 0x1000 || deviceId == 0x2000 || deviceId == 0x3000);				
			}
		},
		
		
		FibaroFGSD002 {
			
			public String getDescription() {
				return "Fibaro Smoke Sensor";
			};
			
			public boolean matches(DeviceDeclaration importDeclaration) {
				
				int manufactererId 	= importDeclaration.getZwaveManufacturerId();
				int deviceType 		= importDeclaration.getzwaveDeviceType();
				int deviceId 		= importDeclaration.getZwaveDeviceId();
				
				return (manufactererId == 0x10F) &&
					   (deviceType == 0x0C02) &&
					   (deviceId == 0x1002);				
			}
		};
	
		/**
		 * The description of the device type
		 */
		public abstract String  getDescription();

		/**
		 * Whether this type matches the import declaration
		 */
		public abstract boolean matches(DeviceDeclaration importDeclaration);
		
		/**
		 * Get the supported device type, if any, matching a giving declaration
		 */
		public static final SupportedDeviceType getMatching(DeviceDeclaration importDeclaration) {
		
			for (SupportedDeviceType deviceType : SupportedDeviceType.values()) {
				if (deviceType.matches(importDeclaration)) {
					return deviceType;
				}
			}
			
			return null;
		}
		
	}
	
	
	private static final Logger LOG = LoggerFactory.getLogger(DeviceImporter.class);

	private final Map<SupportedDeviceType, Creator.Entity<?>> openhabCreators;
	
	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.openhab.FibaroMotionSensor> 		openhabMotionSensorCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.openhab.FibaroWallPlug> 			openhabWallPlugCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.openhab.FibaroSmokeSensor> 		openhabSmokeSensorCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.openhab.FibaroDoorWindowSensor> 	openhabDoorWindowSensorCreator;


	
	private final Map<SupportedDeviceType, Creator.Entity<?>> zwave4jCreators;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.FibaroDoorWindowSensor>	zwave4jDoorWindowSensorCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.FibaroWallPlug> 			zwave4jWallPlugCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.FibaroMotionSensor> 			zwave4jFibaroMotionSensorCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.FibaroSmokeSensor> 			zwave4jFibaroSmokeSensorCreator;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.AeonRepeaterSlave> 			zwave4jAeonRepeaterSlave;

	@Creator.Field(origin = OriginEnum.local)
	Creator.Entity<fr.liglab.adele.zwave.device.proxies.zwave4j.AeonMultiSensor> 			zwave4jAeonMultiSensor;


	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(zwave.device.manufacturer.id=*)(zwave.device.id=*)(zwave.node.id=*)(zwave.device.type.id=*))")
	private String filter;

	public DeviceImporter(BundleContext context) {
		
		openhabCreators = new HashMap<>();
		
		openhabCreators.put(SupportedDeviceType.FibaroGFMS001,openhabMotionSensorCreator);
		openhabCreators.put(SupportedDeviceType.FibaroFGWPE101,openhabWallPlugCreator);
		openhabCreators.put(SupportedDeviceType.FibaroFGK101,openhabDoorWindowSensorCreator);
		openhabCreators.put(SupportedDeviceType.FibaroFGSD002,openhabSmokeSensorCreator);
		
		zwave4jCreators = new HashMap<>();

		zwave4jCreators.put(SupportedDeviceType.FibaroFGK101,zwave4jDoorWindowSensorCreator);
		zwave4jCreators.put(SupportedDeviceType.FibaroFGWPE101,zwave4jWallPlugCreator);
		zwave4jCreators.put(SupportedDeviceType.FibaroGFMS001,zwave4jFibaroMotionSensorCreator);
		zwave4jCreators.put(SupportedDeviceType.FibaroGSS001,zwave4jFibaroSmokeSensorCreator);
		zwave4jCreators.put(SupportedDeviceType.AeonDSD37,zwave4jAeonRepeaterSlave);
		zwave4jCreators.put(SupportedDeviceType.AeonSMARTENERGYILLUMINATOR,zwave4jWallPlugCreator);
		zwave4jCreators.put(SupportedDeviceType.AeonMULTISENSOR,zwave4jAeonMultiSensor);


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
	public String getName() {
		return name;
	}

	@Override
	protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
		
		String libraryName 		= (String) importDeclaration.getMetadata().get("library");
		ZWaveLibrary library 	= libraryName != null ? ZWaveLibrary.valueOf(libraryName) : null;
		
		if (library == null) {
			LOG.warn("Zwave library"+ libraryName +" not supported by iCasa");
			return;
		}

		Map<SupportedDeviceType, Creator.Entity<?>> creators = null;
		if 	(library == ZWaveLibrary.openhab) {
			creators	= openhabCreators;
		}
		else if (library == ZWaveLibrary.zwave4j) {
			creators	= zwave4jCreators;
		}

		if (creators == null) {
			LOG.warn("Zwave library"+ libraryName +" not supported by iCasa");
			return;
		}
		
		DeviceDeclaration deviceDeclaration = new DeviceDeclaration(importDeclaration);
		SupportedDeviceType deviceType		= SupportedDeviceType.getMatching(deviceDeclaration);

		if (deviceType == null) {
			LOG.warn("Zwave device type for node "+ deviceDeclaration.getZwaveNodeId() +" not supported by iCasa");
			return;
		}
		
		Creator.Entity<?> creator = creators.get(deviceType);

		if (creator == null){
			LOG.warn("Zwave device type for node "+ deviceDeclaration.getZwaveNodeId() +" not supported by iCasa using library "+library);
			return;
		}

		LOG.info(" Creating ZWave device : "+deviceType.getDescription()+ " node "+deviceDeclaration.getZwaveNodeId());
		
		Map<String,Object> properties = new HashMap<>();
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.HOME_ID),deviceDeclaration.getZwaveHomeId());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.NODE_ID),deviceDeclaration.getZwaveNodeId());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.DEVICE_ID),deviceDeclaration.getZwaveDeviceId());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.DEVICE_TYPE),deviceDeclaration.getzwaveDeviceType());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.MANUFACTURER_ID),deviceDeclaration.getZwaveManufacturerId());
		properties.put(ContextEntity.State.id(ZwaveDevice.class,ZwaveDevice.NEIGHBORS),new ArrayList<>());
		properties.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),"ZwaveDevice#"+deviceDeclaration.getZwaveNodeId());

		creator.create("ZwaveDevice#"+deviceDeclaration.getZwaveNodeId(),properties);
	}

	@Override
	protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		String libraryName = (String) importDeclaration.getMetadata().get("library");
		ZWaveLibrary library = ZWaveLibrary.valueOf(libraryName);
		
		if (library == null) {
			LOG.warn("Zwave library"+ libraryName +" not supported by iCasa");
			return;
		}

		Map<SupportedDeviceType, Creator.Entity<?>> creators = null;
		if 	(library == ZWaveLibrary.openhab) {
			creators	= openhabCreators;
		}
		else if (library == ZWaveLibrary.zwave4j) {
			creators	= zwave4jCreators;
		}

		if (creators == null) {
			LOG.warn("Zwave library"+ libraryName +" not supported by iCasa");
			return;
		}
		
		DeviceDeclaration deviceDeclaration = new DeviceDeclaration(importDeclaration);
		SupportedDeviceType deviceType		= SupportedDeviceType.getMatching(deviceDeclaration);

		if (deviceType == null) {
			LOG.warn("Zwave device type for node "+ deviceDeclaration.getZwaveNodeId() +" not supported by iCasa");
			return;
		}
		
		Creator.Entity<?> creator = creators.get(deviceType);

		if (creator == null){
			LOG.warn("Zwave device type for node "+ deviceDeclaration.getZwaveNodeId() +" not supported by iCasa using library "+library);
			return;
		}

		LOG.info(" Destroying ZWave device : "+deviceType.getDescription()+ " node "+deviceDeclaration.getZwaveNodeId());
		creator.delete("ZwaveDevice#"+deviceDeclaration.getZwaveNodeId());
	}


}
