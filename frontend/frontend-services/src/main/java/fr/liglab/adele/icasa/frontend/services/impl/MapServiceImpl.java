package fr.liglab.adele.icasa.frontend.services.impl;

import fr.liglab.adele.icasa.frontend.services.MapService;
import fr.liglab.adele.icasa.frontend.services.utils.AbstractXMLParser;
import fr.liglab.adele.icasa.frontend.services.utils.ICasaMap;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

/**
 *
 */
@Component(name = "maps-frontend-service")
@Instantiate(name = "maps-frontend-service-0")
@Provides
public class MapServiceImpl extends AbstractXMLParser implements MapService {

    @Property(name="location", value = "maps")
    private String location;

    private Map<String, ICasaMap> icasaMaps = new HashMap();

    private Object lockObject = new Object();

    @Validate
    public void validate(){
        loadMaps();
    }

    @Override
    public Set<ICasaMap> getMaps() {
        HashSet returnSet = new HashSet();
        synchronized (lockObject){
            for(String id: icasaMaps.keySet()){
                returnSet.add(icasaMaps.get(id));
            }
        }
        return returnSet;
    }

    @Override
    public boolean contains(String mapId) {
        synchronized (lockObject){
            return icasaMaps.containsKey(mapId);
        }
    }

    @Override
    public ICasaMap getMap(String mapId) {
        synchronized (lockObject){
            return icasaMaps.get(mapId);
        }
    }

    public void loadMaps(){
        synchronized (lockObject){
            List<Map> maps = loadFile();
            if(maps == null){
                return;
            }
            icasaMaps.clear();
            for (Map map: maps){
                ICasaMap iCasaMap = new ICasaMap(map);
                icasaMaps.put(iCasaMap.getId(), iCasaMap);
            }
        }
    }

    public void saveMaps(){
        writeFile();
    }

    @Override
    public List getInfo() {
        Set<ICasaMap> maps = getMaps();
        List<Map> mapsInList = new ArrayList();
        for (ICasaMap map : maps) {
            mapsInList.add(map.toMap());
        }
        return mapsInList;
    }



    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String addMap(ICasaMap map) {
        String id = null;
        synchronized (lockObject){
            if(map.getId() == null){
                id = generateId(map.getName());
                map.setId(id);
            }
            if(map.getImgFile() == null){
                map.setImgFile(map.getId() + ".png");
            }
            icasaMaps.put(map.getId(), map);
            saveMaps();
        }
        return id;
    }

    @Override
    public ICasaMap updateMap(String id, String name, String description, String gatewayURL, String libs) {
        synchronized (lockObject){
            if(!contains(id)){
                return null;
            }
            ICasaMap map = getMap(id);
            if(name != null){
                map.setName(name);
            }
            if(description != null){
                map.setDescription(description);
            }
            if(gatewayURL!= null){
                map.setGatewayURL(gatewayURL);
            }
            if(libs!= null){
                map.setLibs(libs);
            }
            saveMaps();
            return map;
        }

    }

    /**
     * Removes a map
     *
     * @param id the identifier of the map.
     * @return the reference of the removed map.
     */
    @Override
    public ICasaMap removeMap(String id) {
        synchronized (lockObject){
            ICasaMap map = icasaMaps.remove(id);
            saveMaps();
            return map;
        }
    }

    @Override
    public String getFileName() {
        return "maps";
    }



    private String generateId(String name){
        String mapId = name.replaceAll("[^A-Za-z0-9]", "");//remove non-alphanumeric char
        Random random = new Random();
        while (contains(mapId)){   //Synchronization issue, map could be aggregated meanwhile.
            mapId = mapId + random.nextInt(); //add a number.
        }
        return mapId;
    }

}
