package fr.liglab.adele.icasa.context.manager.impl.test;

import fr.liglab.adele.icasa.context.manager.impl.ContextManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * TEMP
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerFactory.class})
public class ContextManagerTest {

    @Test
    public void modifyingSchedule() throws Exception{
        /*Config Mockito*/
        Logger mockedLogger = Mockito.mock(Logger.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                System.out.println(args[0]);
                return null;
            }
        }).when(mockedLogger).info(anyString());


        PowerMockito.mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger((Class)any())).thenReturn(mockedLogger);


        /*Test*/
        ContextManager contextManager = new ContextManager();

        System.out.println("\n1st config");
        contextManager.setDelay(100L, TimeUnit.MILLISECONDS);
        System.out.println("-");
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("\n2nd config");
        contextManager.setDelay(500L, TimeUnit.MILLISECONDS);
        System.out.println("-");
        TimeUnit.MILLISECONDS.sleep(5000);
    }
}