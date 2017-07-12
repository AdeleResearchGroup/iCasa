/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.apps.demo.pet.care.app;

import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.PetInfo;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.FeedingRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.services.regulators.WateringRegulator;
import fr.liglab.adele.icasa.apps.demo.pet.care.context.util.PetCareInfo;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import org.apache.felix.ipojo.annotations.*;

import java.util.Collections;
import java.util.List;

@Component(name = "demo-pet-care-component")
public class AppPetCare {
    /*Light management with presence service*/

    @Validate
    @SuppressWarnings("unused")
    public void start() {
        if(momentOfTheDay != null)
            planPetCare(momentOfTheDay.getCurrentPartOfTheDay(), petsInfo);
    }

    @Invalidate
    @SuppressWarnings("unused")
    public void stop() {

    }


    @Requires(id="pets",optional = false, specification = PetInfo.class, proxy = false)
    @SuppressWarnings("all")
    private List<PetInfo> petsInfo;

    @Requires(id="momentOfTheDay",optional = false, specification = MomentOfTheDay.class, proxy = false)
    @SuppressWarnings("unused")
    private MomentOfTheDay momentOfTheDay;


    @Bind(id="pets")
    @SuppressWarnings("unused")
    public synchronized void bindPetInfo(PetInfo petInfo){
        planPetCare(momentOfTheDay.getCurrentPartOfTheDay(), Collections.singletonList(petInfo));
    }

    @Bind(id="momentOfTheDay")
    @SuppressWarnings("unused")
    public synchronized void bindMomentOfTheDay(MomentOfTheDay momentOfTheDay){
        planPetCare(momentOfTheDay.getCurrentPartOfTheDay(), petsInfo);
    }

    @Modified(id = "momentOfTheDay")
    @SuppressWarnings("unused")
    public synchronized void modifiedMomentOfTheDay(MomentOfTheDay momentOfTheDay){
        planPetCare(momentOfTheDay.getCurrentPartOfTheDay(), petsInfo);
    }


    private void planPetCare(MomentOfTheDay.PartOfTheDay partOfTheDay, List<PetInfo> petsInfo){
        for(PetInfo petInfo : petsInfo){
            try{
                if(petInfoValidity(petInfo)){
                    PetCareInfo petCareInfo = petInfo.getPetCareInfo();
                    int waterQuantity = petCareInfo.getWaterQuantityPerMomentOfTheDay().get(partOfTheDay);
                    int foodQuantity = petCareInfo.getFoodQuantityPerMomentOfTheDay().get(partOfTheDay);

                    if(waterQuantity>0){
                        WateringRegulator wateringRegulator = petInfo.getWateringRegulator();
                        wateringRegulator.giveWater(petCareInfo.getWaterDoseRefInMilliliters());
                    }

                    if(foodQuantity>0){
                        FeedingRegulator feedingRegulator = petInfo.getFeedingRegulator();
                        feedingRegulator.giveFood(petCareInfo.getFoodDoseRefInGrams());
                    }
                }
            } catch (NullPointerException ne){
                ne.printStackTrace();
            }
        }
    }

    private boolean petInfoValidity(PetInfo petInfo){
        return (!petInfo.getPetCareInfoValidity())
                && (petInfo.getWateringRegulator() == null)
                && (petInfo.getFeedingRegulator() == null);
    }

}