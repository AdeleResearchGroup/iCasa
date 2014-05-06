package models.values;

import models.User;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 *
 */
@Entity
@Table(name="Orders")
public class Order extends Model {
    @Id
    @Column(name="id")
    public int id;

    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    public User user;

    @ManyToOne
    @JoinColumn(name="product_Price_id", referencedColumnName = "id")
    public ProductPrice price;
}
