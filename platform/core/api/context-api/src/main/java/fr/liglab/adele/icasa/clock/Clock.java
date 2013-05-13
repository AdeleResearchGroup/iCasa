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
package fr.liglab.adele.icasa.clock;


/**
 * This class represents the platform clock. It's provided as an OSGi service. 
 * 
 * @author Gabriel Pedraza Ferreira
 * 
 */
public interface Clock {

	/**
	 * Returns the current time in (virtual) milliseconds.
	 * 
	 * @return current time. 
	 */
	public long currentTimeMillis();

	/**
	 * Sets the start date of the clock.
	 * 
	 * @param startDate the new start date.
	 */
	public void setStartDate(long startDate);

	/**
	 * Set the clock factor (speed of virtual time)
	 * 
	 * @param factor the new factor.
	 */
	public void setFactor(int factor);

	/**
	 * gets the elapsed time in (virtual) milliseconds from the start date.
	 * 
	 * @return the elapsed time.
	 */
	public long getElapsedTime();

	/**
	 * Pauses the (virtual) time flowing.
	 */
	public void pause();

	/**
	 * Resumes the (virtual) time flowing.
	 */
	public void resume();

	/**
	 * Sets the elapsed time to 0 and pauses the (virtual) time flowing.
	 */
	public void reset();

	/**
	 * Returns the clock factor.
	 * 
	 * @return the clock factor.
	 */
	public int getFactor();

	/**
	 * Returns the clock start date.
	 * 
	 * @return the start date.
	 */
	public long getStartDate();
	
	/**
	 * Returns true if the clock is paused, false otherwise
	 * 
	 * @return boolean to know id the clock is paused
	 */
	public boolean isPaused();
	
	/**
	 * Adds a listener to the clock.
	 * 
	 * @param listener The listener to be added.
	 */
	void addListener(ClockListener listener);
	
	/**
	 * Removes a listener from the clock.
	 * 
	 * @param listener The listener to be removed.
	 */
	void removeListener(ClockListener listener);
}
