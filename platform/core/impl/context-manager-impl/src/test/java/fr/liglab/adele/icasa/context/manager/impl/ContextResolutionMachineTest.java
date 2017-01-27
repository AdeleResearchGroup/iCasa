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

import fr.liglab.adele.cream.model.ContextEntity;
import fr.liglab.adele.cream.model.introspection.EntityProvider;
import fr.liglab.adele.icasa.context.manager.api.ContextGoal;
import fr.liglab.adele.icasa.context.manager.impl.ContextManager;
import fr.liglab.adele.icasa.context.manager.impl.ContextResolutionMachine;
import org.junit.Assert;
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

import java.util.*;
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
public class ContextResolutionMachineTest {

    @Test
    public void resolutionAlgorithm() throws Exception{
        /*Config Mockito*/
        /*Mocked logger*/
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

        /*Services*/
        String serviceLight = "BinaryLight";
        String servicePresence = "PresenceSensor";
        String serviceTemp = "TemperatureSensor";
        String serviceZwave = "Zwave";


        /*Mocked entities*/
        ContextEntity mockedBinaryLightZwave = Mockito.mock(ContextEntity.class);
        String nameBinaryLightZwave = "BinaryLightZwave";
        Set<String> servicesBinaryLightZwave = new HashSet<>();
        servicesBinaryLightZwave.add(serviceLight);
        servicesBinaryLightZwave.add(serviceZwave);
        when(mockedBinaryLightZwave.getServices()).thenReturn(servicesBinaryLightZwave);
        when(mockedBinaryLightZwave.toString()).thenReturn(nameBinaryLightZwave);
//        Class mockedLightClass = Mockito.mock(Class.class);
//        when(mockedLightClass.toString()).thenReturn(nameBinaryLightZwave);
//        when(mockedBinaryLightZwave.getClass()).thenReturn(mockedLightClass);

        ContextEntity mockedPresenceSensorZigbee = Mockito.mock(ContextEntity.class);
        String namePresenceSensorZigbee = "PresenceSensorZigbee";
        Set<String> servicesPresenceSensorZigbee = new HashSet<>();
        servicesPresenceSensorZigbee.add(servicePresence);
        when(mockedPresenceSensorZigbee.getServices()).thenReturn(servicesPresenceSensorZigbee);
        when(mockedPresenceSensorZigbee.toString()).thenReturn(namePresenceSensorZigbee);
//        Class mockedPresClass = Mockito.mock(Class.class);
//        when(mockedPresClass.toString()).thenReturn(namePresenceSensorZigbee);
//        when(mockedPresenceSensorZigbee.getClass()).thenReturn(mockedPresClass);

        ContextEntity mockedTemperatureSensorZwave = Mockito.mock(ContextEntity.class);
        String nameTemperatureSensorZwave = "TemperatureSensorZwave";
        Set<String> servicesTemperatureSensorZwave = new HashSet<>();
        servicesTemperatureSensorZwave.add(serviceTemp);
        servicesTemperatureSensorZwave.add(serviceZwave);
        when(mockedTemperatureSensorZwave.getServices()).thenReturn(servicesTemperatureSensorZwave);
        when(mockedTemperatureSensorZwave.toString()).thenReturn(nameTemperatureSensorZwave);
//        Class mockedTempClass = Mockito.mock(Class.class);
//        when(mockedTempClass.toString()).thenReturn(nameTemperatureSensorZwave);
//        when(mockedTemperatureSensorZwave.getClass()).thenReturn(mockedTempClass);

        /*Mocked providers*/
        EntityProvider mockedSensorProvider = Mockito.mock(EntityProvider.class);
        Set<String> providedSensors = new HashSet<>();
        providedSensors.add(nameTemperatureSensorZwave);
        providedSensors.add(namePresenceSensorZigbee);
        when(mockedSensorProvider.getProvidedEntities()).thenReturn(providedSensors);
        when(mockedSensorProvider.getName()).thenReturn("mockedSensorProvider");

        EntityProvider mockedActuatorProvider = Mockito.mock(EntityProvider.class);
        Set<String> providedActuators = new HashSet<>();
        providedActuators.add(nameBinaryLightZwave);
        when(mockedActuatorProvider.getProvidedEntities()).thenReturn(providedActuators);
        when(mockedActuatorProvider.getName()).thenReturn("mockedActuatorProvider");


        /*Test*/
        System.out.println("\nTEST Algorithme de resolution du contexte");
        ContextResolutionMachine contextResolutionMachine = new ContextResolutionMachine();
        Map<String, ContextGoal> contextGoals = new HashMap<>();
        ContextEntity[] contextEntities;
        EntityProvider[] entityProviders;

        System.out.println("\n1ere config");
        /*Goal config*/
        Set<String> config = new HashSet<String>();
        config.add(serviceLight);
        config.add(servicePresence);
        contextGoals.put("LightFollowMe", new ContextGoal(config));

        /*Entity config*/
        contextEntities = new ContextEntity[3];
        contextEntities[0] = mockedBinaryLightZwave;
        contextEntities[1] = mockedPresenceSensorZigbee;
        contextEntities[2] = mockedTemperatureSensorZwave;

        /*Provider config*/
        entityProviders = new EntityProvider[2];
        entityProviders[0] = mockedSensorProvider;
        entityProviders[1] = mockedActuatorProvider;

        /*Run*/
        contextResolutionMachine.configureState(contextGoals,contextEntities,entityProviders);
        contextResolutionMachine.run();


        /*TODO MODIFY*/
    }
}
