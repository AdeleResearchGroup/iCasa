package fr.liglab.adele.icasa.frontend.services.rest;


import fr.liglab.adele.icasa.frontend.services.MapService;
import fr.liglab.adele.icasa.frontend.services.utils.ICasaMap;
import fr.liglab.adele.icasa.frontend.services.utils.JSONMap;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Attribute;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.*;


import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Service implementation to retrieve available maps in the frontend service.
 */
@Component
@Instantiate
@Provides
//@Path(value = "/frontend/maps")
public class MapRemoteServiceImpl extends DefaultController {

    @Requires
    MapService mapService;



    @Route(method = org.wisdom.api.http.HttpMethod.GET, uri = "/icasa/frontend/maps")
    public Result getMaps() {
        return ok(getMapsInString()).as(MimeTypes.JSON);
    }

    public String getMapsInString(){
        JSONArray currentMaps = new JSONArray();
        for (ICasaMap map : mapService.getMaps()) {


            JSONObject mapJSON = null;
            try {
                mapJSON = JSONMap.toJSON(map);
            } catch (JSONException e) {
                mapJSON = null;
                e.printStackTrace();
            }
            if (mapJSON == null)
                continue;
            currentMaps.put(mapJSON);
        }

        return currentMaps.toString();
    }

    @Route(method = org.wisdom.api.http.HttpMethod.POST, uri = "/icasa/frontend/maps")
    public Result postMap(
            @Attribute("mapId") String id,
            @Attribute("mapName") String name,
            @Attribute("mapDescription") String description,
            @Attribute("gatewayURL") String gatewayURL,
            @Attribute("libs") String libs,
            @Attribute("picture") FileItem fileDetail
            //@FormDataParam("picture") FormDataContentDisposition fileDetail,
            //@HeaderParam("Referer") String refer
            ) {

        ICasaMap map;
        //get new or to update map.
        if(!id.isEmpty()){ //update map.
            map = mapService.updateMap(id, name, description, gatewayURL, libs);
            if(map == null){
                return notFound();
            }
        }  else{ // new map.
            map = new ICasaMap(name, description, gatewayURL, libs);
            mapService.addMap(map);
        }

        //handle image

        if(!fileDetail.name().isEmpty()){
            handleImage(map, fileDetail.stream());
        }
        //we don't know if its an upload from simulator or dashboard, so we redirect to referer.
        String host = context().header("Referer");
        System.out.println("Referer" + host);
        return status(Status.SEE_OTHER).with("host",host);
    }

    @Route(method = org.wisdom.api.http.HttpMethod.DELETE, uri = "/icasa/frontend/maps/delete")
    public Result deleteMap(
            @Attribute("mapId") String id/*,
            /*@HeaderParam("Referer") String refer*/) {

        ICasaMap map = mapService.removeMap(id);

        //remove image file.
        File file = new File(mapService.getLocation() + "/" + map.getImgFile());
        if(file.exists()){
            file.delete();
        }

        //we don't know if its a remove from simulator or dashboard, so we redirect to referer.
        String host = context().header("Referer");
        System.out.println("Referer" + host);
        return status(Status.SEE_OTHER).with("host",host);
    }

    @Route(method = HttpMethod.GET, uri = "/icasa/maps/{mapFile}")
    public Result getMapFile(@Parameter("mapFile") String mapFile){
        File file = new File("maps", mapFile);
        return ok(file);
    }

    private boolean handleImage(ICasaMap map, InputStream uploadedInputStream){
        String fileURL = mapService.getLocation() + "/" + map.getImgFile();
        try {
            OutputStream newFileStream = new FileOutputStream(new File(fileURL));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = uploadedInputStream.read(buffer)) != -1) {
                newFileStream.write(buffer, 0, len);
            }
            uploadedInputStream.close();
            newFileStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }
}
