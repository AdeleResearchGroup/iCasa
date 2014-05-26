/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.habits.monitoring.measure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;

/**
 *
 */
public class MeasureEnricher {

   private static final Logger logger = LoggerFactory.getLogger(MeasureEnricher.class);
   
   public Data process(Data data) {
      if (data != null) {
         Measure measure = (Measure) data.getContent();
         measure.setGatewayId("303");
         logger.info("The gatewayId was set");
         return data;
      }      
      return null;
   } 
}
