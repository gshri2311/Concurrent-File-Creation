package com.example.voicedictionary.ui.searches;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.voicedictionary.R;
import com.example.voicedictionary.SQLiteHelper;
import com.example.voicedictionary.dictionary;

import java.util.ArrayList;
import java.util.HashSet;

public class SearchesFragment extends Fragment {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private ListView simpleList;
    ArrayList<String> word;
    SQLiteHelper helper;
    SQLiteDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_searches, container, false);
        simpleList = (ListView) root.findViewById(R.id.simpleListView);
        word = new ArrayList<>();

        pref = getContext().getSharedPreferences("word", 0); // 0 - for private mode
        editor = pref.edit();

        helper = new SQLiteHelper(getContext());
        db = helper.getReadableDatabase();
        getword();
        simpleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                delete_word(i);
                return true;
            }
        });
        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editor.putString("word", word.get(position));
                editor.commit();
                startActivity(new Intent(getContext(), dictionary.class));
            }
        });
        return root;
    }

    private void getword() {


        Cursor c = helper.get_words(db);
        while (c.moveToNext()) {
            word.add(c.getString(0));


        }
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(word);
        word.clear();
        word.addAll(hashSet);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.activity_list_view, R.id.textView, word);
        simpleList.setAdapter(arrayAdapter);
    }




    public void delete_word(final int i) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setMessage("Do you want to delete this ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                helper.delete_word(db, word.get(i));
               word.clear();

                getword();

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}