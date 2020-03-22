package com.example.voicedictionary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class dictionary extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private SQLiteHelper helper;
    private SQLiteDatabase db;
    private TextToSpeech textToSpeech;
    SharedPreferences pref;
    HashMap<String,String> words,speech1;
    SharedPreferences.Editor editor;
    Button button;
    ProgressBar progressBar;
    TextView tv;
    Timer timer;
    TimerTask delayedThreadStartTask1;
    private TextToSpeech tts;
    private RequestQueue queue;
    String url,def="",speech="";
    boolean online=true;
    TimerTask delayedThreadStartTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        pref = getApplicationContext().getSharedPreferences("word", 0); // 0 - for private mode
        editor = pref.edit();
        words=new HashMap<String, String>();
        speech1=new HashMap<String, String>();
        tv=findViewById(R.id.tv);
        tts = new TextToSpeech(this, this);
        queue = Volley.newRequestQueue(this);
        helper=new SQLiteHelper(this);
        db=helper.getReadableDatabase();
        get_words();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = findViewById(R.id.button);
        new dictionary.RetrieveFeedTask().execute();
        timer = new Timer();
        if(words.get(pref.getString("word","").toUpperCase())!=null){
            if(!words.get(pref.getString("word","").toUpperCase()).equals(""))
            online=false;
        }

        delayedThreadStartTask = new TimerTask() {
            @Override
            public void run() {

                //captureCDRProcess();
                //moved to TimerTask
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        speakOut(speech);
                    }
                }).start();
            }
        };





        delayedThreadStartTask1 = new TimerTask() {
            @Override
            public void run() {

                //captureCDRProcess();
                //moved to TimerTask
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                       finish();

                    }
                }).start();
            }
        };


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOut(speech);
            }
        });

        tts.setSpeechRate(0.5f);


    }

    private void get_words() {
        Cursor c = helper.get_words(db);
        while (c.moveToNext()) {
            words.put(c.getString(0),c.getString(1));
            speech1.put(c.getString(0),c.getString(2));

        }
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_SHORT).show();
            } else {
                button.setEnabled(true);
            }

        } else {
            Toast.makeText(getApplicationContext(), "Init failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut(String speech) {


        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {


            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                timer.schedule(delayedThreadStartTask1, 10 * 1000);
            }

            @Override
            public void onError(String s) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

        String text = speech;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "Dummy String");
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public void back(View view) {
      finish();

    }

    public void pause(View view) {
        if (tts != null) {
            tts.stop();

        }
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            tv.setVisibility(View.INVISIBLE);

        }

        protected String doInBackground(Void... urls) {

            // Do some validation here
          if(online) {
              try {
                  editor.putString("def", "");
                  editor.commit();
                  String word = pref.getString("word", null);
                  def = def + "Word : " + word;
                  speech = speech + "Definition of " + word + " ";
                  URL url = new URL("https://api.dictionaryapi.dev/api/v1/entries/en/" + word);
                  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                  try {
                      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                      StringBuilder stringBuilder = new StringBuilder();
                      String line;
                      while ((line = bufferedReader.readLine()) != null) {
                          stringBuilder.append(line).append("\n");
                      }
                      bufferedReader.close();
                      return stringBuilder.toString();
                  } finally {
                      urlConnection.disconnect();
                  }
              } catch (Exception e) {
                  Log.e("ERROR", e.getMessage(), e);

              }
          }
            return null;
        }

        protected void onPostExecute(String response) {

        if(online) {
            if (response == null) {
                response = "Can't  find meaning";
            }
            progressBar.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            Log.i("INFO", response);
            tv.setText(response);
            // TODO: check this.exception
            // TODO: do something with the feed

            try {
                JSONArray array = (JSONArray) new JSONTokener(response).nextValue();

                for (int n = 0; n < array.length(); n++) {
                    JSONObject mean = array.getJSONObject(n).getJSONObject("meaning");
                    Iterator<String> iter = mean.keys();
                    while (iter.hasNext()) {
                        String type = iter.next();
                        def = def + "\n\n" + type + " :";
                        speech = speech + "  \n    in " + type + " ";
                        JSONArray ad = mean.getJSONArray(type);
                        for (int i = 0; i < ad.length(); i++) {
                            JSONObject jo = ad.getJSONObject(i);

                            if (!jo.optString("definition").equals("")) {
                                def = def + "\n" + (i + 1) + ") " + "Definition : " + jo.getString("definition");
                                speech = speech + "\n Definition " + (i + 1) + "    \n  " + jo.getString("definition");
                            }
                            if (!jo.optString("example").equals("")) {
                                def = def + "\n\t\t\t\t\tExample : " + jo.getString("example");

                                speech = speech + "\n Example " + "    \n  " + jo.getString("example");
                            }
                        }

                    }
                    tv.setText(def);
                    helper.addword(db, pref.getString("word", ""), def, speech);
                    timer.schedule(delayedThreadStartTask, 1 * 1000);

                }

            } catch (Exception e) {
                e.printStackTrace();


            }

        }
        else {
            progressBar.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            tv.setText(words.get(pref.getString("word","").toUpperCase()));
            speech=speech1.get(pref.getString("word","").toUpperCase());
            timer.schedule(delayedThreadStartTask, 1 * 1000);
            online=false;
        }
      }
    }
}






