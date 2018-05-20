package fr.liglab.adele.icasa.layering.services.icasaLayer;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate = true,publicFactory = false)
@Instantiate
public class icasaLayerProvider extends iCasaLayerImpl{
    @Creator.Field Creator.Entity<iCasaLayerImpl> creator;
    @Creator.Field Creator.Entity<Zone> zn;

    @Creator.Field(iCasaLayerImpl.RELATION_IS_ATTACHED) Creator.Relation<iCasaLayerImpl,Zone> attach;

    @Validate
    public void start(){
        if(creator.getInstances().isEmpty()){
            creator.create("iCasa_root");
        }

    }
    @Invalidate
    public void stop(){
        creator.delete("iCasa_root");


    }

    @Bind(id="zones",specification = Zone.class,aggregate = true,optional = true)
    public void bindZone(Zone zone){
        attach.create("iCasa_root",zone);
        System.out.println(zn);


    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone){
        attach.delete("iCasa_root",zone);
    }
}
