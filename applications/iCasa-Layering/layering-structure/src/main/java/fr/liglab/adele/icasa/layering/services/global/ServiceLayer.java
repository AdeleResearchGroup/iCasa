package fr.liglab.adele.icasa.layering.services.global;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

/**
 * A service doesn't provide data, provides services
 * serviceCompletion allows
 */

public @ContextService
interface ServiceLayer {
    @State String CURRENT_STATE = "current.state";
    @State String SERVICE_QOS = "service.qos";
    @State String ZONE_ATTACHED = "zone.attached";
    @State String LOCKED = "service.locked";
    @State String NAME = "service.name";
    int getMinQos();
    String getServiceName();
    boolean getRegistrationState();
    int getServiceQoS();
    String getState();
    String setState(int setterVar);

}
