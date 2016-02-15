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
package fr.liglab.adele.icasa.simulator.model.day.part;

import fr.liglab.adele.icasa.context.model.annotations.provider.Creator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;

@Component(immediate = true,publicFactory = false)
@Instantiate
public class PartOfTheDayProvider {

    @Creator.Field Creator.Entity<MomentOfTheDaySimulatedImpl> creator;

    @Validate
    public void start(){
        creator.create("MomentOfTheDay");
    }

    @Invalidate
    public void stop(){

    }
}