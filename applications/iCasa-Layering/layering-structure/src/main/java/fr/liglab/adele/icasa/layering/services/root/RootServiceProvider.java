package fr.liglab.adele.icasa.layering.services.root;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.layering.services.api.RootService;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate = true, publicFactory = false)
@Instantiate

public class RootServiceProvider {
	
    @Creator.Field Creator.Entity<RootServiceImpl> creator;

    @Creator.Field(RootService.ZONES) Creator.Relation<RootService,Zone> zoneAttachmentCreator;

    @Validate
    public void start(){
    	creator.create("iCasa_root");
    }
    @Invalidate
    public void stop(){
        creator.delete("iCasa_root");
    }

    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true, proxy = false)
    public void bindZone(Zone zone){
    	zoneAttachmentCreator.link("iCasa_root",zone);
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone){
    	zoneAttachmentCreator.unlink("iCasa_root",zone);
    }
}
