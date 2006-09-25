/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.icqaccregwizz;

import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;

import org.osgi.framework.*;

/**
 * The <tt>IcqAccountRegistrationWizard</tt> is an implementation of the
 * <tt>AccountRegistrationWizard</tt> for the ICQ protocol. It should allow
 * the user to create and configure a new ICQ account.
 *
 * @author Yana Stamcheva
 */
public class IcqAccountRegistrationWizard implements AccountRegistrationWizard {

    private FirstWizardPage firstWizardPage;

    private IcqAccountRegistration registration
        = new IcqAccountRegistration();

    private WizardContainer wizardContainer;
    
    private ProtocolProviderService protocolProvider;
    
    /**
     * Creates an instance of <tt>IcqAccountRegistrationWizard</tt>.
     * @param wizardContainer the wizard container, where this wizard
     * is added
     */
    public IcqAccountRegistrationWizard(WizardContainer wizardContainer) {
        this.wizardContainer = wizardContainer;
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getIcon</code> method.
     * Returns the icon to be used for this wizard.
     */
    public byte[] getIcon() {
        return Resources.getImage(Resources.ICQ_LOGO);
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolName</code>
     * method. Returns the protocol name for this wizard.
     */
    public String getProtocolName() {
        return Resources.getString("protocolName");
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolDescription
     * </code> method. Returns the description of the protocol for this wizard.
     */
    public String getProtocolDescription() {
        return Resources.getString("protocolDescription");
    }

    /**
     * Returns the set of pages contained in this wizard.
     */
    public Iterator getPages() {
        ArrayList pages = new ArrayList();
        firstWizardPage = new FirstWizardPage(registration, wizardContainer);
        
        pages.add(firstWizardPage);
        
        return pages.iterator();
    }

    /**
     * Returns the set of data that user has entered through this wizard.
     */
    public Iterator getSummary() {
        Hashtable summaryTable = new Hashtable();

        summaryTable.put("UIN", registration.getUin());
        summaryTable.put("Remember password",
                new Boolean(registration.isRememberPassword()));

        return summaryTable.entrySet().iterator();
    }

    /**
     * Installs the account created through this wizard.
     */
    public ProtocolProviderService finish() {
        firstWizardPage = null;
        ProtocolProviderFactory factory 
            = IcqAccRegWizzActivator.getIcqProtocolProviderFactory();
      
        return this.installAccount(factory, 
                registration.getUin(), registration.getPassword());
    }

    /**
     * Creates an account for the given user and password.
     * @param providerFactory the ProtocolProviderFactory which will create
     * the account
     * @param user the user identifier
     * @param passwd the password
     * @return the <tt>ProtocolProviderService</tt> for the new account.
     */
    public ProtocolProviderService installAccount(
            ProtocolProviderFactory providerFactory,
            String user,
            String passwd) {

        Hashtable accountProperties = new Hashtable();

        if(registration.isRememberPassword()) {
            accountProperties.put(ProtocolProviderFactory.PASSWORD, passwd);
        }
        
        if(protocolProvider != null) {
            providerFactory.uninstallAccount(protocolProvider.getAccountID());
            this.protocolProvider = null;
        }
        
        AccountID accountID = providerFactory.installAccount(
                    user, accountProperties);
                
        ServiceReference serRef = providerFactory
            .getProviderForAccount(accountID);

        ProtocolProviderService protocolProvider
            = (ProtocolProviderService) IcqAccRegWizzActivator.bundleContext
                .getService(serRef);        
        
        return protocolProvider;
    }
    
    /**
     * Fills the UIN and Password fields in this panel with the data comming
     * from the given protocolProvider.
     * @param protocolProvider The <tt>ProtocolProviderService</tt> to load the
     * data from.
     */
    public void loadAccount(ProtocolProviderService protocolProvider) {
        
        this.protocolProvider = protocolProvider;
        
        this.firstWizardPage.loadAccount(protocolProvider);
    }
}
