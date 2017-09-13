package fr.liglab.adele.icasa.apps.demo.interop.comfort.management.app;

import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component(name = "demo-interop-comfort-management")
@SuppressWarnings("unused")
public class ComfortMgmt {

    /*ILLUMINANCE*/
    private static final Unit<Illuminance> LUM_UNIT = Units.LUX;
    private static final double MAX_OUTSIDE_LUM = 800.0d;
    private static final double MAX_OUTSIDE_LUM_BEFORE_INFLUENCING_TEMP = 600.0d;
    private static final double LUM_TOLERANCE = 25.0d;

    /*TEMPERATURE*/
    private static final Unit<Temperature> TEMP_UNIT = Units.CELSIUS;
    /*rq: MAX_OUTSIDE_TEMP = MAX_OUTSIDE_TEMP_SHUTTER = MAX_OUTSIDE_TEMP_COOLER_WITHOUT_SHUTTER*/
    private static final double MAX_OUTSIDE_TEMP = 27.0d;
    private static final double MAX_OUTSIDE_TEMP_COOLER_WITH_SHUTTER = 32.0d;
    private static final double TEMP_TOLERANCE = 0.5d;

    /*SHUTTER POSITION*/
    /*Percentage*/
    private static final double SHUTTER_CLOSED= 0.0d;
    private static final double SHUTTER_OPENED = 1.0d;

    /*COOLING POWER*/
    /*Percentage*/
    private static final double NO_POWER = 0.0d;
    private static final double COOLING_POWER= 0.3d;

    /*SCHEDULING*/
    /*Only scheduled mode - Thread management*/
    private static final TimeUnit periodUnit = TimeUnit.SECONDS;
    private static final long period = 20;
    private static ScheduledExecutorService scheduledExecutorService = null;
    private static ScheduledFuture scheduledFuture = null;

    /*GETTING INFO*/
    public double getMaxOutsideLum(){
        return MAX_OUTSIDE_LUM;
    }
    public double getMinOutsideTempShutter(){
        return MAX_OUTSIDE_LUM_BEFORE_INFLUENCING_TEMP;
    }
    public double getMaxOutsideTemp(){
        return MAX_OUTSIDE_TEMP;
    }
    public double getMaxOutsideLumBeforeInfluencingTemp(){
        return MAX_OUTSIDE_TEMP_COOLER_WITH_SHUTTER;
    }

    /*SERVICE-ORIENTED COMPONENT METHODS*/
    @Validate
    @SuppressWarnings("unused")
    public void start() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::manageComfort,
                0, period, periodUnit);
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {
        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdown();


        for(WindowShutter shutter : shutters){
            try{
                shutter.setShutterLevel(SHUTTER_OPENED);
            } catch (NullPointerException ne){
                ne.printStackTrace();
            }
        }

        for(Cooler cooler : coolers){
            try{
                cooler.setPowerLevel(NO_POWER);
            } catch (NullPointerException ne){
                ne.printStackTrace();
            }
        }
    }

    /*Context components*/
    @Requires(id="photometers",optional = true,specification = Photometer.class)
    @SuppressWarnings("all")
    private List<Photometer> externalPhotometers;

    @Requires(id="shutters",optional = true,specification = WindowShutter.class)
    @SuppressWarnings("all")
    private List<WindowShutter> shutters;

    @Requires(id="thermometers",optional = true,specification = Thermometer.class)
    @SuppressWarnings("all")
    private List<Thermometer> externalThermometers;

    @Requires(id="coolers",optional = true,specification = Cooler.class)
    @SuppressWarnings("all")
    private List<Cooler> coolers;

    /*Context state*/
    private Quantity<Illuminance> meanLum(){
        Quantity<Illuminance> sumLum = Quantities.getQuantity(0, LUM_UNIT);
        int n = 0;
        for(Photometer photometer : externalPhotometers){
            Quantity<Illuminance> illuminance = photometer.getIlluminance();
            if (illuminance != null){
                n++;
                sumLum = sumLum.add(illuminance);
            }
        }

        return (n==0) ? sumLum : sumLum.divide(n);
    }

    private Quantity<Temperature> meanTemp(){
        Quantity<Temperature> sumTemp = Quantities.getQuantity(0, TEMP_UNIT);
        int n = 0;
        for(Thermometer thermometer : externalThermometers){
            Quantity<Temperature> temperature = thermometer.getTemperature();
            if (temperature != null) {
                n++;
                sumTemp = sumTemp.add(temperature);
            }
        }

        return (n==0) ? sumTemp : sumTemp.divide(n);
    }

    /*MANAGEMENT*/
    private static boolean isOverMaxOutsideLum = false;
    private static boolean isOverMaxOutsideLumBeforeInfluencingTemp = false;
    private static boolean isOverMaxOutsideTemp = false;
    private static boolean isOverMaxOutsideTempCoolerWithShutter = false;

    private synchronized void manageComfort(){
        /*Sensor data*/
        double meanLum  = meanLum().to(LUM_UNIT).getValue().doubleValue();
        double meanTemp = meanTemp().to(TEMP_UNIT).getValue().doubleValue();

        /*Hysteresis - stabilising decisions*/
        isOverMaxOutsideLum = isOverMaxOutsideLum ?
                meanLum > MAX_OUTSIDE_LUM + LUM_TOLERANCE
                : meanLum > MAX_OUTSIDE_LUM - LUM_TOLERANCE;

        isOverMaxOutsideLumBeforeInfluencingTemp = isOverMaxOutsideLumBeforeInfluencingTemp ?
                meanLum > MAX_OUTSIDE_LUM_BEFORE_INFLUENCING_TEMP + LUM_TOLERANCE
                : meanLum > MAX_OUTSIDE_LUM_BEFORE_INFLUENCING_TEMP - LUM_TOLERANCE;

        isOverMaxOutsideTemp = isOverMaxOutsideTemp ?
                meanTemp > MAX_OUTSIDE_TEMP + TEMP_TOLERANCE
                : meanTemp > MAX_OUTSIDE_TEMP - TEMP_TOLERANCE;

        isOverMaxOutsideTempCoolerWithShutter = isOverMaxOutsideTempCoolerWithShutter ?
                meanTemp > MAX_OUTSIDE_TEMP_COOLER_WITH_SHUTTER + TEMP_TOLERANCE
                : meanTemp > MAX_OUTSIDE_TEMP_COOLER_WITH_SHUTTER - TEMP_TOLERANCE;

        /*Management logic*/
        boolean closingShutters;
        boolean activatingCoolers;

        closingShutters = isOverMaxOutsideLum || (isOverMaxOutsideLumBeforeInfluencingTemp && isOverMaxOutsideTemp);
        activatingCoolers = closingShutters ? isOverMaxOutsideTempCoolerWithShutter : isOverMaxOutsideTemp;

        /*Actuation*/
        double shutterPosition = closingShutters ? SHUTTER_CLOSED : SHUTTER_OPENED;
        double coolerPowerLevel = activatingCoolers ? COOLING_POWER : NO_POWER;

        for(WindowShutter shutter : shutters){
            shutter.setShutterLevel(shutterPosition);
        }

        for(Cooler cooler : coolers){
            cooler.setPowerLevel(coolerPowerLevel);
        }
    }
}