/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.autoaway;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>ConfigurationForm</tt> that would be added in the user interface
 * configuration window.
 * 
 * @author Damien Roth
 */
public class AutoAwayConfigForm
    extends TransparentPanel
    implements ConfigurationForm
{
    private JCheckBox enable;
    private JSpinner timer;

    /**
     * Create an instance of <tt>StatusConfigForm</tt>
     */
    public AutoAwayConfigForm()
    {
        super(new BorderLayout(10, 10));

        Component mainPanel = init();
        initValues();

        this.add(mainPanel);
    }

    /**
     * Init the widgets
     */
    private Component init()
    {
        JPanel autoStatusPanel = new TransparentPanel(new BorderLayout());

        // Title : automatic status
        JLabel autoStatusLabel 
            = new JLabel(AutoAwayActivator.getResources()
                    .getI18NString("automaticStatus"));


        Font bold = autoStatusLabel.getFont().deriveFont(Font.BOLD);
        autoStatusLabel.setFont(bold);

        JPanel fieldsPanel = new TransparentPanel(new BorderLayout(5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 0, 0));

        enable =
            new SIPCommCheckBox(AutoAwayActivator.getResources().getI18NString(
                "enableChangeStatus"));
        fieldsPanel.add(enable, BorderLayout.NORTH);
        enable.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                timer.setEnabled(enable.isSelected());
                saveData();
            }
        });


        JPanel timerPanel =
            new TransparentPanel(new FlowLayout(FlowLayout.LEFT));
        // Texte
        timerPanel.add(new JLabel(
                AutoAwayActivator.getResources().getI18NString("awayMinutes")));
        // Spinner
        timer = new JSpinner(new SpinnerNumberModel(15, 1, 180, 1));
        timerPanel.add(timer);
        timer.addChangeListener(new ChangeListener()
        {

            public void stateChanged(ChangeEvent e)
            {
                saveData();
            }
        });

        fieldsPanel.add(timerPanel, BorderLayout.WEST);
        autoStatusPanel.add(autoStatusLabel, BorderLayout.NORTH);
        autoStatusPanel.add(fieldsPanel, BorderLayout.CENTER);

        Container mainPanel = new TransparentPanel(new BorderLayout());
        mainPanel.add(autoStatusPanel, BorderLayout.NORTH);
        return mainPanel;
    }

    /**
     * Init the values of the widgets
     */
    private void initValues()
    {
        ConfigurationService configService 
            = AutoAwayActivator.getConfigService();

        String e = (String) configService.getProperty(Preferences.ENABLE);
        if (e != null)
        {
            try
            {
                this.enable.setSelected(Boolean.parseBoolean(e));
                this.timer.setEnabled(Boolean.parseBoolean(e));
            } catch (NumberFormatException ex)
            {
                this.enable.setSelected(false);
                this.timer.setEnabled(false);
            }
        } else
        {
            this.enable.setSelected(false);
            this.timer.setEnabled(false);
        }

        String t = (String) configService.getString(Preferences.TIMER);
        if (t != null)
        {
            try
            {
                this.timer.setValue(Integer.parseInt(t));
            } catch (NumberFormatException ex)
            {
            }
        }
    }

    /**
     * Save data in the configuration file
     */
    private void saveData()
    {
        ConfigurationService configService 
            = AutoAwayActivator.getConfigService();

        configService.setProperty(Preferences.ENABLE, 
                                  Boolean.toString(enable.isSelected()));
        Integer interval = (Integer) timer.getValue();
        configService.setProperty(Preferences.TIMER, interval);
    }

    /**
     * Implements the <tt>ConfigurationForm.getForm()</tt> method. Returns the
     * component corresponding to this configuration form.
     */
    public Object getForm()
    {
        return this;
    }

    /**
     * Implements the <tt>ConfigurationForm.getIcon()</tt> method. Returns the
     * icon of this configuration form.
     */
    public byte[] getIcon()
    {
        return AutoAwayActivator.getResources()
            .getImageInBytes("plugin.autoaway.PLUGIN_ICON");
    }

    /**
     * Implements the <tt>ConfigurationForm.getIndex()</tt> method.
     */
    public int getIndex()
    {
        return -1;
    }

    /**
     * Implements the <tt>ConfigurationForm.getIcon()</tt> method. Returns the
     * icon of this configuration form.
     */
    public String getTitle()
    {
        return AutoAwayActivator.getResources()
            .getI18NString("automaticStatus");
    }

}
