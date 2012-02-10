/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;

import net.java.sip.communicator.service.audionotifier.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.*;

/**
 * Handles DTMF sending and playing sound notifications for that.
 *
 * @author Damian Minkov
 */
public class DTMFHandler
    implements KeyEventDispatcher
{
    /**
     * Our class logger
     */
    private final Logger logger = Logger.getLogger(DTMFHandler.class);

    /**
     * The call dialog, where this handler is registered.
     */
    private CallPanel callContainer;

    /**
     * Parent windows we listen key entering on them.
     */
    ArrayList<Window> parents = new ArrayList<Window>();

    /**
     * If we are currently playing an audio for a DTMF tone. Used
     * to play in Loop and stop it if forced to do or new tone has come.
     */
    private SCAudioClip currentlyPlayingAudio = null;

    /**
     * All available tones and its properties like images for buttons, and
     * sounds to be played during send.
     */
    public static final DTMFToneInfo[] availableTones = new DTMFToneInfo[]
    {
        new DTMFToneInfo(
            DTMFTone.DTMF_1,
            KeyEvent.VK_1,
            '1',
            ImageLoader.ONE_DIAL_BUTTON,
            ImageLoader.ONE_DIAL_BUTTON_MAC,
            ImageLoader.ONE_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_ONE),
        new DTMFToneInfo(
            DTMFTone.DTMF_2,
            KeyEvent.VK_2,
            '2',
            ImageLoader.TWO_DIAL_BUTTON,
            ImageLoader.TWO_DIAL_BUTTON_MAC,
            ImageLoader.TWO_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_TWO),
        new DTMFToneInfo(
            DTMFTone.DTMF_3,
            KeyEvent.VK_3,
            '3',
            ImageLoader.THREE_DIAL_BUTTON,
            ImageLoader.THREE_DIAL_BUTTON_MAC,
            ImageLoader.THREE_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_THREE),
        new DTMFToneInfo(
            DTMFTone.DTMF_4,
            KeyEvent.VK_4,
            '4',
            ImageLoader.FOUR_DIAL_BUTTON,
            ImageLoader.FOUR_DIAL_BUTTON_MAC,
            ImageLoader.FOUR_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_FOUR),
        new DTMFToneInfo(
            DTMFTone.DTMF_5,
            KeyEvent.VK_5,
            '5',
            ImageLoader.FIVE_DIAL_BUTTON,
            ImageLoader.FIVE_DIAL_BUTTON_MAC,
            ImageLoader.FIVE_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_FIVE),
        new DTMFToneInfo(
            DTMFTone.DTMF_6,
            KeyEvent.VK_6,
            '6',
            ImageLoader.SIX_DIAL_BUTTON,
            ImageLoader.SIX_DIAL_BUTTON_MAC,
            ImageLoader.SIX_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_SIX),
        new DTMFToneInfo(
            DTMFTone.DTMF_7,
            KeyEvent.VK_7,
            '7',
            ImageLoader.SEVEN_DIAL_BUTTON,
            ImageLoader.SEVEN_DIAL_BUTTON_MAC,
            ImageLoader.SEVEN_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_SEVEN),
        new DTMFToneInfo(
            DTMFTone.DTMF_8,
            KeyEvent.VK_8,
            '8',
            ImageLoader.EIGHT_DIAL_BUTTON,
            ImageLoader.EIGHT_DIAL_BUTTON_MAC,
            ImageLoader.EIGHT_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_EIGHT),
        new DTMFToneInfo(
            DTMFTone.DTMF_9,
            KeyEvent.VK_9,
            '9',
            ImageLoader.NINE_DIAL_BUTTON,
            ImageLoader.NINE_DIAL_BUTTON_MAC,
            ImageLoader.NINE_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_NINE),
        new DTMFToneInfo(
            DTMFTone.DTMF_A,
            KeyEvent.VK_A,
            'a',
            null,
            null,
            null,
            null),
        new DTMFToneInfo(
            DTMFTone.DTMF_B,
            KeyEvent.VK_B,
            'b',
            null,
            null,
            null,
            null),
        new DTMFToneInfo(
            DTMFTone.DTMF_C,
            KeyEvent.VK_C,
            'c',
            null,
            null,
            null,
            null),
        new DTMFToneInfo(
            DTMFTone.DTMF_D,
            KeyEvent.VK_D,
            'd',
            null,
            null,
            null,
            null),
        new DTMFToneInfo(
            DTMFTone.DTMF_STAR,
            KeyEvent.VK_ASTERISK,
            '*',
            ImageLoader.STAR_DIAL_BUTTON,
            ImageLoader.STAR_DIAL_BUTTON_MAC,
            ImageLoader.STAR_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_STAR),
        new DTMFToneInfo(
            DTMFTone.DTMF_0,
            KeyEvent.VK_0,
            '0',
            ImageLoader.ZERO_DIAL_BUTTON,
            ImageLoader.ZERO_DIAL_BUTTON_MAC,
            ImageLoader.ZERO_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_ZERO),
        new DTMFToneInfo(
            DTMFTone.DTMF_SHARP,
            KeyEvent.VK_NUMBER_SIGN,
            '#',
            ImageLoader.DIEZ_DIAL_BUTTON,
            ImageLoader.DIEZ_DIAL_BUTTON_MAC,
            ImageLoader.DIEZ_DIAL_BUTTON_MAC_ROLLOVER,
            SoundProperties.DIAL_DIEZ)
    };

    /**
     * Creates DTMF handler for a call.
     * @param callContainer the <tt>CallContainer</tt>, where this handler is
     * registered
     */
    public DTMFHandler(CallPanel callContainer)
    {
        this.callContainer = callContainer;

        if(callContainer != null)
            this.addParent(callContainer.getCallWindow().getFrame());

        KeyboardFocusManager keyManager
            = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyManager.addKeyEventDispatcher(this);
    }

    /**
     * Creates DTMF handler for a call.
     */
    public DTMFHandler()
    {
        this(null);
    }

    /**
     * Add parent on which we listen for key entering.
     * @param w
     */
    public void addParent(Window w)
    {
        parents.add(w);
    }

    /**
     * This one dispatches key events and if they are on one of our focused
     * parents.
     * @param e the event.
     * @return whether to continue dispatching this event, for now
     *  always return false, so it continues.
     */
    public boolean dispatchKeyEvent(KeyEvent e)
    {
        if(e.getID() == KeyEvent.KEY_TYPED)
            return false;

        boolean singleWindowRule
            = GuiActivator.getUIService().getSingleWindowContainer() == null
                || callContainer.isFocusOwner();

        boolean dispatch = false;
        for (int i = 0; i < parents.size(); i++)
        {
            if(parents.get(i).isFocused() && singleWindowRule)
            {
                dispatch = true;
                break;
            }
        }

        // if we are not in focus skip further processing
        if(!dispatch)
            return false;

        for (int i = 0; i < availableTones.length; i++)
        {
            DTMFToneInfo info = availableTones[i];

            if(info.keyChar == e.getKeyChar())
            {
                switch (e.getID())
                {
                case KeyEvent.KEY_PRESSED:
                    startSendingDtmfTone(info);
                    break;
                case KeyEvent.KEY_RELEASED:
                    stopSendingDtmfTone();
                    break;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Sends a DTMF tone to the current DTMF operation set.
     *
     * @param toneValue the value of the DTMF tone to send.
     */
    public void startSendingDtmfTone(String toneValue)
    {
        for (int i = 0; i < availableTones.length; i++)
        {
            DTMFToneInfo info = availableTones[i];
            if(info.tone.getValue().equals(toneValue))
            {
                startSendingDtmfTone(info);
                return;
            }
        }
    }
    /**
     * Sends a DTMF tone to the current DTMF operation set.
     *
     * @param info The DTMF tone to send.
     */
    private synchronized void startSendingDtmfTone(DTMFToneInfo info)
    {
        AudioNotifierService audioNotifier = GuiActivator.getAudioNotifier();

        if(info.sound != null)
        {
            if(currentlyPlayingAudio != null)
                currentlyPlayingAudio.stop();

            currentlyPlayingAudio =
                audioNotifier.createAudio(info.sound);

            // some little silence, must have a non-zero or it won't loop
            if(currentlyPlayingAudio != null)
                currentlyPlayingAudio.playInLoop(10);
        }

        if (callContainer != null)
            startSendingDtmfTone(
                callContainer.getCurrentCallRenderer().getCall(),
                info);
        else
        {
            Collection<Call> activeCalls = CallManager.getActiveCalls();

            if (activeCalls != null)
            {
                Iterator<Call> callsIter = activeCalls.iterator();

                while (callsIter.hasNext())
                {
                    startSendingDtmfTone(callsIter.next(), info);
                }
            }
        }
    }

    /**
     * Sends a DTMF tone to the current DTMF operation set of the given call.
     *
     * @param call The call to which we send DTMF-s.
     * @param info The DTMF tone to send.
     */
    private void startSendingDtmfTone(Call call, DTMFToneInfo info)
    {
        Iterator<? extends CallPeer> callPeers = call.getCallPeers();

        try
        {
            while (callPeers.hasNext())
            {
                CallPeer peer = callPeers.next();
                OperationSetDTMF dtmfOpSet
                    = peer.getProtocolProvider()
                        .getOperationSet(OperationSetDTMF.class);

                if (dtmfOpSet != null)
                {
                    dtmfOpSet.startSendingDTMF(peer, info.tone);

                    CallPeerRenderer peerRenderer
                        = callContainer.getCurrentCallRenderer()
                            .getCallPeerRenderer(peer);
                    if (peerRenderer != null)
                        peerRenderer.printDTMFTone(info.keyChar);
                }
            }
        }
        catch (Throwable e1)
        {
            logger.error("Failed to send a DTMF tone.", e1);
        }
    }

    /**
     * Stop sending DTMF tone.
     */
    public synchronized void stopSendingDtmfTone()
    {
        if(currentlyPlayingAudio != null)
            currentlyPlayingAudio.stop();

        currentlyPlayingAudio = null;

        if (callContainer != null)
            stopSendingDtmfTone(
                callContainer.getCurrentCallRenderer().getCall());
        else
        {
            Collection<Call> activeCalls = CallManager.getActiveCalls();

            if (activeCalls != null)
            {
                Iterator<Call> callsIter = activeCalls.iterator();

                while (callsIter.hasNext())
                {
                    stopSendingDtmfTone(callsIter.next());
                }
            }
        }
    }

    /**
     * Stops sending DTMF tone to the given call.
     *
     * @param call The call to which we send DTMF-s.
     */
    private void stopSendingDtmfTone(Call call)
    {
        Iterator<? extends CallPeer> callPeers = call.getCallPeers();

        try
        {
            while (callPeers.hasNext())
            {
                CallPeer peer = callPeers.next();
                OperationSetDTMF dtmfOpSet
                    = peer
                        .getProtocolProvider()
                            .getOperationSet(OperationSetDTMF.class);

                if (dtmfOpSet != null)
                    dtmfOpSet.stopSendingDTMF(peer);
            }
        }
        catch (Throwable e1)
        {
            logger.error("Failed to send a DTMF tone.", e1);
        }
    }

    /**
     * DTMF extended information.
     */
    public static class DTMFToneInfo
    {
        /**
         * The tone itself
         */
        public DTMFTone tone;

        /**
         * The key code when entered from keyboard.
         */
        public int keyCode;

        /**
         * The char associated with this DTMF tone.
         */
        public char keyChar;

        /**
         * The image to display in buttons sending DTMFs.
         */
        public ImageID imageID;

        /**
         * The image to display on Mac buttons.
         */
        public ImageID macImageID;

        /**
         * The id of the image to display on Mac buttons on rollover.
         */
        public ImageID macImageRolloverID;

        /**
         * The sound to play during send of this tone.
         */
        public String sound;

        /**
         * Creates DTMF extended info.
         * @param tone the tone.
         * @param keyCode its key code.
         * @param keyChar the char associated with the DTMF
         * @param imageID the image if any.
         * @param macImageID the Mac image if any.
         * @param sound the sound if any.
         */
        public DTMFToneInfo(
            DTMFTone tone, int keyCode, char keyChar,
            ImageID imageID, ImageID macImageID,
            ImageID macImageRolloverID, String sound)
        {
            this.tone = tone;
            this.keyCode = keyCode;
            this.keyChar = keyChar;
            this.imageID = imageID;
            this.macImageID = macImageID;
            this.macImageRolloverID = macImageRolloverID;
            this.sound = sound;
        }
    }
}
