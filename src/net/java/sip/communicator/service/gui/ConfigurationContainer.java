package net.java.sip.communicator.service.gui;

public interface ConfigurationContainer
{
    /**
     * Shows or hides this configuration container depending on the value of
     * parameter <code>visible</code>.
     *
     * @param visible if <code>true</code>, shows the main application window;
     *            otherwise, hides the main application window.
     */
    public void setVisible(boolean visible);

    /**
     * Selects the given <tt>ConfigurationForm</tt> if it exists in this
     * container.
     *
     * @param configForm the <tt>ConfigurationForm</tt> to select
     */
    public void setSelected(ConfigurationForm configForm);
}
