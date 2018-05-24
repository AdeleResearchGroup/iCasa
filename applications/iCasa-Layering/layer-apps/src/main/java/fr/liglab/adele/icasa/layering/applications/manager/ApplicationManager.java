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
    	
    	Map<String,Set<String>> instancesByEntity 	= new HashMap<>();
    	Map<String,Boolean> statusByEntity 			= new HashMap<>();

        for (EntityProvider provider: providers) {
        	for (String entity : provider.getProvidedEntities()) {
        		
        		if (! isApplicationEntity(provider,entity)) {
        			continue;
        		}
        		
        		if (! instancesByEntity.containsKey(entity)) {
        			instancesByEntity.put(entity,new HashSet<>());
        		}
        		
        		if (! statusByEntity.containsKey(entity)) {
        			statusByEntity.put(entity,false);
        		}

        		instancesByEntity.get(entity).addAll(provider.getInstances(entity,true));
        		statusByEntity.put(entity,statusByEntity.get(entity) || ! provider.getInstances(entity,false).isEmpty());
			}
		}

        List<ApplicationDescription> result = new ArrayList<>();
        for (String entity : instancesByEntity.keySet()) {
			ApplicationDescription description = new ApplicationDescription(entity,instancesByEntity.get(entity).toString(),statusByEntity.get(entity));
			result.add(description);
		}

		return result;
    }

    public boolean enable(String entity) {

    	boolean found = false;
    	
    	for (EntityProvider provider: providers) {
        	for (String provided : provider.getProvidedEntities()) {
        		
        		if  (isApplicationEntity(provider,provided) && entity.equals(provided)) {
        			provider.enable(entity);
        			found = true;
        		}
        		
 			}
		}
    	
    	return found;
    }

    public boolean disable(String entity) {

    	boolean found = false;
    	
    	for (EntityProvider provider: providers) {
        	for (String provided : provider.getProvidedEntities()) {
        		
        		if  (isApplicationEntity(provider,provided) && entity.equals(provided)) {
        			provider.disable(entity);
        			found = true;
        		}
        		
 			}
		}
    	
    	return found;
    	
    }

}
