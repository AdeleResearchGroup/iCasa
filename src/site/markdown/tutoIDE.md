# Tutorial: Light Follow Me

The principle of the *Light Follow Me* application is to switch on/off the lights in the different rooms if there is somebody or anybody. The following screenshot is an example of the application in action:

![Follow Me in action](tutorial/followMe.png "Follow Me")

## 1. Prerequisite

- Install Java Development Kit 6 (Avoid Java 7 that may cause troubles).
- Install iCasa-IDE following installation instructions on <a href="http://adeleresearchgroup.github.com/iCasa-IDE/">iCasa-IDE website</a>.
- Install an iCasa distribution. We recommend to use the iCasa Teaching distribution (See <a href="download.html">download page</a>).

## 2. Application Development

### 2.1. Project Creation and Skeleton Generation

You need to create and generate the skeleton of the unique class of your application.

1. Create a new iPOJO Project.

   ![Create New iPojo Projet](tutorial/fig1-project.png "Create New iPojo Projet")

   ![Create New iPojo Projet](tutorial/fig2-iPOJOProject.png "Create New iPojo Projet")

   ![iPojo Projet Content](tutorial/fig4-iPOJOProjectContent.png "iPojo Projet Content")

2. Configure the iPOJO Preferences. Go to *Windows -> Preferences -> iPOJO Preferences.*

   ![iPojo Preferences](tutorial/fig5-setiPojoPreferences.png "iPojo Preferences")

3. Create a new component *BinaryFollowMe*. Open the `metadata.xml` file with the iPOJO Metadata Editor.

4. Click *Add* and change the component name to *BinaryFollowMe*.

    ![Create BinaryFollowMe Component](tutorial/fig6-metadataEditor.png "Create BinaryFollowMe Component")

5. Add to service dependencies (i.e. required services) with **Multiple** and **Optional** characteristics:

   * one dependency to `BinaryLight` with a field `binaryLights` and bind/unbind methods named respectively `bindBinaryLight` and `unbindBinaryLight`;
   * one dependency to `PresenceSensor` with a field `presenceSensors` and bind/unbind methods named respectively `bindPresenceSensor` and `unbindPresenceSensor`.

   ![Add binary light dependency](tutorial/fig7-binaryLightDependency.png "Add binary light dependency")

   ![Add presence sensor dependency](tutorial/fig8-presenceSensorDependency.png "Add presence sensor dependency")

6. Add new methods `start` and `stop` for the *Component Lifecycle Callbacks*.

   ![Setup component lifecycle](tutorial/fig9-lifecycle.png "Setup component lifecycle")

7. Generate the class with right click on your component.

   ![Generate component implementation Action](tutorial/fig10-generate.png "Generate component implementation Action")

8. Complete the package field.

   ![Generate component implementation Setup](tutorial/fig11-generate2.png "Generate component implementation Setup")

9. If you have done that successfully, you will have a skeleton like this:
  
   <pre><code>package com.example.binary.follow.me;
       
   import fr.liglab.adele.icasa.device.light.BinaryLight;
   import fr.liglab.adele.icasa.device.presence.PresenceSensor;
   import java.util.Map;
   
   public class BinaryLightFollowMeImpl {
     
      /** Field for binaryLights dependency */
      private BinaryLight[] binaryLights;
      
      /** Field for presenceSensors dependency */
      private PresenceSensor[] presenceSensors;
      
      /** Bind Method for null dependency */
      public void bindBinaryLight(BinaryLight binaryLight, Map properties) {
         // TODO: Add your implementation code here
      }
      
      /** Unbind Method for null dependency */
      public void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
         // TODO: Add your implementation code here
      }
      
      /** Bind Method for null dependency */
      public void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
         // TODO: Add your implementation code here
      }
      
      /** Unbind Method for null dependency */
      public void unbindPresenceSensor(PresenceSensor presenceSensor,
      	Map properties) {
         // TODO: Add your implementation code here
      }
      
      /** Component Lifecycle Method */
      public void stop() {
         // TODO: Add your implementation code here
      }
       
      /** Component Lifecycle Method */
      public void start() {
         // TODO: Add your implementation code here
      }
   }</code></pre>

### 2.2. Complete the Code Skeleton

1. In order to be notified when something is modified in the environment, the `BinaryLightFollowMeImpl` class must implement `DeviceListener` interface.
   
   <pre><code>public class BinaryLightFollowMeImpl implements DeviceListener</code></pre>

2. Add two attributes in this `BinaryLightFollowMeImpl` class:
   * `listBinaryLight` is a list containing all the lights available in the environment;
   * `mapPresenceSensor`is a map containing all the presence sensors available in the environment.
   
        <pre><code>/** List containing all the lights in the house */
     private List<BinaryLight> listBinaryLights;
      
     /** Map containing all the presenceSensors in the house */
	 private Map<String, PresenceSensor> mapPresenceSensors;</code></pre>
     
3. Complete the code of `bind` and `unbind` methods by adding and removing devices from their respective sets.
 
   <pre><code>/** Bind Method for null dependency */
   public void bindBinaryLight(BinaryLight binaryLight, Map properties) {
       listBinaryLights.add(binaryLight);
   }

   /** Unbind Method for null dependency */
   public void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
       listBinaryLights.remove(binaryLight);
   }

   /** Bind Method for null dependency */
   public void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
	   mapPresenceSensors.put(presenceSensor.getSerialNumber(), presenceSensor);
   }

   /** Unbind Method for null dependency */
   public void unbindPresenceSensor(PresenceSensor presenceSensor,
	  	Map properties) {
	   mapPresenceSensors.remove(presenceSensor.getSerialNumber());
   }</code></pre>

4. Attach the listener to the interesting devices (in our case all the presence sensors) in the `bind` method. Also unregister the listener when the sensor is leaving in the `unbind` method.

   <pre><code>/** Bind Method for null dependency */
   public void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
	   presenceSensor.addListener(this);
	   mapPresenceSensors.put(presenceSensor.getSerialNumber(), presenceSensor);
   }

   /** Unbind Method for null dependency */
   public void unbindPresenceSensor(PresenceSensor presenceSensor,
		Map properties) {
	   presenceSensor.removeListener(this);
	   mapPresenceSensors.remove(presenceSensor.getSerialNumber());
   }</code></pre>

5. Complete the `start` and `stop` lifecycle methods with a message. Initialize the required fields at validate and clear those fields at invalidate.

   <pre><code>/** Component Lifecycle Method */
   public void stop() {
	   System.out.println("Component is stopping...");
	   listBinaryLights = null;
	   mapPresenceSensors = null;
   }

   /** Component Lifecycle Method */
   public void start() {
	   System.out.println("Component is starting...");
	   listBinaryLights = new ArrayList<BinaryLight>();
	   mapPresenceSensors = new HashMap<String, PresenceSensor>();
   }</code></pre>

6. Create a method named `getBinaryLightFromLocation`. This method will create a list of all binary lights from a location.

   <pre><code>/**
    * Method which	catches all BinaryLights from a location
    * @param location
    * @return List of BinaryLights
    */
    private List<BinaryLight> getBinaryLightFromLocation(String location) {
	   List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
	
  	   for(BinaryLight binaryLight : listBinaryLights) {
		   if(binaryLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
 			   binaryLightsLocation.add(binaryLight);
		   }
	   }
	   return binaryLightsLocation;
    }</code></pre>
   `LOCATION_PROPERTY_NAME` is a constant defined and intialized previously in this class:
   <pre><code>public static String LOCATION_PROPERTY_NAME = "Location";</code></pre>

7. Manage the light(s) if a presence is sensed in a location. In the method `devicePropertyModified`, there is the core of the application: if the device is an instance of `PresenceSensor`, the device is identified by its serial number. According to its location, all the lights in the same location are turn on or off. 

   <pre><code>@Override
   public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		if(device instanceof PresenceSensor) {
			PresenceSensor activSensor = mapPresenceSensors.get(device.getSerialNumber());
		    if(activSensor != null && propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
		    	String detectorLocation = (String) activSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
		    	if(!detectorLocation.equals(LOCATION_UNKNOW)) {
		    		List<BinaryLight> sameLocationLights = getBinaryLightFromLocation(detectorLocation);
		    		for(BinaryLight binaryLight : sameLocationLights) {
		    			binaryLight.setPowerStatus(!(Boolean) oldValue);
		    		}
		    	}
		    }
		}
	}</code></pre>

   `LOCATION_UNKNOWN` is a constant defined and intialized previously in this class:
   <pre><code>public static String LOCATION_UNKNOWN = "unknown";</code></pre>
   
8. Create a component instance from the metadata.xml file with the *iPOJO Metadata Editor*. Open the second tab *Component Configuration*. Right click on *BinaryLightFollowMe* bundle -> New Instance Declaration. 
   
   ![Create an instance](tutorial/fig12-instance.png "Create an instance")


## 3. Application Deployment and Test

1. Right click on your project: *iCasa -> iCasa Bundle Deployment*
   
   ![Deploy action](tutorial/fig13-deploy.png "Deploy action")
   
2. Test your application with iCasa Simulator.
