import fr.liglab.adele.icasa.context.manager.impl.ContextManager;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by Eva on 25/01/2017.
 */
public class ContextManagerTest {


    @Test
    public void modifyingSchedule() throws Exception{
        ContextManager contextManager = new ContextManager();

        System.out.println("\n1st config");
        contextManager.setDelay(10L, TimeUnit.MILLISECONDS);
        System.out.println("-");
        TimeUnit.MILLISECONDS.sleep(100);

        System.out.println("\n2nd config");
        contextManager.setDelay(5L, TimeUnit.MILLISECONDS);
        System.out.println("-");
        TimeUnit.MILLISECONDS.sleep(50);
    }
}
