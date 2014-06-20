package fr.liglab.adele.icasa.apps.jabber.chat.regulators.TempReg;

import fr.liglab.adele.icasa.apps.jabber.chat.regulators.regInt.Regulator;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by donatien on 21/05/14.
 */
@Component
@Provides
@Instantiate
public class TemperatureRegulator implements Regulator {

    private static final Logger LOG= LoggerFactory.getLogger(TemperatureRegulator.class);

    //private static final Logger LOG = LoggerFactory.getLogger(TemperatureRegulator.class);

    @Requires(optional=true)
    private Cooler[] coolers;

    @Requires(optional=true)
    private Heater[] heaters;

    @Requires(optional=true)
    private Thermometer[] thermometers;

    private ArrayList<Cooler> locatedCool;
    private ArrayList<Heater> locatedHeat;
    private ArrayList<Thermometer> locatedTh;

    private synchronized ArrayList<Cooler> getLocatedCoolers(String location){
        ArrayList<Cooler> located = new ArrayList<Cooler>();
        for (Cooler dim: coolers){
            if(dim.getPropertyValue(Heater.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(dim);
            }else if(location.equals("all")){
                located.add(dim);
            }
        }
        return located;
    }
    private synchronized ArrayList<Heater> getLocatedHeaters(String location){
        ArrayList<Heater> located = new ArrayList<Heater>();
        for (Heater bin: heaters){
            if(bin.getPropertyValue(Heater.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(bin);
            }else if(location.equals("all")){
                located.add(bin);
            }
        }
        return located;
    }
    private synchronized ArrayList<Thermometer> getLocatedThermometers(String location){
        ArrayList<Thermometer> located = new ArrayList<Thermometer>();
        for (Thermometer Ph: thermometers){
            if(Ph.getPropertyValue(Heater.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(Ph);
            }else if(location.equals("all")){
                located.add(Ph);
            }
        }
        return located;
    }
    private String getDevice(String location){
        String detectedDevice=null;
        if(coolers.length>0){
            locatedCool =getLocatedCoolers(location);
            detectedDevice="Cooler(s) detecte(s):";
        }
        if(heaters.length>0){
            locatedHeat =getLocatedHeaters(location);
            if(detectedDevice!=null){
                detectedDevice.replace(":",",");
                detectedDevice+=" Heater(s) detecte(s)," ;
            }
            else detectedDevice=" Heater(s) detecte(s):\n" ;
        }
        if(thermometers.length>0){
            locatedTh =getLocatedThermometers(location);
            if(detectedDevice!=null){
                detectedDevice.replace(":",",");
                detectedDevice+=" Thermometer  detecte: \n";
            }
            else detectedDevice=" Thermometer  detecte : \n";
        }
        return detectedDevice;
    }
    public String setto(String location, String level) {
        String message=getDevice(location);
        if(locatedHeat !=null){
            for (Heater Ht: locatedHeat){
                if(Ht.getPowerLevel()>0.0d){
                    Ht.setPowerLevel(0.0d);
                }
            }
        }
        if(locatedCool !=null){

        }
        if(locatedTh !=null){
            for(Thermometer ph: locatedTh){
                //if(ph.getIlluminance());
            }
        }
        return message;
    }

    public String setOn(String location) {
        String message=getDevice(location);
        if(locatedHeat !=null){
            for (Heater heater: locatedHeat){
                if(heater.getPowerLevel()>0.0d){
                    LOG.info("Heater "+heater.getSerialNumber()+" is already on");
                    message+="Heater "+heater.getSerialNumber()+" is already on\n";
                }else{
                    heater.setPowerLevel(1.0d);
                    LOG.info("Heater "+heater.getSerialNumber()+" is on");
                    message+="Heater "+heater.getSerialNumber()+" is on\n";
                }
            }
        }
        if(locatedCool !=null){
            for (Cooler cooler: locatedCool){
                if(cooler.getPowerLevel()>0.0d){
                    LOG.info("Cooler "+cooler.getSerialNumber()+" is already on");
                    message+="Cooler "+cooler.getSerialNumber()+" is already on\n";
                }else{
                    cooler.setPowerLevel(1.0d);
                    LOG.info("Cooler "+cooler.getSerialNumber()+" is on");
                    message+="Cooler "+cooler.getSerialNumber()+" is on\n";
                }
            }
        }
        return message;
    }

    public String setOff(String location) {

        String message=getDevice(location);
        if(locatedHeat !=null){
            for (Heater heater: locatedHeat){
                if(heater.getPowerLevel()>0.0d){
                    heater.setPowerLevel(0.0d);
                    LOG.info("Heater "+heater.getSerialNumber()+" is off");
                    message+="Heater "+heater.getSerialNumber()+" is off\n";
                }else{
                    LOG.info("Heater "+heater.getSerialNumber()+" is already off");
                    message+="Heater "+heater.getSerialNumber()+" is already off\n";
                }
            }
        }
        if(locatedCool !=null){
            for (Cooler dim: coolers){
                if(dim.getPowerLevel()>0.0d){
                    dim.setPowerLevel(0.0d);
                    LOG.info("Cooler"+dim.getSerialNumber()+" is off");
                    message+="Cooler"+dim.getSerialNumber()+" is off\n";

                }else{
                    LOG.info("Cooler "+dim.getSerialNumber()+" is already dead");
                    message+="Cooler "+dim.getSerialNumber()+" is already off\n";
                }
            }
        }
        return message;
    }

    public String getStatus(String location) {

        String message=getDevice(location);
        if(locatedHeat !=null){
            for (Heater heater: locatedHeat){
                if(heater.getPowerLevel()>0.0d){
                    LOG.info("Heater "+heater.getSerialNumber()+" is on");
                    message+="Heater "+heater.getSerialNumber()+" is on\n";
                }else{
                    LOG.info("Heater "+heater.getSerialNumber()+" is off");
                    message+="Heater "+heater.getSerialNumber()+" is off\n";
                }
            }
        }
        if(locatedCool !=null){
            for (Cooler cooler: coolers){
                if(cooler.getPowerLevel()>0.0d){
                    LOG.info("Cooler "+cooler.getSerialNumber()+" is on");
                    message+="Cooler "+cooler.getSerialNumber()+" is on\n";
                }else{
                    LOG.info("Cooler "+cooler.getSerialNumber()+" is off");
                    message+="Cooler "+cooler.getSerialNumber()+" is off\n";
                }
            }
        }
        if(locatedTh !=null){
            for(Thermometer Th: locatedTh){
                LOG.info("Temperature " + location + " is : " + Th.getTemperature());
                message+="Temperature "+location+" is : "+Th.getTemperature()+"\n";
            }
        }
        return message;
    }

    @Validate
    public void start(){
        LOG.info("Temperature regulator start");
    }

    @Invalidate
    public void stop(){
        LOG.info("Temperature regulator stopped");
    }
}
