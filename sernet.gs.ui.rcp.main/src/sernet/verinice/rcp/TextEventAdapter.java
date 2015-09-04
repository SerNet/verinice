package sernet.verinice.rcp;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

public abstract class TextEventAdapter implements KeyListener, FocusListener {

    /**
     * Sent when a key is pressed on the system keyboard.
     *
     * @param e
     *            an event containing information about the key press
     */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Sent when a key is released on the system keyboard.
     *
     * @param e
     *            an event containing information about the key release
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Sent when a control gets focus.
     *
     * @param e
     *            an event containing information about the focus change
     */
    @Override
    public void focusGained(FocusEvent e) {
    }

    /**
     * Sent when a control loses focus.
     *
     * @param e
     *            an event containing information about the focus change
     */
    @Override
    public void focusLost(FocusEvent e) {

    }

}
