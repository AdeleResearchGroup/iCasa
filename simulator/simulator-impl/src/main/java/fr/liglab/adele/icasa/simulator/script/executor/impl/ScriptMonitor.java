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
package fr.liglab.adele.icasa.simulator.script.executor.impl;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.core.services.Watcher;

import java.io.File;

/**
 *
 */
@Component
@Instantiate
public class ScriptMonitor {

    @Requires
    Watcher watcher;

    private File file;

    private boolean alreadyWatch = false;

    public ScriptMonitor(){
        file = new File(ScriptExecutorImpl.SCRIPTS_DIRECTORY);
    }

    @Validate
    public void start() {
        alreadyWatch = !(watcher.add(file,2000));
    }

    @Invalidate
    public void stop(){
        if (!alreadyWatch){
            watcher.removeAndStopIfNeeded(file);
        }
    }

}
