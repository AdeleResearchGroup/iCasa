package fr.liglab.adele.icasa.layering.services.weather;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;


public @ContextService interface AccuweatherService extends ServiceLayer {

	public int getWeatherValue();
}
