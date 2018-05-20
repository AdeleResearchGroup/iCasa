package fr.liglab.adele.icasa.layering.services.icasaLayer;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.global.icasaLayer;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ContextEntity(coreServices = icasaLayer.class)
@SuppressWarnings("unused")
public class iCasaLayerImpl implements icasaLayer{
    public static final String RELATION_IS_ATTACHED="model.attached.to";
    private static final Logger LOG = LoggerFactory.getLogger(iCasaLayerImpl.class);
    private static final String LOG_PREFIX = "iCASA";
    //States from icasaLayer
   // @ContextEntity.State.Field(service=icasaLayer.class,state = icasaLayer.ZONE_ATTACHED)
    @ContextEntity.State.Field(service = icasaLayer.class,state = icasaLayer.CURRENT_STATE,value="init")
    public String status;

    private final static String taskName = "home-lighting";
    private boolean isReconfiguring = false;
    private boolean needToBeReconfigured = true;
    //4 integration with context
    private static boolean registered;

    @Validate
    @SuppressWarnings("unused")
    public void start(){
        //toDo
        //registered=false;
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop(){if (registered){
        //ToDO
    }
    }

    //REQUIREMENTS

    //relations
    @ContextEntity.Relation.Field(RELATION_IS_ATTACHED)
    @Requires(id="zone",specification=Zone.class,optional=true)
    List<Zone> zone;

    @Bind(id="zone")
    public void bindZone(Zone zone){
        //System.out.println("FROM ICASA:"+zone.getZoneName());
    }
}
