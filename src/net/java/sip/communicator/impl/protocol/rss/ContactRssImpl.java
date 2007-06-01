/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.rss;

import java.util.*;
import java.text.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * An implementation of a rss Contact.
 *
 * @author Jean-Albert Vescovo
 */
public class ContactRssImpl
    implements Contact
{
    private String lastDate = null;
    private Date date = null;
    private String nickName = null;
    
    private static final Logger logger
        = Logger.getLogger(ContactRssImpl.class);

    private static SimpleDateFormat DATE_FORMATTER = 
        new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    
    /**
     * The id of the contact.
     */
    private String contactID = null;

    /**
     * The provider that created us.
     */
    private ProtocolProviderServiceRssImpl parentProvider = null;

    /**
     * The group that belong to.
     */
    private ContactGroupRssImpl parentGroup = null;

    /**
     * The presence status of the contact.
     */
    private PresenceStatus presenceStatus = RssStatusEnum.ONLINE;

    /**
     * Determines whether this contact is persistent, i.e. member of the contact
     * list or whether it is here only temporarily.
     */
    private boolean isPersistent = true;

    /**
     * Determines whether the contact has been resolved (i.e. we have a
     * confirmation that it is still on the server contact list).
     */
    private boolean isResolved = false;

    /**
     * Creates an instance of a meta contact with the specified string used
     * as a name and identifier.
     *
     * @param id the identifier of this contact (also used as a name).
     * @param parentProvider the provider that created us.
     */
    public ContactRssImpl(
                String id,
                ProtocolProviderServiceRssImpl parentProvider)
    {
        this.contactID = id;
        this.parentProvider = parentProvider;
    }

    /**
     * This method is only called when the contact is added to a new
     * <tt>ContactGroupRssImpl</tt> by the
     * <tt>ContactGroupRssImpl</tt> itself.
     *
     * @param newParentGroup the <tt>ContactGroupRssImpl</tt> that is now
     * parent of this <tt>ContactRssImpl</tt>
     */
    void setParentGroup(ContactGroupRssImpl newParentGroup)
    {
        this.parentGroup = newParentGroup;
    }

    /**
     * Returns a String that can be used for identifying the contact.
     *
     * @return a String id representing and uniquely identifying the contact.
     */
    public String getAddress()
    {
        return contactID;
    }

    /**
     * Returns a String that could be used by any user interacting modules
     * for referring to this contact.
     *
     * @return a String that can be used for referring to this contact when
     *   interacting with the user.
     */
    public String getDisplayName()
    {
        if(nickName == null) return contactID;
        else  return nickName;
    }

    public void setDisplayName(String nickName){
        this.nickName = nickName;
    }
    
    /**
     * Returns a Date corresponding to the date of the last query
     * on this rss contact.
     *
     * @return a Date in order to compare with a new one obtained via
     * a query on the feed.
     */
    public Date getDate()
    {
        return this.date;
    }
    
    /**
     * This method is only called when a new date is found after a query
     * on the feed corresponding to this contact
     *
     * @param date the <tt>Date</tt> that is now
     * the last update date of the <tt>ContactRssImpl</tt>
     */
    public void setDate(Date date)
    {
        this.date = date;
        this.lastDate = convertDateToString(this.date);
    }    

    /**
     * Updating the lastDate in String format of the contact
     * 
     * @param lastDate the <tt>String</tt> that is now
     * the last update date of the <tt>ContactRssImpl</tt>
     */
    public void setLastDate(String lastDate)
    {
        this.lastDate = lastDate;
    }
    
    /**
     * Returns a String corresponding to the date of the last query
     * on this rss contact.
     *
     * @return a String representing a Date in order to compare with
     * a new one obtained via a query on the feed.
     */
    public String getLastDate()
    {
        return this.lastDate; 
    }
    
    /**
     * Returns a String corresponding to a date after a conversion
     * from a Date
     *
     * @param date the date
     * @return a String which is placed in the lastDate variable of the
     * present contact
     */
    private String convertDateToString(Date date)
    {
        return DATE_FORMATTER.format(date);
    }
    
    /**
     * This method is called when a the contact is restored and a
     * previous saved lastDate is found as persistent-data: this
     * data is in a String format, and this method convert it into
     * a Date usable by the protocol.
     * @param lastDate date as String
     */
    private void convertStringToDate(String lastDate)
    {
        try
        {
            this.date = DATE_FORMATTER.parse(lastDate);
        }
        catch(ParseException ex)
        {
            logger.error("Cannot parse Date", ex);
        }
    }
    
    /**
     * Returns an array of String corresponding to a date bursted in multiple
     * fields as this:
     * ddd mmm DD HH:mm:ss ZZZZ YYYY
     *
     * @return an Array of String
     */
    private String[] getToken(String param1, String param2)
    {
        int i = 0;
        String data[] = new String[8];
        StringTokenizer tmp = new StringTokenizer(param1, param2);

        while(tmp.hasMoreTokens())
        {
            data[i] = tmp.nextToken();
            i++;
        }
        return data;
    }
    
    /**
     * Returns a byte array containing an image (most often a photo or an
     * avatar) that the contact uses as a representation.
     *
     * @return byte[] an image representing the contact.
     */
    public byte[] getImage()
    {
        return null;
    }

    /**
     * Returns the status of the contact.
     *
     * @return RssStatusEnum.STATUS.
     */
    public PresenceStatus getPresenceStatus()
    {
        return this.presenceStatus;
    }

    /**
     * Sets <tt>rssPresenceStatus</tt> as the PresenceStatus that this
     * contact is currently in.
     * @param rssPresenceStatus the <tt>RssPresenceStatus</tt>
     * currently valid for this contact.
     */
    public void setPresenceStatus(PresenceStatus rssPresenceStatus)
    {
        this.presenceStatus = rssPresenceStatus;
    }

    /**
     * Returns a reference to the protocol provider that created the contact.
     *
     * @return a refererence to an instance of the ProtocolProviderService
     */
    public ProtocolProviderService getProtocolProvider()
    {
        return parentProvider;
    }

    /**
     * Determines whether or not this contact represents our own identity.
     *
     * @return true in case this is a contact that represents ourselves and
     *   false otherwise.
     */
    public boolean isLocal()
    {
        return false;
    }

    /**
     * Returns the group that contains this contact.
     * @return a reference to the <tt>ContactGroupRssImpl</tt> that
     * contains this contact.
     */
    public ContactGroup getParentContactGroup()
    {
        return this.parentGroup;
    }

    /**
     * Returns a string representation of this contact, containing most of its
     * representative details.
     *
     * @return  a string representation of this contact.
     */
    public String toString()
    {
        StringBuffer buff
            = new StringBuffer("ContactRssImpl[ DisplayName=")
                .append(getDisplayName()).append("]");

        return buff.toString();
    }

    /**
     * Determines whether or not this contact is being stored by the server.
     * Non persistent contacts are common in the case of simple, non-persistent
     * presence operation sets. They could however also be seen in persistent
     * presence operation sets when for example we have received an event
     * from someone not on our contact list. Non persistent contacts are
     * volatile even when coming from a persistent presence op. set. They would
     * only exist until the application is closed and will not be there next
     * time it is loaded.
     *
     * @return true if the contact is persistent and false otherwise.
     */
    public boolean isPersistent()
    {
        return isPersistent;
    }

    /**
     * Specifies whether or not this contact is being stored by the server.
     * Non persistent contacts are common in the case of simple, non-persistent
     * presence operation sets. They could however also be seen in persistent
     * presence operation sets when for example we have received an event
     * from someone not on our contact list. Non persistent contacts are
     * volatile even when coming from a persistent presence op. set. They would
     * only exist until the application is closed and will not be there next
     * time it is loaded.
     *
     * @param isPersistent true if the contact is persistent and false
     * otherwise.
     */
    public void setPersistent(boolean isPersistent)
    {
        this.isPersistent = isPersistent;
    }
    
    /**
     * Returns null as no persistent data is required and the contact address is
     * sufficient for restoring the contact.
     * <p>
     * @return null as no such data is needed.
     */
    public String getPersistentData()
    {
        // to store data only when lastDate is set
        if(lastDate != null)
            return "lastDate=" + lastDate + ";";
        else
            return null;
    }
     
    public void setPersistentData(String persistentData)
    {
        if(persistentData == null)
        {
            return;
        }
        
        StringTokenizer dataToks = new StringTokenizer(persistentData, ";");
        while(dataToks.hasMoreTokens())
        {
            String data[] = dataToks.nextToken().split("=");
            if(data[0].equals("lastDate") && data.length > 1)
            {
                this.lastDate = data[1];
                convertStringToDate(this.lastDate);
            }
        }
    }
     
    /**
     * Determines whether or not this contact has been resolved against the
     * server. Unresolved contacts are used when initially loading a contact
     * list that has been stored in a local file until the presence operation
     * set has managed to retrieve all the contact list from the server and has
     * properly mapped contacts to their on-line buddies.
     *
     * @return true if the contact has been resolved (mapped against a buddy)
     * and false otherwise.
     */
    public boolean isResolved()
    {
        return isResolved;
    }

    /**
     * Makes the contact resolved or unresolved.
     *
     * @param resolved  true to make the contact resolved; false to
     *                  make it unresolved
     */
    public void setResolved(boolean resolved)
    {
        this.isResolved = resolved;
    }

    /**
     * Indicates whether some other object is "equal to" this one which in terms
     * of contacts translates to having equal ids. The resolved status of the
     * contacts deliberately ignored so that contacts would be declared equal
     * even if it differs.
     * <p>
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this contact has the same id as that of the
     * <code>obj</code> argument.
     */
    public boolean equals(Object obj)
    {
        if (obj == null
            || ! (obj instanceof ContactRssImpl))
            return false;

        ContactRssImpl rssContact = (ContactRssImpl) obj;

        return this.getAddress().equals(rssContact.getAddress());
    }


    /**
     * Returns the persistent presence operation set that this contact belongs
     * to.
     *
     * @return the <tt>OperationSetPersistentPresenceRssImpl</tt> that
     * this contact belongs to.
     */
    public OperationSetPersistentPresenceRssImpl
                                            getParentPresenceOperationSet()
    {
        return (OperationSetPersistentPresenceRssImpl)parentProvider
            .getOperationSet(OperationSetPersistentPresence.class);
    }
}
