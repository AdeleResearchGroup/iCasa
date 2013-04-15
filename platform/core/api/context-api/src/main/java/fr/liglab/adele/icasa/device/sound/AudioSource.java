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
package fr.liglab.adele.icasa.device.sound;

import java.io.InputStream;

import fr.liglab.adele.icasa.device.GenericDevice;

/**
 * Service definition of a device that is an audio source, could be iPhone,
 * radio, MP3 player, etc., anything that returns an audio stream.
 * 
 * @author jander
 */
public interface AudioSource extends GenericDevice {

    // The actual format of the audio stream must be defined, or detailed in a
    // service method, so the stream reader know how to decode it.

    /**
     * TODO comments.
     */
    String AUDIO_SOURCE_IS_PLAYING = "audioSource.isPlaying";

    /**
     * Return the audio stream.
     * 
     * @return the stream.
     */
    public InputStream getStream();

    /**
     * TODO comments.
     * 
     * @return
     */
    public boolean isPlaying();

    /**
     * Start the audio stream playback.
     */
    public void play();

    /**
     * Pause the audio stream playback.
     */
    public void pause();

}
