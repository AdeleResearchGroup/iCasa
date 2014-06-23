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
package fr.liglab.adele.icasa.electricity.manager;


import java.util.Set;
import java.util.HashSet;
import org.joda.time.DateTime;
import org.wisdom.api.model.Crud;
import org.wisdom.api.annotations.Model;
import org.apache.felix.ipojo.annotations.*;
import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.clock.ClockListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.electricity.viewer.ElectricityViewer;
import fr.liglab.adele.icasa.electricity.viewer.ElectricityViewerListener;
import fr.liglab.adele.icasa.electricity.manager.sample.ConsumptionSampleZone;
import fr.liglab.adele.icasa.electricity.manager.sample.ConsumptionSampleDevice;


/**
 * Created by horakm on 4/3/14.
 */
@Component(name = "consumption-manager")
@Instantiate(name = "consumption-manager-1")
@Provides
public class ElectricityManagerImpl implements ElectricityManager, ClockListener, ZoneListener, ElectricityViewerListener {

    private Set<ElectricityManagerListener> _listener;
    private DateTime startDate;
    private int yearOfStart;
    private int dayOfStart;
    private int minuteOfStart;
    private final Object m_lock;
    private static long sampleIdDev;
    private static long sampleIdZone;

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    @Model(value = ConsumptionSampleDevice.class)
    Crud<ConsumptionSampleDevice, Long> devSamples;

    @Model(value = ConsumptionSampleZone.class)
    Crud<ConsumptionSampleZone, Long> zoneSamples;

    @Requires
    private Clock clock;

    @Requires
    private ElectricityViewer electricityViewer;

    @Requires
    private ContextManager _contextMgr;

    public ElectricityManagerImpl() {
        m_lock = new Object();
    }

    /** Component Lifecycle Method */
    @Validate
    private void start() {
        _listener = new HashSet<ElectricityManagerListener>();
        startDate = new DateTime(clock.currentTimeMillis());
        yearOfStart = startDate.getYear();
        dayOfStart = startDate.getDayOfYear();
        minuteOfStart = startDate.getMinuteOfDay();
        sampleIdDev = 0;
        sampleIdZone = 0;

        _contextMgr.addListener(this);
        electricityViewer.addListener(this);
        System.out.println("Component consumption manager is starting...");
    }

    /** Component Lifecycle Method */
    @Invalidate
    private void stop() {
        _contextMgr.removeListener(this);
        electricityViewer.removeListener(this);
        System.out.println("Component consumption manager is stopping...");
    }

    @Override
    public void factorModified(int i) {

    }

    @Override
    public void startDateModified(long l) {
        resetClock();
    }

    private void resetClock() {
        startDate = new DateTime(clock.currentTimeMillis());
        yearOfStart = startDate.getYear();
        dayOfStart = startDate.getDayOfYear();
        minuteOfStart = startDate.getMinuteOfDay();

        for(long i = sampleIdDev; sampleIdDev >= 0; i--)
        {
            devSamples.delete(sampleIdDev);
        }

        for(long y = sampleIdZone; sampleIdZone >=0; y--)
        {
            zoneSamples.delete(sampleIdZone);
        }

        sampleIdDev = 0;
        sampleIdZone = 0;
    }

    @Override
    public void clockPaused() {

    }

    @Override
    public void clockResumed() {

    }

    @Override
    public void clockReset() {
        resetClock();
    }

    @Override
    public void deviceConsumptionModified(GenericDevice device, double newConsumption, double oldConsumption) {
        try {
            synchronized ( m_lock ){
                String loc = (String) device.getPropertyValue(LOCATION_PROPERTY_NAME);
                String dev = device.getSerialNumber();
                ConsumptionSampleDevice sample = new ConsumptionSampleDevice(sampleIdDev ,loc, dev, newConsumption, new DateTime(clock.currentTimeMillis()));
                //TODO uncomment to use embedded database
                //devSamples.save(sample);
                notifyListener(sample);
                sampleIdDev++;
            }
        }catch (Exception e)
        {
            System.out.println("Exception " + e.toString());
        }
    }

    @Override
    public void zoneConsumptionModified(String zoneId, double newConsumption, double oldConsumption) {
        try {
            synchronized ( m_lock ){
                ConsumptionSampleZone sample = new ConsumptionSampleZone(sampleIdZone ,zoneId, newConsumption, new DateTime(clock.currentTimeMillis()));
                //TODO uncomment to use embedded database
                //zoneSamples.save(sample);
                notifyListener(sample);
                sampleIdZone++;
            }
        }catch (Exception e)
        {
            System.out.println("Exception " + e.toString());
        }
    }

    private void notifyListener(ConsumptionSampleDevice sample) {
        if(!_listener.isEmpty()){
            for(ElectricityManagerListener listener : _listener){
                listener.deviceConsumptionModified(sample.getLocation(), sample.getDevice(), sample.getConsumption(), sample.getDate());
            }
        }
    }

    private void notifyListener(ConsumptionSampleZone zone) {
        if(!_listener.isEmpty()){
            for(ElectricityManagerListener listener : _listener){
                listener.zoneConsumptionModified(zone.getZone(), zone.getConsumption(), zone.getDate());
            }
        }
    }

    @Override
    public void addListener(ElectricityManagerListener listener) {
        _listener.add(listener);
    }

    @Override
    public void removeListener(ElectricityManagerListener listener) {
        _listener.remove(listener);
    }

    @Override
    public void zoneAdded(Zone zone) {
        if(!_listener.isEmpty()){
            for(ElectricityManagerListener listener : _listener){
                listener.zoneAdded(zone);
            }
        }
    }

    @Override
    public void zoneRemoved(Zone zone) {
        if(!_listener.isEmpty()){
            for(ElectricityManagerListener listener : _listener){
                listener.zoneRemoved(zone);
            }
        }

    }

    @Override
    public void zoneMoved(Zone zone, Position position, Position position2) {

    }

    @Override
    public void zoneResized(Zone zone) {

    }

    @Override
    public void zoneParentModified(Zone zone, Zone zone2, Zone zone3) {

    }

    @Override
    public void deviceAttached(Zone zone, LocatedDevice locatedDevice) {

    }

    @Override
    public void deviceDetached(Zone zone, LocatedDevice locatedDevice) {

    }

    @Override
    public void zoneVariableAdded(Zone zone, String s) {

    }

    @Override
    public void zoneVariableRemoved(Zone zone, String s) {

    }

    @Override
    public void zoneVariableModified(Zone zone, String s, Object o, Object o2) {

    }
}
