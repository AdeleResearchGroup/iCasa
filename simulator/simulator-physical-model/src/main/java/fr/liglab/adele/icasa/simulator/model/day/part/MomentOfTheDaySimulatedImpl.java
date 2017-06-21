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

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import org.apache.felix.ipojo.annotations.Requires;
import org.joda.time.DateTime;

import java.util.function.Supplier;

/**
 *
 */
@ContextEntity(coreServices = MomentOfTheDay.class)
public class MomentOfTheDaySimulatedImpl implements MomentOfTheDay {

    @ContextEntity.State.Field(service = MomentOfTheDay.class,state = MomentOfTheDay.CURRENT_MOMENT_OF_THE_DAY)
    private PartOfTheDay currentMomentOfTheDay;

    @Override
    public PartOfTheDay getCurrentPartOfTheDay() {
        return currentMomentOfTheDay;
    }

    @Requires
    Clock clock;

    @ContextEntity.State.Pull(service = MomentOfTheDay.class,state = MomentOfTheDay.CURRENT_MOMENT_OF_THE_DAY)
    Supplier<PartOfTheDay> pullMomentOfTheDay = () ->{
        DateTime dateTimeEli =new DateTime(clock.currentTimeMillis());
        int hour = dateTimeEli.getHourOfDay();
        return PartOfTheDay.getCorrespondingMoment(hour);
    };
}
