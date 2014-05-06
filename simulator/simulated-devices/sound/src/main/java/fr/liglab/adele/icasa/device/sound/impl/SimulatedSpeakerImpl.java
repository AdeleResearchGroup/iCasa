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
package fr.liglab.adele.icasa.device.sound.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Constants;

import fr.liglab.adele.icasa.device.sound.AudioSource;
import fr.liglab.adele.icasa.device.sound.Speaker;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

/**
 * Implementation of a simulated speaker device.
 *
 */
@Component(name="iCasa.Speaker")
@Provides(properties = {
        @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
public class SimulatedSpeakerImpl extends AbstractDevice implements Speaker, SimulatedDevice {

    @ServiceProperty(name = Speaker.DEVICE_SERIAL_NUMBER, mandatory = true)
    private String m_serialNumber;

    @ServiceProperty(name = Speaker.SPEAKER_VOLUME, value = "0.0d")
    private double m_volume;
    
    @ServiceProperty(name = Speaker.SPEAKER_NOISE_LEVEL, value="0.0d")
    private double m_noiseLevel;

    // The maximum noise emitted by this speaker
    @ServiceProperty(name = "speaker.maxNoise", value = "100.0d")
    private double m_maxNoise;

    @Property(name = "updaterThread.period", value = "250")
    private long m_period;

    // The sound input stream
    private AudioSource m_audioSource;

    // The last byte read from the input stream (or zero if no input stream has
    // been set).
    private byte m_lastByte = Byte.MIN_VALUE;


    // private volatile SimulatedEnvironment m_env;

    private Thread m_updaterThread;

    @Override
    public String getSerialNumber() {
        return m_serialNumber;
    }

    @Validate
    public synchronized void start() {
        m_updaterThread = new Thread(new UpdaterThread(this), "SpeakerUpdaterThread-" + m_serialNumber);
        m_updaterThread.start();
    }

    @Invalidate
    public synchronized void stop() throws InterruptedException {
        m_updaterThread.interrupt();
        m_updaterThread.join();
    }

    @Override
    public synchronized double getVolume() {
        return m_volume;
    }

    @Override
    public synchronized double setVolume(double volume) {
   	 /*
        if (volume < 0.0d || volume > 1.0d || Double.isNaN(volume)) {
            throw new IllegalArgumentException("Invalid volume : " + volume);
        }
        double save = m_volume;
        double noiseBefore = noise();
        m_volume = volume;
        m_noiseLevel = noise();
        m_logger.debug("Volume set to " + volume);
        if (m_env != null) {
            notifyEnvironment(m_noiseLevel - noiseBefore);
        }
        notifyListeners(new DeviceEvent(this, DeviceEventType.PROP_MODIFIED, Speaker.SPEAKER_VOLUME, save));
        return save;
        */
   	 return 0.0;
    }
    
    @Override
    public synchronized double getNoiseLevel() {
        return m_noiseLevel;
    }

    @Override
    public synchronized void setAudioSource(AudioSource source) {
        m_audioSource = source;
    }

    /**
     * Notify the bound simulated simulator that the noise emitted by this
     * speaker has changed.
     * 
     * @param noiseDiff
     *            the noise difference
     */
    private void notifyEnvironment(double noiseDiff) {
   	 /*
        m_env.lock();
        try {
            double current = m_env.getProperty(SimulatedEnvironment.NOISE);
            m_env.setProperty(SimulatedEnvironment.NOISE, current + noiseDiff);
        } finally {
            m_env.unlock();
        }
        
        */
    }

    /**
     * Return the noise currently emitted by this speaker, according to its
     * state.
     * 
     * @return the noise currently emitted by this speaker.
     */
    private double noise() {
        return m_volume * m_maxNoise * ((double) (m_lastByte - Byte.MIN_VALUE)) / 255.0d;
    }

    /**
     * The updater thread that updates the current noise periodically.
     *
     */
    private class UpdaterThread implements Runnable {

        private SimulatedSpeakerImpl _device;

        public UpdaterThread(SimulatedSpeakerImpl device) {
            _device = device;
        }

        @Override
        public void run() {
      	  /*
            boolean isInterrupted = false;
            while (!isInterrupted) {
                try {
                    Thread.sleep(m_period);
                    synchronized (SimulatedSpeakerImpl.this) {
                        double noiseBefore = noise();
                        if (m_audioSource != null) {
                            // The provided input stream must NOT block
                            try {
                                int i = m_audioSource.getStream().read();
                                if (i != -1) {
                                    m_lastByte = (byte) i;
                                } else {
                                    // End of stream, means that the audio
                                    // source is paused or has paused.
                                    m_lastByte = Byte.MIN_VALUE;
                                }
                            } catch (IOException e) {
                                // Read error
                                m_logger.warning("Cannot read audio input stream.", e);
                                m_lastByte = Byte.MIN_VALUE;
                            }
                        } else {
                            // Audio input stream not set. Maybe because end of
                            // the previous stream has been reached
                            m_lastByte = Byte.MIN_VALUE;
                        }
                        m_noiseLevel = noise();
                        if (m_env != null) {
                            notifyEnvironment(m_noiseLevel - noiseBefore);
                        }
                        notifyListeners(new DeviceEvent(_device, DeviceEventType.PROP_MODIFIED, Speaker.SPEAKER_NOISE_LEVEL, noiseBefore));
                    }
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
            */
        }
    }


	@Override
   public void enterInZones(List<Zone> zones) {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void leavingZones(List<Zone> zones) {
	   // TODO Auto-generated method stub
	   
   } 

}
