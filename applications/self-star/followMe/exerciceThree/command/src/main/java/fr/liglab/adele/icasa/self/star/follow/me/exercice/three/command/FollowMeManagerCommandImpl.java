package fr.liglab.adele.icasa.self.star.follow.me.exercice.three.command;


import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.self.star.follow.me.exercice.three.manager.FollowMeAdministration;
import fr.liglab.adele.icasa.self.star.follow.me.exercice.three.manager.IlluminanceGoal;
import org.apache.felix.ipojo.annotations.*;

//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "follow.me.mananger.command")
//Use the handler command and declare the command as a command provider. The
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "icasa")
public class FollowMeManagerCommandImpl {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private FollowMeAdministration m_administrationService;


    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */

    // Each command should start with a @Command annotation
    @Command
    public void setIlluminancePreference(String goal) {
        // The targeted goal
        IlluminanceGoal illuminanceGoal;


        // goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
        try{
            illuminanceGoal = IlluminanceGoal.valueOf(goal.toUpperCase());

            System.out.println(" ILLUMINANCE "+illuminanceGoal);
            //call the administration service to configure it :
            m_administrationService.setIlluminancePreference(illuminanceGoal);

        }catch(Exception e){
            System.out.println("Invalid Argument (must be soft, medium or full");
        }
    }

    @Command
    public void getIlluminancePreference(){
        System.out.println(" Actual Illuminance preference is " + m_administrationService.getIlluminancePreference());

    }

    @Command
    public void exist(){
        System.out.println("Using Exist");
        m_administrationService.exist();
    }

    /** Component Lifecycle Method */
    @Validate
    public void stop() {
        System.out.println("Component is stopping...");
    }

    /** Component Lifecycle Method */
    @Invalidate
    public void start() {
        System.out.println("Component is starting...");
    }

}