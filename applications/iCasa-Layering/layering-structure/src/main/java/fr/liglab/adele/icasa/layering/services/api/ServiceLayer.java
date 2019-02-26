package fr.liglab.adele.icasa.layering.services.api;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * A service doesn't provide data, provides services
 * 
 */

public @ContextService interface ServiceLayer {
    
	public final static  @State String SERVICE_QOS = "service.qos";

    public int getQoS();

    public final static @State String NAME = "service.name";

    public String getServiceName();


}
