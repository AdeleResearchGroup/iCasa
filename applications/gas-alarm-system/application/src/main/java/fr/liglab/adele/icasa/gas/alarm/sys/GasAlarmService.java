package fr.liglab.adele.icasa.gas.alarm.sys;

/**
 *
 */
public interface GasAlarmService {

    public void addListener(GasAlarmListener listener);

    public void removeListener(GasAlarmListener listener);


}
