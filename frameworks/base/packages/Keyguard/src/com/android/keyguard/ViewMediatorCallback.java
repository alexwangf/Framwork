/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.android.keyguard;

import android.os.Bundle;

/**
 * The callback used by the keyguard view to tell the {@link KeyguardViewMediator}
 * various things.
 */
public interface ViewMediatorCallback {
    /**
     * Reports user activity and requests that the screen stay on.
     */
    void userActivity();

    /**
     * Report that the keyguard is done.
     * @param authenticated Whether the user securely got past the keyguard.
     *   the only reason for this to be false is if the keyguard was instructed
     *   to appear temporarily to verify the user is supposed to get past the
     *   keyguard, and the user fails to do so.
     */
    void keyguardDone(boolean authenticated);

    /**
     * Report that the keyguard is done drawing.
     */
    void keyguardDoneDrawing();

    /**
     * Tell ViewMediator that the current view needs IME input
     * @param needsInput
     */
    void setNeedsInput(boolean needsInput);

    /**
     * Tell view mediator that the keyguard view's desired user activity timeout
     * has changed and needs to be reapplied to the window.
     */
    void onUserActivityTimeoutChanged();

    /**
     * Report that the keyguard is dismissable, pending the next keyguardDone call.
     */
    void keyguardDonePending();

    /**
     * Report when keyguard is actually gone
     */
    void keyguardGone();

    /**
     * Report when the UI is ready for dismissing the whole Keyguard.
     */
    void readyForKeyguardDone();

    /**
     * Play the "device trusted" sound.
     */
    void playTrustedSound();

    /**
     * Return is keyguard showing or not.
     * @return showing or not
     */
    boolean isShowing() ;

    /**
     * Ask to show keyguard.
     * @param options bring some info
     */
    void showLocked(Bundle options);

    /**
     * Ask to reset lock view.
     */
    void resetStateLocked();

    /**
     * Ask to dismiss keyguard.
     */
    void dismiss();

    /**
     * Ask to dismiss keyguard with authentication info.
     * @param authenticated verified or not.
     */
    void dismiss(boolean authenticated);

    /**
     * Ask to adjust status bar.
     */
    void adjustStatusBarLocked() ;

    /**
     * Added for VOW. Return is keyguard is enabled externally.
     * @return externally enabled or not.
     */
    boolean isKeyguardExternallyEnabled();

    /**
     * Ask to hide keyguard.
     */
    void hideLocked();

    /**
     * Return keyguard is secure or not.
     * @return keyguard is secure or not.
     */
    boolean isSecure();

    /**
     * Ask to supress keyguard lock/unlock sound.
     */
    void setSuppressPlaySoundFlag();

    /**
     * M: Force to update navi-bar status.
     */
    void updateNavbarStatus() ;

}
