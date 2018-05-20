package fr.liglab.adele.icasa.layering.controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

@Controller
public class ContextVisualization extends DefaultController{

    @View("ContextStatus")
    Template ContextStatus;

    @Route(method=HttpMethod.GET, uri ="/layers/ContextStatus")
    public Result ContextStatus() { return ok(render(ContextStatus));}
}
