package com.vo.adertechaudioapp_v1;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vo.adertechaudioapp_v1.localAudio.AudioModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private final Map<String, List<AudioModel>> folderMap;
    private final List<String> folderNames;
    private final OnFolderClickListener listener;

    // Интерфейс для обработки клика по папке
    public interface OnFolderClickListener {
        void onFolderClick(String folderName, List<AudioModel> tracks);
    }

    public FolderAdapter(Map<String, List<AudioModel>> folderMap, OnFolderClickListener listener) {
        this.folderMap = folderMap;
        this.folderNames = new ArrayList<>(folderMap.keySet()); // Получаем список названий папок
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        String folderName = folderNames.get(position);
        List<AudioModel> tracksInFolder = folderMap.get(folderName);
        int tracksCount = tracksInFolder != null ? tracksInFolder.size() : 0;

        holder.textViewFolderName.setText(folderName);
        holder.textViewTracksCount.setText(String.format(Locale.US,"%3d tracks", tracksCount));

        // Обработка клика
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && tracksInFolder != null) {
                listener.onFolderClick(folderName, tracksInFolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderNames.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFolderName, textViewTracksCount;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFolderName = itemView.findViewById(R.id.TextView_folderName);
            textViewTracksCount = itemView.findViewById(R.id.TextView_tracksCount);
        }
    }
}
