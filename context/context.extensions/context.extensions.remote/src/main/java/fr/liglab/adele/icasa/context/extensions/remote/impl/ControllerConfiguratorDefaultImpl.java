package fr.liglab.adele.icasa.context.extensions.remote.impl;



import fr.liglab.adele.icasa.context.extensions.remote.api.ControllerConfigurator;
import fr.liglab.adele.icasa.context.model.ContextEntity;
import org.apache.felix.ipojo.annotations.*;

@Component
@Provides(specifications = ControllerConfigurator.class)
public class ControllerConfiguratorDefaultImpl implements ControllerConfigurator {


    public int getEntityGroup(ContextEntity contextEntity) {
        return 0;
    }
}