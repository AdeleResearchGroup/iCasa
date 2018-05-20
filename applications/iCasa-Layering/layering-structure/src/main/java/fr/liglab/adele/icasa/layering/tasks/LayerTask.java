package fr.liglab.adele.icasa.layering.tasks;

import fr.liglab.adele.cream.annotations.State;

public interface LayerTask {
    public static final @State String idk = "current.state";

    public String getState();
    public Object setState(String state, int mode);
}
