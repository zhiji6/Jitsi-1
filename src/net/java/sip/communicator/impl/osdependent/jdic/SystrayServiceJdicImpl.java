/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.osdependent.jdic;

import org.osgi.framework.*;

import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.impl.osdependent.*;
import net.java.sip.communicator.impl.systray.mac.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.service.systray.event.*;
import net.java.sip.communicator.util.*;

/**
 * The <tt>Systray</tt> provides a Icon and the associated <tt>TrayMenu</tt>
 * in the system tray using the Jdic library.
 *
 * @author Nicolas Chamouard
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Symphorien Wanko
 */
public class SystrayServiceJdicImpl
    implements SystrayService
{

    /**
     * The systray.
     */
    private SystemTray systray;

    /**
     * The icon in the system tray.
     */
    private TrayIcon trayIcon;

    /**
     * The menu that spring with a right click.
     */
    private Object menu;

    /**
     * The popup handler currently used to show popup messages
     */
    private PopupMessageHandler activePopupHandler;

    /**
     * A set of usable <tt>PopupMessageHandler</tt>
     */
    private final Hashtable<String, PopupMessageHandler> popupHandlerSet =
        new Hashtable<String, PopupMessageHandler>();

    /**
     * A reference of the <tt>ConfigurationService</tt> obtained from the
     * <tt>SystrayServiceActivator</tt>
     */
    private final ConfigurationService configService = OsDependentActivator.
        getConfigurationService();

    /**
     * The logger for this class.
     */
    private static final Logger logger =
        Logger.getLogger(SystrayServiceJdicImpl.class);

    /**
     * The various icons used on the systray
     */
    private ImageIcon currentIcon;

    private ImageIcon logoIcon;

    private ImageIcon logoIconOffline;

    private ImageIcon logoIconAway;

    private ImageIcon logoIconFFC;

    private ImageIcon logoIconWhite;

    private ImageIcon envelopeIcon;

    private ImageIcon envelopeIconWhite;

    /**
     * The dock Icons used only in Mac version
     */
    private URL dockIconOffline;

    private URL dockIconAway;

    private URL dockIconFFC;

    private boolean initialized = false;

    /**
     * the listener we will use for popup message event (clicks on the popup)
     */
    private final SystrayPopupMessageListener popupMessageListener =
        new SystrayPopupMessageListenerImpl();

    /**
     * Creates an instance of <tt>Systray</tt>.
     */
    public SystrayServiceJdicImpl()
    {
        try
        {
            systray = SystemTray.getDefaultSystemTray();
        } catch (Throwable e)
        {
            logger.error("Failed to create a systray!", e);
        }

        if (systray != null)
        {
            this.initSystray();

            UIService ui = OsDependentActivator.getUIService();
            if (ui != null)
                ui.setExitOnMainWindowClose(false);
        }
    }

    /**
     * Initializes the systray icon and related listeners.
     */
    private void initSystray()
    {
        menu = TrayMenuFactory.createTrayMenu(this, systray.isSwing());

        String osName = System.getProperty("os.name");
        // If we're running under Windows, we use a special icon without
        // background.
        if (osName.startsWith("Windows"))
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON_WINDOWS");
            logoIconOffline = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_OFFLINE");
            logoIconAway = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_AWAY");
            logoIconFFC = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_FFC");
            envelopeIcon = Resources.getImage(
                "service.systray.MESSAGE_ICON_WINDOWS");
        } // If we're running under MacOSX, we use a special black and
        // white icons without background.
        else if (osName.startsWith("Mac OS X"))
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON_MACOSX");
            logoIconWhite = Resources.getImage(
                "service.systray.TRAY_ICON_MACOSX_WHITE");
            envelopeIcon = Resources.getImage(
                "service.systray.MESSAGE_ICON_MACOSX");
            envelopeIconWhite = Resources.getImage(
                "service.systray.MESSAGE_ICON_MACOSX_WHITE");
        } else
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON");
            logoIconOffline = Resources.getImage(
                "service.systray.TRAY_ICON_OFFLINE");
            logoIconAway = Resources.getImage("service.systray.TRAY_ICON_AWAY");
            logoIconFFC = Resources.getImage("service.systray.TRAY_ICON_FFC");
            envelopeIcon = Resources.getImage("service.systray.MESSAGE_ICON");
        }

        if (!osName.startsWith("Mac OS X"))
        {
            // default to set offline , if any protocols become
            // online will set it to online
            currentIcon = logoIconOffline;
        } else
        {
            currentIcon = logoIcon;
        }

        trayIcon = new TrayIcon(
            currentIcon,
            Resources.getApplicationString("service.gui.APPLICATION_NAME"),
            menu);

        trayIcon.setIconAutoSize(true);

        if (osName.startsWith("Mac OS X"))
        {
            // init dock Icons
            dockIconOffline = Resources.getImageURL(
                "service.systray.DOCK_ICON_OFFLINE");
            dockIconAway = Resources.getImageURL(
                "service.systray.DOCK_ICON_AWAY");
            dockIconFFC = Resources.getImageURL("service.systray.DOCK_ICON_FFC");
        }

        //Show/hide the contact list when user clicks on the systray.
        trayIcon.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UIService uiService = OsDependentActivator.getUIService();
                ExportedWindow win =
                    uiService.getExportedWindow(ExportedWindow.MAIN_WINDOW);
                boolean setIsVisible = !win.isVisible();

                win.setVisible(setIsVisible);
                configService.setProperty(
                "net.java.sip.communicator.impl.systray.showApplication",
                Boolean.toString(setIsVisible));

                if(win.isVisible())
                {
                    win.bringToFront();
                }
            }
        });

        // Change the MacOSX icon with the white one when the popup
        // menu appears
        if (osName.startsWith("Mac OS X"))
        {
            TrayMenuFactory.addPopupMenuListener(menu, new PopupMenuListener()
            {

                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                    if (currentIcon == envelopeIcon)
                    {
                        trayIcon.setIcon(envelopeIconWhite);
                        currentIcon = envelopeIconWhite;
                    } else
                    {
                        trayIcon.setIcon(logoIconWhite);
                        currentIcon = logoIconWhite;
                    }
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                    if (currentIcon == envelopeIconWhite)
                    {
                        trayIcon.setIcon(envelopeIcon);
                        currentIcon = envelopeIcon;
                    } else
                    {
                        getTrayIcon().setIcon(logoIcon);
                        currentIcon = logoIcon;
                    }
                }

                public void popupMenuCanceled(PopupMenuEvent e)
                {
                    popupMenuWillBecomeInvisible(e);
                }
            });
        }

        PopupMessageHandler pph = null;
        if (!osName.startsWith("Mac OS X"))
        {
            pph = new PopupMessageHandlerTrayIconImpl(trayIcon);
            popupHandlerSet.put(pph.getClass().getName(), pph);
            OsDependentActivator.bundleContext.registerService(
                PopupMessageHandler.class.getName(),
                pph, null);
        }
        try
        {
            OsDependentActivator.bundleContext.addServiceListener(
                new ServiceListenerImpl(),
                "(objectclass=" + PopupMessageHandler.class.getName() + ")");
        } catch (Exception e)
        {
            logger.warn(e);
        }

        // now we look if some handler has been registered before we start
        // to listen
        ServiceReference[] handlerRefs = null;
        try
        {
            handlerRefs = OsDependentActivator.bundleContext.getServiceReferences(
                PopupMessageHandler.class.getName(),
                null);
        } catch (InvalidSyntaxException ex)
        {
            logger.error("Error while retrieving service refs", ex);
        }
        if (handlerRefs != null)
        {
            String configuredHandler = (String) configService.getProperty(
                "systray.POPUP_HANDLER");
            for (int i = 0; i < handlerRefs.length; i++)
            {
                PopupMessageHandler handler =
                    (PopupMessageHandler) OsDependentActivator.bundleContext.
                    getService(handlerRefs[i]);
                String handlerName = handler.getClass().getName();
                if (!popupHandlerSet.containsKey(handlerName))
                {
                    popupHandlerSet.put(handlerName, handler);
                    logger.info("added the following popup handler : " +
                        handler);
                    if (configuredHandler.equals(handler.getClass().getName()))
                    {
                        setActivePopupMessageHandler(handler);
                    }
                }

            }
        }

        // either we have an incorrect config value or the default popup handler
        // is not yet available. we use the available popup handler and will
        // auto switch to the configured one when it will be available.
        // we will be aware of it since we listen for new registerred
        // service in the bundle context.
        if (activePopupHandler == null)
        {
            setActivePopupMessageHandler(pph);
        }

        systray.addTrayIcon(trayIcon);

        initialized = true;
    }

    /**
     * Saves the last status for all accounts. This information is used
     * on logging. Each time user logs in he's logged with the same status
     * as he was the last time before closing the application.
     *
     * @param protocolProvider  the protocol provider for which we save the
     * last selected status
     * @param statusName the status name to save
     */
    public void saveStatusInformation(
        ProtocolProviderService protocolProvider,
        String statusName)
    {
        if (configService != null)
        {
            String prefix = "net.java.sip.communicator.impl.gui.accounts";

            List<String> accounts = configService.getPropertyNamesByPrefix(
                prefix, true);

            boolean savedAccount = false;

            for (String accountRootPropName : accounts)
            {
                String accountUID = configService.getString(accountRootPropName);

                if (accountUID.equals(protocolProvider.getAccountID().
                    getAccountUniqueID()))
                {

                    configService.setProperty(
                        accountRootPropName + ".lastAccountStatus",
                        statusName);

                    savedAccount = true;
                }
            }

            if (!savedAccount)
            {
                String accNodeName = "acc" + Long.toString(System.
                    currentTimeMillis());

                String accountPackage =
                    "net.java.sip.communicator.impl.gui.accounts." + accNodeName;

                configService.setProperty(accountPackage,
                    protocolProvider.getAccountID().getAccountUniqueID());

                configService.setProperty(
                    accountPackage + ".lastAccountStatus",
                    statusName);
            }
        }
    }

    /**
     * Implements <tt>SystraService#showPopupMessage()</tt>
     *
     * @param popupMessage the message we will show
     */
    public void showPopupMessage(PopupMessage popupMessage)
    {
        // since popup handler could be loaded and unloader on the fly,
        // we have to check if we currently have a valid one.
        if (activePopupHandler != null)
        {
            activePopupHandler.showPopupMessage(popupMessage);
        }
    }

    /**
     * Implements the <tt>SystrayService.addPopupMessageListener</tt> method.
     *
     * @param listener the listener to add
     */
    public void addPopupMessageListener(SystrayPopupMessageListener listener)
    {
        if (activePopupHandler != null)
        {
            activePopupHandler.addPopupMessageListener(listener);
        }
    }

    /**
     * Implements the <tt>SystrayService.removePopupMessageListener</tt> method.
     *
     * @param listener the listener to remove
     */
    public void removePopupMessageListener(SystrayPopupMessageListener listener)
    {
        if (activePopupHandler != null)
        {
            activePopupHandler.removePopupMessageListener(listener);
        }
    }

    /**
     * Sets a new Systray icon.
     *
     * @param imageType the type of the image to set.
     */
    public void setSystrayIcon(int imageType)
    {
        if (!checkInitialized())
        {
            return;
        }

        String osName = System.getProperty("os.name");

        ImageIcon toChangeSystrayIcon = null;

        if (imageType == SystrayService.SC_IMG_TYPE)
        {
            if (osName.startsWith("Mac OS X") && TrayMenuFactory.isVisible(menu))
            {
                toChangeSystrayIcon = logoIconWhite;
            } else
            {
                toChangeSystrayIcon = logoIcon;
            }
        } else if (imageType == SystrayService.SC_IMG_OFFLINE_TYPE)
        {
            if (!osName.startsWith("Mac OS X"))
            {
                toChangeSystrayIcon = logoIconOffline;
            }
        } else if (imageType == SystrayService.SC_IMG_AWAY_TYPE)
        {
            if (!osName.startsWith("Mac OS X"))
            {
                toChangeSystrayIcon = logoIconAway;
            }
        } else if (imageType == SystrayService.SC_IMG_FFC_TYPE)
        {
            if (!osName.startsWith("Mac OS X"))
            {
                toChangeSystrayIcon = logoIconFFC;
            }
        } else if (imageType == SystrayService.ENVELOPE_IMG_TYPE)
        {
            if (osName.startsWith("Mac OS X") && TrayMenuFactory.isVisible(menu))
            {
                toChangeSystrayIcon = envelopeIconWhite;
            } else
            {
                toChangeSystrayIcon = envelopeIcon;
            }
        }

        if (toChangeSystrayIcon != null)
        {
            this.trayIcon.setIcon(toChangeSystrayIcon);
            this.currentIcon = toChangeSystrayIcon;
        }

        if (osName.startsWith("Mac OS X"))
        {
            URL toChangeDockIcon = null;
            switch (imageType)
            {
                case SystrayService.SC_IMG_TYPE:
                    // online will restore the original image
                    break;
                case SystrayService.SC_IMG_OFFLINE_TYPE:
                    toChangeDockIcon = dockIconOffline;
                    break;
                case SystrayService.SC_IMG_AWAY_TYPE:
                    toChangeDockIcon = dockIconAway;
                    break;
                case SystrayService.SC_IMG_FFC_TYPE:
                    toChangeDockIcon = dockIconFFC;
                    break;
            }

            try
            {
                if (toChangeDockIcon != null)
                {
                    Dock.setDockTileImage(toChangeDockIcon);
                } else
                {
                    Dock.restoreDockTileImage();
                }
            } catch (Exception e)
            {
                logger.error("failed to change dock icon", e);
            }
        }
    }

    private boolean checkInitialized()
    {
        if (!initialized)
        {
            logger.error("Systray not init");
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * @return the trayIcon
     */
    public TrayIcon getTrayIcon()
    {
        return trayIcon;
    }

    /**
     * Set the handler which will be used for popup message
     * @param newHandler the handler to set. providing a null handler is like
     * disabling popup.
     * @return the previously used popup handler
     */
    public PopupMessageHandler setActivePopupMessageHandler(
        PopupMessageHandler newHandler)
    {
        PopupMessageHandler oldHandler = activePopupHandler;
        if (oldHandler != null)
        {
            oldHandler.removePopupMessageListener(popupMessageListener);
        }

        if (newHandler != null)
        {
            newHandler.addPopupMessageListener(popupMessageListener);
        }
        activePopupHandler = newHandler;

        return oldHandler;
    }

    /**
     * Get the handler currently used by this implementation to popup message
     * @return the current handler
     */
    public PopupMessageHandler getActivePopupMessageHandler()
    {
        return activePopupHandler;
    }

    /** our listener for popup message click */
    private static class SystrayPopupMessageListenerImpl
        implements SystrayPopupMessageListener
    {

        /**
         * Handles a user click on a systray popup message. If the
         * popup notification was the result of an incoming message from a
         * contact, the chat window with that contact will be opened if not already,
         * and brought to front.
         *
         * @param evt the event triggered when user clicks on a systray popup
         * message
         */
        public void popupMessageClicked(SystrayPopupMessageEvent evt)
        {
            Object o = evt.getTag();

            if (o instanceof Contact)
                OsDependentActivator.getUIService().
                    getChat((Contact) o).setChatVisible(true);
        }
    }

    /** an implementation of <tt>ServiceListener</tt> we will use */
    private class ServiceListenerImpl implements ServiceListener
    {

        /** implements <tt>ServiceListener.serviceChanged</tt> */
        public void serviceChanged(ServiceEvent serviceEvent)
        {
            try
            {
                PopupMessageHandler handler =
                    (PopupMessageHandler) OsDependentActivator.bundleContext.
                    getService(serviceEvent.getServiceReference());

                if (serviceEvent.getType() == ServiceEvent.REGISTERED)
                {
                    if (!popupHandlerSet.containsKey(
                        handler.getClass().getName()))
                    {
                        logger.info(
                            "adding the following popup handler : " + handler);
                        popupHandlerSet.put(
                            handler.getClass().getName(), handler);
                    } else
                        logger.warn("the following popup handler has not " +
                            "been added since it is already known : " + handler);
                    
                    String configuredHandler = (String) configService.
                        getProperty("systray.POPUP_HANDLER");
                    
                    if (configuredHandler.equals(handler.getClass().getName()))
                        setActivePopupMessageHandler(handler);
                } else if (serviceEvent.getType() == ServiceEvent.UNREGISTERING)
                {
                    popupHandlerSet.remove(handler.getClass().getName());
                    if (activePopupHandler == handler)
                    {
                        activePopupHandler.removePopupMessageListener(
                            popupMessageListener);
                        activePopupHandler = null;
                        //we just lost our default handler, we replace it
                        //with the first one we find
                        if (!popupHandlerSet.isEmpty())
                            setActivePopupMessageHandler(
                                popupHandlerSet.get(
                                popupHandlerSet.keys().nextElement()));
                    }
                }
            } catch (IllegalStateException e)
            {
                logger.debug(e);
            }
        }
    }
}