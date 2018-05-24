package fr.liglab.adele.icasa.layering.services.api;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;


public @ContextService interface RootService {
    
	public final static @State String CURRENT_STATE = "current.state";
	
    public final static String ZONES = "zones";
}
