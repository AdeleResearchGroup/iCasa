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
package fr.liglab.adele.icasa.device.bathroomscale;

public interface Sphygmometer {
		
	public final String SPHYGMOMETER_CURRENT_SYSTOLIC = "sphygmometer.currentSystolic";
	
	public final String SPHYGMOMETER_CURRENT_DIASTOLIC = "sphygmometer.currentDiastolic";
	
	public final String SPHYGMOMETER_CURRENT_PULSATIONS = "sphygmometer.currentPulsations";
	
	public int getSystolic();
	
	public int getDiastolic();
	
	public int getPulsations(); 
	
}
