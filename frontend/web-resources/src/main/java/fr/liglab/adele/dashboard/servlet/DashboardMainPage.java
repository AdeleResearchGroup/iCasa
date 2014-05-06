package fr.liglab.adele.dashboard.servlet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.IOException;

/**
 *
 */

@Component(name="icasa-dashboard-main-page")
@Provides
public class DashboardMainPage extends DefaultController {


    private final BundleContext context;

    public DashboardMainPage(BundleContext c){
        this.context = c;
    }

    @Route(method = HttpMethod.GET, uri = "/dashboard")
    public Result getDashboard(){
        String result = null;
        try {
            result = ResourceHandler.getTemplate(context, "www/index.html").toString();
        } catch (IOException e) {
            return internalServerError();
        }
        result = result.replace("@servletType", "dashboard");//dashboard or simulator.
        return ok(result).as(MimeTypes.HTML);
    }


}
