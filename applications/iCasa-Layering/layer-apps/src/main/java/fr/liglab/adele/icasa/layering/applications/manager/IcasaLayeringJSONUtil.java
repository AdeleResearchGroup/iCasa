package fr.liglab.adele.icasa.layering.applications.manager;

import fr.liglab.adele.icasa.layering.applications.global.ApplicationLayer;
import fr.liglab.adele.icasa.remote.wisdom.util.IcasaJSONUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class IcasaLayeringJSONUtil  extends IcasaJSONUtil {

    public static JSONObject getLayerapp(ApplicationLayer layerapp){
        JSONObject layerappJSON = null;
        try{
            layerappJSON = new JSONObject();
                layerappJSON.putOnce("name",layerapp.getName());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return layerappJSON;
    }
}
