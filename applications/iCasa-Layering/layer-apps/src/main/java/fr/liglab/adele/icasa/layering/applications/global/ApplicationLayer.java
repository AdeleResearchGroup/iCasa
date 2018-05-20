package fr.liglab.adele.icasa.layering.applications.global;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

@ContextService
public interface ApplicationLayer {
    @State String status = "application.status";

    boolean getStatus();
    boolean setStaus();
    String getName();
    String getProvider();

}
