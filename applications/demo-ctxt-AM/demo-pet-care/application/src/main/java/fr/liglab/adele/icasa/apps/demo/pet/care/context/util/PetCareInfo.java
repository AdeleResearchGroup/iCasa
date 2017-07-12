package fr.liglab.adele.icasa.apps.demo.pet.care.context.util;

import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PetCareInfo {

    /*WATER QUANTITY*/
    private boolean waterValidity = false;

    private int waterDoseRefInMilliliters = 0;

    private Map<MomentOfTheDay.PartOfTheDay, Integer> waterQuantityPerMomentOfTheDay = new HashMap<>();


    /*FOOD QUANTITY*/
    private boolean foodValidity = false;

    private int foodDoseRefInGrams = 0;

    private Map<MomentOfTheDay.PartOfTheDay, Integer> foodQuantityPerMomentOfTheDay = new HashMap<>();


    public PetCareInfo(){}

    public boolean setPetCareInfo(PetCareInfo petCareInfo){
        if(!petCareInfo.isValid())
            return false;
        this.setWaterDoseRefInMilliliters(petCareInfo.getWaterDoseRefInMilliliters());
        this.setWaterQuantityPerMomentOfTheDay(petCareInfo.getWaterQuantityPerMomentOfTheDay());
        this.setFoodDoseRefInGrams(petCareInfo.getFoodDoseRefInGrams());
        this.setFoodQuantityPerMomentOfTheDay(petCareInfo.getFoodQuantityPerMomentOfTheDay());
        return isValid();
    }

    public boolean isValid() {
        return checkAllInfo();
    }

    public int getWaterDoseRefInMilliliters() {
        return waterDoseRefInMilliliters;
    }

    public boolean setWaterDoseRefInMilliliters(int waterDoseRefInMilliliters) {
        this.waterDoseRefInMilliliters = waterDoseRefInMilliliters;
        checkAllInfo();
        return this.waterDoseRefInMilliliters > 0;
    }

    public Map<MomentOfTheDay.PartOfTheDay, Integer> getWaterQuantityPerMomentOfTheDay() {
        return Collections.unmodifiableMap(waterQuantityPerMomentOfTheDay);
    }

    /*Define quantity for each partOfTheDay*/
    public boolean setWaterQuantityPerMomentOfTheDay(Map<MomentOfTheDay.PartOfTheDay, Integer> waterQuantityPerMomentOfTheDay) {
        this.waterQuantityPerMomentOfTheDay = Collections.emptyMap();
        waterValidity = quantityPerDayIsValid(waterQuantityPerMomentOfTheDay);
        if(waterValidity)
            this.waterQuantityPerMomentOfTheDay.putAll(waterQuantityPerMomentOfTheDay);
        checkAllInfo();
        return waterValidity;
    }

    public int getFoodDoseRefInGrams() {
        return foodDoseRefInGrams;
    }

    public boolean setFoodDoseRefInGrams(int foodDoseRefInGrams) {
        this.foodDoseRefInGrams = foodDoseRefInGrams;
        checkAllInfo();
        return this.foodDoseRefInGrams > 0;
    }

    public Map<MomentOfTheDay.PartOfTheDay, Integer> getFoodQuantityPerMomentOfTheDay() {
        return Collections.unmodifiableMap(foodQuantityPerMomentOfTheDay);
    }

    /*Define quantity for each partOfTheDay*/
    public boolean setFoodQuantityPerMomentOfTheDay(Map<MomentOfTheDay.PartOfTheDay, Integer> foodQuantityPerMomentOfTheDay) {
        this.foodQuantityPerMomentOfTheDay = Collections.emptyMap();
        foodValidity = quantityPerDayIsValid(foodQuantityPerMomentOfTheDay);
        if(foodValidity)
            this.foodQuantityPerMomentOfTheDay.putAll(foodQuantityPerMomentOfTheDay);
        checkAllInfo();
        return foodValidity;
    }

    private boolean checkAllInfo(){
        return (waterDoseRefInMilliliters > 0) && waterValidity && (foodDoseRefInGrams > 0) && foodValidity;
    }

    private boolean quantityPerDayIsValid(Map<MomentOfTheDay.PartOfTheDay, Integer> quantityPerMomentOfTheDay){
        if ((quantityPerMomentOfTheDay == null) || quantityPerMomentOfTheDay.isEmpty())
            return false;

        boolean anyFood = false;
        for(MomentOfTheDay.PartOfTheDay moment : MomentOfTheDay.PartOfTheDay.values()){
            Integer quantity = quantityPerMomentOfTheDay.get(moment);
            if ((quantity == null) || (quantity<0)){
                return false;
            }
            anyFood = anyFood || (quantity >0);
        }
        return anyFood;
    }
}
