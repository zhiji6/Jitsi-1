/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.service.protocol.event;

import java.util.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * A class representing the event of a call reception.
 * @author Emil Ivov
 */
public class CallReceivedEvent
    extends EventObject
{
    private Call call = null;


    public CallReceivedEvent(Call call)
    {
        super(call);
    }

    public CallParticipant getCallParticipant()
    {
        return (CallParticipant)getSource();
    }

    public Call getCall()
    {
        return (Call)getSource();
    }
}
