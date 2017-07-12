package fr.liglab.adele.icasa.apps.demo.pet.care.context.entities;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.PetInfo;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.FeedingRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.WateringRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.util.PetCareInfo;
import org.apache.felix.ipojo.annotations.Requires;

@ContextEntity(coreServices = {PetInfo.class})
public class Pet implements PetInfo {

    @ContextEntity.State.Field(service = PetInfo.class, state = PetInfo.NAME)
    @SuppressWarnings("unused")
    private String name;

    @ContextEntity.State.Field(service = PetInfo.class, state = PetInfo.VALIDITY, directAccess = true)
    @SuppressWarnings("unused")
    private boolean infoValidity;

    @ContextEntity.State.Field(service = PetInfo.class, state = PetInfo.CARE_INFO, directAccess = true)
    @SuppressWarnings("unused")
    private PetCareInfo petCareInfo;


    public static final String RELATION_FEED_BY = "feed.by";
    public static final String RELATION_WATERED_BY = "watered.by";

    @ContextEntity.Relation.Field(value = RELATION_WATERED_BY,owner = PetInfo.class)
    @Requires(specification=WateringRegulator.class,optional=true)
    @SuppressWarnings("unused")
    private WateringRegulator wateringRegulator;

    @ContextEntity.Relation.Field(value = RELATION_FEED_BY,owner = PetInfo.class)
    @Requires(specification=FeedingRegulator.class,optional=true)
    @SuppressWarnings("unused")
    private FeedingRegulator feedingRegulator;

    @Override
    public String getPetName() {
        return name;
    }

    @Override
    public boolean getPetCareInfoValidity() {
        return infoValidity;
    }

    @Override
    public PetCareInfo getPetCareInfo() {
        return petCareInfo;
    }

    @Override
    public boolean setPetCareInfo(PetCareInfo petCareInfo) {
        infoValidity = this.petCareInfo.setPetCareInfo(petCareInfo);
        return infoValidity;
    }

    @Override
    public WateringRegulator getWateringRegulator() {
        return wateringRegulator;
    }

    @Override
    public FeedingRegulator getFeedingRegulator() {
        return feedingRegulator;
    }
}
