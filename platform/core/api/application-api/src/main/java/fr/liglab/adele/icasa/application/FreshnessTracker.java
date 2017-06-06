package fr.liglab.adele.icasa.application;

import java.util.List;

/**
 * Created by jhonnymertz on 06/06/17.
 */
public interface FreshnessTracker {

    void computeDemands(List<Application> applications);
}
