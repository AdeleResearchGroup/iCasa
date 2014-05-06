package models;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.List;

/**
 *
 */
@Entity
@Table(name="OwnedDevice")
public class OwnedDevice extends Model {
    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;

    @Column(name="name")
    public String name;

    @Column(name="url")
    public String url;

    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    public User user;



    public static ObjectNode toJson(OwnedDevice product){
        ObjectNode result = Json.newObject();
        result.put("id", product.id);
        result.put("name", product.name);
        result.put("url", product.url);
        return result;
    }

    public static ArrayNode toJson(List<OwnedDevice> devices){
        ArrayNode result = Json.newObject().arrayNode();
        for(OwnedDevice device: devices){
            result.add(OwnedDevice.toJson(device));
        }
        return result;
    }

}
