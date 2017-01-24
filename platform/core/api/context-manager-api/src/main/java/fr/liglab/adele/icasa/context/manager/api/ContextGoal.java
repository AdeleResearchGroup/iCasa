package fr.liglab.adele.icasa.context.manager.api;

import java.util.HashSet;
import java.util.Set;

/**
 * TEMP
 * Les besoins en API de contexte sont exprimés
 */
public class ContextGoal {
    private Set<String> optimalConfig = null;

    private Set<String> minimumConfig = null;

    public ContextGoal() {
        this(null, null);
    }

    public ContextGoal(Set<String> minimumConfig) {
        this(null, minimumConfig);
    }

    public ContextGoal(Set<String> optimalConfig, Set<String> minimumConfig) {
        setOptimalConfig(optimalConfig);
        setMinimumConfig(minimumConfig);
    }

    public Set<String> getOptimalConfig() {
        return optimalConfig;
    }

    public boolean setOptimalConfig(Set<String> optimalConfig) {
        boolean check = checkConfig(optimalConfig);
        if(check){this.optimalConfig = new HashSet<String>(optimalConfig);}
        return check;
    }

    public Set<String> getMinimumConfig() {
        return minimumConfig;
    }

    public boolean setMinimumConfig(Set<String> minimumConfig) {
        boolean check = checkConfig(minimumConfig);
        if(check){this.minimumConfig = new HashSet<String>(minimumConfig);}
        return check;
    }

    private boolean checkConfig(Set<String> config){
        boolean check = true;
        /*Check if all the interfaces exist*/
        try {
            for(String contextInterface : config){
               Class.forName(contextInterface);
            }
        } catch (ClassNotFoundException e) {
            check = false;
        }
        return check;
    }
}