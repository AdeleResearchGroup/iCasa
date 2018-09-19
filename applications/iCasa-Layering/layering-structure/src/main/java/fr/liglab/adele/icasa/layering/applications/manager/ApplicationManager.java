package fr.liglab.adele.icasa.layering.applications.manager;

import java.util.*;


import org.apache.felix.ipojo.annotations.*;

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
		return provider.getProvidedServices(entity).contains(ApplicationLayer.class.getCanonicalName());
    }
    
    @Command
    public void providers() {
        
        for (EntityProvider provider: providers) {
        	System.out.println("Provider : "+provider.getName());
        	for (String entity : provider.getProvidedEntities()) {
				System.out.println(" provided entity "+entity);
				System.out.println(" 	services "+provider.getProvidedServices(entity));
				System.out.println(" 	instances "+provider.getInstances(entity));
			}
        }
        
    }
    
    @Command
    public void applications() {
        
        for (ApplicationDescription description: getApplications()) {
			System.out.println(" Application "+description.implementation);
			System.out.println(" 	instances	"+description.instances);
			System.out.println(" 	status		"+description.enabled);
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

        		instancesByEntity.get(entity).addAll(provider.getInstances(entity));
        		statusByEntity.put(entity,statusByEntity.get(entity) || provider.isEnabled(entity));
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
        		
        		if  (entity.equals(provided) && isApplicationEntity(provider,provided)) {
        			provider.enable(entity);
        			found = true;
        		}
        		
 			}
		}
    	
    	return found;
    }

    public void enable() {

    	for (EntityProvider provider: providers) {
        	for (String provided : provider.getProvidedEntities()) {
        		
        		if  (isApplicationEntity(provider,provided)) {
        			provider.enable(provided);
        		}
        		
 			}
		}
    }

    public boolean disable(String entity) {

    	boolean found = false;
    	
    	for (EntityProvider provider: providers) {
        	for (String provided : provider.getProvidedEntities()) {
        		
        		if  (entity.equals(provided) && isApplicationEntity(provider,provided)) {
        			provider.disable(entity);
        			found = true;
        		}
        		
 			}
		}
    	
    	return found;
    	
    }

    public void disable() {

    	for (EntityProvider provider: providers) {
        	for (String provided : provider.getProvidedEntities()) {
        		
        		if  (isApplicationEntity(provider,provided)) {
        			provider.disable(provided);
        		}
        		
 			}
		}
    	
    }

}
