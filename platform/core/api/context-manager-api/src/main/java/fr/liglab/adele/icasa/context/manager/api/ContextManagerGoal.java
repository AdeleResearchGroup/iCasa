package fr.liglab.adele.icasa.context.manager.api;

/**
 * TEMP
 * Les besoins en API de contexte sont exprimés
 */
public class ContextManagerGoal {
    private String[] optimalConfig;

    private String[] minimumConfig;

    public ContextManagerGoal() {
        optimalConfig = null;
        minimumConfig = null;
    }

    public String[] getOptimalConfig() {
        return optimalConfig;
    }

    public boolean setOptimalConfig(String[] optimalConfig) {
        boolean check = checkConfig(optimalConfig);
        if(check){this.optimalConfig = optimalConfig;}
        return check;
    }

    public String[] getMinimumConfig() {
        return minimumConfig;
    }

    public boolean setMinimumConfig(String[] minimumConfig) {
        boolean check = checkConfig(minimumConfig);
        if(check){this.minimumConfig = minimumConfig;}
        return check;
    }

    private boolean checkConfig(String[] config){
        boolean check = true;
        for(String contextInterface : config){
            /*TODO*/
            /*Check if all the interfaces exist*/

        }
        return check;
    }
}