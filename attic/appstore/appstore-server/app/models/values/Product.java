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
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Entity
@Table(name="Product")
public class Product extends Model  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6533282470018272197L;

	@Id
	@Column(name="id")
    @GeneratedValue
	public int id;
	
	@Required
	@Column(name="name", length=50)
	public String name;
	
	@Column(name="description", columnDefinition="TEXT")
	public String description;
	
	@Column(name="imageURL")
	public String imageURL;

    @OneToMany (cascade=CascadeType.ALL, mappedBy = "product")
    public List<ProductVersion> versions;

    @OneToMany
    @JoinTable(name = "Product_has_Category")
    public List<ProductHasCategory> productHasCategory = new ArrayList();

    @OneToOne
    @JoinColumn(name="productVersion_id", referencedColumnName = "id")
    public ProductVersion lastVersion;

    @OneToMany
    @JoinTable(name = "Product_Price")
    public List<ProductPrice> prices;


    public List<Category> categories = new ArrayList();

    public List<Application> applications = new ArrayList();

    public List<Service> services = new ArrayList();

    public List<Device> devices = new ArrayList();


	/**
	 * To locate Products
	 */
	public static Finder<Integer,Product> find = new Finder(
		    Integer.class, Product.class
		  );
	
	/**
	 * get the list of all products
	 * @return the list of products
	 */
	public static List<Product> all() {
		return find.all();
	}

	
	public static List<Product> getTopProducts(int topNumber){
		//TODO change the search mechanism.
		return getPageProducts(topNumber, 0);
	}
	
	public static List<Product> getPageProducts(int pageSize, int pageNumber){
        return find.all();//findPagingList(pageSize).getPage(pageNumber).getList();
	}
	
	public static int getPageCount(int pageSize){
		return find.findPagingList(pageSize).getTotalPageCount();
	}
	
	/**
	 * Add a product to the DB
	 * @param product
	 */
	public static void create(Product product) {
        //Add the given version as the current version
        Ebean.beginTransaction();
        try{
        Ebean.save(product);

        Ebean.refresh(product);

        if (product.versions != null && product.lastVersion == null) {
            product.lastVersion = product.versions.get(product.versions.size()-1); //When creating there is only one
            Ebean.save(product);
        }

        for(Category cat:product.categories){
            ProductHasCategory phc=new ProductHasCategory();
            phc.product=product;
            phc.category=cat;
            Ebean.save(phc);
            product.productHasCategory.add(phc);
            Ebean.update(product);
        }

        for(Application application:product.applications){
            ProductVersionHasApplications pvha = new ProductVersionHasApplications();
            Ebean.refresh(application);
            ApplicationVersion aVersion = application.lastVersion;
            ProductVersion pversion = product.lastVersion;
            Ebean.refresh(aVersion);
            Ebean.refresh(pversion);
            pvha.applicationVersion = aVersion;
            pvha.productVersion = pversion;
            pversion.applicationVersions.add(aVersion);
            Ebean.update(pversion);
            Ebean.save(pvha);
        }

        for(Service service:product.services){
            ProductVersionHasServices pvha = new ProductVersionHasServices();
            Ebean.refresh(service);
            pvha.serviceVersion = service.lastVersion;
            pvha.productVersion = product.lastVersion;
            Ebean.save(pvha);
        }

        for(Device device:product.devices){
            ProductVersionHasDevices pvhd = new ProductVersionHasDevices();
            Ebean.refresh(device);
            pvhd.device = device;
            pvhd.productVersion = product.lastVersion;
            Ebean.save(pvhd);
         }
        Ebean.update(product);
        }finally {
            Ebean.commitTransaction();
        }
        //Ebean.saveAssociation(product, "categories");

	}
	
	/**
	 * Remove an existent product
	 * @param identifier
	 */
	public static void remove(Integer identifier){
		find.byId(identifier).delete();
	}

	public static ObjectNode toJson(Product product){
		ObjectNode result = Json.newObject();
		result.put("id", product.id);
		result.put("name", product.name);
		result.put("description", product.description);
        result.put("imageURL", product.imageURL);
        result.put("versions", ProductVersion.toJson(product.versions));
        result.put("lastVersion", ProductVersion.toJson(product.lastVersion));
        result.put("categories", Category.toJson(product.categories));
        result.put("applications", ProductVersion.getApplications(product.lastVersion));
        result.put("services", ProductVersion.getServices(product.lastVersion));
        result.put("devices", ProductVersion.getDevices(product.lastVersion));
		return result;
	}


    public ProductPrice getPrice(){
        ProductPrice price = null;
        if (prices == null){
            prices = new ArrayList();
        }
        if (prices.size()<1){
            price = new ProductPrice();
            price.price = 0.0f;
            price.product = this;
            price.unit = "€";
            prices.add(price);
            price.save();
            this.save();
            System.out.println("New Price");
        } else {
            price = prices.get(0);
            System.out.println("Existing Price");

        }
        return price;
    }

    public static List<Product> getProductByCategory(int categoryId){

        List <ProductHasCategory> productHC = Ebean.find(ProductHasCategory.class).where("category.id="+categoryId).findList();//where().eq("category",Category.find.byId(categoryId))
        ArrayList<Product> products=new ArrayList<Product>();

        for(ProductHasCategory s:productHC){
            products.add(s.product);
        }

        return products;
    }


}
