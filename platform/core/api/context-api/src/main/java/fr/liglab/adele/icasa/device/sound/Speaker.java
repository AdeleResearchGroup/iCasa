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
package fr.liglab.adele.icasa.device.sound;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a simple speaker device.
 *
 */
public interface Speaker extends GenericDevice {

    /**
     * Service property indicating the current volume of the speaker.
     * 
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.Double</code></b>, between
     * <code>0.0d</code> and <code>1.0d</code></li>
     * <li>Description : value is <code>0.0d</code> when the speaker is
     * completely muted, <code>1.0d</code> when volume is set to the maximum.</li>
     * </ul>
     * 
     * @see #getVolume()
     * @see #setVolume(double)
     */
    String SPEAKER_VOLUME = "speaker.volume";

    /**
     * TODO comments.
     */
    String SPEAKER_NOISE_LEVEL = "speaker.noiseLevel";

    /**
     * Return the current volume of this speaker.
     * 
     * @return the current volume of this speaker.
     * @see #setVolume(double)
     * @see #SPEAKER_VOLUME
     */
    double getVolume();

    /**
     * Change the volume of this speaker.
     * 
     * @param volume
     *            the new volume of this speaker.
     * @return the previous volume of this speaker.
     * @see #getVolume()
     * @see #SPEAKER_VOLUME
     */
    double setVolume(double volume);

    /**
     * TODO comments.
     * 
     * @return
     */
    double getNoiseLevel();

    /**
     * Set the audio stream that this speaker must play.
     * 
     * @param source
     *            the audio source to play, or {@code null} to stop playing.
     */
    void setAudioSource(AudioSource source);

}
