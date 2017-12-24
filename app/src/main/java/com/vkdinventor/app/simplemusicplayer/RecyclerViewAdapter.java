package com.vkdinventor.app.simplemusicplayer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Adapter for songList
 * Created by vikash on 22-12-2017.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    interface OnItemClickListner {
        void onItemClicked(int position);
    }

    private OnItemClickListner onItemClickListner;

    List<Audio> list = Collections.emptyList();
    Context context;

    public RecyclerViewAdapter(List<Audio> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title;
        ImageView play_pause;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            play_pause = (ImageView) itemView.findViewById(R.id.play_pause);
            cardView = itemView.findViewById(R.id.cardview);
            title.setOnClickListener(this);
            play_pause.setOnClickListener(this);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            LogUtil.v("Item Clickeed: "+getAdapterPosition());
            if(onItemClickListner != null ){
                onItemClickListner.onItemClicked(getAdapterPosition());
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Audio audio = list.get(position);
        //Glide.with(context).load(audio.getData()).into(holder.play_pause);
        holder.title.setText(list.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }
}
