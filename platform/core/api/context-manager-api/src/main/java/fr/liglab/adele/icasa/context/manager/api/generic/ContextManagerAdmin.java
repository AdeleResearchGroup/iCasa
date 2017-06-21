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
package fr.liglab.adele.icasa.context.manager.api.generic;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.context.manager.api.generic.goals.ContextAPIConfig;
import fr.liglab.adele.icasa.context.manager.api.generic.goals.ContextDependencyRegistration;
import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Programmatic context manager main class
 */
@Component(immediate = true, publicFactory = false)
@Instantiate
@CommandProvider(namespace = "AM-CTXT")
public class ContextManagerAdmin {

    private static final Logger LOG = LoggerFactory.getLogger(ContextManagerAdmin.class);

    /*MANAGER RUNNING MODE*/
    public static final int MODE_SCHEDULED = 0;
    public static final int MODE_EVENT_DRIVEN = 1;
    public static final int INTERNAL_CONFIGURATION_MODE = MODE_SCHEDULED;


    /*INITIAL CONFIGURATION*/
    /*Starting configuration of the context model - mandatory goals*/
    /*(includes context components that are useful for RoSe*/
    private static final ContextAPIConfig startingGoalsConfig = new ContextAPIConfig(
            new HashSet<>(Collections.singletonList(ContextAPIEnum.IOPController)));

    @Requires
    @SuppressWarnings("unused")
    private ContextDependencyRegistration contextDependencyRegistration;

    @Validate
    public void start(){
        contextDependencyRegistration.registerContextDependencies(ContextManagerAdmin.class.toGenericString(), startingGoalsConfig);
    }

    @Invalidate
    public void stop(){
        contextDependencyRegistration.unregisterContextDependencies(ContextManagerAdmin.class.toGenericString());
    }

    /*SCHEDULING*/
    /*Scheduled mode - period*/
    private static long delay = 10L;
    private static TimeUnit timeUnit = TimeUnit.SECONDS;

    public static long getDelay() {
        return delay;
    }
    public static TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public static void setDelay(long delay) {
        if(delay > 1){
            ContextManagerAdmin.delay = delay;
        }
    }
    public static void setTimeUnit(TimeUnit timeUnit) {
        if(timeUnit != null){
            ContextManagerAdmin.timeUnit = timeUnit;
        }
    }


    /*AUTO-LOOKUP*/
    /*Environment lookup mode*/
    private static boolean autoLookup = false;

    public static boolean getAutoLookup(){
        return autoLookup;
    }

    @Command
    @SuppressWarnings("unused")
    public void ctxtAmAutoLookup(){
        autoLookup = true;
    }

    @Command
    @SuppressWarnings("unused")
    public void ctxtAmManualLookup(){
        autoLookup = false;
    }


    /*LOG LEVEL*/
    /*Log level - 0: nothing, 3: all*/
    private static final int logLevelMax = 3;
    private static int logLevel = 2;

    public static int getLogLevel(){
        return logLevel;
    }

    /*Context manager debug interface*/
    @Command
    @SuppressWarnings("unused")
    public void ctxtAmLogLevel(int level){
        if(level<0) level = 0;
        if(level>logLevelMax) level = logLevelMax;
        logLevel = level;
    }
}
