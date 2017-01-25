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
        TimeUnit.SECONDS.sleep(10);
        contextManager.setDelay(1L);
        TimeUnit.SECONDS.sleep(10);
        contextManager.setDelay(5L);
        TimeUnit.SECONDS.sleep(25);
    }
}
