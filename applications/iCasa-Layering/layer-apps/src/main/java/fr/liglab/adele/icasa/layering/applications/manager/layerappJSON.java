package fr.liglab.adele.icasa.layering.applications.manager;

import org.json.JSONException;
import org.json.JSONObject;

public class layerappJSON {
    public static final String NAME_PROP = "name";
    public static final String TYPE_PROP = "type";
    public static final String ID_PROP = "id";

    private String name;
    private String type;
    private String id;

    public layerappJSON(){

    }

    public static layerappJSON fromString(String jsonStr){
        layerappJSON layerapp = null;
        JSONObject json = null;
        try{
            json = new JSONObject(jsonStr);
            if(json.has(ID_PROP)){
               // layerappJSON.
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }


}
