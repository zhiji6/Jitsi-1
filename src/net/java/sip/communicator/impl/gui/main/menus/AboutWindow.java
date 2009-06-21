/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.menus;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.swing.*;

public class AboutWindow
    extends JDialog
    implements  ActionListener,
                ExportedWindow
{
    public AboutWindow()
    {
        WindowBackground mainPanel = new WindowBackground();
        JLabel versionLabel = new JLabel(" "
            + System.getProperty("sip-communicator.version"));

        this.setModal(false);
        this.setResizable(false);

        this.setTitle(
            GuiActivator.getResources().getI18NString(
                "plugin.branding.ABOUT_WINDOW_TITLE",
                new String[]{ GuiActivator.getResources()
                    .getSettingsString("service.gui.APPLICATION_NAME")}));

        mainPanel.setLayout(null);

        versionLabel.setForeground(new Color(
            GuiActivator.getResources()
                .getColor("service.gui.SPLASH_SCREEN_TITLE_COLOR")));
        versionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        mainPanel.add(versionLabel);

        Insets insets = mainPanel.getInsets();
        versionLabel.setBounds(370 + insets.left, 307 + insets.top, 200, 20);

        this.getContentPane().add(mainPanel);

        // Close the splash screen on simple click or Esc.
        this.getGlassPane().addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                AboutWindow.this.close();
            }
        });

        this.getGlassPane().setVisible(true);

        ActionMap amap = this.getRootPane().getActionMap();

        amap.put("close", new CloseAction());

        InputMap imap = this.getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }

    protected void close()
    {
        this.dispose();
    }

    /**
     * The action invoked when user presses Escape key.
     */
    private class CloseAction extends UIAction
    {
        public void actionPerformed(ActionEvent e)
        {
            AboutWindow.this.close();
        }
    }

    /**
     * Constructs the window background in order to have a background image.
     */
    private static class WindowBackground
        extends JPanel
    {
        private final Image bgImage;

        public WindowBackground()
        {
            this.setOpaque(true);

            bgImage = ImageLoader.getImage(
                        ImageLoader.ABOUT_WINDOW_BACKGROUND);

            this.setPreferredSize(new Dimension(bgImage.getWidth(this),
                bgImage.getHeight(this)));
        }

        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g = g.create();
            try
            {
                AntialiasingManager.activateAntialiasing(g);
                g.drawImage(bgImage, 0, 0, null);
            }
            finally
            {
                g.dispose();
            }
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        this.dispose();
    }

    /**
     * Implements the <tt>ExportedWindow.getIdentifier()</tt> method.
     */
    public WindowID getIdentifier()
    {
        return ExportedWindow.ABOUT_WINDOW;
    }

    /**
     * This dialog could not be minimized.
     */
    public void minimize()
    {
    }

    /**
     * This dialog could not be maximized.
     */
    public void maximize()
    {
    }

    /**
     * Implements the <tt>ExportedWindow.bringToFront()</tt> method. Brings
     * this window to front.
     */
    public void bringToFront()
    {
        this.toFront();
    }

    /**
     * The source of the window
     * @return the source of the window
     */
    public Object getSource()
    {
        return this;
    }

    /**
     * Implementation of {@link ExportedWindow#setParams(Object[])}.
     */
    public void setParams(Object[] windowParams)
    {
    }
}
