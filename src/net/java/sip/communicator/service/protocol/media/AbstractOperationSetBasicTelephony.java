/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.media;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * Represents a default implementation of <tt>OperationSetBasicTelephony</tt> in
 * order to make it easier for implementers to provide complete solutions while
 * focusing on implementation-specific details.
 *
 * @param <T> the implementation specific provider class like for example
 * <tt>ProtocolProviderServiceSipImpl</tt>.
 *
 * @author Lubomir Marinov
 * @author Emil Ivov
 * @author Dmitri Melnikov
 */
public abstract class AbstractOperationSetBasicTelephony
                                        <T extends ProtocolProviderService>
    implements OperationSetBasicTelephony<T>
{
    /**
     * The <tt>Logger</tt> used by the
     * <tt>AbstractOperationSetBasicTelephony</tt> class and its instances for
     * logging output.
     */
    private static final Logger logger
        = Logger.getLogger(AbstractOperationSetBasicTelephony.class);

    /**
     * A list of listeners registered for call events.
     */
    private final List<CallListener> callListeners = new Vector<CallListener>();

    /**
     * Registers <tt>listener</tt> with this provider so that it
     * could be notified when incoming calls are received.
     *
     * @param listener the listener to register with this provider.
     */
    public void addCallListener(CallListener listener)
    {
        synchronized(callListeners)
        {
            if (!callListeners.contains(listener))
                callListeners.add(listener);
        }
    }

    /**
     * Creates and dispatches a <tt>CallEvent</tt> notifying registered
     * listeners that an event with id <tt>eventID</tt> has occurred on
     * <tt>sourceCall</tt>.
     *
     * @param eventID the ID of the event to dispatch
     * @param sourceCall the call on which the event has occurred.
     */
    public void fireCallEvent(int eventID, Call sourceCall)
    {
        CallEvent cEvent = new CallEvent(sourceCall, eventID);
        List<CallListener> listeners;

        synchronized (callListeners)
        {
            listeners = new ArrayList<CallListener>(callListeners);
        }

        if (logger.isDebugEnabled())
            logger.debug("Dispatching a CallEvent to " + listeners.size()
            + " listeners. event is: " + cEvent);

        for (CallListener listener : listeners)
        {
            switch (eventID)
            {
            case CallEvent.CALL_INITIATED:
                listener.outgoingCallCreated(cEvent);
                break;
            case CallEvent.CALL_RECEIVED:
                listener.incomingCallReceived(cEvent);
                break;
            case CallEvent.CALL_ENDED:
                listener.callEnded(cEvent);
                break;
            }
        }
    }

    /**
     * Removes the <tt>listener</tt> from the list of call listeners.
     *
     * @param listener the listener to unregister.
     */
    public void removeCallListener(CallListener listener)
    {
        synchronized(callListeners)
        {
            callListeners.remove(listener);
        }
    }

    /**
     * Sets the mute state of the <tt>Call</tt>.
     * <p>
     * Muting audio streams sent from the call is implementation specific
     * and one of the possible approaches to it is sending silence.
     * </p>
     *
     * @param call the <tt>Call</tt> whose mute state is to be set
     * @param mute <tt>true</tt> to mute the call streams being sent to
     * <tt>peers</tt>; otherwise, <tt>false</tt>
     */
    public void setMute(Call call, boolean mute)
    {
        /*
         * While throwing UnsupportedOperationException may be a possible
         * approach, putOnHold/putOffHold just do nothing when not supported so
         * this implementation takes inspiration from them.
         */
    }

    /**
     * Starts the recording of a specific <tt>Call</tt> into a file with a specific name.
     *
     * @param call the <tt>Call</tt> to start recording into the file with the
     * specified <tt>name</tt>
     * @param filename the name of the file into which the specified
     * <tt>call</tt> is to be recorded
     */
    public void startRecording(Call call, String filename)
    {
        if (call instanceof MediaAwareCall)
            ((MediaAwareCall) call).startRecording(filename);
    }

    /**
     * Stops the recording of the <tt>Call</tt>.
     *
     * @param call the <tt>Call</tt> to stop recording
     */
    public void stopRecording(Call call)
    {
        if (call instanceof MediaAwareCall)
            ((MediaAwareCall) call).stopRecording();
    }
}
