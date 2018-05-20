package fr.liglab.adele.icasa.layering.controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.liglab.adele.cream.administration.api.AdministrationService;
import fr.liglab.adele.cream.administration.api.ImmutableContextEntity;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

@Controller
@SuppressWarnings("unused")
public class ContextContent extends DefaultController {

    @Requires
    @SuppressWarnings("unused")
    private Json json;

    @Requires(optional = true)
    @SuppressWarnings("unused")
    private AdministrationService contextAdministrationService;

    @Route(method = HttpMethod.GET, uri = "/layers/context")
    @SuppressWarnings("unused")
    public Result getContext(){
        ArrayNode result = json.newArray();

        if(contextAdministrationService != null){
            for(ImmutableContextEntity entity : contextAdministrationService.getContextEntities()){
                result.add(json.toJson(entity));
            }
        }

        return ok(result);
    }

    @Route(method = HttpMethod.GET, uri = "/layers/context/{id}")
    @SuppressWarnings("unused")
    public Result getContextEntity(@Parameter("id") String id){

        ImmutableContextEntity immutableContextEntity = null;

        if(contextAdministrationService != null){
            immutableContextEntity = contextAdministrationService.getContextEntity(id);
        }

        return ok(immutableContextEntity).json();
    }


}
