package models.values;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Entity
@Table(name="Category")
public class Category extends Model{
    @Id
    @Column(name="id")
    public int id;

    @Constraints.Required
    @Column(name="name", length=50)
    public String name;

    @Column(name="description", columnDefinition="TEXT")
    public String description;

    @OneToMany
    @JoinTable(name = "Product_has_Category")
    public List<ProductHasCategory> products = new ArrayList<ProductHasCategory>();

    /**
     * To locate Products
     */
    public static Finder<Integer,Category> find = new Finder<Integer, Category>(
            Integer.class, Category.class
    );

    public static ArrayNode toJson(List<Category> categories){
        ArrayNode result = Json.newObject().arrayNode();
        for(Category category: categories){
            result.add(Category.toJson(category));
        }
        return result;
    }

    public static ObjectNode toJson(Category category){
        ObjectNode result = Json.newObject();
        result.put("id", category.id);
        result.put("name", category.name);
        result.put("description", category.description);
        return result;
    }

    /**
     * get the list of all products
     * @return the list of products
     */
    public static List<Category> allAvailable() {
        List<Category> cats = find.all();
        System.out.println(cats);
        return cats;
    }

}

