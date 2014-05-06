/*
 * Copyright ADELE Research Group LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package models.values;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
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
@Table(name="Service")
public class Service extends Model  {

	private static final long serialVersionUID = 498903206959165812L;



	@Id
	@Column(name="id", length=25)
    @GeneratedValue
    public int id;

	@Column(name="name", length=50)
	public String name;

	@Column(name="description", columnDefinition="TEXT")
	public String description;

    @OneToMany (cascade=CascadeType.ALL, mappedBy = "service")
    public List<ServiceVersion> versions;

    @OneToOne
    @JoinColumn(name="lastVersion_id", referencedColumnName = "id")
    public ServiceVersion lastVersion;

	public static Finder<Integer,Service> find = new Finder<Integer, Service>(
			Integer.class, Service.class
			);

	public static List<Service> all() {
		return find.all();

	}


	public static void create(Service service) {
        Transaction trans = Ebean.beginTransaction();
        Ebean.save(service);

        Ebean.refresh(service);
        service.save();
        if (service.versions != null && service.lastVersion == null) {
            service.lastVersion = service.versions.get(service.versions.size()-1); //When creating there is only one
            Ebean.save(service);
        }
        Ebean.refresh(service);
        trans.commit();
	}

	public static void remove(Integer identifier){
		find.byId(identifier).delete();
	}

    public static ObjectNode toJson(Service service){
        ObjectNode result = Json.newObject();
        if (service != null) {
            result.put("id", service.id);
            result.put("name", service.name);
            result.put("description", service.description);
        }
        return result;
    }

    public static ArrayNode toJson(List<Service> list){
        ArrayNode result = Json.newObject().arrayNode();
        if (list != null ){
            for (Service service: list){
                result.add(Service.toJson(service));
            }
        }
        return result;
    }

}
