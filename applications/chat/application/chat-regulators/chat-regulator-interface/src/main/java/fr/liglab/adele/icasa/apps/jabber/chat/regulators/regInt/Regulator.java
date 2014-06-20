package fr.liglab.adele.icasa.apps.jabber.chat.regulators.regInt;

/**
 * Created by donatien on 21/05/14.
 */
public interface Regulator {
    public String setto(String location, String level);
    public String setOn(String location);
    public String setOff(String location);
    public String getStatus(String location);
}
