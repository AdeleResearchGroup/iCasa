package fr.liglab.adele.icasa.gas.alarm.sys;

import fr.liglab.adele.icasa.clock.Clock;
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.mail.service.MailSender;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Component(name = "ReportManager")
@Instantiate(name = "ReportManager-0")
@CommandProvider(namespace = "reportManager")
public class ReportManager implements GasAlarmListener{


    private Set<String> adressToNotify = new HashSet<String>();

    private final Object m_lock;
    @Requires
    private Clock clock;

    @Requires(id="mailService")
    private MailSender mailSender;

    @Requires
    private GasAlarmService gasAlarmService;

    public ReportManager() {
        m_lock = new Object();
    }

    @Invalidate
    public void stop() {
        System.out.println(" Report component stop ... ");
        gasAlarmService.removeListener(this);
    }


    @Validate
    public void start() {
        System.out.println(" Report Manager component start ... ");
        gasAlarmService.addListener(this);
    }

    @Override
    public void thresholdCrossUp() {
        synchronized (m_lock){
            for(String adress : adressToNotify){
                DateTime dateTime = new DateTime(clock.currentTimeMillis());

                if(mailSender.sendSimpleMail(adress, "CO2 concentration is too hight", " Hello , \n\r" +
                        "The Co2 concentration is higher than the maximum Threshold. The Alarm procedure start at " + dateTime.getHourOfDay() +":"+dateTime.getMinuteOfHour()+":"+dateTime.getSecondOfMinute() + " the " + dateTime.getDayOfMonth()+"/" + dateTime.getMonthOfYear()+"/"+ dateTime.getYear())){
                    System.out.println(" Report manager can't send to " + adress);
                }
            }
        }
    }

    @Override
    public void thresholdCrossDown() {

    }

    @Command
    public void addReportAdress(String adress){
        synchronized (m_lock){
            adressToNotify.add(adress);
        }
        System.out.println(" Adress " + adress + " was add to the reporting list");
    }
}
