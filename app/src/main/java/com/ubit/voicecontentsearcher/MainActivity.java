package com.ubit.voicecontentsearcher;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubit.VoiceSearchActivity;
import com.ubit.voicecontentsearcher.R;
import com.ubit.voicecontentsearcher.adapter.VoiceRecyclerViewAdapter;
import com.ubit.voicecontentsearcher.fragment.VoiceDialogFragment;
import com.ubit.voicecontentsearcher.model.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQ_CODE_SPEECH_INPUT = 1;
    private static final int REQUEST_CODE = 101;

    @BindView(R.id.fabVoiceRecord)
    FloatingActionButton fabVoiceRecord;
    @BindView(R.id.recyclerViewRecords)
    RecyclerView recyclerViewRecords;
    @BindView(R.id.textViewNoRecord)
    TextView textViewNoRecord;
    @BindView(R.id.etSearch)
    EditText editText;
    @BindView(R.id.btnMic)
    ImageView btnMic;


    List<File> files = new ArrayList<>();
    List<Record> records = new ArrayList<Record>();
    MediaMetadataRetriever mmr;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View bottom_sheet;
    private VoiceRecyclerViewAdapter voiceRecyclerViewAdapter;
    FirebaseDatabase database;
    DatabaseReference myRef, ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initRecyclerView();

        // Write a message to the database
        myRef = FirebaseDatabase.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference().child("VoiceText");

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
                voiceRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


        fabVoiceRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VoiceSearchActivity.class);
                startActivity(intent);
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    myRef.child("VoiceText").push().setValue(editText.getText().toString());
                    Toast.makeText(MainActivity.this, "Text Uploaded To Firebase", Toast.LENGTH_SHORT).show();
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

    /**
     * Showing google speech input dialog
     */
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void initRecyclerView() {
        textViewNoRecord.setVisibility(View.INVISIBLE);
        recyclerViewRecords.setVisibility(View.VISIBLE);
        voiceRecyclerViewAdapter = new VoiceRecyclerViewAdapter(getApplicationContext(), records);
        recyclerViewRecords.setAdapter(voiceRecyclerViewAdapter);
        recyclerViewRecords.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
}
