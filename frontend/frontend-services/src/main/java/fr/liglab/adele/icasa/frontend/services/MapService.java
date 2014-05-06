package fr.liglab.adele.icasa.frontend.services;

import fr.liglab.adele.icasa.frontend.services.utils.ICasaMap;

import java.util.Set;

/**
 *
 */
public interface MapService {

    /**
     *
     * Retrieves the set of ICasaMaps in the server.
     * @return
     */
    Set<ICasaMap> getMaps();

    /**
     * Check the existence of a map.
     * @param mapId
     * @return
     */
    boolean contains(String mapId);

    /**
     * Get a specific iCasaMap information
     * @param mapId
     * @return
     */
    ICasaMap getMap(String mapId);

    /**
     * Get the path where the maps are
     * @return
     */
    String getLocation();

    /**
     * Add a new map to the service
     * @param map the new map
     * @return the identifier of the map.
     */
    String addMap(ICasaMap map);

    /**
     * Update map information.
     * @param id
     * @param name
     * @param description
     * @param gatewayURL
     * @param libs
     * @return
     */
    ICasaMap updateMap(String id, String name, String description, String gatewayURL, String libs);

    /**
     * Removes a map
     * @param id the identifier of the map.
     * @return the reference of the removed map.
     */
    ICasaMap removeMap(String id);

}
