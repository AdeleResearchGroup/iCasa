package controllers;

import models.values.Category;
import org.codehaus.jackson.node.ArrayNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.List;

/**
 *
 */
public class CategoryController extends Controller {
    @SecureSocial.SecuredAction
    public static Result getAvailableCategories(){
        List<Category> categoryList = Category.allAvailable();
        ArrayNode categories = Json.newObject().arrayNode();
        for (Category category: categoryList){
            categories.add(Category.toJson(category));
        }
        return ok(categories);
    }
}
