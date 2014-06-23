package fr.liglab.adele.icasa.electricity.manager.sample;

import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Created by horakm on 4/10/14.
 */
@Entity
public class ConsumptionSampleDevice implements ConsumptionSample {

    @Id
    private long id;

    @NotNull
    private String location;

    @NotNull
    private String device;

    @NotNull
    private double consumption;

    @NotNull
    private DateTime date;

    public ConsumptionSampleDevice(long id, String location, String device, double consumption, DateTime date) {
        this.id = id;
        this.location = location;
        this.device = device;
        this.consumption = consumption;
        this.date = date;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public double getConsumption() {
        return this.consumption;
    }

    @Override
    public DateTime getDate() {
        return this.date;
    }

    public String getLocation() {
        return this.location;
    }

    public String getDevice() {
        return this.device;
    }
}
