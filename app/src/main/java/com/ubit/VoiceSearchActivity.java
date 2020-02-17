package com.ubit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubit.voicecontentsearcher.MainActivity;
import com.ubit.voicecontentsearcher.R;
import com.ubit.voicecontentsearcher.adapter.VoiceRecyclerViewAdapter;
import com.ubit.voicecontentsearcher.model.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VoiceSearchActivity extends AppCompatActivity {
    @BindView(R.id.sTextInput)
    EditText editText;

    @BindView(R.id.btnMic)
    ImageView btnMic;

    @BindView(R.id.backward)
    FloatingActionButton backward;

    FirebaseDatabase database;
    DatabaseReference ref;
    List<Record> records = new ArrayList<Record>();
    List<Record> temp = new ArrayList<Record>();
    private VoiceRecyclerViewAdapter voiceRecyclerViewAdapter;

    private static final int REQ_CODE_SPEECH_INPUT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_search);
        ButterKnife.bind(this);

        ref = FirebaseDatabase.getInstance().getReference().child("VoiceText");

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VoiceSearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    final String txt_val=editText.getText().toString();
                    editText.getText().clear();
                    // Read from the database
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            records.clear();
                            voiceRecyclerViewAdapter.notifyDataSetChanged();
                            int count = 1;
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String user = (String) postSnapshot.getValue();
                                records.add(new Record("Record_00"+count+"",user));
                                count++;
                            }

                            /*for (int inc=1; inc < records.size();inc++) {
                                if(records.get(inc).getValue().toLowerCase().contains(txt_val.toLowerCase())){
                                    //temp.add(records.get(inc));
                                    Toast.makeText(getApplicationContext(),
                                            records.get(inc).getValue().toString(),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }*/
                            //Toast.makeText(temp.get(0).getValue().toString(),);

                            voiceRecyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });


                    return true;
                }
                return false;
            }
        });

        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Search some word");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Text to speech not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //on google speech input result
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null) {
                    String searchWord = result.get(0);
                    editText.setText(searchWord);
                }
            }
        }
    }
    
}
