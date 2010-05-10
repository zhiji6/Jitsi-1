/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist.contactsource;

import java.util.*;
import java.util.regex.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.contactlist.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * The <tt>MetaContactListSource</tt> is an abstraction of the
 * <tt>MetaContactListService</tt>, which makes the correspondence between a
 * <tt>MetaContact</tt> and an <tt>UIContact</tt> and between a
 * <tt>MetaContactGroup</tt> and an <tt>UIGroup</tt>. It is also responsible
 * for filtering of the <tt>MetaContactListService</tt> through a given pattern.
 *
 * @author Yana Stamcheva
 */
public class MetaContactListSource
{
    /**
     * The data key of the MetaContactDescriptor object used to store a
     * reference to this object in its corresponding MetaContact.
     */
    public static final String UI_CONTACT_DATA_KEY
        = MetaUIContact.class.getName() + ".uiContactDescriptor";

    /**
     * The data key of the MetaGroupDescriptor object used to store a
     * reference to this object in its corresponding MetaContactGroup.
     */
    public static final String UI_GROUP_DATA_KEY
        = MetaUIGroup.class.getName() + ".uiGroupDescriptor";

    /**
     * Indicates if we should be filtering.
     */
    private boolean isFiltering = false;

    /**
     * The <tt>MetaContactQueryListener</tt> listens for <tt>MetaContact</tt>s
     * and <tt>MetaGroup</tt>s received as a result of a filtering.
     */
    private static MetaContactQueryListener queryListener;

    /**
     * Returns the <tt>UIContact</tt> corresponding to the given
     * <tt>MetaContact</tt>.
     * @param metaContact the <tt>MetaContact</tt>, which corresponding UI
     * contact we're looking for
     * @return the <tt>UIContact</tt> corresponding to the given
     * <tt>MetaContact</tt>
     */
    public static UIContact getUIContact(MetaContact metaContact)
    {
        return (UIContact) metaContact.getData(UI_CONTACT_DATA_KEY);
    }

    /**
     * Returns the <tt>UIGroup</tt> corresponding to the given
     * <tt>MetaContactGroup</tt>.
     * @param metaGroup the <tt>MetaContactGroup</tt>, which UI group we're
     * looking for
     * @return the <tt>UIGroup</tt> corresponding to the given
     * <tt>MetaContactGroup</tt>
     */
    public static UIGroup getUIGroup(MetaContactGroup metaGroup)
    {
        return (UIGroup) metaGroup.getData(UI_GROUP_DATA_KEY);
    }

    /**
     * Creates a <tt>UIContact</tt> for the given <tt>metaContact</tt>.
     * @param metaContact the <tt>MetaContact</tt> for which we would like to
     * create an <tt>UIContact</tt>
     * @return an <tt>UIContact</tt> for the given <tt>metaContact</tt>
     */
    public static UIContact createUIContact(MetaContact metaContact)
    {
        MetaUIContact descriptor
            = new MetaUIContact(metaContact);
        metaContact.setData(UI_CONTACT_DATA_KEY, descriptor);

        return descriptor;
    }

    /**
     * Removes the <tt>UIContact</tt> from the given <tt>metaContact</tt>.
     * @param metaContact the <tt>MetaContact</tt>, which corresponding UI
     * contact we would like to remove
     */
    public static void removeUIContact(MetaContact metaContact)
    {
        metaContact.setData(UI_CONTACT_DATA_KEY, null);
    }

    /**
     * Creates a <tt>UIGroupDescriptor</tt> for the given <tt>metaGroup</tt>.
     * @param metaGroup the <tt>MetaContactGroup</tt> for which we would like to
     * create an <tt>UIContact</tt>
     * @return a <tt>UIGroup</tt> for the given <tt>metaGroup</tt>
     */
    public static UIGroup createUIGroup(MetaContactGroup metaGroup)
    {
        MetaUIGroup descriptor = new MetaUIGroup(metaGroup);
        metaGroup.setData(UI_GROUP_DATA_KEY, descriptor);

        return descriptor;
    }

    /**
     * Removes the descriptor from the given <tt>metaGroup</tt>.
     * @param metaGroup the <tt>MetaContactGroup</tt>, which descriptor we
     * would like to remove
     */
    public static void removeUIGroup(
        MetaContactGroup metaGroup)
    {
        metaGroup.setData(UI_GROUP_DATA_KEY, null);
    }

    /**
     * Indicates if the given <tt>MetaContactGroup</tt> is the root group.
     * @param group the <tt>MetaContactGroup</tt> to check
     * @return <tt>true</tt> if the given <tt>group</tt> is the root group,
     * <tt>false</tt> - otherwise
     */
    public static boolean isRootGroup(MetaContactGroup group)
    {
        return group.equals(GuiActivator.getContactListService().getRoot());
    }

    /**
     * Stops the meta contact list filtering.
     */
    public void stopFiltering()
    {
        isFiltering = false;
    }

    /**
     * Filters the <tt>MetaContactListService</tt> to match the given
     * <tt>filterPattern</tt> and stores the result in the given
     * <tt>treeModel</tt>.
     * @param filterPattern the pattern to filter through
     */
    public void filter(Pattern filterPattern)
    {
        isFiltering = true;

        filter(filterPattern, GuiActivator.getContactListService().getRoot());

        isFiltering = false;
    }

    /**
     * Filters the children in the given <tt>MetaContactGroup</tt> to match the
     * given <tt>filterPattern</tt> and stores the result in the given
     * <tt>treeModel</tt>.
     * @param filterPattern the pattern to filter through
     * @param parentGroup the <tt>MetaContactGroup</tt> to filter
     */
    private void filter(Pattern filterPattern,
                        MetaContactGroup parentGroup)
    {
        Iterator<MetaContact> childContacts = parentGroup.getChildContacts();

        while (childContacts.hasNext() && isFiltering)
        {
            MetaContact metaContact = childContacts.next();

            if (isMatching(filterPattern, metaContact))
            {
                fireQueryEvent(metaContact);
            }
        }

        Iterator<MetaContactGroup> subgroups = parentGroup.getSubgroups();
        while (subgroups.hasNext() && isFiltering)
        {
            MetaContactGroup subgroup = subgroups.next();

            filter(filterPattern, subgroup);
        }
    }

    /**
     * Checks if the given <tt>metaContact</tt> is matching the given
     * <tt>filterPattern</tt>.
     * A <tt>MetaContact</tt> would be matching the filter if one of the
     * following is true:<br>
     * - its display name contains the filter string
     * - at least one of its child protocol contacts has a display name or an
     * address that contains the filter string.
     * @param filterPattern the filter pattern to check for matches
     * @param metaContact the <tt>MetaContact</tt> to check
     * @return <tt>true</tt> to indicate that the given <tt>metaContact</tt> is
     * matching the current filter, otherwise returns <tt>false</tt>
     */
    private boolean isMatching(Pattern filterPattern, MetaContact metaContact)
    {
        Matcher matcher = filterPattern.matcher(metaContact.getDisplayName());

        if(matcher.find())
            return true;

        Iterator<Contact> contacts = metaContact.getContacts();
        while (contacts.hasNext())
        {
            Contact contact = contacts.next();

            matcher = filterPattern.matcher(contact.getDisplayName());

            if (matcher.find())
                return true;

            matcher = filterPattern.matcher(contact.getAddress());

            if (matcher.find())
                return true;
        }
        return false;
    }

    /**
     * Checks if the given <tt>metaGroup</tt> is matching the current filter. A
     * group is matching the current filter only if it contains at least one
     * child <tt>MetaContact</tt>, which is matching the current filter.
     * @param filterPattern the filter pattern to check for matches
     * @param metaGroup the <tt>MetaContactGroup</tt> to check
     * @return <tt>true</tt> to indicate that the given <tt>metaGroup</tt> is
     * matching the current filter, otherwise returns <tt>false</tt>
     */
    public boolean isMatching(Pattern filterPattern, MetaContactGroup metaGroup)
    {
        Iterator<MetaContact> contacts = metaGroup.getChildContacts();

        while (contacts.hasNext())
        {
            MetaContact metaContact = contacts.next();

            if (isMatching(filterPattern, metaContact))
                return true;
        }
        return false;
    }

    /**
     * Sets the given <tt>MetaContactQueryListener</tt> to listen for query
     * events coming from <tt>MetaContactListService</tt> filtering.
     * @param l the <tt>MetaContactQueryListener</tt> to set
     */
    public static void setMetaContactQueryListener(MetaContactQueryListener l)
    {
        queryListener = l;
    }

    /**
     * Returns the currently registered <tt>MetaContactQueryListener</tt>.
     * @return the currently registered <tt>MetaContactQueryListener</tt>
     */
    public static MetaContactQueryListener getMetaContactQueryListener()
    {
        return queryListener;
    }

    /**
     * Notifies the <tt>MetaContactQueryListener</tt> that a new
     * <tt>MetaContact</tt> has been received as a result of a search.
     * @param metaContact the received <tt>MetaContact</tt>
     */
    public static void fireQueryEvent(MetaContact metaContact)
    {
        if (queryListener != null)
            queryListener.metaContactReceived(metaContact);
    }

    /**
     * Notifies the <tt>MetaContactQueryListener</tt> that a new
     * <tt>MetaGroup</tt> has been received as a result of a search.
     * @param metaGroup the received <tt>MetaGroup</tt>
     */
    public static void fireQueryEvent(MetaContactGroup metaGroup)
    {
        if (queryListener != null)
            queryListener.metaGroupReceived(metaGroup);
    }
}
