package fr.liglab.adele.icasa.layering.services.lightning;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

@Component(immediate=true,publicFactory = false)
@Instantiate
public class LightningServiceProvider {
    @Creator.Field Creator.Entity<LightningServiceImpl> creator;
   // @Creator.Field Creator.Entity<AccuweatherServiceLayer> creator2;
    @Creator.Field(LightningServiceImpl.RELATION_IS_ATTACHED) Creator.Relation<LightningServiceImpl,Zone> attachedLighthingServiceCreator;

    @Bind(id="zones",specification = Zone.class,aggregate = true,optional = false)
    public void bindZone(Zone zone){
        String name=generateEntityName(zone);
        creator.create(name);
        //System.out.println("nm: "+name+" zn: "+zone);
        attachedLighthingServiceCreator.create(name,zone);
       // creator2.create(name,zone);
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone){
        String name = generateEntityName(zone);
        creator.delete(name);
        attachedLighthingServiceCreator.delete(name,zone);
    }

    private String generateEntityName(Zone zone){return zone.getZoneName()+".lightning";}



}
