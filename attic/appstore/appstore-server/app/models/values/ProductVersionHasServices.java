package models.values;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 *
 */
@Entity
@Table(name="ProductVersion_has_ServiceVersion")
public class ProductVersionHasServices extends Model {
    @OneToOne
    @JoinColumn(name = "productVersion_id")
    public ProductVersion productVersion;

    @OneToOne
    @JoinColumn(name = "serviceVersion_id")
    public ServiceVersion serviceVersion;
}

