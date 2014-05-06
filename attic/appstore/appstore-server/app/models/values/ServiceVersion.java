package models.values;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Entity
@Table(name="ServiceVersion")
public class ServiceVersion extends Model {
    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;

    @Column(name="version")
    public String version;

    @ManyToMany
    @JoinTable(name="ProductVersion_has_ServiceVersion")
    Set<ProductVersion> productVersions;

    @ManyToOne
    @JoinColumn(table = "Service", name = "service_id")
    public Service service;

    public static ObjectNode toJson(ServiceVersion serviceVersion){
        ObjectNode result = Json.newObject();
        if (serviceVersion != null) {
            result.put("id", serviceVersion.id);
            result.put("version", serviceVersion.version);
        }
        return result;
    }

    public static ArrayNode toJson(List<ServiceVersion> list){
        ArrayNode result = Json.newObject().arrayNode();
        if (list != null ){
            for (ServiceVersion serviceVersion: list){
                result.add(ServiceVersion.toJson(serviceVersion));
            }
        }
        return result;
    }
}

