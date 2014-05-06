package models.values;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 *
 */
@Entity
@Table(name="ProductVersion_has_ApplicationVersion")
public class ProductVersionHasApplications extends Model {
    @Id
    @GeneratedValue
    @Column(name="id")
    public int id;
    @OneToOne
    @JoinColumn(name = "productVersion_id")
    public ProductVersion productVersion;

    @OneToOne
    @JoinColumn(name = "applicationVersion_id")
    public ApplicationVersion applicationVersion ;


}
