/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.yahoo;

import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomInvitation;

/**
 * The Yahoo implementation of the <tt>ChatRoomInvitation</tt> interface.
 * 
 * @author Rupert Burchardi
 */

public class ChatRoomInvitationYahooImpl implements ChatRoomInvitation
{
   /**
    * Corresponding chat room instance.
    */
   private ChatRoom chatRoom;
   /**
    * The name of the inviter
    */
   private String inviter;
   /**
    * The invitation reason. Note: Not supported in the msn protocol.
    */
   private String reason;

   /**
    * The password. Note: Not supported in the msn protocol.
    */
   private byte[] password;

   /**
    * Creates an instance of the <tt>ChatRoomInvitationMsnImpl</tt> by
    * specifying the targetChatRoom, the inviter, the reason and the password.
    * 
    * @param targetChatRoom The <tt>ChatRoom</tt> for which the invitation is
    * @param inviter The <tt>ChatRoomMember</tt>, which sent the invitation
    * @param reason The Reason for the invitation
    * @param password The password
    */
   public ChatRoomInvitationYahooImpl( ChatRoom targetChatRoom,
                                       String inviter,
                                       String reason,
                                       byte[] password)
   {
       this.chatRoom = targetChatRoom;
       this.inviter = inviter;
       this.reason = reason;
       this.password = password;
   }

   /**
    * Returns the corresponding chat room.
    * @return The chat room
    */
   public ChatRoom getTargetChatRoom()
   {
       return chatRoom;
   }

   /**
    * Returns the corresponding inviter.
    * @return The name of the inviter
    */
   public String getInviter()
   {
       return inviter;
   }

   /**
    * Returns the invitation reason.
    * @return the invitation reason
    */
   public String getReason()
   {
       return reason;
   }
   /**
    * Returns the password of the chat room.
    * @return The password
    */
   public byte[] getChatRoomPassword()
   {
       return password;
   }
}
