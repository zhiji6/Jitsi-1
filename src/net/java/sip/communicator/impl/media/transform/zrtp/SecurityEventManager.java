/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.media.transform.zrtp;

import gnu.java.zrtp.*;

import java.util.*;

import net.java.sip.communicator.impl.media.*;
import net.java.sip.communicator.service.media.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * The user callback class for ZRTP4J.
 * 
 * This class constructs and sends events to the ZRTP GUI implementation. The
 * <code>showMessage()<code> function implements a specific check to start 
 * associated ZRTP multi-stream sessions.
 * 
 * Coordinate this callback class with the associated GUI implementation class
 * net.java.sip.communicator.impl.gui.main.call.ZrtpPanel
 *
 * @see net.java.sip.communicator.impl.gui.main.call.SecurityPanel
 *
 * @author Emanuel Onica
 * @author Werner Dittmann
 * @author Yana Stamcheva
 */
public class SecurityEventManager extends ZrtpUserCallback
{
    private static final Logger logger
        = Logger.getLogger(SecurityEventManager.class);

    public static final String WARNING_NO_RS_MATCH = MediaActivator
            .getResources().getI18NString(
                    "impl.media.security.WARNING_NO_RS_MATCH");

    public static final String WARNING_NO_EXPECTED_RS_MATCH = MediaActivator
            .getResources().getI18NString(
                    "impl.media.security.WARNING_NO_EXPECTED_RS_MATCH");

    private CallParticipant callParticipant;

    private final CallSession callSession;

    /**
     * Is this a ZRTP DH (Master) session?
     */
    private boolean isDHSession = false;

    /**
     * Type of session
     */
    private int sessionType;

    /**
     * SAS string.
     */
    private String sas;

    /**
     * Cipher.
     */
    private String cipher;

    /**
     * Indicates if the SAS has already been verified in a previous session.
     */
    private boolean isSasVerified;

    /**
     * The class constructor.
     */
    public SecurityEventManager(CallSession callSession)
    {
        this.callSession = callSession;

        // At this moment we're supporting a security call between only two
        // participants. In the future the call participant would be passed
        // as a parameter to the SecurityEventManager.
        Iterator<CallParticipant> callParticipants
            = callSession.getCall().getCallParticipants();

        while (callParticipants.hasNext())
        {
            this.callParticipant = callParticipants.next();
        }
    }

    /**
     * Set the type of this session.
     * 
     * @param type the session type. The session type could be either
     * CallSessionImpl.AUDIO_SESSION or CallSessionImpl.VIDEO_SESSION.
     */
    public void setSessionType(String type)
    {
        if (type.equals(CallSessionImpl.AUDIO_SESSION))
            sessionType = CallParticipantSecurityStatusEvent.AUDIO_SESSION;
        else if (type.equals(CallSessionImpl.VIDEO_SESSION))
            sessionType = CallParticipantSecurityStatusEvent.VIDEO_SESSION;
    }

    /**
     * Set the DH session flag.
     * 
     * @param isDHSession the DH session flag.
     */
    public void setDHSession(boolean isDHSession)
    {
        this.isDHSession = isDHSession;
    }

    /*
     * The following methods implement the ZrtpUserCallback interface
     */

    /**
     * Reports the security algorithm that the ZRTP protocol negotiated.
     * 
     * @see gnu.java.zrtp.ZrtpUserCallback#secureOn(java.lang.String)
     */
    public void secureOn(String cipher)
    {
        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType) + ": cipher enabled: "
                + cipher);

        this.cipher = cipher;
    }

    /**
     * ZRTP computes the SAS string after nearly all the negotiation
     * and computations are done internally.
     * 
     * @see gnu.java.zrtp.ZrtpUserCallback#showSAS(java.lang.String, boolean)
     */
    public void showSAS(String sas, boolean isVerified)
    {
        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType) + ": SAS is: " + sas);

        this.sas = sas;
        this.isSasVerified = isVerified;
    }

    /**
     * @see gnu.java.zrtp.ZrtpUserCallback#showMessage(
     * gnu.java.zrtp.ZrtpCodes.MessageSeverity, java.util.EnumSet)
     */
    public void showMessage(ZrtpCodes.MessageSeverity sev,
                            EnumSet<?> subCode)
    {
        int multiStreams = 0;

        Iterator<?> ii = subCode.iterator();
        Object msgCode = ii.next();

        String messageType = null;
        String i18nMessage = null;
        int severity = 0;
        boolean sendEvent = true;
        
        if (msgCode instanceof ZrtpCodes.InfoCodes)
        {
            ZrtpCodes.InfoCodes inf = (ZrtpCodes.InfoCodes) msgCode;
            
            /*
             * Use the following fields if INFORMATION type messages shall be
             * shown to the user via SecurityMessageEvent, i.e. if
             * sendEvent is set to true 
            severity = CallParticipantSecurityMessageEvent.INFORMATION;
            messageType = MediaActivator.getResources().getI18NString(
            "impl.media.security.INFO");
            */
            
            // Don't spam user with ino messages, only internal processing or logging
            sendEvent = false;

            // If the ZRTP Master session (DH mode) signals "security on"
            // then start multi-stream sessions.
            // Signal SAS to GUI only if this is a DH mode session.
            // Multi-stream session don't have own SAS data
            if (inf == ZrtpCodes.InfoCodes.InfoSecureStateOn)
            {
                if (isDHSession)
                {
                    multiStreams = ((CallSessionImpl) callSession)
                            .startZrtpMultiStreams();

                    ((AbstractCallParticipant) callParticipant)
                        .setSecurityOn( sessionType, cipher,
                                        sas, isSasVerified);
                }
                else 
                {
                    ((AbstractCallParticipant) callParticipant)
                        .setSecurityOn(sessionType, cipher, null, false);
                }
            }
        }
        else if (msgCode instanceof ZrtpCodes.WarningCodes)
        {
            // Warning codes usually do not affect encryption or security. Only
            // in few cases inform the user and ask to verify SAS.
            ZrtpCodes.WarningCodes warn = (ZrtpCodes.WarningCodes) msgCode;
            severity = CallParticipantSecurityMessageEvent.WARNING;
            messageType = MediaActivator.getResources().getI18NString(
                    "impl.media.security.WARNING");
            
            if (warn == ZrtpCodes.WarningCodes.WarningNoRSMatch)
            {
                i18nMessage = WARNING_NO_RS_MATCH;
            }
            else if (warn == ZrtpCodes.WarningCodes.WarningNoExpectedRSMatch)
            {
                i18nMessage = WARNING_NO_EXPECTED_RS_MATCH;
            }
            else if (warn == ZrtpCodes.WarningCodes.WarningCRCmismatch)
            {
                i18nMessage = MediaActivator.getResources().getI18NString(
                    "impl.media.security.CHECKSUM_MISMATCH");
            }
            else 
            {
                // Other warnings are  internal only, no user action requied
                sendEvent = false;
            }
        }
        else if (msgCode instanceof ZrtpCodes.SevereCodes)
        {
            ZrtpCodes.SevereCodes severe = (ZrtpCodes.SevereCodes) msgCode;
            severity = CallParticipantSecurityMessageEvent.SEVERE;
            messageType = MediaActivator.getResources().getI18NString(
                    "impl.media.security.SEVERE");

            if (severe == ZrtpCodes.SevereCodes.SevereCannotSend)
            {
                i18nMessage = MediaActivator.getResources().getI18NString(
                    "impl.media.security.DATA_SEND_FAILED",
                    new String[]{msgCode.toString()});
            }
            else if (severe == ZrtpCodes.SevereCodes.SevereTooMuchRetries)
            {
                i18nMessage = MediaActivator.getResources().getI18NString(
                    "impl.media.security.RETRY_RATE_EXCEEDED",
                    new String[]{msgCode.toString()});
            }
            else if (severe == ZrtpCodes.SevereCodes.SevereProtocolError)
            {
                i18nMessage = MediaActivator.getResources().getI18NString(
                    "impl.media.security.INTERNAL_PROTOCOL_ERROR",
                    new String[]{msgCode.toString()});
            }
            else
            {
                i18nMessage =  MediaActivator.getResources().getI18NString(
                    "impl.media.security.ZRTP_GENERIC_MSG",
                    new String[]{msgCode.toString()});
            }
        }
        else if (msgCode instanceof ZrtpCodes.ZrtpErrorCodes)
        {
            severity = CallParticipantSecurityMessageEvent.ZRTP;
            messageType = MediaActivator.getResources().getI18NString(
                    "impl.media.security.ZRTP");

            i18nMessage =  MediaActivator.getResources().getI18NString(
                "impl.media.security.ZRTP_GENERIC_MSG",
                new String[]{msgCode.toString()});
        }

        if (sendEvent)
            ((AbstractCallParticipant) callParticipant)
                .setSecurityMessage(messageType, i18nMessage, severity);

        if (logger.isInfoEnabled())
        {
            logger.info(sessionTypeToString(sessionType) + ": "
                + "ZRTP message: severity: " + sev + ", sub code: " + msgCode
                + ", DH session: " + isDHSession + ", multi: " + multiStreams);
        }
    }

    /**
     * @see gnu.java.zrtp.ZrtpUserCallback#zrtpNegotiationFailed(
     * gnu.java.zrtp.ZrtpCodes.MessageSeverity, java.util.EnumSet)
     */
    public void zrtpNegotiationFailed(  ZrtpCodes.MessageSeverity severity,
                                        EnumSet<?> subCode)
    {
        Iterator<?> ii = subCode.iterator();
        Object msgCode = ii.next();

        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType)
                + ": ZRTP key negotiation failed, sub code: " + msgCode);
    }

    /**
     * @see gnu.java.zrtp.ZrtpUserCallback#secureOff()
     */
    public void secureOff()
    {
        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType) + ": Security off");

        // If this event has been triggered because of a call end event and the
        // call is already ended we don't need to alert the user for
        // security off.
        if (callParticipant.getCall() != null
            && !callParticipant.getCall().getCallState()
                .equals(CallState.CALL_ENDED))
        {
            ((AbstractCallParticipant) callParticipant)
                .setSecurityOff(sessionType);
        }
    }

    /**
     * @see gnu.java.zrtp.ZrtpUserCallback#zrtpNotSuppOther()
     */
    public void zrtpNotSuppOther()
    {
        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType)
                + ": Other party does not support ZRTP key negotiation protocol,"
                + " no secure calls possible.");
    }

    /**
     * @see gnu.java.zrtp.ZrtpUserCallback#confirmGoClear()
     */
    public void confirmGoClear()
    {
        if (logger.isInfoEnabled())
            logger.info(sessionTypeToString(sessionType)
                + ": GoClear confirmation requested.");
    }

    private String sessionTypeToString(int sessionType)
    {
        switch (sessionType)
        {
        case CallParticipantSecurityStatusEvent.AUDIO_SESSION:
            return "AUDIO_SESSION";
        case CallParticipantSecurityStatusEvent.VIDEO_SESSION:
            return "VIDEO_SESSION";
        default:
            throw new IllegalArgumentException("sessionType");
        }
    }
}
