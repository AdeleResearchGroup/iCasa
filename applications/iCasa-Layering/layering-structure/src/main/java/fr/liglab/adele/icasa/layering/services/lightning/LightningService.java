package fr.liglab.adele.icasa.layering.services.lightning;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;

public @ContextService interface  LightningService extends ServiceLayer {

	public void setSchedule(MomentOfTheDay.PartOfTheDay period);
}
