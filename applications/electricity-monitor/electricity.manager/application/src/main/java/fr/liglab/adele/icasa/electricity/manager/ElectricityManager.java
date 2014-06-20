package fr.liglab.adele.icasa.electricity.manager;



/**
 * Created by horakm on 4/3/14.
 */
public interface ElectricityManager {

    /**
     * Add a listener, must be synchronized.
     *
     * @param listener : listener to add.
     */
    public void addListener(ElectricityManagerListener listener);

    /**
     * Remove a listener, must be synchronized.
     *
     * @param listener : listener to remove.
     */
    public void removeListener(ElectricityManagerListener listener);

}
