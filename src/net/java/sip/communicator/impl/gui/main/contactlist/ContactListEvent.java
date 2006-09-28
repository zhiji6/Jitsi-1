/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import java.util.*;

import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.protocol.*;

public class ContactListEvent
    extends EventObject{

    private int eventID = -1;
    
    /**
     * Indicates that the ContactListEvent instance was triggered by
     * selecting a contact in the contact list.
     */
    public static final int CONTACT_SELECTED = 1;
   
    /**
     * Indicates that the ContactListEvent instance was triggered by
     * selecting a protocol contact in the contact list.
     */
    public static final int PROTOCOL_CONTACT_SELECTED = 2;

    private MetaContact sourceContact;
    
    private Contact sourceProtoContact;
    
    /**
     * Creates a new ContactListEvent according to the specified parameters.
     * @param source the MetaContact which was selected
     * @param eventID one of the XXX_SELECTED static fields indicating the
     * nature of the event.
     */
    public ContactListEvent(MetaContact source, int eventID)
    {
        super(source);
        this.eventID = eventID;
        this.sourceContact = source;
    }

    /**
     * Creates a new ContactListEvent according to the specified parameters.
     * @param source the MetaContact which was selected
     * @param protocolContact the protocol specifique contact which was selected
     * @param eventID one of the XXX_SELECTED static fields indicating the
     * nature of the event.
     */
    public ContactListEvent(MetaContact source,
            Contact protocolContact, int eventID)
    {
        super(source);
        this.eventID = eventID;
        this.sourceContact = source;
        this.sourceProtoContact = protocolContact;
    }
    
    /**
     * Returns an event id specifying whether the type of this event
     * (CONTACT_SELECTED or PROTOCOL_CONTACT_SELECTED)
     * @return one of the XXX_SELECTED int fields of this class.
     */
    public int getEventID()
    {
        return eventID;
    }

    /**
     * Returns the MetaContact for which this event occured.
     * @return the MetaContact for which this event occured
     */
    public MetaContact getSourceContact()
    {
        return sourceContact;
    }

    /**
     * Returns the protocol contact for which this event occured.
     * @return the protocol contact for which this event occured
     */
    public Contact getSourceProtoContact()
    {
        return sourceProtoContact;
    }

}
