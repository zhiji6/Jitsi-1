/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.simpleaccreg;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.osgi.framework.*;

/**
 * The <tt>NoAccountFoundPage</tt> is the page shown in the account
 * registration wizard shown in the beginning of the program, when no registered
 * accounts are found.
 * 
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 */
public class InitialAccountRegistrationFrame
    extends JFrame
    implements ServiceListener
{
    private ConfigurationService configurationService;

    private final Logger logger
        = Logger.getLogger(InitialAccountRegistrationFrame.class);

    private final JPanel mainAccountsPanel =
        new JPanel(new BorderLayout(10, 10));

    private final JPanel accountsPanel =
        new JPanel(new GridLayout(0, 2, 10, 10));

    private final JButton signinButton =
        new JButton(Resources.getString("service.gui.SIGN_IN"));

    private final Collection<AccountRegistrationPanel> registrationForms =
        new Vector<AccountRegistrationPanel>();

    /*
     * The background of all AccountRegistrationPanel instances is one and the
     * same so it not only makes sense to retrieve it once for each panel but to
     * also retrieve it once and use it in all AccountRegistrationPanel
     * instances.
     */
    private final Color accountRegistrationPanelBackground =
        new Color(Resources.getColor("service.gui.DESKTOP_BACKGROUND"));

    /**
     * Creates an instance of <tt>NoAccountFoundPage</tt>.
     */
    public InitialAccountRegistrationFrame()
    {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        MainPanel mainPanel = new MainPanel(new BorderLayout(5, 5));
        JPanel messageAreaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextArea messageArea =
            new JTextArea(Resources.getString("plugin.simpleaccregwizz.INITIAL_ACCOUNT_REGISTRATION"));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton
            = new JButton(Resources.getString("service.gui.CANCEL"));

        this.setTitle(Resources.getString("service.gui.SIGN_IN"));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        this.getContentPane().add(mainPanel);

        mainPanel.add(messageAreaPanel, BorderLayout.NORTH);
        mainPanel.add(mainAccountsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        messageAreaPanel.add(messageArea);
        messageArea.setPreferredSize(new Dimension(350, 20));
        messageArea.setFont(messageArea.getFont().deriveFont(Font.BOLD));

        mainAccountsPanel.add(accountsPanel, BorderLayout.CENTER);

        mainAccountsPanel.setOpaque(false);
        accountsPanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        messageAreaPanel.setOpaque(false);

        SigninActionListener actionListener = new SigninActionListener();

        signinButton.addActionListener(actionListener);
        cancelButton.addActionListener(actionListener);

        buttonPanel.add(signinButton);
        buttonPanel.add(cancelButton);

        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);

        this.getRootPane().setDefaultButton(signinButton);

        this.initAccountWizards();

        // Create the default group
        String groupName
            = Resources.getApplicationProperty("impl.gui.DEFAULT_GROUP_NAME");

        if(groupName != null && groupName.length() > 0)
        {
            MetaContactListService contactList =
                SimpleAccountRegistrationActivator.getContactList();
            Iterator iter = contactList.getRoot().getSubgroups();
            while (iter.hasNext())
            {
                MetaContactGroup gr = (MetaContactGroup) iter.next();
                if (groupName.equals(gr.getGroupName()))
                    return;
            }

            contactList
                .createMetaContactGroup(contactList.getRoot(), groupName);

            getConfigurationService()
                .setProperty(
                    "net.java.sip.communicator.impl.gui.addcontact.lastContactParent",
                    groupName);
        }
    }

    private void initAccountWizards()
    {
        String simpleWizards
            = Resources.getLoginProperty("plugin.simpleaccreg.PROTOCOL_ORDER");

        StringTokenizer tokenizer = new StringTokenizer(simpleWizards, "|");

        ServiceReference[] serviceRefs = null;
        while (tokenizer.hasMoreTokens())
        {
            String protocolToken = tokenizer.nextToken();

            String osgiFilter = "("
                + ProtocolProviderFactory.PROTOCOL
                + "="+protocolToken+")";

            try
            {
                serviceRefs = SimpleAccountRegistrationActivator.bundleContext
                    .getServiceReferences(
                        AccountRegistrationWizard.class.getName(), osgiFilter);

                if (serviceRefs != null && serviceRefs.length > 0)
                {
                    AccountRegistrationWizard wizard
                        = (AccountRegistrationWizard)

                        SimpleAccountRegistrationActivator
                        .bundleContext.getService(serviceRefs[0]);

                    this.addAccountRegistrationForm(wizard);
                }
            }
            catch (InvalidSyntaxException ex)
            {
                logger.error("GuiActivator : ", ex);
            }
        }
    }

    private class AccountRegistrationPanel
        extends JPanel
    {
        private JLabel usernameLabel
            = new JLabel(Resources.getString("service.gui.LOGIN"));

        private JLabel passwordLabel = new JLabel("Password");

        private JTextField usernameField = new JTextField();

        private JLabel usernameExampleLabel = new JLabel();

        private JPasswordField passwordField = new JPasswordField();

        private JPanel labelsPanel = new JPanel(new GridLayout(0, 1, 5, 0));

        private JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 5, 0));

        private JPanel emptyPanel = new JPanel();

        private JPanel inputPanel = new JPanel(new BorderLayout(5, 5));

        private JPanel iconDescriptionPanel = new JPanel(new BorderLayout());

        private JTextArea descriptionArea = new JTextArea();

        private final AccountRegistrationWizard wizard;

        public AccountRegistrationPanel(
            AccountRegistrationWizard accountWizard,
            boolean isPreferredWizard)
        {
            super(new BorderLayout(5, 5));

            this.wizard = accountWizard;

            JLabel protocolLabel = new JLabel();
            JPanel inputRegisterPanel = new JPanel(new BorderLayout());

            this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            this.setPreferredSize(new Dimension(230, 150));

            this.setOpaque(false);

            this.inputPanel.setOpaque(false);
            this.labelsPanel.setOpaque(false);
            this.fieldsPanel.setOpaque(false);
            this.emptyPanel.setOpaque(false);
            inputRegisterPanel.setOpaque(false);
            this.iconDescriptionPanel.setOpaque(false);

            this.add(inputRegisterPanel, BorderLayout.CENTER);

            inputRegisterPanel.add(inputPanel, BorderLayout.NORTH);

            if (wizard.isWebSignupSupported())
            {
                String textKey =
                    isPreferredWizard ? "plugin.simpleaccregwizz.SPECIAL_SIGNUP"
                        : "plugin.simpleaccregwizz.SIGNUP";
                JLabel signupLabel =
                    new JLabel("<html><a href=''>"
                        + Resources.getString(textKey) + "</a></html>",
                        JLabel.RIGHT);

                signupLabel.setFont(signupLabel.getFont().deriveFont(10f));
                signupLabel.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent e)
                    {
                        try
                        {
                            wizard.webSignup();
                        }
                        catch (UnsupportedOperationException ex)
                        {
                            // This should not happen, because we check if the
                            // operation is supported, before adding the sign
                            // up.
                            logger.error("The web sign up is not supported.",
                                ex);
                        }
                    }
                });

                inputRegisterPanel.add(signupLabel, BorderLayout.SOUTH);
            }

            this.inputPanel.add(labelsPanel, BorderLayout.WEST);

            this.inputPanel.add(fieldsPanel, BorderLayout.CENTER);

            this.iconDescriptionPanel.add(
                protocolLabel, BorderLayout.NORTH);

            protocolLabel.setFont(
                protocolLabel.getFont().deriveFont(Font.BOLD, 14f));
            this.usernameExampleLabel.setForeground(Color.DARK_GRAY);
            this.usernameExampleLabel.setFont(
                usernameExampleLabel.getFont().deriveFont(8f));
            this.emptyPanel.setMaximumSize(new Dimension(40, 25));

            this.labelsPanel.add(usernameLabel);
            this.labelsPanel.add(emptyPanel);
            this.labelsPanel.add(passwordLabel);

            this.fieldsPanel.add(usernameField);
            this.fieldsPanel.add(usernameExampleLabel);
            this.fieldsPanel.add(passwordField);

            this.usernameExampleLabel.setText(wizard.getUserNameExample());

            protocolLabel.setText(wizard.getProtocolName());

            Image image = null;
            try
            {
                image = ImageIO.read(
                    new ByteArrayInputStream(wizard.getPageImage()));
            }
            catch (IOException e)
            {
                logger.error("Unable to load image.", e);
            }

            if (image != null)
            {
                image = image.getScaledInstance(28, 28, Image.SCALE_SMOOTH);

                protocolLabel.setIcon(new ImageIcon(image));
            }

            if (isPreferredWizard)
            {
                descriptionArea.setBorder(BorderFactory
                    .createEmptyBorder(10, 0, 0, 0));

                descriptionArea.setFont(
                    descriptionArea.getFont().deriveFont(10f));
                descriptionArea.setPreferredSize(new Dimension(220, 50));
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                descriptionArea.setText(wizard.getProtocolDescription());
                descriptionArea.setOpaque(false);

                this.iconDescriptionPanel.add(
                    descriptionArea, BorderLayout.CENTER);

                this.add(iconDescriptionPanel, BorderLayout.WEST);
            }
            else
            {
                this.add(iconDescriptionPanel, BorderLayout.NORTH);
            }
        }

        public void paintComponent(Graphics g)
        {
            // do the superclass behavior first
            super.paintComponent(g);

            g = g.create();
            try
            {
                AntialiasingManager.activateAntialiasing(g);

                Graphics2D g2d = (Graphics2D) g;

                // paint the background with the chosen color
                g2d.setColor(accountRegistrationPanelBackground);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
            finally
            {
                g.dispose();
            }
        }

        public boolean isFilled()
        {
            String username = usernameField.getText();
            return (username != null) && (username.length() > 0);
        }

        public void signin()
        {
            ProtocolProviderService protocolProvider =
                wizard.signin(usernameField.getText(), new String(passwordField
                    .getPassword()));

            saveAccountWizard(protocolProvider, wizard);
        }
    }

    /**
     * Handles registration of a new account wizard.
     */
    public void serviceChanged(ServiceEvent event)
    {
        Object sService = SimpleAccountRegistrationActivator.bundleContext.
            getService(event.getServiceReference());

        // we don't care if the source service is not a plugin component
        if (! (sService instanceof AccountRegistrationWizard))
            return;

        AccountRegistrationWizard wizard
            = (AccountRegistrationWizard) sService;

        if (event.getType() == ServiceEvent.REGISTERED)
        {
            this.addAccountRegistrationForm(wizard);
        }
    }

    /**
     * Adds a simple account registration form corresponding to the given
     * <tt>AccountRegistrationWizard</tt>.
     * 
     * @param wizard the <tt>AccountRegistrationWizard</tt>, which gives us
     * information to fill our simple form.
     */
    private void addAccountRegistrationForm(AccountRegistrationWizard wizard)
    {
     // We don't need to add wizards that are not interested in a
        // simple sign in form.
        if (!wizard.isSimpleFormEnabled())
            return;

        String preferredWizardName
            = Resources.getLoginProperty("impl.gui.PREFERRED_ACCOUNT_WIZARD");

        AccountRegistrationPanel accountPanel;

        if (preferredWizardName != null
            && preferredWizardName.equals(wizard.getClass().getName()))
        {
            accountPanel = new AccountRegistrationPanel(wizard, true);

            mainAccountsPanel.add(
                accountPanel,
                BorderLayout.NORTH);
        }
        else
        {
            accountPanel = new AccountRegistrationPanel(wizard, false);

            this.accountsPanel.add(accountPanel);
        }

        this.registrationForms.add(accountPanel);

        this.pack();
    }

    /**
     * Handles the event triggered by the "service.gui.SIGN_IN" button.
     */
    private class SigninActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            JButton button = (JButton) evt.getSource();

            if (button.equals(signinButton))
            {
                Iterator<AccountRegistrationPanel> regIterator =
                    registrationForms.iterator();

                if (regIterator.hasNext())
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                while(regIterator.hasNext())
                {
                    AccountRegistrationPanel regForm = regIterator.next();

                    if (regForm.isFilled())
                    {
                        regForm.signin();
                    }
                }
            }

            InitialAccountRegistrationFrame initialAccountRegistrationFrame =
                InitialAccountRegistrationFrame.this;
            initialAccountRegistrationFrame.setVisible(false);
            initialAccountRegistrationFrame.dispose();
        }
    }

    private class MainPanel extends JPanel
    {

        /*
         * We cannot keep retrieving the definition and creating a new Color
         * instance on each and every call of paintComponent() - we have to do
         * the thing once and then reuse it. The InitialAccountRegistrationFrame
         * will be disposed as soon as it's closed so we're not wasting memory,
         * only bettering the execution speed.
         */
        private final Color background =
            new Color(
                Resources
                    .getColor("plugin.simpleaccreg.ACCOUNT_REGISTRATION_BACKGROUND"));

        public MainPanel(LayoutManager layoutManager)
        {
            super(layoutManager);
        }

        public void paintComponent(Graphics g)
        {
            // do the superclass behavior first
            super.paintComponent(g);

            g = g.create();
            try
            {
                AntialiasingManager.activateAntialiasing(g);

                Graphics2D g2d = (Graphics2D) g;

                // paint the background with the chosen color
                g2d.setColor(background);
                g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20,
                    15, 15);
            }
            finally
            {
                g.dispose();
            }
        }
    }

    /**
     * Saves the (protocol provider, wizard) pair in through the
     * <tt>ConfigurationService</tt>.
     * 
     * @param protocolProvider the protocol provider to save
     * @param wizard the wizard to save
     */
    private void saveAccountWizard(ProtocolProviderService protocolProvider,
        AccountRegistrationWizard wizard)
    {
        String prefix = "net.java.sip.communicator.impl.gui.accounts";

        ConfigurationService configService = getConfigurationService();

        List<String> accounts = configService.getPropertyNamesByPrefix(prefix, true);
        boolean savedAccount = false;

        for (String accountRootPropName : accounts)
        {
            String accountUID = configService.getString(accountRootPropName);

            if (accountUID.equals(protocolProvider.getAccountID()
                .getAccountUniqueID()))
            {

                configService.setProperty(accountRootPropName + ".wizard",
                    wizard.getClass().getName().replace('.', '_'));

                savedAccount = true;
            }
        }

        if (!savedAccount)
        {
            String accNodeName =
                "acc" + Long.toString(System.currentTimeMillis());

            String accountPackage =
                "net.java.sip.communicator.impl.gui.accounts." + accNodeName;

            configService.setProperty(accountPackage, protocolProvider
                .getAccountID().getAccountUniqueID());

            configService.setProperty(accountPackage + ".wizard", wizard);
        }
    }

    public ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            BundleContext bundleContext =
                SimpleAccountRegistrationActivator.bundleContext;
            ServiceReference configReference =
                bundleContext.getServiceReference(ConfigurationService.class
                    .getName());

            configurationService =
                (ConfigurationService) bundleContext
                    .getService(configReference);
        }
        return configurationService;
    }
}
