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
package fr.liglab.adele.icasa.application.impl.command;

import fr.liglab.adele.icasa.application.Application;
import fr.liglab.adele.icasa.application.ApplicationManager;
import fr.liglab.adele.icasa.commands.AbstractCommand;
import fr.liglab.adele.icasa.commands.Signature;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONObject;
import org.osgi.framework.Bundle;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;

@Component(name = "ShowApplicationsCommand")
@Provides
@Instantiate(name="ShowApplicationsCommand-0")
public class ShowApplicationsCommand extends AbstractCommand {

    @Requires
    ApplicationManager manager;

    public ShowApplicationsCommand(){
        addSignature(EMPTY_SIGNATURE);
    }

    @Override
    public Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception {
        Set<Application> apps = manager.getApplications();

        for (Application application : apps) {
            out.println("App ID :: " + application.getId());
            out.println("App Name :: " + application.getName());
            out.println("App Version :: " + application.getVersion());
            out.println("App Category :: " + application.getId());
            out.println("App Vendor :: " + application.getName());
            out.println("List of bundle :: " );
            for(Bundle bundle : application.getBundles()) System.out.println("  - Bundle :: " + bundle.getSymbolicName());
            out.println("-----------------------------");
        }
        return null;
    }

    /**
     * Get the name of the Script and command gogo.
     *
     * @return The command name.
     */
    @Override
    public String getName() {
        return "show-applications";
    }
}
