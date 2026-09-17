package com.vo.adertechaudioapp_v1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vo.adertechaudioapp_v1.config.AppConfigItem;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder>{

    private static final String TAG = RouteAdapter.class.getSimpleName();

    public interface IRouteAdapterListeners {
        void onClickListener(View view, AppConfigItem configItem, int selectedPosition);
    }

    IRouteAdapterListeners listeners;

    Context context;
    private List<AppConfigItem> appConfigItemList;
    private int selectedPosition = -1;                      // Позиция выбранной радиокнопки

    public RouteAdapter(Context context, List<AppConfigItem> appConfigItemList, IRouteAdapterListeners listeners) {
        this.context = context;
        this.appConfigItemList = new ArrayList<>(appConfigItemList);
        this.listeners = listeners;
    }

    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    /// Метод указания какой конкретно дизайн мы будем использовать для отображения каждого элемента
    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.fragment_first_item, parent, false);
        return new RouteViewHolder(item);
    }

    /// Метод что конкретно мы будем подставлять в сам дизайн
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        holder.toggleButton.setText(appConfigItemList.get(position).getFriendlyName());
        holder.toggleButton.setChecked(position == selectedPosition);

        holder.toggleButton.setOnClickListener(v -> {
            if (holder.toggleButton.isChecked()) selectedPosition = holder.getAbsoluteAdapterPosition();
            else selectedPosition = -1;
            notifyDataSetChanged();                                             // Обновляем список, чтобы сбросить другие кнопки
            AppConfigItem appConfigItem = appConfigItemList.get(position);

            listeners.onClickListener(v, appConfigItem, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return appConfigItemList.size();
    }


    /// Вложенный класс — с какими элементами в дизайне мы будем работать, текстовыми полями, картинками и т.д
    public static final class RouteViewHolder extends RecyclerView.ViewHolder {

        MaterialButton toggleButton;
        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);

            toggleButton = itemView.findViewById(R.id.button_firstFragmentInputItem);
        }
    }

}
