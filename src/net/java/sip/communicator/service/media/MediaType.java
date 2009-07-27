/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.media;

/**
 * The <tt>MediaType</tt> enumeration contains a list of media types
 * currently known to and handled by the MediaService.
 *
 * @author Emil Ivov
 */
public enum MediaType
{
    /**
     * Represents an AUDIO media type.
     */
    AUDIO("audio"),

    /**
     * Represents a VIDEO media type.
     */
    VIDEO("video");

    /**
     * The name of this MediaType.
     */
    private String mediaTypeName = null;

    /**
     * Creates a <tt>MediaType</tt> instance with the specified name.
     *
     * @param mediaTypeName the name of the <tt>MediaType</tt> we'd like to
     * create.
     */
    private MediaType(String mediaTypeName)
    {
        this.mediaTypeName = mediaTypeName;
    }

    /**
     * Returns the name of this MediaType (e.g. "audio" or "video"). The name
     * returned by this method is meant for use by session description
     * mechanisms such as SIP/SDP or XMPP/Jingle.
     *
     * @return the name of this MediaType (e.g. "audio" or "video").
     */
    public String toString()
    {
        return mediaTypeName;
    }

    /**
     * Returns a <tt>MediaType</tt> value corresponding to the specified
     * <tt>mediaTypeName</tt> or in other words <tt>MediaType.AUDIO</tt> for
     * "audio" and <tt>MediaType.VIDEO</tt> for "video".
     *
     * @param mediaTypeName the name that we'd like to parse.
     * @return a <tt>MediaType</tt> value corresponding to the specified
     * <tt>mediaTypeName</tt>.
     *
     * @throws a <tt>java.lang.IllegalArgumentException</tt> in case
     * <tt>mediaTypeName</tt> is not a valid or currently supported media type.
     */
    public static MediaType parseString(String mediaTypeName)
        throws IllegalArgumentException
    {
        if(AUDIO.toString().equals(mediaTypeName))
            return MediaType.AUDIO;

        if(VIDEO.toString().equals(mediaTypeName))
            return MediaType.VIDEO;

        throw new IllegalArgumentException(
            mediaTypeName + " is not a currently supported MediaType");
    }
}