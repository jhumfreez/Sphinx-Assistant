/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String DATE_SEARCH = "date";
    private static final String TIME_SEARCH = "time";
    private static final String LM_SEARCH = "language";
    private static Model mod = new Model();

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
    private SpeechRecognizer recognizer;

    // Proceed through event set up
    private int ticker = 0;
    private boolean listening = false;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        // Check read calendar permissions
        int checkr = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR);
        if (checkr != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, PERMISSIONS_REQUEST_READ_CALENDAR);
            return;
        }
        // Check write calendar permissions
        int checkw = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR);
        if (checkw != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSIONS_REQUEST_WRITE_CALENDAR);
            return;
        }

        //Handle talk button click
        final Button button_talk = findViewById(R.id.button_talk);
        button_talk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recognizer.stop();

                if (ticker == 0){
                    recognizer.startListening(LM_SEARCH);
                    ((TextView) findViewById(R.id.caption_text)).setText("");
                }
                else if (ticker == 1)
                    recognizer.startListening(DATE_SEARCH);
                else if (ticker == 2)
                    recognizer.startListening(TIME_SEARCH);
                else
                    recognizer.startListening(KWS_SEARCH);
                listening = true;
            }
        });

        //Handle confirm button click
        final Button button_confirm = findViewById(R.id.button_confirm);
        button_confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recognizer.stop();
                ticker++;

                // Push event
                if (ticker > 2) {
                    String title = ((TextView) findViewById(R.id.textView_title)).getText().toString();
                    String date = ((TextView) findViewById(R.id.textView_date)).getText().toString();
                    String time = ((TextView) findViewById(R.id.textView_time)).getText().toString();
                    //check for bad input
                    if (date.equals(" ")|| time.equals(" ") ||
                            date.equals(R.string.date)|| time.equals(R.string.time)||
                            date.equals("wakeup") || time.equals("wakeup")) {
                        ((TextView) findViewById(R.id.caption_text)).setText(R.string.invalid_general);
                    }else{
                        int response = mod.createEvent(title, date, time);
                        if (response == 1) { // Success
                            mod.pushEvent(getApplicationContext());
                        }else if(response == -1) // Error
                            ((TextView) findViewById(R.id.caption_text)).setText(R.string.invalid_general);
                        else
                            ((TextView) findViewById(R.id.caption_text)).setText(R.string.invalid_date);
                    }

                    ResetView();
                }
            }
        });

        // Handle cancel button click
        final Button button_cancel = findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recognizer.stop();
                ResetView();
            }
        });

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }
    // Event is presumed pushed or cancelled -> clear the screen
    private void ResetView() {
        ticker = 0;  // Code here executes on main thread after user presses button
        ((TextView) findViewById(R.id.textView_title)).setText(R.string.title);
        ((TextView) findViewById(R.id.textView_date)).setText(R.string.date);
        ((TextView) findViewById(R.id.textView_time)).setText(R.string.time);
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<PocketSphinxActivity> activityReference;

        SetupTask(PocketSphinxActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText("Failed to init recognizer " + result);
            } else {
                activityReference.get().Search(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

        /**
         * In partial result we get quick updates about current hypothesis. In
         * keyword spotting mode we can react here, in other modes we need to wait
         * for final result in onResult.
         */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        if (ticker == 0)
            Search(LM_SEARCH);
        else if (ticker == 1)
            Search(DATE_SEARCH);
        else if (ticker == 2)
            Search(TIME_SEARCH);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");//default
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            //prints the hypothesis in black bubble
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

            if (ticker == 0)
                ((TextView) findViewById(R.id.textView_title)).setText(text);//Search(text);
            else if (ticker == 1){
                //int[] d = mod.parseDate(text);
                ((TextView) findViewById(R.id.textView_date)).setText(text);//d[0] +" "+ d[1]);
                }
            else if (ticker == 2){
                //int[] t = mod.parseTime(text);
                ((TextView) findViewById(R.id.textView_time)).setText(text);//t[0]+" "+ t[1]);
                }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        listening = false;

        if (ticker == 0 && !recognizer.getSearchName().equals(LM_SEARCH))
            Search(LM_SEARCH);
        else if (ticker == 1 && !recognizer.getSearchName().equals(DATE_SEARCH))
            Search(DATE_SEARCH);
        else if (ticker == 2 && !recognizer.getSearchName().equals(TIME_SEARCH))
            Search(TIME_SEARCH);
        else if (!recognizer.getSearchName().equals(KWS_SEARCH))
            Search(KWS_SEARCH);
    }

    // Acquire speech result
    private void Search(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH)||searchName.equals(DATE_SEARCH)||searchName.equals(LM_SEARCH)||searchName.equals(TIME_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
        ((TextView) findViewById(R.id.caption_text)).setText("");

    }

    // Sets up the speech recognizer
    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        // asset designation
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-adapt"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KWS_SEARCH);// KEYPHRASE);
        // Create grammar-based search for date recognition
        File dateGrammar = new File(assetsDir, "dates.gram");
        recognizer.addGrammarSearch(DATE_SEARCH, dateGrammar);
        // Create grammar-based search for minutes recognition
        File minutesGrammar = new File(assetsDir, "time.gram");
        recognizer.addGrammarSearch(TIME_SEARCH, minutesGrammar);
        // Create language model search
         File languageModel = new File(assetsDir, "en-us.lm.bin");
         recognizer.addNgramSearch(LM_SEARCH, languageModel);


    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        if (ticker == 0)
            Search(LM_SEARCH);
        else if (ticker == 1)
            Search(DATE_SEARCH);
        else if (ticker == 2)
            Search(TIME_SEARCH);
        else
        Search(KWS_SEARCH);
    }

}
