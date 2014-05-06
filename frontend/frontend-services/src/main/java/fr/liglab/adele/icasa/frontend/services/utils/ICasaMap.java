package fr.liglab.adele.icasa.frontend.services.utils;

import java.util.Hashtable;
import java.util.Map;

/**
 * This class represent a map information for the frontend.
 * Each maps in the frontend represents an iCasa Platform and an image file of the map.
 */
public class ICasaMap {

    /**
     * The identifier of the map.
     * The id will be generated using the name.
     */
    private String id;

    /**
     * The name of the map.
     */
    private String name;

    /**
     * A brief description of the map.
     */
    private String description;

    /**
     * The URL of the gateway to connect to.
     */
    private String gatewayURL;
    /**
     * The filename containing the image map.
     */
    private String imgFile;
    /**
     * A coma separated list of libs to load by the frontend.
     */
    private String libs;

    public ICasaMap(Map fromMap){
        this.id = String.valueOf(fromMap.get("id"));
        this.name = String.valueOf(fromMap.get("name"));
        this.description = String.valueOf(fromMap.get("description"));
        this.gatewayURL = String.valueOf(fromMap.get("gatewayURL"));
        this.imgFile = String.valueOf(fromMap.get("imgFile"));
        this.libs = String.valueOf(fromMap.get("libs"));
    }

    public ICasaMap(String name, String description, String gatewayURL, String libs) {
        this.name = name;
        this.description = description;
        this.gatewayURL = gatewayURL;
        this.libs = libs;
    }

    /**
     * The identifier of the map.
     * The id will be generated using the name.
     */
    public String getId() {
        return id;
    }

    /**
     * The identifier of the map.
     * The id will be generated using the name.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the map.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /**
     * A brief description of the map.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * The URL of the gateway to connect to.
     */
    public String getGatewayURL() {
        return gatewayURL;
    }

    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
    }
    /**
     * The filename containing the image map.
     */
    public String getImgFile() {
        return imgFile;
    }

    public void setImgFile(String imgFile) {
        this.imgFile = imgFile;
    }
    /**
     * A coma separated list of libs to load by the frontend.
     */
    public String getLibs() {
        return libs;
    }

    public void setLibs(String libs) {
        this.libs = libs;
    }

    public Map toMap(){
        Map map = new Hashtable();
        map.put("id", getId());
        map.put("name", getName());
        map.put("description", getDescription());
        map.put("gatewayURL", getGatewayURL());
        map.put("imgFile", getImgFile());
        map.put("libs", getLibs());
        return map;
    }

}
