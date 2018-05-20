package fr.liglab.adele.icasa.layering.services.shutterOpening;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

@Component(immediate=true,publicFactory = false)
@Instantiate
public class ShuttersServiceProvider {
    @Creator.Field Creator.Entity<ShuttersServiceImpl> creator;
    @Creator.Field(ShuttersServiceImpl.RELATION_IS_ATTACHED) Creator.Relation<ShuttersServiceImpl,Zone> crerelation;


    @Bind(id="zones",specification = Zone.class,aggregate = true,optional = false)
    public void bindZone(Zone zone){
        //System.out.println("zones:   "+creator.getInstances());
        //System.out.println(creator.getInstances());
        String name=generateEntityName(zone);
        creator.create(name);

        //System.out.println("ZONE:   "+zone);
        //System.out.println(creator.getInstances());

        //System.out.println("nm: "+name+" zn: "+zone);
        crerelation.create(name,zone);
    }
    @Unbind(id="zones")
    public void unbindZone(Zone zone){
        String name = generateEntityName(zone);
        creator.delete(name);
        crerelation.delete(name,zone);
    }

    private String generateEntityName(Zone zone){return zone.getZoneName()+".shutters";}
}
