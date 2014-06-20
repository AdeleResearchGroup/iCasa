package fr.liglab.adele.icasa.electricity.manager.sample;

import org.joda.time.DateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Created by horakm on 4/17/14.
 */

@Entity
public class ConsumptionSampleZone implements ConsumptionSample{

    @Id
    private  long id;

    @NotNull
    private String zone;

    @NotNull
    private double consumption;

    @NotNull
    private DateTime date;

    public ConsumptionSampleZone(long id, String zone, double consumption, DateTime date) {
        this.id = id;
        this.zone = zone;
        this.consumption = consumption;
        this.date = date;
    }

    @Override
    public double getConsumption() {
        return this.consumption;
    }

    @Override
    public DateTime getDate() {
        return this.date;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public String getZone() {
        return zone;
    }
}
