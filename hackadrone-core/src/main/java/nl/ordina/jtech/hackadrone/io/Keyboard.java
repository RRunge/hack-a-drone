/*
 * Copyright (C) 2017 Ordina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.ordina.jtech.hackadrone.io;

import nl.ordina.jtech.hackadrone.models.Command;

import java.awt.*;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

/**
 * Class representing the keyboard controls for a drone.
 *
 * @author Nils Berlijn
 * @version 1.0
 * @since 1.0
 */
public final class Keyboard implements Device, KeyEventDispatcher {

    /**
     * They keyboard focus manager.
     */
    private final KeyboardFocusManager focusManager;

    /**
     * The command listener.
     */
    private CommandListener commandListener;

    /**
     * The command.
     */
    private Command command = new Command();

    /**
     * A keyboard constructor.
     *
     * @param focusManager the focus manager
     */
    public Keyboard(KeyboardFocusManager focusManager) {
        this.focusManager = focusManager;
    }

    /**
     * Starts the keyboard.
     */
    @Override
    public void start() {
        focusManager.addKeyEventDispatcher(this);
    }

    /**
     * Stops the keyboard.
     */
    @Override
    public void stop() {
        focusManager.removeKeyEventDispatcher(this);
    }

    /**
     * Sets the listener.
     *
     * @param commandListener the command listener to set
     */
    @Override
    public void setListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    /**
     * Dispatches a key event.
     *
     * @param e the key event
     * @return the status of the key event
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KEY_PRESSED) {
            onKeyEvent(e, true);
        } else if (e.getID() == KEY_RELEASED) {
            onKeyEvent(e, false);
        }

        return false;
    }

    /**
     * Handles a key event.
     *
     * @param keyEvent the key event to handle
     * @param isPressed if the key is pressed or not
     */
    private void onKeyEvent(KeyEvent keyEvent, boolean isPressed) {
        int value = isPressed ? 127 : 0;
        boolean input = true;

        switch (keyEvent.getKeyCode()) {
            case VK_W:
                command.setPitch(value);
                break;
            case VK_S:
                command.setPitch(-value);
                break;
            case VK_A:
                command.setRoll(-value);
                break;
            case VK_D:
                command.setRoll(value);
                break;
            case VK_Q:
                command.setYaw(-value);
                break;
            case VK_E:
                command.setYaw(value);
                break;
            case VK_LEFT:
                command.setTakeOff(isPressed);
                break;
            case VK_RIGHT:
                command.setLand(isPressed);
                break;
            case VK_UP:
                command.setThrottle(value);
                break;
            case VK_DOWN:
                command.setThrottle(-value);
                break;
            default:
                input = false;
        }

        if (commandListener != null && input) {
            commandListener.onCommandReceived(command);
        }

        keyEvent.consume();
    }

}
