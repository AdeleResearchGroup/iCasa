/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.electricity.viewer;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.PowerObservable;
import fr.liglab.adele.icasa.location.*;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

/**
 *
 */
@Component(name = "electricity-viewer")
@Instantiate(name = "electricity-viewer-0")
@Provides(specifications = ElectricityViewer.class)
public class ElectricityViewerImpl implements  ZoneListener, LocatedDeviceListener,ElectricityViewer {


    public static final String ELECTRICITY_PROP_NAME = "Electricity";

    private Set<ElectricityViewerListener> _listener = new HashSet<ElectricityViewerListener>();

    private Map<LocatedDevice,Double> _electricityLocatedDevices = new HashMap<LocatedDevice,Double>();

    private Map<Zone,Double> mapOfConsumption = new HashMap<Zone,Double>();

    private double _totalConsumption = 0.0d;

    private final Object m_lock ;

    @Requires
    private ContextManager _contextMgr;

    public ElectricityViewerImpl() {
        m_lock = new Object();
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
            _electricityLocatedDevices.put(device, ((PowerObservable) device.getDeviceObject()).getCurrentConsumption());
            notifyListener(device.getDeviceObject() ,((PowerObservable) device.getDeviceObject()).getCurrentConsumption(),-1);
            updateZones(getZones(device.getCenterAbsolutePosition()));
            computeTotalConsumption();
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
            notifyListener(device.getDeviceObject() ,-1,((PowerObservable) device.getDeviceObject()).getCurrentConsumption());
            _electricityLocatedDevices.remove(device);
            computeTotalConsumption();
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
            if (oldValue != newValue ){
                _electricityLocatedDevices.put(device,(Double) newValue);
                notifyListener(device.getDeviceObject() ,(Double)newValue,(Double)oldValue);
                updateZones(getZones(device.getCenterAbsolutePosition()));
                computeTotalConsumption();
            }
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
    public void zoneAdded(Zone zone) {
        synchronized ( m_lock ){
            updateZone(zone);
        }
    }

    @Override
    public void zoneRemoved(Zone zone) {
        synchronized ( m_lock ){
            notifyListener(zone.getId(),-1,mapOfConsumption.get(zone));
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
        for(LocatedDevice locatedDevice : _electricityLocatedDevices.keySet()){
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
        if (mapOfConsumption.containsKey(zone)){
            if(zoneConsumption != mapOfConsumption.get(zone)){
                double oldConsumption = mapOfConsumption.get(zone);
                mapOfConsumption.put(zone, zoneConsumption);
                notifyListener(zone.getId(),zoneConsumption,-1);
            }
        }else{
            mapOfConsumption.put(zone, zoneConsumption);
            notifyListener(zone.getId(),zoneConsumption,-1);
        }
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


    @Override
    public double getTotalConsumption() {
        return _totalConsumption;
    }

    @Override
    public double getZoneConsumption(String zoneId) {
        synchronized (m_lock) {
            return mapOfConsumption.get(zoneId);
        }
    }

    @Override
    public <T extends GenericDevice> double getGroupOfDeviceConsumption(Class<T> clazz) {
        synchronized (m_lock){
            double returnConsumption = 0.0d;
            for(LocatedDevice locatedDevice : _electricityLocatedDevices.keySet()){
                GenericDevice genericDevice = locatedDevice.getDeviceObject();
                if (clazz.isInstance(genericDevice)){
                    PowerObservable powerObservableDevice = (PowerObservable) genericDevice;
                    returnConsumption += powerObservableDevice.getCurrentConsumption();
                }
            }
            return returnConsumption;
        }
    }

    @Override
    public synchronized void addListener(ElectricityViewerListener listener) {
        _listener.add(listener);
    }

    @Override
    public synchronized void removeListener(ElectricityViewerListener listener) {
        _listener.remove(listener);
    }


    private void notifyListener(GenericDevice device,double newValue,double oldValue){
        for(ElectricityViewerListener listener : _listener){
            listener.deviceConsumptionModified(device, newValue, oldValue);
        }
    }

    private void notifyListener(String  zoneId,double newValue,double oldValue){
        for(ElectricityViewerListener listener : _listener){
            listener.zoneConsumptionModified(zoneId, newValue, oldValue);
        }
    }

    private void computeTotalConsumption(){
        if (_electricityLocatedDevices != null && !(_electricityLocatedDevices.isEmpty())){
            double sumOfConsumption = 0.0 ;
            for ( LocatedDevice device : _electricityLocatedDevices.keySet()){
                GenericDevice genericDevice = device.getDeviceObject();
                PowerObservable devicePowerObservable = (PowerObservable) genericDevice;
                sumOfConsumption += devicePowerObservable.getCurrentConsumption();
            }
            if (_totalConsumption != sumOfConsumption){
                _totalConsumption = sumOfConsumption;
            }
        }
    }
}
