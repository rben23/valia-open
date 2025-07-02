package com.rben23.valia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;

public class SelectActivitiesAdapter extends RecyclerView.Adapter<SelectActivitiesAdapter.ViewHolder> {
    private Context context;
    private ActivityTypes[] activityTypes;
    private OnItemClickListener listener;

    public SelectActivitiesAdapter(Context context, ActivityTypes[] activityTypes, OnItemClickListener listener) {
        this.context = context;
        this.activityTypes = activityTypes;
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(ActivityTypes activity);
    }

    // Creamos cada elemento de la lista
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false);
        return new ViewHolder(view);
    }

    // Llenamos cada elemento con los datos de la lista
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityTypes activity = activityTypes[position];
        holder.name.setText(activity.getName());
        holder.icon.setImageResource(activity.getIcon());

        holder.itemView.setOnClickListener(view -> listener.onItemClick(activity));
    }

    @Override
    public int getItemCount() {
        return activityTypes.length;
    }

    // Clase para definir los componentes visuales de cada fila
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolder(View item) {
            super(item);
            icon = item.findViewById(R.id.activityIcon);
            name = item.findViewById(R.id.activityName);
        }

    }
}
