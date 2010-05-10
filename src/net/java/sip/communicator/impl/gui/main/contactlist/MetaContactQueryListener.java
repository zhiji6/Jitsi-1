/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import net.java.sip.communicator.service.contactlist.*;

/**
 * The <tt>MetaContactQueryListener</tt> listens for events coming from a
 * <tt>MetaContactListService</tt> filtering.
 *
 * @author Yana Stamcheva
 */
public interface MetaContactQueryListener
{
    /**
     * Indicates that a <tt>MetaContact</tt> has been received for a search in
     * the <tt>MetaContactListService</tt>.
     * @param metaContact the received <tt>MetaContact</tt>
     */
    public void metaContactReceived(MetaContact metaContact);

    /**
     * Indicates that a <tt>MetaGroup</tt> has been received from a search in
     * the <tt>MetaContactListService</tt>.
     * @param metaGroup the <tt>MetaGroup</tt> that has been received
     */
    public void metaGroupReceived(MetaContactGroup metaGroup);
}
