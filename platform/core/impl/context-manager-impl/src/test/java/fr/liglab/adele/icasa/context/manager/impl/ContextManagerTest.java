/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.context.manager.impl;

import fr.liglab.adele.icasa.context.manager.impl.ContextManager;
import org.junit.Test;
import org.junit.Assert;
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
        }).when(mockedLogger).debug(anyString());


        PowerMockito.mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger((Class)any())).thenReturn(mockedLogger);


        /*Test*/
        System.out.println("\nTEST Context Schedule (Verification manuelle à faire : nombre d'executions par config)");
        ContextManager contextManager = new ContextManager();
        contextManager.start();


        System.out.println("\n1ere config");
        int nExecutions = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        long delay = 100L;
        contextManager.setDelay(delay, timeUnit);
        Assert.assertEquals("Modification de delay non appliquee", delay, contextManager.getDelay());
        Assert.assertEquals("Modification d'unite de temps non appliquee", timeUnit, contextManager.getTimeUnit());
        System.out.println("--- ("+nExecutions+" executions)");
        TimeUnit.MILLISECONDS.sleep(nExecutions*delay);

        System.out.println("\n2eme config");
        delay = 500L;
        contextManager.setDelay(delay, timeUnit);
        Assert.assertEquals("Modification de delay non appliquee", delay, contextManager.getDelay());
        Assert.assertEquals("Modification d'unite de temps non appliquee", timeUnit, contextManager.getTimeUnit());
        System.out.println("--- ("+nExecutions+" executions)");
        TimeUnit.MILLISECONDS.sleep(nExecutions*delay);

        contextManager.stop();
    }
}
