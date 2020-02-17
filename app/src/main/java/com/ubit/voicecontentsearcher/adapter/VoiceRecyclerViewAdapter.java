package com.ubit.voicecontentsearcher.adapter;

import android.content.Context;
import android.media.Image;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubit.voicecontentsearcher.R;
import com.ubit.voicecontentsearcher.model.Record;

import java.util.List;
import java.util.Locale;

public class VoiceRecyclerViewAdapter extends RecyclerView.Adapter<VoiceRecyclerViewAdapter.VoiceViewHolder> {


    private List<Record> records;
    private Context context;
    TextToSpeech t1;


    public VoiceRecyclerViewAdapter(Context context, List<Record> records) {
        this.records = records;
        this.context = context;
        t1=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.getDefault());
                }
            }
        });
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_voice_record, parent, false);
        return new VoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VoiceViewHolder holder, final int position) {
        holder.bind(records.get(position), position);
//        holder.mCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                holder.mCard.setCardBackgroundColor(context.getResources().getColor(R.color.relative_layout));
////                startPlayRecord(records.get(position).getMinute(), records.get(position).getName());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class VoiceViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewRecordName;
        public CardView mCard;
        ImageView imgPlay;

        public VoiceViewHolder(View itemView) {
            super(itemView);


            textViewRecordName = itemView.findViewById(R.id.textViewRecordName);
            imgPlay = itemView.findViewById(R.id.btn_Play);
            mCard = itemView.findViewById(R.id.mCard);
        }

        public void bind(final Record record, final int position) {
            textViewRecordName.setText(record.getName());
            imgPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toSpeak = record.getValue();
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }
    }

}
