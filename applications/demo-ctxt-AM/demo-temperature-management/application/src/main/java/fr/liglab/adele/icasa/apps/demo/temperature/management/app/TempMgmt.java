package fr.liglab.adele.icasa.apps.demo.temperature.management.app;

import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Component(name = "demo-temperature-management")
@SuppressWarnings("unused")
public class TempMgmt {
    private static double TEMP_MIN = 15;
    private static double TEMP_MAX = 30;

    private static double SMALL_DELTA = 1;
    private static double BIG_DELTA = 5;

    private static double NO_POWER = 0.0d;
    private static double POWER_LEVEL_SMALL_DELTA = 0.3d;
    private static double POWER_LEVEL_BIG_DELTA = 1.0d;

    /*Timer*/
    private static Timer checkTemp = new Timer();
    private static final long taskPeriod = TimeUnit.SECONDS.toMillis(20);

    /*Temperature management*/
    @Validate
    @SuppressWarnings("unused")
    public void start() {
        checkTemp = new Timer();
        TimerTask manageTemp = new TimerTask() {
            @Override
            public void run() {
                manageTemp();
            }
        };
        checkTemp.scheduleAtFixedRate(manageTemp, 0, taskPeriod);
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {
        checkTemp.cancel();
        checkTemp.purge();
        /*ToDo !! pbm if task not ended*/
        for(Heater heater : heaters)
            heater.setPowerLevel(NO_POWER);
        for(Cooler cooler : coolers)
            cooler.setPowerLevel(NO_POWER);
    }

    @Requires(id="thermometers",optional = true,specification = Thermometer.class)
    @SuppressWarnings("all")
    private List<Thermometer> thermometers;

    @Requires(id="heaters",optional = true,specification = Heater.class)
    @SuppressWarnings("all")
    private List<Heater> heaters;

    @Requires(id="coolers",optional = true,specification = Cooler.class)
    @SuppressWarnings("all")
    private List<Cooler> coolers;

    /*TARGET TEMPERATURE IN CELSIUS*/
    private Quantity<Temperature> targetTemperature = Quantities.getQuantity(25, Units.CELSIUS);

    public double setTargetTemperature(double target){
        if (target < TEMP_MIN) targetTemperature = Quantities.getQuantity(TEMP_MIN, Units.CELSIUS);
        else if (target > TEMP_MAX) targetTemperature= Quantities.getQuantity(TEMP_MAX, Units.CELSIUS);
        else targetTemperature = Quantities.getQuantity(target, Units.CELSIUS);
        return targetTemperature.getValue().doubleValue();
    }

    private synchronized void manageTemp(){
        Quantity<Temperature> meanTemp = meanTemp();
        double delta = meanTemp.to(Units.CELSIUS).getValue().doubleValue()
                - targetTemperature.to(Units.CELSIUS).getValue().doubleValue();
        double coolerPowerLevel;
        double heaterPowerLevel;
        if(delta > BIG_DELTA){
            coolerPowerLevel = POWER_LEVEL_BIG_DELTA;
            heaterPowerLevel = NO_POWER;
        } else if(delta > SMALL_DELTA){
            coolerPowerLevel = POWER_LEVEL_SMALL_DELTA;
            heaterPowerLevel = NO_POWER;
        } else if(delta < -BIG_DELTA) {
            coolerPowerLevel = NO_POWER;
            heaterPowerLevel = POWER_LEVEL_BIG_DELTA;
        } else if(delta < -SMALL_DELTA) {
            coolerPowerLevel = NO_POWER;
            heaterPowerLevel = POWER_LEVEL_SMALL_DELTA;
        } else {
            coolerPowerLevel = NO_POWER;
            heaterPowerLevel = NO_POWER;
        }

        for(Heater heater : heaters){
            heater.setPowerLevel(heaterPowerLevel);
        }

        for(Cooler cooler : coolers){
            cooler.setPowerLevel(coolerPowerLevel);
        }
    }

    private Quantity<Temperature> meanTemp(){
        /*RQ: Thermometers are in Kelvin*/
        Quantity<Temperature> sumTemp = Quantities.getQuantity(0, Units.KELVIN);
        int n = 0;
        for(Thermometer thermometer : thermometers){
            n++;
            sumTemp.add(thermometer.getTemperature());
        }

        return sumTemp.divide(n);
    }
}
