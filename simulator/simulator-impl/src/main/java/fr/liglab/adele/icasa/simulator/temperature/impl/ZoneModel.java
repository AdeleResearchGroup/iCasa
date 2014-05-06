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
package fr.liglab.adele.icasa.simulator.temperature.impl;


import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Zone;

import java.util.*;

import static fr.liglab.adele.icasa.simulator.temperature.impl.TemperaturePMImpl.VOLUME_PROP_NAME;

import static fr.liglab.adele.icasa.simulator.temperature.impl.TemperaturePMImpl.AIR_MASS;

import static fr.liglab.adele.icasa.simulator.temperature.impl.TemperaturePMImpl.AIR_MASS_CAPACITY;

/**
 * Model of a zone used to compute thermal properties.
 *
 */
public class ZoneModel {

    private double _thermalCapacity;

    private Map<String, Double> _wallSurfaceMap = new HashMap<String, Double>();

    private double _totalPower;

    private Zone _zone;

    private boolean updateDevice = true;

    private boolean updateThermalCapacity = true;

    private boolean updatePower = true;

    private Set<LocatedDevice> _temperatureDevices = new HashSet<LocatedDevice>();

    private Set<GenericDevice> _devices = new HashSet<GenericDevice>();

    private Set<Heater> _heaters = new HashSet<Heater>();

    private Set<Cooler> _coolers = new HashSet<Cooler>();


    public ZoneModel(Zone zone,Set<LocatedDevice> temperatureDevices) {
         this._temperatureDevices = temperatureDevices;
        this._zone = zone;
    }

    public Zone getZone() {
        return _zone;
    }

    public String getZoneId() {
        return _zone.getId();
    }

    public double getThermalCapacity() {
        return _thermalCapacity;
    }

    public void updateThermalCapacity() {
        if (updateThermalCapacity){
            double newVolume = 1.0d; // use this value as default to avoid divide by zero
            Double zoneVolume = (Double) _zone.getVariableValue(VOLUME_PROP_NAME);
            if ((zoneVolume != null) && ((zoneVolume) > 0.0d)){
                newVolume =  zoneVolume;
            }
            _thermalCapacity = AIR_MASS * AIR_MASS_CAPACITY * newVolume;
        }
    }


    public Set<Map.Entry<String, Double>> getWallSurfaces() {
        return _wallSurfaceMap.entrySet();
    }

    public double getWallSurface(String zoneId) {
        Double surface = _wallSurfaceMap.get(zoneId);
        if (surface == null)
            return 0.0d;
        else
            return surface;
    }

    public void setWallSurface(String zoneId, double wallSurface) {
        _wallSurfaceMap.put(zoneId, wallSurface);
    }

    public void removeWallSurface(String zoneId) {
        _wallSurfaceMap.remove(zoneId);
    }


    public boolean isInTheZone(LocatedDevice device) {
        if ((device != null) && (_zone.contains(device))) {
            return true;
        }
        return false;
    }

    public void updateTemperatureDevices() {
        _coolers.clear();
        _heaters.clear();
        _devices.clear();
        for(LocatedDevice device : _temperatureDevices){
            if (isInTheZone(device)){

                GenericDevice deviceObj = device.getDeviceObject();

                if (deviceObj instanceof Cooler){
                    _devices.add(deviceObj);
                    _coolers.add((Cooler)deviceObj);

                }else if(deviceObj instanceof Heater){
                    _heaters.add((Heater)deviceObj);
                    _devices.add(deviceObj);
                }
            }
        }
    }


    public Set<GenericDevice> getDevices() {
        return _devices;
    }



    public void updateTotalPower() {
        _totalPower = 0;
        for (Heater heater : _heaters){
            _totalPower += heater.getPowerLevel() * heater.getMaxPowerLevel();
        }

        for (Cooler cooler : _coolers){
            _totalPower -= cooler.getPowerLevel() * cooler.getMaxPowerLevel();
        }
    }

    public double getTotalPower() {
        return _totalPower;
    }
}
