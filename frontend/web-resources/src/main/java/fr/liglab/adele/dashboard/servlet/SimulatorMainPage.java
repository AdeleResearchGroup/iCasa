package fr.liglab.adele.dashboard.servlet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 */

@Component(name="icasa-simulator-main-page")
@Provides
public class SimulatorMainPage extends DefaultController {


    private final BundleContext context;

    public SimulatorMainPage(BundleContext c){
        this.context = c;
    }

    @Route(method = HttpMethod.GET, uri = "/simulator")
    public Result getSimulator(){
        String result = null;
        try {
            result = ResourceHandler.getTemplate(context, "www/index.html").toString();
        } catch (IOException e) {
            return internalServerError();
        }
        result = result.replace("@servletType", "simulator");//dashboard or simulator.
        return ok(result).as(MimeTypes.HTML);
    }



}
