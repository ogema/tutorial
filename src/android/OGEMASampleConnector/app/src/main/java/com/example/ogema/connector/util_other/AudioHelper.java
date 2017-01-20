package com.example.ogema.connector.util_other;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Util methods for controlling acoustic alarm
 */

public class AudioHelper {
    private static Ringtone ringtone = null;
    private static MediaPlayer mp = null;
    private static boolean ringToneActivated = false;
    /** Toggle between on and off of ringtone. Note that this helper performs an "on" operation first,
     * an "off" operation next etc. Switching the ringtone from other sources than this class
     * considered
     * @return status of ringtone after operation
     */
    public static boolean toggleRingTone(ContextWrapper contextWrapper) {
        setRingtone(!ringToneActivated, contextWrapper);
        return ringToneActivated;
    }

    /**Set ringtone to on or off
     *
     * @param setOn if true the ringtone will be switched on, otherwise switched off
     * @param contextWrapper
     */
    public static void setRingtone(boolean setOn, ContextWrapper contextWrapper) {
        //ring phone
        if(ringtone == null) {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if ((alert == null)) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            ringtone = RingtoneManager.getRingtone(contextWrapper.getApplicationContext(), alert);

            MediaPlayer mp = MediaPlayer.create(contextWrapper.getApplicationContext(), alert);
            //ringtone.play();
        }
        if (setOn) {
            AudioManager amanager = (AudioManager) contextWrapper.getSystemService(Context.AUDIO_SERVICE);
            amanager.setStreamVolume(AudioManager.STREAM_RING, amanager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            ringtone.play();
            if(mp!= null) mp.start();
            ringToneActivated = true;
        } else {
            ringtone.stop();
            if(mp!= null) mp.stop();
            ringToneActivated = false;
        }
    }
}
