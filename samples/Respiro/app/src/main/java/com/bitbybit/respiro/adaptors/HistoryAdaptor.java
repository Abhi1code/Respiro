package com.bitbybit.respiro.adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitbybit.respiro.R;
import com.bitbybit.respiro.gs.HistoryGS;
import com.bitbybit.respiro.ui.Results;
import com.bitbybit.respiro.utils.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdaptor extends RecyclerView.Adapter<HistoryAdaptor.Viewholder> {

    private ArrayList<HistoryGS> arrayList;
    private Context context;
    private Viewholder viewholder;

    public HistoryAdaptor(ArrayList<HistoryGS> arrayList, Context context){
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card, parent, false);
        Viewholder viewholder = new Viewholder(v);
        this.viewholder = viewholder;
        return viewholder;
    }

    @Override
    public void onBindViewHolder(Viewholder viewHolder, final int position) {
        viewHolder.pef.setText(arrayList.get(position).getPef() + "");
        viewHolder.fvc.setText(arrayList.get(position).getFvc() + "");
        SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
        viewHolder.date.setText(mFormat.format(new Date(arrayList.get(position).getTime())));
        viewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.getInstance().setTime(arrayList.get(position).getTime());
                context.startActivity(new Intent(context, Results.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{

        TextView date, pef, fvc;
        CardView card;

        public Viewholder(View itemView){
            super(itemView);
            date = itemView.findViewById(R.id.date);
            pef = itemView.findViewById(R.id.pef);
            fvc = itemView.findViewById(R.id.fvc);
            card = itemView.findViewById(R.id.card);
        }
    }
}
