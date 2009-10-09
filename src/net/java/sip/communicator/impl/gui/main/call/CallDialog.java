/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.call.conference.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;
import net.java.sip.communicator.util.swing.border.*;

/**
 * The dialog created for a given call.
 *
 * @author Yana Stamcheva
 */
public class CallDialog
    extends SIPCommFrame
    implements ActionListener,
               MouseListener,
               CallChangeListener
{
    private static final String DIAL_BUTTON = "DIAL_BUTTON";

    private static final String CONFERENCE_BUTTON = "CONFERENCE_BUTTON";

    private static final String HANGUP_BUTTON = "HANGUP_BUTTON";

    private DialpadDialog dialpadDialog;

    private final Container contentPane = getContentPane();

    private Component callPanel = null;

    private final HoldButton holdButton;

    private final MuteButton muteButton;

    private final LocalVideoButton videoButton;

    private final Call call;

    private boolean isLastConference = false;

    private Date callStartDate;

    private boolean isCallTimerStarted = false;

    private Timer timer;

    /**
     * Creates a <tt>CallDialog</tt> by specifying the underlying call panel.
     * @param call the <tt>call</tt> that this dialog represents
     */
    public CallDialog(Call call)
    {
        this.call = call;

        // The call duration parameter is not known yet.
        this.setCallTitle(null);

        this.isLastConference = isConference();

        if (isLastConference)
        {
            this.callPanel
                = new ConferenceCallPanel(this, call);
        }
        else
        {
            CallPeer callPeer = null;

            if (call.getCallPeers().hasNext())
                callPeer = call.getCallPeers().next();

            if (callPeer != null)
                this.callPanel = new OneToOneCallPanel(
                    this, call, callPeer);

            this.setPreferredSize(new Dimension(500, 400));
        }

        call.addCallChangeListener(this);

        TransparentPanel buttonsPanel
            = new TransparentPanel(new BorderLayout(5, 5));

        TransparentPanel settingsPanel
            = new TransparentPanel();

        SIPCommButton hangupButton = new SIPCommButton(
            ImageLoader.getImage(ImageLoader.HANGUP_BUTTON_BG));

        SIPCommButton dialButton = new SIPCommButton(
            ImageLoader.getImage(ImageLoader.CALL_SETTING_BUTTON_BG),
            ImageLoader.getImage(ImageLoader.DIAL_BUTTON));

        SIPCommButton conferenceButton = new SIPCommButton(
            ImageLoader.getImage(ImageLoader.CALL_SETTING_BUTTON_BG),
            ImageLoader.getImage(ImageLoader.ADD_TO_CALL_BUTTON));

        holdButton = new HoldButton(call);
        muteButton = new MuteButton(call);
        videoButton = new LocalVideoButton(call);

        dialButton.setName(DIAL_BUTTON);
        dialButton.setToolTipText(
            GuiActivator.getResources().getI18NString("service.gui.DIALPAD"));
        dialButton.addActionListener(this);
        dialButton.addMouseListener(this);

        conferenceButton.setName(CONFERENCE_BUTTON);
        conferenceButton.setToolTipText(
            GuiActivator.getResources().getI18NString(
                "service.gui.CREATE_CONFERENCE_CALL"));
        conferenceButton.addActionListener(this);
        conferenceButton.addMouseListener(this);

        contentPane.add(callPanel, BorderLayout.CENTER);
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        hangupButton.setName(HANGUP_BUTTON);
        hangupButton.setToolTipText(
            GuiActivator.getResources().getI18NString("service.gui.HANG_UP"));
        hangupButton.addActionListener(this);

        settingsPanel.add(dialButton);
        settingsPanel.add(conferenceButton);
        settingsPanel.add(holdButton);
        settingsPanel.add(muteButton);

        if (!isLastConference)
            settingsPanel.add(videoButton);

        buttonsPanel.add(settingsPanel, BorderLayout.WEST);
        buttonsPanel.add(hangupButton, BorderLayout.EAST);

        buttonsPanel.setBorder(
            new ExtendedEtchedBorder(EtchedBorder.LOWERED, 1, 0, 0, 0));

        this.timer = new Timer(1000, new CallTimerListener());
        this.timer.setRepeats(true);

    }

    /**
     * Handles action events.
     * @param evt the <tt>ActionEvent</tt> that was triggered
     */
    public void actionPerformed(ActionEvent evt)
    {
        JButton button = (JButton) evt.getSource();
        String buttonName = button.getName();

        if (buttonName.equals(HANGUP_BUTTON))
        {
            actionPerformedOnHangupButton();
        }
        else if (buttonName.equals(DIAL_BUTTON))
        {
            if (dialpadDialog == null)
                dialpadDialog = this.getDialpadDialog();

            if(!dialpadDialog.isVisible())
            {
                dialpadDialog.setSize(
                    this.getWidth() - 20,
                    dialpadDialog.getHeight());

                dialpadDialog.setLocation(
                    this.getX() + 10,
                    getLocationOnScreen().y + getHeight());

                dialpadDialog.setVisible(true);
                dialpadDialog.requestFocus();
            }
            else
            {
                dialpadDialog.setVisible(false);
            }
        }
        else if (buttonName.equals(CONFERENCE_BUTTON))
        {
            ConferenceInviteDialog inviteDialog
                = new ConferenceInviteDialog(call);

            inviteDialog.setVisible(true);
        }
    }

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    /**
     * Updates the dial pad dialog and removes related focus listener.
     * @param e the <tt>MouseEvent</tt> that was triggered
     */
    public void mouseEntered(MouseEvent e)
    {
        if (dialpadDialog == null)
            dialpadDialog = this.getDialpadDialog();
        dialpadDialog.removeWindowFocusListener(dialpadDialog);
    }

    /**
     * Updates the dial pad dialog and adds related focus listener.
     * @param e the <tt>MouseEvent</tt> that was triggered
     */
    public void mouseExited(MouseEvent e)
    {
        if (dialpadDialog == null)
            dialpadDialog = this.getDialpadDialog();
        dialpadDialog.addWindowFocusListener(dialpadDialog);
    }

    /**
     * Executes the action associated with the "Hang up" button which may be
     * invoked by clicking the button in question or closing this dialog.
     */
    private void actionPerformedOnHangupButton()
    {
        Call call = getCall();

        NotificationManager.stopSound(NotificationManager.OUTGOING_CALL);
        NotificationManager.stopSound(NotificationManager.BUSY_CALL);

        if (call != null)
            CallManager.hangupCall(call);

        this.dispose();
    }

    /**
     * Returns the <tt>Call</tt> corresponding to this CallDialog.
     *
     * @return the <tt>Call</tt> corresponding to this CallDialog.
     */
    public Call getCall()
    {
        return call;
    }

    /**
     * Hang ups the current call on close.
     * @param isEscaped indicates if the window was close by pressing the escape
     * button
     */
    protected void close(boolean isEscaped)
    {
        if (!isEscaped)
        {
            actionPerformedOnHangupButton();
        }
    }

    /**
     * Returns the <tt>DialpadDialog</tt> corresponding to this CallDialog.
     *
     * @return the <tt>DialpadDialog</tt> corresponding to this CallDialog.
     */
    private DialpadDialog getDialpadDialog()
    {
        Iterator<? extends CallPeer> callPeers =
            (call == null)
                ? new Vector<CallPeer>().iterator()
                : call.getCallPeers();

        return new DialpadDialog(callPeers);
    }

    /**
     * Returns <code>true</code> if the hold button is selected,
     * <code>false</code> - otherwise.
     *
     * @return  <code>true</code> if the hold button is selected,
     * <code>false</code> - otherwise.
     */
    public boolean isHoldButtonSelected()
    {
        return holdButton.isSelected();
    }

    /**
     * Selects or unselects the hold button in this call dialog.
     *
     * @param isSelected indicates if the hold button should be selected or not
     */
    public void setHoldButtonSelected(boolean isSelected)
    {
        this.holdButton.setSelected(true);
    }

    /**
     * Returns <code>true</code> if the mute button is selected,
     * <code>false</code> - otherwise.
     *
     * @return  <code>true</code> if the mute button is selected,
     * <code>false</code> - otherwise.
     */
    public boolean isMuteButtonSelected()
    {
        return muteButton.isSelected();
    }

    /**
     * Selects or unselects the mute button in this call dialog.
     *
     * @param isSelected indicates if the mute button should be selected or not
     */
    public void setMuteButtonSelected(boolean isSelected)
    {
        this.muteButton.setSelected(true);
    }

    /**
     * Returns <code>true</code> if the video button is selected,
     * <code>false</code> - otherwise.
     *
     * @return  <code>true</code> if the video button is selected,
     * <code>false</code> - otherwise.
     */
    public boolean isVideoButtonSelected()
    {
        return videoButton.isSelected();
    }

    /**
     * Selects or unselects the video button in this call dialog.
     *
     * @param isSelected indicates if the video button should be selected or not
     */
    public void setVideoButtonSelected(boolean isSelected)
    {
        this.videoButton.setSelected(true);
    }

    /**
     * Implements the <tt>CallChangeListener.callPeerAdded</tt> method.
     * Adds the according user interface when a new peer is added to the call.
     * @param evt the <tt>CallPeerEvent</tt> that notifies us for the change
     */
    public void callPeerAdded(CallPeerEvent evt)
    {
        if (evt.getSourceCall() == call)
        {
            if (isLastConference)
            {
                ((ConferenceCallPanel) callPanel)
                    .addCallPeerPanel(evt.getSourceCallPeer());
            }
            else
            {
                this.isLastConference = isConference();

                // We've been in one-to-one call and we're now in a conference.
                if (isLastConference)
                {
                    contentPane.remove(callPanel);
                    this.callPanel
                        = new ConferenceCallPanel(this, call);
                    contentPane.add(callPanel, BorderLayout.CENTER);
                }
                // We're still in one-to-one call and we receive the remote peer.
                else
                {
                    CallPeer onlyCallPeer = null;
                    if (call.getCallPeers().hasNext())
                        onlyCallPeer = call.getCallPeers().next();

                    if (onlyCallPeer != null)
                        ((OneToOneCallPanel) callPanel)
                            .addCallPeerPanel(onlyCallPeer);
                }
            }
        }
        if (contentPane.isVisible())
        {
            contentPane.validate();
            contentPane.repaint();
        }
    }

    /**
     * Implements the <tt>CallChangeListener.callPeerRemoved</tt> method.
     * Removes all related user interface when a peer is removed from the call.
     * @param evt the <tt>CallPeerEvent</tt> that has been triggered
     */
    public void callPeerRemoved(CallPeerEvent evt)
    {
        if (evt.getSourceCall() == call)
        {
            Timer timer = new Timer(5000,
                new RemovePeerPanelListener(evt.getSourceCallPeer()));

            timer.setRepeats(false);
            timer.start();
        }
    }

    public void callStateChanged(CallChangeEvent evt) {}

    /**
     * Checks if the contained call is a conference call.
     *
     * @return <code>true</code> if the contained <tt>Call</tt> is a conference
     * call, otherwise - returns <code>false</code>.
     */
    private boolean isConference()
    {
        // If we have more than one peer, we're in a conference call.
        if (call.getCallPeerCount() > 1)
            return true;

        // If one of our peers is a conference focus, we're in a
        // conference call.
        Iterator<? extends CallPeer> callPeers = call.getCallPeers();

        while (callPeers.hasNext())
        {
            CallPeer callPeer = callPeers.next();

            if (callPeer.isConferenceFocus())
                return true;
        }

        return false;
    }

    /**
     * Starts the timer that counts call duration.
     */
    public void startCallTimer()
    {
        this.callStartDate = new Date();
        this.timer.start();
        this.isCallTimerStarted = true;
    }

    /**
     * Stops the timer that counts call duration.
     */
    public void stopCallTimer()
    {
        this.timer.stop();
    }

    /**
     * Returns <code>true</code> if the call timer has been started, otherwise
     * returns <code>false</code>.
     * @return <code>true</code> if the call timer has been started, otherwise
     * returns <code>false</code>
     */
    public boolean isCallTimerStarted()
    {
        return isCallTimerStarted;
    }

    /**
     * Each second refreshes the time label to show to the user the exact
     * duration of the call.
     */
    private class CallTimerListener
        implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            Date time =
                GuiUtils.substractDates(new Date(), callStartDate);

            setCallTitle(time);
        }
    }

    /**
     * Sets the title of this dialog by specifying the call duration.
     * @param callDuration the duration of the call represented as Date object
     */
    private void setCallTitle(Date callDuration)
    {
        String titleString
            = GuiActivator.getResources().getI18NString("service.gui.CALL")
                + " | ";

        if (callDuration != null)
            this.setTitle(titleString + GuiUtils.formatTime(callDuration));
        else
            this.setTitle(titleString + "00:00:00");
    }

    /**
     * Removes the given CallPeer panel from this CallPanel.
     */
    private class RemovePeerPanelListener
        implements ActionListener
    {
        private CallPeer peer;

        public RemovePeerPanelListener(CallPeer peer)
        {
            this.peer = peer;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (isLastConference)
            {
                if (call.getCallPeerCount() > 2)
                {
                    ((ConferenceCallPanel) callPanel)
                        .removeCallPeerPanel(peer);
                }
                else
                {
                    contentPane.remove(callPanel);
                    CallPeer singlePeer = call.getCallPeers().next();

                    if (singlePeer != null)
                        callPanel = new OneToOneCallPanel(
                            CallDialog.this, call, singlePeer);

                    contentPane.add(callPanel, BorderLayout.CENTER);

                    isLastConference = false;
                }

                if (contentPane.isVisible())
                {
                    contentPane.validate();
                    contentPane.repaint();
                }
            }
            else
            {
                // Dispose the window
                dispose();
            }
        }
    }
}
