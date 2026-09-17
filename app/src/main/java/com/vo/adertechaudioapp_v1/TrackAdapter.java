package com.vo.adertechaudioapp_v1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vo.adertechaudioapp_v1.localAudio.AudioModel;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<AudioModel> tracks;

    public TrackAdapter(List<AudioModel> tracks) {
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        AudioModel track = tracks.get(position);
        holder.textViewTrackName.setText(track.getTitle());
        holder.textViewTrackDuration.setText(track.getDuration());

        // Загрузка обложки через Glide с дефолтными заглушками
        Glide.with(holder.itemView.getContext())
                .load(track.getAlbumArtUri())
                .placeholder(android.R.drawable.ic_media_play) // если грузится
                .error(android.R.drawable.ic_media_play)       // если обложки нет
                .into(holder.imageViewIvAlbumArt);
    }

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTrackName, textViewTrackDuration;
        ImageView imageViewIvAlbumArt;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTrackName = itemView.findViewById(R.id.TextView_trackTitle);
            textViewTrackDuration = itemView.findViewById(R.id.TextView_trackDuration);
            imageViewIvAlbumArt = itemView.findViewById(R.id.ImageView_ivAlbumArt);
        }
    }
}
