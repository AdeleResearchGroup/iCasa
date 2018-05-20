package fr.liglab.adele.icasa.layering.services.shutterOpening;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;

public @ContextService
interface ShutterServiceInterface extends ServiceLayer {
    //@State String test="test";
    boolean setShutterSLevel(Double level);

}
