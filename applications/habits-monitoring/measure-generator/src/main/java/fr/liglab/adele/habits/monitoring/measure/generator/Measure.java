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

/**
 * @author Gabriel Pedraza Ferreira
 *
 */
public class Measure {
   
   private String gatewayId;
   private String patientId;
   private String deviceId;
   private String localisation;
   private float realibility;
   private long timestamp;
   
   /**
    * @return the gatewayId
    */
   public String getGatewayId() {
      return gatewayId;
   }
   /**
    * @param gatewayId the gatewayId to set
    */
   public void setGatewayId(String gatewayId) {
      this.gatewayId = gatewayId;
   }
   /**
    * @return the patientId
    */
   public String getPatientId() {
      return patientId;
   }
   /**
    * @param patientId the patientId to set
    */
   public void setPatientId(String patientId) {
      this.patientId = patientId;
   }
   /**
    * @return the deviceId
    */
   public String getDeviceId() {
      return deviceId;
   }
   /**
    * @param deviceId the deviceId to set
    */
   public void setDeviceId(String deviceId) {
      this.deviceId = deviceId;
   }
   /**
    * @return the localisation
    */
   public String getLocalisation() {
      return localisation;
   }
   /**
    * @param localisation the localisation to set
    */
   public void setLocalisation(String localisation) {
      this.localisation = localisation;
   }
   /**
    * @return the realibility
    */
   public float getRealibility() {
      return realibility;
   }
   /**
    * @param realibility the realibility to set
    */
   public void setRealibility(float realibility) {
      this.realibility = realibility;
   }
   /**
    * @return the timestamp
    */
   public long getTimestamp() {
      return timestamp;
   }
   /**
    * @param timestamp the timestamp to set
    */
   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }
   
   
}
