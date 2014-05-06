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
@Table(name="ApplicationVersion")
public class ApplicationVersion extends Model {
    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;

    @Column(name="version")
    public String version;

//    @Column(name="application_id")
//    public int application_id;

    @ManyToMany
    @JoinTable(name="ProductVersion_has_ApplicationVersion")
    Set<ProductVersion> productVersions;

    @ManyToOne
    @JoinColumn(table = "Application", name = "application_id")
    public Application application;

    public static ObjectNode toJson(ApplicationVersion applicationVersion){
        ObjectNode result = Json.newObject();
        if (applicationVersion != null) {
            result.put("id", applicationVersion.id);
            result.put("version", applicationVersion.version);
        }
        return result;
    }

    public static ArrayNode toJson(List<ApplicationVersion> list){
        ArrayNode result = Json.newObject().arrayNode();
        if (list != null ){
            for (ApplicationVersion applicationVersion: list){
                result.add(ApplicationVersion.toJson(applicationVersion));
            }
        }
        return result;
    }
}

