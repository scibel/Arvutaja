package ee.ioc.phon.android.arvutaja;

/*
 * Copyright 2011-2016, Institute of Cybernetics at Tallinn University of Technology
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
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.List;

import ee.ioc.phon.android.speechutils.AudioCue;
import ee.ioc.phon.android.speechutils.Extras;
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;

/**
 * @author Kaarel Kaljurand
 */
public abstract class AbstractRecognizerActivity extends Activity {

    /**
     * Launches an activity which the user probably does not want to see
     * if he presses HOME while in this activity and then starts Arvutaja again
     * from the launcher.
     * <p/>
     * TODO: Note that activities cannot be launched when the app was launched via voice interaction.
     * We get the exception:
     * java.lang.SecurityException: Starting under voice control not allowed for:
     * Intent { act=android.intent.action.VIEW dat=http://maps.google.com/... flg=0x80000 }
     */
    protected void startForeignActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // If in split-screen mode, launch the foreign activity into the 2nd screen.
            // TODO: not sure this has any effect,
            // the launched activity ends up in the 2nd screen anyway.
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            // TODO: localize the error message
            toast(e.getMessage());
        }
    }


    protected List<ResolveInfo> getIntentActivities(Intent intent) {
        PackageManager pm = getPackageManager();
        return pm.queryIntentActivities(intent, 0);
    }


    protected void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    protected void showError(int msg) {
        toast(getString(msg));
    }

    /**
     * If the caller defines the EXTRA_LANGUAGE, then use that language.
     * Otherwise take the language from the Language-setting.
     */
    protected String getLang(SharedPreferences prefs, Resources res) {
        Intent intent = getIntent();
        if (intent != null) {
            String lang = intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE);
            if (lang != null) {
                return lang;
            }
        }
        return PreferenceUtils.getPrefString(prefs, res, R.string.keyLanguage, R.string.defaultLanguage);
    }

    /**
     * If the caller defines the EXTRA_AUDIO_CUES, then if it is boolean and true then play the audio
     * cues.
     * Otherwise play the audio cues if the AudioCues-setting is true.
     * TODO: future work: just pass this EXTRA on to the recognizer engine (i.e. do not play cues in Arvutaja)
     */
    protected AudioCue createAudioCue(SharedPreferences prefs, Resources res) {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Extras.EXTRA_AUDIO_CUES)) {
            if (intent.getBooleanExtra(Extras.EXTRA_AUDIO_CUES, false)) {
                return new AudioCue(this);
            }
            return null;
        }

        if (PreferenceUtils.getPrefBoolean(prefs, res, R.string.keyAudioCues, R.bool.defaultAudioCues)) {
            return new AudioCue(this);
        }
        return null;
    }


    protected boolean useExternalEvaluator(SharedPreferences prefs, Resources res) {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Extras.EXTRA_USE_EXTERNAL_EVALUATOR)) {
            return intent.getBooleanExtra(Extras.EXTRA_USE_EXTERNAL_EVALUATOR, false);
        }
        return PreferenceUtils.getPrefBoolean(prefs, res, R.string.keyUseExternalEvaluator, R.bool.defaultUseExternalEvaluator);
    }
}