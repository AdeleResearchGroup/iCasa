package fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.command;


import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.manager.EnergyGoal;
import fr.liglab.icasa.self.star.temperature.management.exercice.four.temperature.manager.TemperatureManagerAdministration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;


/**
 *
 */
//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "temperature.administration.command")
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "temperature")
public class TemperatureCommandImpl {

    // Declare a dependency to a TemperatureAdministration service
    @Requires
    private TemperatureManagerAdministration m_administrationService;


    /**
     * Command implementation to express that the temperature is too high in the given room
     *
     * @param room the given room
     */

    // Each command should start with a @Command annotation
    @Command
    public synchronized void tempTooHigh(String room,String name) {
        m_administrationService.temperatureIsTooHigh(room,name);
    }

    @Command
    public synchronized void tempTooLow(String room,String name){
        m_administrationService.temperatureIsTooHigh(room,name);
    }

    @Command
    public synchronized void setTemperatureEnergyLevel(String room){

        EnergyGoal energyGoal;

        energyGoal = EnergyGoal.valueOf(room.toUpperCase());

        m_administrationService.setTemperatureEnergyGoal(energyGoal);
    }

    @Command
    public synchronized void getTemperatureEnergyLevel(){
        EnergyGoal energyGoal = m_administrationService.getTemperatureEnergyGoal();
        System.out.println(" Temperature energy policy is : " + energyGoal.name() );
    }

    @Command
    public synchronized void getStatutSavingMode(){
        System.out.println(" Energy saving mode enable ? " + m_administrationService.isPowerSavingEnabled());
    }


    @Command
    public synchronized void enableSavingMode(){
        if ( !m_administrationService.isPowerSavingEnabled()){
            m_administrationService.turnOnEnergySavingMode();
        }else {
            System.out.println(" Saving mode is already enable ");
        }
    }

    @Command
    public synchronized void disableSavingMode(){
        if (m_administrationService.isPowerSavingEnabled()){
            m_administrationService.turnOffEnergySavingMode();
        }else {
            System.out.println(" Saving mode is already disable");
        }
    }

    @Command
    public synchronized void roomOccupancy(String room,String user){
        for(int i = 0 ; i <= 1439 ; i++ ){
            System.out.println(" At " + (i/60) + " : " + (i - (int)(i/60) * 60) + " proba is " + m_administrationService.getRoomOccupancy(room,i,user));
        }
    }

}
