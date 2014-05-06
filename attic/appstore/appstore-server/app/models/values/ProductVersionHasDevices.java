package models.values;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 *
 */
@Entity
@Table(name="ProductVersion_has_Device")
public class ProductVersionHasDevices extends Model {

    @OneToOne
    @JoinColumn(name = "productVersion_id")
    public ProductVersion productVersion;

    @OneToOne
    @JoinColumn(name = "device_id")
    public Device device;

}