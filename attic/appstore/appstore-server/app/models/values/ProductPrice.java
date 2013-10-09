package models.values;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: torito
 * Date: 1/15/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="Product_Price")
public class ProductPrice extends  Model {
    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;
    @Column(name="price")
    public float price;
    @Column(name="unit")
    public String unit;

    @ManyToOne
    @JoinColumn(name="product_id", referencedColumnName = "id")
    public Product product;

    @OneToMany
    @JoinTable(name="Orders")
    public Set<Order> orders;

}
