package fr.liglab.adele.icasa.layering.services.weather;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.global.ServiceLayer;

@ContextService
public interface AccuweatherService extends ServiceLayer{
   //@State String TEST = "test";
   int getWeatherValue();
}
