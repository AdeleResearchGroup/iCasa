package fr.liglab.icasa.self.star.follow.me.exercice.five.sum.set.algorithm;

import fr.liglab.adele.icasa.dependency.handler.annotations.RequiresDevice;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

/**
 *
 */

@Component(name="DimmerLightFollowMeWithSumSetAlgorithm")
@Instantiate
@Provides(specifications = FollowMeConfiguration.class)
public class DimmerLightFollowMeWithSumSetAlgorithmImpl implements DeviceListener,FollowMeConfiguration {

    /**
     * The targeted illuminance in each room
     **/
    private double targetedIlluminance = 7000.d;

    /**
     * Watt to lumens conversion factor
     * It has been considered that: 1 Watt=680.0 lumens at 555nm.
     */
    public final static double ONE_WATT_TO_ONE_LUMEN = 680.0d;

    public static final double ZONE_SCALE_FACTOR = 0.014d;

    /**
     * The maximum number of lights to turn on when a user enters the room :
     **/
    private int maxLightsToTurnOnPerRoom = 2;

    /**
     * The maximum energy consumption allowed in a room in Watt:
     **/
    private double maximumEnergyConsumptionAllowedInARoom = 145.0d;

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    /** Field for binaryLights dependency */
    @RequiresDevice(id="binaryLights", type="field", optional=true)
    private BinaryLight[] binaryLights;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="presenceSensors", type="field", optional=true)
    private PresenceSensor[] presenceSensors;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="dimmerLights", type="field", optional=true)
    private DimmerLight[] dimmerLights;

    /** Field for presenceSensors dependency */
    @RequiresDevice(id="photometers", type="field", optional=true)
    private Photometer[] photometers;


    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="bind")
    public void bindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        System.out.println("bind binary light " + binaryLight.getSerialNumber());
        binaryLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="binaryLights", type="unbind")
    public void unbindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
        System.out.println("unbind binary light " + binaryLight.getSerialNumber());
        binaryLight.removeListener(this);
    }

    /**
     * Bind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="bind")
    public void bindPresenceSensor(PresenceSensor presenceSensor, Map<Object, Object> properties) {
        System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
        presenceSensor.addListener(this);

    }

    /**
     * Unbind Method for PresenceSensors dependency.
     * This method will be used to manage device listener.
     */
    @RequiresDevice(id="presenceSensors", type="unbind")
    public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
        System.out.println("Unbind presence sensor "+ presenceSensor.getSerialNumber());
        presenceSensor.removeListener(this);
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLights", type="bind")
    public void bindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        System.out.println("bind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.addListener(this);
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="dimmerLights", type="unbind")
    public void unbindDimmerLight(DimmerLight dimmerLight, Map<Object, Object> properties) {
        System.out.println("unbind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.removeListener(this);
    }

    /**
     * Bind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="photometers", type="bind")
    public void bindPhotometers(Photometer photometer, Map<Object, Object> properties) {
        System.out.println("bind dimmer light " + photometer.getSerialNumber());
    }

    /**
     * Unbind Method for binaryLights dependency.
     * This method is not mandatory and implemented for debug purpose only.
     */
    @RequiresDevice(id="photometers", type="unbind")
    public void unbindPhotometers(Photometer photometer, Map<Object, Object> properties) {
        System.out.println("unbind dimmer light " + photometer.getSerialNumber());
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
                /*
		 * It is extremely important to unregister the device listener.
		 * Otherwise, iCasa will continue to send notifications to the
		 * unpredictable and invalid component instance.
		 * This will also causes problem when the bundle is stopped as iCasa
		 * will still hold a reference on the device listener object.
		 * Consequently, it (and its bundle) won't be garbage collected
		 * causing a memory issue known as stale reference.
		 */
        for (PresenceSensor presenceSensor : presenceSensors) {
            presenceSensor.removeListener(this);
        }
        for(BinaryLight binaryLight  : binaryLights) {
            binaryLight.removeListener(this);
        }

        for(DimmerLight dimmerLight  : dimmerLights) {
            dimmerLight.removeListener(this);
        }
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");
    }

    @Override
    public void deviceAdded(GenericDevice device) {
        System.out.println("Device Added");
    }

    @Override
    public void deviceRemoved(GenericDevice device) {
        System.out.println("Device Removed");
    }


    /**
     * This method is part of the DeviceListener interface and is called when a
     * subscribed device property is modified.
     *
     * @param device
     *            is the device whose property has been modified.
     * @param propertyName
     *            is the name of the modified property.
     */
    @Override
    public void devicePropertyModified(GenericDevice device,String propertyName, Object oldValue, Object newValue) {

        System.out.println("Device Property Modified");
        //we assume that we listen only to presence sensor events (otherwise there is a bug)
        if (device instanceof PresenceSensor ){
            System.out.println("PRESENCE SENSOR ");
            PresenceSensor changingSensor = (PresenceSensor) device;
            presenceModified( changingSensor, propertyName,  oldValue,  newValue);
        }

        if (device instanceof BinaryLight ){
            System.out.println("Binary Light ");
            BinaryLight changingBinaryLight = (BinaryLight) device;
            binaryLightModified( changingBinaryLight, propertyName, oldValue,newValue);
        }

        if (device instanceof DimmerLight ){
            System.out.println("Dimmer Light ");
            DimmerLight changingDimmerLight = (DimmerLight) device;
            dimmerLightModified( changingDimmerLight, propertyName, oldValue,newValue);
        }
    }



    @Override
    public void devicePropertyAdded(GenericDevice device, String propertyName) {
        System.out.println("Device Property Added");
    }

    @Override
    public void devicePropertyRemoved(GenericDevice device, String propertyName) {
        System.out.println("Device Property Removed");
    }

    @Override
    public void deviceEvent(GenericDevice device, Object data) {
        System.out.println("Device Event");
    }

    /**
     * Return all BinaryLight from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<BinaryLight> getBinaryLightFromLocation(String location) {
        List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
        for (BinaryLight binLight : binaryLights) {
            if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                binaryLightsLocation.add(binLight);
            }
        }
        return binaryLightsLocation;
    }

    /**
     * Return all BinaryLight from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<DimmerLight> getDimmerLightFromLocation(String location) {
        List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
        for (DimmerLight dimmerLight : dimmerLights) {
            if (dimmerLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                dimmerLightsLocation.add(dimmerLight);
            }
        }
        return dimmerLightsLocation;
    }

    private synchronized int getLightTurnOn(String location) {
        int countTurnOn= 0;
        //count binary light on
        countTurnOn += getBinaryLightTurnOn(getBinaryLightFromLocation(location));
        //count dimmer light on
        countTurnOn += getDimmerLightTurnOn(getDimmerLightFromLocation(location));
        return countTurnOn;
    }

    private synchronized int getBinaryLightTurnOn(List<BinaryLight> listBinaryLights) {
        int countTurnOn= 0;
        for (BinaryLight binLight : listBinaryLights) {
            if (binLight.getPowerStatus()) {
                countTurnOn +=1;
            }
        }
        return countTurnOn;
    }

    private synchronized int getDimmerLightTurnOn(List<DimmerLight> listDimmerLights) {
        int countTurnOn= 0;
        for (DimmerLight binLight : listDimmerLights) {
            if (binLight.getPowerLevel() > 0) {
                countTurnOn +=1;
            }
        }
        return countTurnOn;
    }

    private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location) {
        List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        for (PresenceSensor presenceSensor : presenceSensors) {
            if (presenceSensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                presenceSensorLocation.add(presenceSensor);
            }
        }
        return presenceSensorLocation;
    }

    private synchronized List<Photometer> getPhotometerFromLocation(String location) {
        List<Photometer> photometerLocation = new ArrayList<Photometer>();
        for (Photometer photometer : photometers) {
            if (photometer.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                photometerLocation.add(photometer);
            }
        }
        return photometerLocation;
    }

    private synchronized double getConsumption(List<BinaryLight> binaryLightList,List<DimmerLight> dimmerLightList) {
        double sum = 0.0;
        int count = 0 ;
        for (DimmerLight dimmerLight : dimmerLightList) {
            sum +=dimmerLight.getMaxPowerLevel()*dimmerLight.getPowerLevel();
        }
        for (BinaryLight binaryLight : binaryLightList) {
            sum +=binaryLight.getMaxPowerLevel();
        }
        return sum;
    }

    private synchronized double getIlluminanceInRoom(String location) {
        List<Photometer> photometerLocation = getPhotometerFromLocation(location);
        double sum = 0.0;
        int count = 0 ;
        for (Photometer photometer : photometerLocation) {
            sum +=photometer.getIlluminance();
            count ++;
        }
        return (sum/count);
    }

    private synchronized boolean presenceFromLocation(String location) {
        int switchOn = 0;
        int switchOff = 0;
        List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        presenceSensorLocation = getPresenceSensorFromLocation(location);
        for (PresenceSensor presenceSensor : presenceSensorLocation) {
            if (presenceSensor.getSensedPresence()) {
                switchOn +=1;
            }
            else{
                switchOff +=1;
            }
        }
        if (switchOn > switchOff){
            return true;
        }
        else{
            return false;
        }
    }

    private synchronized List<String> getLocationWherePresence() {
        List<String> listOfLocationWithPresence= new ArrayList<String>();
        List<String>  listOfLocationCheck= new ArrayList<String>();
        for(PresenceSensor presenceSensor : presenceSensors){
            String location = (String) presenceSensor.getPropertyValue(presenceSensor.LOCATION_PROPERTY_NAME);
            if (!listOfLocationCheck.contains(location)){
                if(presenceFromLocation(location)){
                    listOfLocationWithPresence.add(location);
                }

                listOfLocationCheck.add(location);
            }
        }
        return listOfLocationWithPresence;
    }

    private synchronized void setOffAllLightsInLocation(String location) {

        List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(location);
        List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(location);
        for(BinaryLight binaryLight : sameLocationLigths){
            binaryLight.turnOff();
        }

        for(DimmerLight dimmerLight : sameLocationDimmerLigths){
            dimmerLight.setPowerLevel(0);
        }
    }

    /*
    private synchronized void applyMaximumNumberOfLightTurnOn(Set<Zone> zoneToApply) {
        for(Zone zone : zoneToApply){

          int zoneX = zone.getXLength();
            int zoneY = zone.getYLength();
            double zoneSquare = zoneX*ZONE_SCALE_FACTOR*zoneY*ZONE_SCALE_FACTOR;
            System.out.println("SURFACE " + zoneSquare);
            List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(zone.getId());
            List<BinaryLight> sameLocationBinaryLigths = getBinaryLightFromLocation(zone.getId());
            List<Photometer> sameLocationPhotometer = getPhotometerFromLocation(zone.getId());
            double valueToAjust = (targetedIlluminance*zoneSquare) / (ONE_WATT_TO_ONE_LUMEN * 100.0);
            System.out.println("Value to ajust " + valueToAjust);
            int binaryLightToTurnOnvalueToAjust
            if (valueToAjust < 1 ){
                for(DimmerLight dimmerLight: sameLocationDimmerLigths){
                    dimmerLight.setPowerLevel(valueToAjust);
                }
            }else{
                for(DimmerLight dimmerLight: sameLocationDimmerLigths){
                    dimmerLight.setPowerLevel(1);
                }
            }
            for(Photometer photometer: sameLocationPhotometer){
                System.out.println("Photometer " + photometer.getIlluminance());
                System.out.println("Calculate " + ( 1*100*ONE_WATT_TO_ONE_LUMEN/zoneSquare));
            }

        }
    }*/

    private synchronized void applyMaximumNumberOfLightTurnOn(List<String> locations) {
        for(String location : locations){


            List<Photometer> sameLocationPhotometer = getPhotometerFromLocation(location);
            List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(location);
            List<BinaryLight> sameLocationBinaryLigths = getBinaryLightFromLocation(location);
            setOffAllLightsInLocation(location);
            int maxBinaryOn = sameLocationBinaryLigths.size();
            int maxDimmerOn = sameLocationDimmerLigths.size();
            int countLighton = 0;
            int countDimmerOn = 0;
            int countbinary = 0;

            boolean tryBinary = true;
            boolean tryDimmer = false;
            boolean tryToAjust = false;

            double valueToAjust = 1.0;
            double valueToAjustBefore = 0.0;
            List<DimmerLight> listOfDimmerOn = new ArrayList<DimmerLight>();
            List<BinaryLight> listOfBinaryOn = new ArrayList<BinaryLight>();
            if (!sameLocationPhotometer.isEmpty()){
                if (getIlluminanceInRoom(location) > (targetedIlluminance)) {
                    break;
                }
                while( !( ( (targetedIlluminance*(0.97)) < getIlluminanceInRoom(location) ) && ((getIlluminanceInRoom(location) < (targetedIlluminance*(1.03)))) ) ){

                    if ( (getIlluminanceInRoom(location) > (targetedIlluminance*(1.03)))){
                        if (tryBinary) {
                            listOfBinaryOn.get(countLighton-1).turnOff();
                            listOfBinaryOn.remove(countLighton-1);
                            countLighton --;
                        }else if (tryDimmer) {
                            if (tryToAjust){
                                countDimmerOn --;
                                countLighton--;
                                listOfDimmerOn.remove(countDimmerOn);
                                double temp;
                                temp = valueToAjust;
                                valueToAjust -= Math.abs(valueToAjust-valueToAjustBefore)/2;
                                valueToAjustBefore = temp;
                            }else{
                                valueToAjust = 1.0;
                                valueToAjustBefore = 0.0;
                                listOfDimmerOn.remove(countDimmerOn-1)   ;
                            }
                        }
                    }

                    if ( (getIlluminanceInRoom(location) < (targetedIlluminance*(0.97)))){
                        if (tryDimmer) {
                            if(valueToAjust != 1.0){
                                if (tryToAjust){
                                    double temp;
                                    temp = valueToAjust;
                                    valueToAjust += Math.abs(valueToAjust-valueToAjustBefore)/2;
                                    valueToAjustBefore = temp;
                                    countDimmerOn --;
                                    countLighton--;
                                    listOfDimmerOn.remove(countDimmerOn);
                                }
                            }
                        }
                    }

                    if (getConsumption(listOfBinaryOn,listOfDimmerOn) >= maximumEnergyConsumptionAllowedInARoom){
                        if (tryBinary){
                            if (maxDimmerOn != 0 ){
                                BinaryLight binaryLight = sameLocationBinaryLigths.get(countbinary-1);
                                binaryLight.turnOff();
                                listOfBinaryOn.remove(countbinary-1);
                                countbinary -=1;
                                countLighton -=1;
                                tryDimmer = true;
                                tryBinary = false;
                            }else{
                                break;
                            }
                        }else if (tryDimmer) {
                            if (tryToAjust){
                                DimmerLight dimmerLight = sameLocationDimmerLigths.get(countDimmerOn-1);
                                dimmerLight.setPowerLevel(valueToAjustBefore);
                                valueToAjustBefore -= (getConsumption(listOfBinaryOn,listOfDimmerOn) - maximumEnergyConsumptionAllowedInARoom) / 100 ;
                                dimmerLight = sameLocationDimmerLigths.get(countDimmerOn-1);
                                dimmerLight.setPowerLevel(valueToAjustBefore);
                                break;

                            }
                        }
                    }

                    if (countLighton == maxLightsToTurnOnPerRoom){
                        break;
                    }

                    if (tryBinary){
                        if(countbinary < maxBinaryOn){
                            BinaryLight binaryLight = sameLocationBinaryLigths.get(countbinary);
                            binaryLight.turnOn();
                            listOfBinaryOn.add(binaryLight);
                            countLighton ++;
                            countbinary ++;
                        }else{
                            tryBinary = false;
                            tryDimmer = true;
                        }
                    }else if (tryDimmer){
                        if(countDimmerOn < maxDimmerOn){
                            tryToAjust = true;
                            DimmerLight dimmerLight = sameLocationDimmerLigths.get(countDimmerOn);
                            dimmerLight.setPowerLevel(valueToAjust);
                            listOfDimmerOn.add(dimmerLight);
                            countDimmerOn ++;
                            countLighton++;
                        }else{
                            tryDimmer = false;
                            break;
                        }
                    }

                    if((((targetedIlluminance*(0.97)) < getIlluminanceInRoom(location))  && (getIlluminanceInRoom(location) < (targetedIlluminance*(1.03))) )){
                        if (countLighton > maxLightsToTurnOnPerRoom){
                            if (tryBinary){
                                BinaryLight binaryLight = sameLocationBinaryLigths.get(countbinary-1);
                                binaryLight.turnOff();
                                listOfBinaryOn.remove(countLighton-1);
                                break;

                            }
                            if (tryDimmer){
                                DimmerLight dimmerLight = sameLocationDimmerLigths.get(countDimmerOn-1);
                                dimmerLight.setPowerLevel(valueToAjust);
                                break;
                            }


                        }else if (getConsumption(listOfBinaryOn,listOfDimmerOn) >= maximumEnergyConsumptionAllowedInARoom){
                            if (tryBinary){
                                BinaryLight binaryLight = sameLocationBinaryLigths.get(countbinary-1);
                                binaryLight.turnOff();
                                listOfBinaryOn.remove(countbinary-1);
                                countbinary -=1;
                                tryDimmer = true;
                                tryBinary = false;
                            }else if (tryDimmer) {
                                if (tryToAjust){
                                    valueToAjust -= (getConsumption(listOfBinaryOn,listOfDimmerOn) - maximumEnergyConsumptionAllowedInARoom)/100;
                                    DimmerLight dimmerLight = sameLocationDimmerLigths.get(countDimmerOn-1);
                                    dimmerLight.setPowerLevel(valueToAjust/100.0);
                                    break;
                                }
                            }
                        }else{
                            break;
                        }
                    }
                }
            }else{
                applySumSetAlgo(location);
            }

        }
    }

    private synchronized void applySumSetAlgo(String location) {
        // get the related binary lights
        List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(location);
        List<DimmerLight> sameLocationDimmerLigths = getDimmerLightFromLocation(location);

        double[] items = new double[sameLocationLigths.size()] ;
        int i=0;
        boolean dimmerToAjust = false;
        Map<String,Double> MapOfBinaryConsumption = new HashMap<String, Double>();
        for (BinaryLight binaryLight : sameLocationLigths) {
            items[i] = binaryLight.getMaxPowerLevel();
            i ++;
            MapOfBinaryConsumption.put(binaryLight.getSerialNumber(),binaryLight.getMaxPowerLevel());
        }

        List<Double> listOfDimmerConsumption = new ArrayList<Double>();
        Map<String,Double> MapOfDimmerConsumption = new HashMap<String, Double>();
        for (DimmerLight dimmerLight : sameLocationDimmerLigths) {
            listOfDimmerConsumption.add(dimmerLight.getMaxPowerLevel());
            MapOfDimmerConsumption.put(dimmerLight.getSerialNumber(),dimmerLight.getMaxPowerLevel());
        }
        // Compute the best combination :
        double[] result = ClosestSumAlgorithm.greadySubSetClosestSum(maximumEnergyConsumptionAllowedInARoom, items);
        double sumCalculate = 0.0;
        for (double _double : result) {
            sumCalculate += _double;
        }
        List<Double> listResults = new ArrayList<Double>();
        for(double _double : result){
            if(Double.valueOf(_double) != 0.0)
                listResults.add((Double.valueOf(_double)));
        }

        Collections.sort(listOfDimmerConsumption, Collections.reverseOrder());
        Collections.sort(listResults, Collections.reverseOrder());
        double valueToAjust = 0.0;

        if (listResults.size()<= maxLightsToTurnOnPerRoom){
            if (sumCalculate != maximumEnergyConsumptionAllowedInARoom ){
                valueToAjust = maximumEnergyConsumptionAllowedInARoom - sumCalculate;
                dimmerToAjust = true;
            }
        }else{
            int k ;
            System.out.println(listResults.size() +" - "+ maxLightsToTurnOnPerRoom);
            for( k =0 ;(k< (listResults.size() - maxLightsToTurnOnPerRoom ));k ++){
                listResults.remove(listResults.size() -k );
            }
            if(k !=0 ){
                if (listResults.get(listResults.size()-1) < sameLocationDimmerLigths.get(0).getMaxPowerLevel()){
                    listResults.remove(listResults.size()-1);
                    sumCalculate = 0.0;
                    for (Double _double : listResults) {
                        sumCalculate += _double.doubleValue();
                    }
                    valueToAjust = maximumEnergyConsumptionAllowedInARoom - sumCalculate;
                    dimmerToAjust = true;
                }
            }
        }

        List<BinaryLight> binaryOn = new ArrayList<BinaryLight>();
        int countLightOn = 0 ;
        for (Double consumptionDouble : listResults) {
            double consumption = consumptionDouble.doubleValue();
            for (BinaryLight binaryLight :sameLocationLigths ){
                if (!binaryOn.contains(binaryLight)){
                    if (consumption == binaryLight.getMaxPowerLevel()){
                        countLightOn ++;
                        binaryLight.turnOn();
                        binaryOn.add(binaryLight);
                        break;
                    }
                }
            }
        }

        for (BinaryLight binaryLight :sameLocationLigths ){
            if (!binaryOn.contains(binaryLight)){
                binaryLight.turnOff();
            }
        }

        if (dimmerToAjust){
            for (DimmerLight dimmerLight : sameLocationDimmerLigths) {
                //check if we can turn off more lights
                if ((countLightOn < maxLightsToTurnOnPerRoom ) && ( valueToAjust != 0.0) ){
                    if (valueToAjust >= dimmerLight.getMaxPowerLevel()){
                        dimmerLight.setPowerLevel(1);
                        countLightOn ++;
                        valueToAjust -= dimmerLight.getMaxPowerLevel();
                    }else {
                        dimmerLight.setPowerLevel(valueToAjust/100);
                        countLightOn ++;
                        valueToAjust = 0.0;
                    }
                }else{
                    dimmerLight.setPowerLevel(0);
                }
            }
        }

    }


    private synchronized void dimmerLightModified(DimmerLight changingDimmerLight,String propertyName, Object oldValue, Object newValue) {

        if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)){
            String dimmerLightLocation = (String) changingDimmerLight.getPropertyValue(LOCATION_PROPERTY_NAME);
            if (!dimmerLightLocation.equals(LOCATION_UNKNOWN)) {
                List<String> locations = new ArrayList<String>();
                locations.add(dimmerLightLocation);
                if (presenceFromLocation(dimmerLightLocation)){
                    applyMaximumNumberOfLightTurnOn(locations);
                }else{
                    setOffAllLightsInLocation(dimmerLightLocation);
                }



                String oldLocation = (String) oldValue;
                if (!oldLocation.equals(LOCATION_UNKNOWN)) {
                    List<String> oldLocations = new ArrayList<String>();
                    oldLocations.add(oldLocation);
                    //check if in the old location have always a person
                    if (presenceFromLocation(oldLocation)){
                        applyMaximumNumberOfLightTurnOn(oldLocations);
                    }else{
                        setOffAllLightsInLocation(oldLocation);
                    }
                }
            }
            else{
                changingDimmerLight.setPowerLevel(0);
            }
        }
    }

    private synchronized void presenceModified(PresenceSensor changingSensor,String propertyName, Object oldValue, Object newValue) {
        // check the change is related to presence sensing
        if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
            // get the location where the sensor is:
            String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
            // if the location is known :
            if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
                if ((Boolean) oldValue){
                    setOffAllLightsInLocation(detectorLocation);
                }else{
                    List<String> locations = new ArrayList<String>();
                    locations.add(detectorLocation);
                    applyMaximumNumberOfLightTurnOn(locations);
                }

            }
        }
    }

    private synchronized void binaryLightModified(BinaryLight changingBinaryLight,String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)){
            String binaryLightLocation = (String) changingBinaryLight.getPropertyValue(LOCATION_PROPERTY_NAME);

            if (!binaryLightLocation.equals(LOCATION_UNKNOWN)) {
                List<String> locations = new ArrayList<String>();
                locations.add(binaryLightLocation);
                if (presenceFromLocation(binaryLightLocation)){
                    applyMaximumNumberOfLightTurnOn(locations);
                }else{
                    setOffAllLightsInLocation(binaryLightLocation);
                }



                String oldLocation = (String) oldValue;
                if (!oldLocation.equals(LOCATION_UNKNOWN)) {
                    List<String> oldLocations = new ArrayList<String>();
                    oldLocations.add(oldLocation);
                    //check if in the old location have always a person
                    if (presenceFromLocation(oldLocation)){
                        applyMaximumNumberOfLightTurnOn(oldLocations);
                    }else{
                        setOffAllLightsInLocation(oldLocation);
                    }
                }
            }
            else{
                changingBinaryLight.turnOff();
            }
        }
    }


    @Override
    public int getMaximumNumberOfLightsToTurnOn() {
        return maxLightsToTurnOnPerRoom;
    }

    @Override
    public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
        maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
        List<String> listOflocation = getLocationWherePresence();
        applyMaximumNumberOfLightTurnOn(listOflocation);
    }

    @Override
    public double getMaximumAllowedEnergyInRoom() {
        return maximumEnergyConsumptionAllowedInARoom;
    }

    @Override
    public void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
        maximumEnergyConsumptionAllowedInARoom = maximumEnergy;
        List<String> listOflocation = getLocationWherePresence();
        applyMaximumNumberOfLightTurnOn(listOflocation);
    }

    @Override
    public double getTargetedIlluminance() {
        return targetedIlluminance;
    }

    @Override
    public void setTargetedIlluminance(double illuminance) {
        targetedIlluminance = illuminance;
        List<String> listOflocation = getLocationWherePresence();
        applyMaximumNumberOfLightTurnOn(listOflocation);
    }

}
