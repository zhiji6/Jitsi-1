/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat;

import java.awt.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;

/**
 * @author Yana Stamcheva
 * @author Valentin Martinet
 */
public class ChatOperationReasonDialog extends MessageDialog
{
    private final JLabel reasonLabel = new JLabel(
        GuiActivator.getResources()
            .getI18NString("service.gui.REASON") + ":");

    private final JTextField reasonField = new JTextField();

    /**
     * Creates an instance of <tt>ChatOperationReasonDialog</tt> using the
     * default title and message.
     */
    public ChatOperationReasonDialog()
    {
        this(null,
            GuiActivator.getResources().getI18NString(
            "service.gui.REASON"),
            GuiActivator.getResources().getI18NString(
            "service.gui.SPECIFY_REASON"),
            GuiActivator.getResources().getI18NString(
            "service.gui.OK"));
    }

    /**
     * Creates an instance of <tt>ChatOperationReasonDialog</tt> by specifying
     * the title and the message shown in the dialog.
     * @param title the title of this dialog
     * @param message the message shown in this dialog
     */
    public ChatOperationReasonDialog(String title, String message)
    {
        this(null,
            title,
            message,
            GuiActivator.getResources().getI18NString(
            "service.gui.OK"));
    }

    /**
     * Creates an instance of <tt>ChatOperationReasonDialog</tt> by specifying
     * the parent window, the title and the message to show.
     * @param chatWindow the parent window
     * @param title the title of this dialog
     * @param message the message shown in this dialog
     * @param okButtonName the custom name of the ok button
     */
    public ChatOperationReasonDialog(
        ChatWindow chatWindow, String title, String message, String okButtonName)
    {
        super(chatWindow, title, message, okButtonName, false);

        JPanel reasonPanel = new JPanel(new BorderLayout());
        reasonPanel.add(reasonLabel, BorderLayout.WEST);
        reasonPanel.add(reasonField, BorderLayout.CENTER);

        this.getContentPane().add(reasonPanel, BorderLayout.CENTER);

        this.pack();
    }

    /**
     * Returns the text entered in the reason field.
     * @return the text entered in the reason field
     */
    public String getReason()
    {
        return reasonField.getText();
    }
}