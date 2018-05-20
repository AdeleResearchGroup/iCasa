package fr.liglab.adele.icasa.layering.applications.test;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;

@ContextEntity(coreServices = {ApplicationLayer.class})
@SuppressWarnings("unused")
public class testAppImpl implements ApplicationLayer{
    @ContextEntity.State.Field(service = ApplicationLayer.class, state = ApplicationLayer.status,value = "init")
    public String stat;
    @Override
    public boolean getStatus() {
        return false;
    }

    @Override
    public boolean setStaus() {
        return false;
    }

    @Override
    public String getName() {
        return "testApp";
    }

    @Override
    public String getProvider() {
        return null;
    }
}
