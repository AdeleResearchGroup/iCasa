package fr.liglab.adele.icasa.apps.demo.pet.care.app;

import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.PetInfo;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

@Component(immediate = true)
@Instantiate
@CommandProvider(namespace = "demo-pet-care")
@SuppressWarnings("unused")
public class AppUserInterface {

    @Requires(specification = PetCareAppManagerInterface.class)
    PetCareAppManagerInterface petCareAppManagerInterface;

    @Requires(specification = PetInfo.class, optional = true)
    List<PetInfo> petInfoList;

    /*App command interface*/
//    @Command
//    @SuppressWarnings("unused")
//    public void toggleRegistration() {
//        petCareAppManagerInterface.toggleRegistration();
//    }

    @Command
    @SuppressWarnings("unused")
    public void petCareModeToggle(){
        petCareAppManagerInterface.petCareModeToggle();
    }
}
