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
package fr.liglab.adele.icasa.simulator.electricity.impl;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.location.*;
import fr.liglab.adele.icasa.simulator.PhysicalModel;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

/**
 *
 */
@Component(name = "electricity-model")
@Instantiate(name = "electricity-model-0")
@Provides(specifications = PhysicalModel.class)
public class ElectricityPMImpl implements PhysicalModel, ZoneListener, LocatedDeviceListener {


    public static final String ELECTRICITY_PROP_NAME = "Electricity";


    private Set<Variable> _computedVariables;

    private Set<LocatedDevice> _electricityDevices = new HashSet<LocatedDevice>();

    private Map<Zone,Double> mapOfConsumption = new HashMap<Zone,Double>();

    private final Object m_lock ;

    @Requires
    private ContextManager _contextMgr;

    public ElectricityPMImpl() {

        m_lock = new Object();

        this._computedVariables = new HashSet<Variable>();
        this._computedVariables.add(new Variable(ELECTRICITY_PROP_NAME, Double.class, "in Watt"));
    }

    @Validate
    private void start() {
        _contextMgr.addListener(this);
    }

    @Invalidate
    private void stop() {
        _contextMgr.removeListener(this);
    }


    @Override
    public void deviceAdded(LocatedDevice device) {
        if(!((device != null) && (device.getDeviceObject() instanceof PowerObservable))){
            return;
        }

        synchronized ( m_lock ){
            _electricityDevices.add(device);
            updateZones(getZones(device.getCenterAbsolutePosition()));
        }

    }

    @Override
    public void deviceRemoved(LocatedDevice device) {
        if(!((device != null) && (device.getDeviceObject() instanceof PowerObservable))){
            return;
        }
        synchronized ( m_lock ){
            Set<Zone> oldZone = getZones(device.getCenterAbsolutePosition());
            updateZones(oldZone);
            _electricityDevices.remove(device);
        }


    }

    @Override
    public void deviceMoved(LocatedDevice device, Position oldPosition, Position newPosition) {
        synchronized ( m_lock ){
            Set<Zone> oldZone = getZones(oldPosition);
            updateZones(oldZone);
            Set<Zone> newZone  = getZones(newPosition);
            updateZones(newZone);
        }
    }

    private boolean propChangeHasImpactOnPower(String propName) {
        return (PowerObservable.POWER_OBSERVABLE_CURRENT_POWER_LEVEL.equals(propName));
    }

    @Override
    public void devicePropertyModified(LocatedDevice device, String propertyName, Object oldValue, Object newValue) {
        if (!(propChangeHasImpactOnPower(propertyName))){
            return;
        }
        synchronized (m_lock){
            updateZones(getZones(device.getCenterAbsolutePosition()));
        }
    }

    @Override
    public void devicePropertyAdded(LocatedDevice device, String propertyName) {

    }

    @Override
    public void devicePropertyRemoved(LocatedDevice device, String propertyName) {

    }

    @Override
    public void deviceAttached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(LocatedDevice container, LocatedDevice child) {

    }

    @Override
    public void deviceEvent(LocatedDevice device, Object data) {

    }

    @Override
    public Set<Variable> getComputedZoneVariables() {
        return _computedVariables;
    }

    @Override
    public Set<Variable> getRequiredZoneVariables() {
        return null;
    }

    @Override
    public Set<LocatedDevice> getUsedDevices() {
        return _electricityDevices;
    }

    @Override
    public void zoneAdded(Zone zone) {
        synchronized ( m_lock ){
            updateZone(zone);
        }
    }

    @Override
    public void zoneRemoved(Zone zone) {
        synchronized ( m_lock ){
            mapOfConsumption.remove(zone);
        }
    }

    @Override
    public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {
        synchronized ( m_lock ){
            updateZone(zone);
        }
    }

    @Override
    public void zoneResized(Zone zone) {

        synchronized ( m_lock ){
            updateZone(zone);
        }
    }

    @Override
    public void zoneParentModified(Zone zone, Zone oldParentZone, Zone newParentZone) {

    }

    @Override
    public void deviceAttached(Zone container, LocatedDevice child) {

    }

    @Override
    public void deviceDetached(Zone container, LocatedDevice child) {

    }

    @Override
    public void zoneVariableAdded(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableRemoved(Zone zone, String variableName) {

    }

    @Override
    public void zoneVariableModified(Zone zone, String variableName, Object oldValue, Object newValue) {

    }

    public Set<GenericDevice> getGenericDeviceFromLocation(Zone zone){
        Set<GenericDevice> returnMap = new HashSet<GenericDevice>();
        for(LocatedDevice locatedDevice : _electricityDevices){
            if(zone.contains(locatedDevice)){
                returnMap.add(locatedDevice.getDeviceObject());
            }
        }
        return returnMap;
    }

    public void updateZones(Set<Zone> zones){
        for(Zone zone : zones){
            updateZone(zone);
        }
    }

    public void updateZone(Zone zone){
        Set<GenericDevice> returnMap = getGenericDeviceFromLocation(zone);
        double zoneConsumption = computeConsumption(returnMap);
        mapOfConsumption.put(zone,zoneConsumption);
        zone.setVariableValue(ELECTRICITY_PROP_NAME, zoneConsumption);
    }

    public double computeConsumption(Set<GenericDevice> setOfDevice){

        if (setOfDevice != null && !(setOfDevice.isEmpty())){
            double sumOfConsumption = 0.0 ;
            for ( GenericDevice device : setOfDevice){
                PowerObservable devicePowerObservable = (PowerObservable) device;
                sumOfConsumption += devicePowerObservable.getCurrentConsumption();
            }
            return sumOfConsumption;
        }else{
            return 0.0;
        }
    }

    private Set<Zone> getZones(Position devicePosition) {
        List<Zone> zones = _contextMgr.getZones();
        Set<Zone> zonesToUpdate = new HashSet<Zone>();
        for (Zone zone : zones) {
            if (zone.contains(devicePosition))
                zonesToUpdate.add(zone);
        }
        return zonesToUpdate;
    }



}

