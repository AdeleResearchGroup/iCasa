package fr.liglab.adele.icasa.layering.controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

@Controller
@Path("/icasa/layers")
public class ContextVisualization extends DefaultController{

    @View("ContextStatus")
    Template ContextStatus;

    @Route(method=HttpMethod.GET, uri ="/diagram/context")
    public Result contextStatus() { return ok(render(ContextStatus));}
}
