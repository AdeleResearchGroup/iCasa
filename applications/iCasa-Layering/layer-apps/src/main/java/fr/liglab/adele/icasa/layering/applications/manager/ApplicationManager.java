package fr.liglab.adele.icasa.layering.applications.manager;

import java.util.*;


import org.apache.felix.ipojo.annotations.*;
import org.json.JSONException;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

@CommandProvider(namespace = "application-layer")

@Component(immediate=true, publicFactory = false)
@Provides(specifications={ApplicationManager.class})
@Instantiate

public class ApplicationManager {

    @Requires(id="providers", specification = EntityProvider.class, optional=true)
    List<EntityProvider> providers;

    public ApplicationManager() {
	}
    
    private boolean isApplicationEntity(EntityProvider provider, String entity) {
		return provider.getPotentiallyProvidedEntityServices(entity).contains(ApplicationLayer.class.getCanonicalName());
    }
    
    @Command
    public void providers() {
        
        for (EntityProvider provider: providers) {
        	System.out.println("Provider : "+provider.getName());
        	for (String entity : provider.getProvidedEntities()) {
				System.out.println(" provided entity "+entity);
				System.out.println(" 	services "+provider.getPotentiallyProvidedEntityServices(entity));
				System.out.println(" 	instances "+provider.getInstances(entity,true));
			}
        }
        
    }
    
    @Command
    public void applications() {
        
    	try {
            for (ApplicationDescription description: getApplications()) {
            	System.out.println(description.serialize().toString());
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
    }

    public Collection<ApplicationDescription> getApplications() {
    	
    	Collection<ApplicationDescription> result = new ArrayList<>();

        for (EntityProvider provider: providers) {
        	for (String entity : provider.getProvidedEntities()) {
        		
        		if (! isApplicationEntity(provider,entity)) {
        			continue;
        		}
        		
        		for (String instance : provider.getInstances(entity, true)) {
       				ApplicationDescription description = new ApplicationDescription(provider.getName(),entity,instance,true);
					result.add(description);
        		}
			}
		}
        
		return result;
    }



}
