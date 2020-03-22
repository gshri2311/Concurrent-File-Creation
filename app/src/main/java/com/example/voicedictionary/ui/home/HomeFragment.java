package com.example.voicedictionary.ui.home;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.voicedictionary.MainActivity;
import com.example.voicedictionary.R;
import com.example.voicedictionary.dictionary;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment  {

    private final int REQ_CODE = 100;

    EditText words;
    Button go;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;
    ImageView img;
    RelativeLayout r;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        go = root.findViewById(R.id.mean);
        words = root.findViewById(R.id.words);
        img=root.findViewById(R.id.imageView2);
        pref = getContext().getSharedPreferences("word", 0); // 0 - for private mode
        editor = pref.edit();


        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!words.getText().toString().equals("")){
                    editor.putString("word",words.getText().toString());
                    editor.commit();
                    startActivity(new Intent(getContext(), dictionary.class));
                }
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
        return root;
    }
    private void start() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak the word");
        try {
            startActivityForResult(intent, REQ_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    "Sorry your device not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editor.putString("word",result.get(0));
                    editor.commit();
                    startActivity(new Intent(getActivity(),dictionary.class));
                }

                break;

            }
            default:start();
        }
    }
}