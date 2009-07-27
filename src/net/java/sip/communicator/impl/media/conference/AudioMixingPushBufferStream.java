/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.media.conference;

import java.io.*;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

import net.java.sip.communicator.impl.media.*;

/**
 * Represents a <code>PushBufferStream</code> containing the result of the audio
 * mixing of <code>DataSource</code>s.
 * 
 * @author Lubomir Marinov
 */
public class AudioMixingPushBufferStream
    implements PushBufferStream
{

    /**
     * The <code>AudioMixer.AudioMixerPushBufferStream</code> which reads data
     * from the input <code>DataSource</code>s and pushes it to this instance to
     * be mixed.
     */
    private final AudioMixer.AudioMixerPushBufferStream audioMixerStream;

    /**
     * The <code>AudioMixingPushBufferDataSource</code> which created and owns
     * this instance and defines the input data which is to not be mixed in the
     * output of this <code>PushBufferStream</code>.
     */
    private final AudioMixingPushBufferDataSource dataSource;

    /**
     * The collection of input audio samples still not mixed and read through
     * this <code>AudioMixingPushBufferStream</code>.
     */
    private int[][] inputSamples;

    /**
     * The maximum number of per-stream audio samples available through
     * <code>inputSamples</code>.
     */
    private int maxInputSampleCount;

    /**
     * The <code>BufferTransferHandler</code> through which this
     * <code>PushBufferStream</code> notifies its clients that new data is
     * available for reading.
     */
    private BufferTransferHandler transferHandler;

    /**
     * Initializes a new <code>AudioMixingPushBufferStream</code> mixing the
     * input data of a specific
     * <code>AudioMixer.AudioMixerPushBufferStream</code> and excluding from the
     * mix the audio contributions of a specific
     * <code>AudioMixingPushBufferDataSource</code>.
     * 
     * @param audioMixerStream the
     *            <code>AudioMixer.AudioMixerPushBufferStream</code> reading
     *            data from input <code>DataSource</code>s and to push it to the
     *            new <code>AudioMixingPushBufferStream</code>
     * @param dataSource the <code>AudioMixingPushBufferDataSource</code> which
     *            has requested the initialization of the new instance and which
     *            defines the input data to not be mixed in the output of the
     *            new instance
     */
    public AudioMixingPushBufferStream(
        AudioMixer.AudioMixerPushBufferStream audioMixerStream,
        AudioMixingPushBufferDataSource dataSource)
    {
        this.audioMixerStream = audioMixerStream;
        this.dataSource = dataSource;
    }

    /*
     * Implements SourceStream#endOfStream(). Delegates to the wrapped
     * AudioMixer.AudioMixerPushBufferStream.
     */
    public boolean endOfStream()
    {
        /*
         * TODO If the inputSamples haven't been consumed yet, don't report the
         * end of this stream even if the wrapped stream has reached its end.
         */
        return audioMixerStream.endOfStream();
    }

    /*
     * Implements SourceStream#getContentDescriptor(). Delegates to the wrapped
     * AudioMixer.AudioMixerPushBufferStream.
     */
    public ContentDescriptor getContentDescriptor()
    {
        return audioMixerStream.getContentDescriptor();
    }

    /*
     * Implements SourceStream#getContentLength(). Delegates to the wrapped
     * AudioMixer.AudioMixerPushBufferStream.
     */
    public long getContentLength()
    {
        return audioMixerStream.getContentLength();
    }

    /*
     * Implements Controls#getControl(String). Does nothing.
     */
    public Object getControl(String controlType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * Implements Controls#getControls(). Does nothing.
     */
    public Object[] getControls()
    {
        // TODO Auto-generated method stub
        return new Object[0];
    }

    /**
     * Gets the <code>AudioMixingPushBufferDataSource</code> which created and
     * owns this instance and defines the input data which is to not be mixed in
     * the output of this <code>PushBufferStream</code>.
     * 
     * @return the <code>AudioMixingPushBufferDataSource</code> which created
     *         and owns this instance and defines the input data which is to not
     *         be mixed in the output of this <code>PushBufferStream</code>
     */
    public AudioMixingPushBufferDataSource getDataSource()
    {
        return dataSource;
    }

    /*
     * Implements PushBufferStream#getFormat(). Delegates to the wrapped
     * AudioMixer.AudioMixerPushBufferStream.
     */
    public AudioFormat getFormat()
    {
        return audioMixerStream.getFormat();
    }

    /**
     * Gets the maximum possible value for an audio sample of a specific
     * <code>AudioFormat</code>.
     * 
     * @param outputFormat the <code>AudioFormat</code> of which to get the
     *            maximum possible value for an audio sample
     * @return the maximum possible value for an audio sample of the specified
     *         <code>AudioFormat</code>
     * @throws UnsupportedFormatException
     */
    private static int getMaxOutputSample(AudioFormat outputFormat)
        throws UnsupportedFormatException
    {
        switch(outputFormat.getSampleSizeInBits())
        {
        case 8:
            return Byte.MAX_VALUE;
        case 16:
            return Short.MAX_VALUE;
        case 32:
            return Integer.MAX_VALUE;
        case 24:
        default:
            throw
                new UnsupportedFormatException(
                        "Format.getSampleSizeInBits()",
                        outputFormat);
        }
    }

    /**
     * Mixes as in audio mixing a specified collection of audio sample sets and
     * returns the resulting mix audio sample set in a specific
     * <code>AudioFormat</code>.
     * 
     * @param inputSamples the collection of audio sample sets to be mixed into
     *            one audio sample set in the sense of audio mixing 
     * @param outputFormat the <code>AudioFormat</code> in which the resulting
     *            mix audio sample set is to be produced 
     * @param outputSampleCount the size of the resulting mix audio sample set
     *            to be produced
     * @return the resulting audio sample set of the audio mixing of the
     *         specified input audio sample sets
     */
    private static int[] mix(
        int[][] inputSamples,
        AudioFormat outputFormat,
        int outputSampleCount)
    {
        int[] outputSamples = new int[outputSampleCount];
        int maxOutputSample;
    
        try
        {
            maxOutputSample = getMaxOutputSample(outputFormat);
        }
        catch (UnsupportedFormatException ufex)
        {
            throw new UnsupportedOperationException(ufex);
        }
    
        for (int[] inputStreamSamples : inputSamples)
        {
    
            if (inputStreamSamples == null)
                continue;
    
            int inputStreamSampleCount = inputStreamSamples.length;
    
            if (inputStreamSampleCount <= 0)
                continue;
    
            for (int i = 0; i < inputStreamSampleCount; i++)
            {
                int inputStreamSample = inputStreamSamples[i];
                int outputSample = outputSamples[i];
    
                outputSamples[i]
                    = inputStreamSample
                            + outputSample
                            - Math.round(
                                    inputStreamSample
                                        * (outputSample
                                                / (float) maxOutputSample));
            }
        }
        return outputSamples;
    }

    /*
     * Implements PushBufferStream#read(Buffer). If inputSamples are available,
     * mixes them and writes them to the specified Buffer performing the
     * necessary data type conversions.
     */
    public void read(Buffer buffer)
        throws IOException
    {
        int[][] inputSamples = this.inputSamples;
        int maxInputSampleCount = this.maxInputSampleCount;

        this.inputSamples = null;
        this.maxInputSampleCount = 0;

        if ((inputSamples == null)
                || (inputSamples.length == 0)
                || (maxInputSampleCount <= 0))
            return;

        AudioFormat outputFormat = getFormat();
        int[] outputSamples
            = mix(inputSamples, outputFormat, maxInputSampleCount);
    
        Class<?> outputDataType = outputFormat.getDataType();
    
        if (Format.byteArray.equals(outputDataType))
        {
            byte[] outputData;
    
            switch (outputFormat.getSampleSizeInBits())
            {
            case 16:
                outputData = new byte[outputSamples.length * 2];
                for (int i = 0; i < outputSamples.length; i++)
                    ArrayIOUtils.writeInt16(outputSamples[i], outputData, i * 2);
                break;
            case 32:
                outputData = new byte[outputSamples.length * 4];
                for (int i = 0; i < outputSamples.length; i++)
                    writeInt(outputSamples[i], outputData, i * 4);
                break;
            case 8:
            case 24:
            default:
                throw
                    new UnsupportedOperationException(
                            "AudioMixingPushBufferStream.read(Buffer)");
            }
    
            buffer.setData(outputData);
            buffer.setFormat(outputFormat);
            buffer.setLength(outputData.length);
            buffer.setOffset(0);
        }
        else
            throw
                new UnsupportedOperationException(
                        "AudioMixingPushBufferStream.read(Buffer)");
    }

    /**
     * Sets the collection of audio sample sets to be mixed in the sense of
     * audio mixing by this stream when data is read from it. Triggers a push to
     * the clients of this stream.
     * 
     * @param inputSamples the collection of audio sample sets to be mixed by
     *            this stream when data is read from it
     * @param maxInputSampleCount the maximum number of per-stream audio samples
     *            available through <code>inputSamples</code>
     */
    void setInputSamples(int[][] inputSamples, int maxInputSampleCount)
    {
        this.inputSamples = inputSamples;
        this.maxInputSampleCount = maxInputSampleCount;

        if (transferHandler != null)
            transferHandler.transferData(this);
    }

    /*
     * Implements PushBufferStream#setTransferHandler(BufferTransferHandler).
     */
    public void setTransferHandler(BufferTransferHandler transferHandler)
    {
        this.transferHandler = transferHandler;
    }

    /**
     * Starts the pushing of data out of this stream.
     */
    void start()
    {
        audioMixerStream.addOutputStream(this);
    }

    /**
     * Stops the pushing of data out of this stream.
     */
    void stop()
    {
        audioMixerStream.removeOutputStream(this);
    }

    /**
     * Converts an integer to a series of bytes and writes the result into a
     * specific output array of bytes starting the writing at a specific offset
     * in it.
     * 
     * @param input the integer to be written out as a series of bytes
     * @param output the output to receive the conversion of the specified
     *            integer to a series of bytes
     * @param outputOffset the offset in <code>output</code> at which the
     *            writing of the result of the conversion is to be started
     */
    private static void writeInt(int input, byte[] output, int outputOffset)
    {
        output[outputOffset] = (byte) (input & 0xFF);
        output[outputOffset + 1] = (byte) ((input >>> 8) & 0xFF);
        output[outputOffset + 2] = (byte) ((input >>> 16) & 0xFF);
        output[outputOffset + 3] = (byte) (input >> 24);
    }
}