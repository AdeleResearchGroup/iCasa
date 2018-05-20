package fr.liglab.adele.icasa.layering.applications.test;

import fr.liglab.adele.cream.annotations.provider.Creator;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate =true,publicFactory = false)
@Instantiate
public class testAppProvider {
    @Creator.Field Creator.Entity<testAppImpl> testapp;
    @Requires(id="appEnabler",specification = testApplEnabler.class)
    testApplEnabler enbl;

    @Validate
    public void start(){
        testapp.create("testApp");
    }

    @Invalidate
    public void stop(){

    };
}
