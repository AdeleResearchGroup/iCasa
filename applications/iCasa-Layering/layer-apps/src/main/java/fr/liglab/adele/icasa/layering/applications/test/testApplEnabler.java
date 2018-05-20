package fr.liglab.adele.icasa.layering.applications.test;

import fr.liglab.adele.icasa.layering.applications.global.globalEnabler;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;

@Component(immediate =true,publicFactory = false)
@Instantiate
public class testApplEnabler implements globalEnabler {

    public boolean getstatus(){
        return true;
    }
}
