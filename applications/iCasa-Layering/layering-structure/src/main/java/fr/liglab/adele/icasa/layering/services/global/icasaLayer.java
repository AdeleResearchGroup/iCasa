package fr.liglab.adele.icasa.layering.services.global;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;


public @ContextService
interface icasaLayer {
    @State String CURRENT_STATE = "current.state";
   /* @State
    String ZONE_ATTACHED = "zone.attached";*/
}
