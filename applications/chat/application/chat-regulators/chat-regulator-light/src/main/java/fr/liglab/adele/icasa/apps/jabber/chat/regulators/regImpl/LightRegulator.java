package fr.liglab.adele.icasa.apps.jabber.chat.regulators.regImpl;

import fr.liglab.adele.icasa.apps.jabber.chat.regulators.regInt.Regulator;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
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
public class LightRegulator implements Regulator {

    private static final Logger LOG = LoggerFactory.getLogger(LightRegulator.class);

    @Requires(optional=true)
    private DimmerLight[] dimmerLights;

    @Requires(optional=true)
    private BinaryLight[] binaryLights;

    @Requires(optional=true)
    private Photometer[] photometers;

    private ArrayList<DimmerLight> locatedDim;
    private ArrayList<BinaryLight> locatedBin;
    private ArrayList<Photometer> locatedPh;

    private synchronized ArrayList<DimmerLight> getLocatedDimmerLights(String location){
        ArrayList<DimmerLight> located = new ArrayList<DimmerLight>();
        for (DimmerLight dim:dimmerLights){
            if(dim.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(dim);
            }else if(location.equals("all")){
                located.add(dim);
            }
        }
        return located;
    }
    private synchronized ArrayList<BinaryLight> getLocatedBinaryLights(String location){
        ArrayList<BinaryLight> located = new ArrayList<BinaryLight>();
        for (BinaryLight bin:binaryLights){
            if(bin.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(bin);
            }else if(location.equals("all")){
                located.add(bin);
            }
        }
        return located;
    }
    private synchronized ArrayList<Photometer> getLocatedPhotometers(String location){
        ArrayList<Photometer> located = new ArrayList<Photometer>();
        for (Photometer Ph:photometers){
            if(Ph.getPropertyValue(BinaryLight.LOCATION_PROPERTY_NAME).equals(location)){
                located.add(Ph);
            }else if(location.equals("all")){
                located.add(Ph);
            }
        }
        return located;
    }
    private synchronized String getDevice(String location){
        String detectedDevice=null;
        if(dimmerLights.length>0){
            locatedDim=getLocatedDimmerLights(location);
            detectedDevice="Dimmer light(s) detecte(s):";
        }
        if(binaryLights.length>0){
            locatedBin=getLocatedBinaryLights(location);
            if(detectedDevice!=null){
                detectedDevice.replace(":",",");
                detectedDevice+=" Binary light(s) detecte(s)," ;
            }
            else detectedDevice=" Binary light(s) detecte(s):\n" ;
        }
        if(photometers.length>0){
            locatedPh=getLocatedPhotometers(location);
            if(detectedDevice!=null){
                detectedDevice.replace(":",",");
                detectedDevice+=" Photometer  detecte: \n";
            }
            else detectedDevice=" Photometer  detecte : \n";
        }
        return detectedDevice;
    }
    public synchronized String setto(String location, String level) {
        String message=getDevice(location);

        if(locatedDim!=null){
            if(locatedBin!=null){
                for (BinaryLight bin:locatedBin){
                    if(bin.getPowerStatus()){
                        bin.turnOff();
                    }
                }
            }
            for(DimmerLight dim:locatedDim) {
                dim.setPowerLevel(0.7d);
                LOG.info("L'ampoule Dim "+dim.getSerialNumber()+" is set to :"+ dim.getPowerLevel()+"\n");
                message+="L'ampoule Dim "+dim.getSerialNumber()+"is set to :"+ dim.getPowerLevel()+"\n";
            }

        }else{
            message+="Unable to set illuminance in this room - dimmer lights not detected";
        }
        if(locatedPh!=null){
            for(Photometer ph:locatedPh){
                LOG.info("illuminance in "+location+" is: "+ ph.getIlluminance());
                message+="illuminance in "+location+" is: "+ ph.getIlluminance()+"\n";
            }
        }
        return message;
    }

    public synchronized String setOn(String location) {
        String message=getDevice(location);
        if(locatedBin!=null){
            for (BinaryLight bin:locatedBin){
                if(bin.getPowerStatus()){
                    LOG.info("L'ampoule Bin "+bin.getSerialNumber()+" is already on");
                    message+="L'ampoule Bin "+bin.getSerialNumber()+" is already on\n";
                }else{
                    bin.turnOn();
                    LOG.info("L'ampoule Bin"+bin.getSerialNumber()+" is on");
                    message+="L'ampoule Bin"+bin.getSerialNumber()+" is on\n";
                }
            }
        }
        if(locatedDim!=null){
            for (DimmerLight dim:locatedDim){

                    dim.setPowerLevel(1.0d);
                    LOG.info("L'ampoule Dim"+dim.getSerialNumber()+" is on");
                    message+="L'ampoule Dim"+dim.getSerialNumber()+" is on\n";

            }
        }
        return message;
    }

    public synchronized String setOff(String location) {

        String message=getDevice(location);
        if(locatedBin!=null){
            for (BinaryLight bin:locatedBin){
                if(bin.getPowerStatus()){
                    bin.turnOff();
                    LOG.info("L'ampoule "+bin.getSerialNumber()+" is off");
                    message+="L'ampoule Bin "+bin.getSerialNumber()+" is off\n";
                }else{
                    LOG.info("L'ampoule "+bin.getSerialNumber()+" is already off");
                    message+="L'ampoule Bin"+bin.getSerialNumber()+" is already off\n";
                }
            }
        }
        if(locatedDim!=null){
            for (DimmerLight dim:locatedDim){
                if(dim.getPowerLevel()>0.0d){
                    dim.setPowerLevel(0.0d);
                    LOG.info("L'ampoule "+dim.getSerialNumber()+" is off");
                    message+="L'ampoule Dim"+dim.getSerialNumber()+" is off\n";

                }else{
                    LOG.info("L'ampoule "+dim.getSerialNumber()+" is already dead");
                    message+="L'ampoule Dim "+dim.getSerialNumber()+" is already off\n";
                }
            }
        }
        return message;
    }

    public synchronized String getStatus(String location) {

        String message=getDevice(location);
        if(locatedBin!=null){
            for (BinaryLight bin:locatedBin){
                if(bin.getPowerStatus()){
                    LOG.info("L'ampoule Bin "+bin.getSerialNumber()+" is on");
                    message+="L'ampoule Bin "+bin.getSerialNumber()+" is on\n";
                }else{
                    LOG.info("L'ampoule Bin "+bin.getSerialNumber()+" is off");
                    message+="L'ampoule Bin "+bin.getSerialNumber()+" is off\n";
                }
            }
        }
        if(locatedDim!=null){
            for (DimmerLight dim:locatedDim){
                if(dim.getPowerLevel()>0.0d){
                    LOG.info("L'ampoule Dim "+dim.getSerialNumber()+" is on");
                    message+="L'ampoule Dim "+dim.getSerialNumber()+" is on\n";
                }else{
                    LOG.info("L'ampoule Dim "+dim.getSerialNumber()+" is off");
                    message+="L'ampoule Dim "+dim.getSerialNumber()+" is off\n";
                }
            }
        }
        if(locatedPh!=null){
            for(Photometer ph:locatedPh){
                LOG.info("illuminance in "+location+" is : "+ph.getIlluminance());
                message+="illuminance in "+location+" is : "+ph.getIlluminance()+"\n";
            }
        }
        return message;
    }
    @Validate
    public void start(){
       LOG.info("Light Regulator starts");
    }
    @Invalidate
    public void stop(){
        LOG.info("Light regulator stop");
    }
}
