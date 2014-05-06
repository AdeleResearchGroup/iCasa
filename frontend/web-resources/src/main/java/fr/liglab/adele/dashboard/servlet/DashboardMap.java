package fr.liglab.adele.dashboard.servlet;

import fr.liglab.adele.icasa.frontend.services.MapService;
import fr.liglab.adele.icasa.frontend.services.utils.ICasaMap;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;


import java.io.*;
import java.net.URL;

/**
 *
 */

@Component(name="icasa-dashboard-map-page")
@Provides
public class DashboardMap extends DefaultController {

    private final BundleContext context;

    public DashboardMap(BundleContext c){
        this.context = c;
    }

    @Requires
    MapService mapService;


    @Route(method = HttpMethod.GET, uri = "/dashboard/map/{mapId}")
    public Result getDashboardMap(@Parameter("mapId") String mapId){
        String result = null;

        if (!mapService.contains(mapId)){
            return notFound();
        } else {
            ICasaMap map = mapService.getMap(mapId);
            try {
                result = ResourceHandler.getTemplate(context, "www/map.html").toString();
            } catch (IOException e) {
                e.printStackTrace();
                return internalServerError();
            }
            result = result.replace("@mapId", mapId);
            result = result.replace("@pluginIds", "");
            result = result.replace("@widgetIds", "");
            result = result.replace("@servletType", "dashboard");//dashboard or simulator.
            result = result.replace("@gatewayURL", map.getGatewayURL());
            result = result.replace("@mapImgSrc", "/icasa/maps/" + map.getImgFile());
        }
        result = result.replace("@servletType", "dashboard");//dashboard or simulator.
        return ok(result).as(MimeTypes.HTML);
    }



}
