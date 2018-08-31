package fr.liglab.adele.icasa.layering.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/icasa/layers")
public class ContextContent extends DefaultController {

    @Requires
    private Json json;

    @Requires(optional = true)
    private AdministrationService contextAdministrationService;

    @Route(method = HttpMethod.GET, uri = "/context.json")
    public Result getContextJSON() {
        ArrayNode result = json.newArray();

        if(contextAdministrationService != null){
            for(ImmutableContextEntity entity : contextAdministrationService.getContextEntities()){
                result.add(json.toJson(entity));
            }
        }

        return ok(result);
    }

    @Route(method = HttpMethod.GET, uri = "/context/{id}")
    public Result getContextEntity(@Parameter("id") String id) {

        ImmutableContextEntity immutableContextEntity = null;

        if(contextAdministrationService != null){
            immutableContextEntity = contextAdministrationService.getContextEntity(id);
        }

        return ok(immutableContextEntity).json();
    }


}
