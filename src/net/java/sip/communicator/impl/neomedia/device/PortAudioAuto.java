/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.device;

import java.util.*;

import javax.media.*;
import javax.media.format.*;

import net.java.sip.communicator.impl.neomedia.*;
import net.java.sip.communicator.impl.neomedia.portaudio.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.service.configuration.*;

import com.sun.media.util.*;

/**
 * Creates PortAudio capture devices by enumerating all host devices that have
 * input channels.
 *
 * @author Damian Minkov
 */
public class PortAudioAuto
{
    /**
     * The <tt>Logger</tt> used by the <tt>PortAudioAuto</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(PortAudioAuto.class);

    /**
     * The protocol of the <tt>MediaLocator</tt>s identifying PortAudio
     * <tt>CaptureDevice</tt>s
     */
    public static final String LOCATOR_PROTOCOL = "portaudio";

    /**
     * An array of the devices that can be used for playback.
     */
    public static CaptureDeviceInfo[] playbackDevices = null;

    /**
     * The default playback device.
     */
    public static CaptureDeviceInfo defaultPlaybackDevice = null;

    /**
     * The default capture device.
     */
    public static CaptureDeviceInfo defaultCaptureDevice = null;

    /**
     * Is portaudio supported on current platform.
     */
    private static boolean supported = false;

    /**
     * Initializes a new <tt>PortAudioAuto</tt> instance which creates PortAudio
     * capture devices by enumerating all host devices with input channels.
     *
     * @throws Exception if anything wrong happens while creating the PortAudio
     * capture devices
     */
    PortAudioAuto()
        throws Exception
    {
        // enable jmf logging, so we can track codec chains and formats
        if(logger.isDebugEnabled())
            Registry.set("allowLogging", true);

        // if PortAudio has a problem initializing like missing native
        // components it will trow exception here and PortAudio rendering will
        // not be inited.
        int deviceCount = PortAudio.Pa_GetDeviceCount();

        int defaultInputDeviceIx = PortAudio.Pa_GetDefaultInputDevice();
        int defaultOutputDeviceIx = PortAudio.Pa_GetDefaultOutputDevice();

        Vector<CaptureDeviceInfo> playbackDevVector =
            new Vector<CaptureDeviceInfo>();

        for (int deviceIndex = 0; deviceIndex < deviceCount; deviceIndex++)
        {
            long deviceInfo = PortAudio.Pa_GetDeviceInfo(deviceIndex);
            int maxInputChannels =
                PortAudio.PaDeviceInfo_getMaxInputChannels(deviceInfo);
            int maxOutputChannels =
                PortAudio.PaDeviceInfo_getMaxOutputChannels(deviceInfo);

            String devName
                = PortAudio.PaDeviceInfo_getCharsetAwareName(deviceInfo);
            if (devName != null)
                devName = devName.trim();

            CaptureDeviceInfo jmfInfo
                = new CaptureDeviceInfo(
                        devName,
                        new MediaLocator(LOCATOR_PROTOCOL + ":#" + deviceIndex),
                        new Format[]
                                {
                                    new AudioFormat(
                                            AudioFormat.LINEAR,
                                            PortAudio.DEFAULT_SAMPLE_RATE,
                                            16 /* sampleSizeInBits */,
                                            1 /* channels */,
                                            AudioFormat.LITTLE_ENDIAN,
                                            AudioFormat.SIGNED,
                                            16 /* frameSizeInBits */,
                                            Format.NOT_SPECIFIED /* frameRate */,
                                            Format.byteArray)
                                });

            if(maxInputChannels > 0)
                CaptureDeviceManager.addDevice(jmfInfo);
            if(maxOutputChannels > 0)
                playbackDevVector.add(jmfInfo);

            if(deviceIndex == defaultInputDeviceIx)
                defaultCaptureDevice = jmfInfo;
            if(deviceIndex == defaultOutputDeviceIx)
                defaultPlaybackDevice = jmfInfo;
        }

        playbackDevices = playbackDevVector.toArray(new CaptureDeviceInfo[0]);

        CaptureDeviceManager.commit();

        // now add it as available audio system to DeviceConfiguration
        DeviceConfiguration.addAudioSystem(
            DeviceConfiguration.AUDIO_SYSTEM_PORTAUDIO);

        // now extract other sound related configs
        try
        {
            ConfigurationService config
                = NeomediaActivator.getConfigurationService();

            boolean echoCancelEnabled =
                config.getBoolean(
                    DeviceConfiguration.PROP_AUDIO_ECHOCANCEL_ENABLED,
                    PortAudioManager.isEnabledEchoCancel());
            if(echoCancelEnabled)
            {
                long echoCancelFilterLengthInMillis =
                    config.getLong(
                        DeviceConfiguration
                            .PROP_AUDIO_ECHOCANCEL_FILTER_LENGTH_IN_MILLIS,
                        PortAudioManager.getFilterLengthInMillis());

                PortAudioManager.setEchoCancel(
                    echoCancelEnabled,
                    echoCancelFilterLengthInMillis);
            }

            boolean denoiseEnabled =
                config.getBoolean(
                    DeviceConfiguration.PROP_AUDIO_DENOISE_ENABLED,
                    PortAudioManager.isEnabledDeNoise());
            PortAudioManager.setDeNoise(denoiseEnabled);

            // suggested latency is saved in configuration as
            // milliseconds but PortAudioManager use it as seconds
            int defaultAudioLatency
                = (int) (PortAudioManager.getSuggestedLatency()*1000);
            int audioLatency = config.getInt(
                DeviceConfiguration.PROP_AUDIO_LATENCY,
                defaultAudioLatency);
            if(audioLatency != defaultAudioLatency)
                    PortAudioManager.setSuggestedLatency(
                        (double)audioLatency/1000d);
        }
        catch (Exception e)
        {
            logger.error("Error parsing audio config", e);
        }

        supported = true;
    }

    /**
     * Whether portaudio is supported.
     * @return the supported
     */
    public static boolean isSupported()
    {
        return supported;
    }
}
