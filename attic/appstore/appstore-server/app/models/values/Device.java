package models.values;

import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.List;

/**
 *
 */
@Entity
@Table(name="Device")
public class Device extends Model{
    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;

    @Constraints.Required
    @Column(name="name", length=50)
    public String name;

    @Column(name="description", columnDefinition="TEXT")
    public String description;

    @Column(name="imageURL")
    public String imageURL;

    @ManyToMany
    @JoinTable(name="ProductVersion_has_Device")
    List<ProductVersion> productVersions;

    /**
     * To locate Products
     */
    public static Model.Finder<Integer,Device> find = new Model.Finder<Integer, Device>(
            Integer.class, Device.class
    );

    /**
     * get the list of all products
     * @return the list of products
     */
    public static List<Device> all() {
        return find.all();
    }


    public static ObjectNode toJson(Device device){
        ObjectNode result = Json.newObject();
        if (device != null) {
            result.put("id", device.id);
            result.put("name", device.name);
            result.put("description", device.description);
            result.put("imageURL", device.imageURL);
        }
        return result;
    }

    public static void create(Device device){
        device.save();
    }

}
