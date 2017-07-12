package fr.liglab.adele.icasa.apps.demo.global;

import fr.liglab.adele.cream.annotations.provider.Creator;

public class Util {

    /*ToDo A verifier*/
    public static final String LOG_PREFIX = "DEMO APPS - ";

    public static boolean checkIfItExists(Creator.Entity creator, String id){
        if (id == null){
            return true;
        }
        for (Object otherId : creator.getInstances()){
            if (id.equals(otherId)){
                return true;
            }
        }
        return false;
    }

//    public static boolean checkIfItExists(Creator.Relation creator, String id){
//        if (id == null){
//            return true;
//        }
//        for (Object otherId : creator.getInstances()){
//            if (id.equals(otherId)){
//                return true;
//            }
//        }
//        return false;
//    }
}
