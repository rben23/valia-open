package com.rben23.valia.adapters;

import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;
import com.rben23.valia.managements.OnActivityRouteClickListener;
import com.rben23.valia.models.joins.ActivityRouteJoin;

import java.util.List;

public class ListActivitiesRoutesAdapter extends RecyclerView.Adapter<ListActivitiesRoutesAdapter.ActivityRouteViewHolder> {
    private List<ActivityRouteJoin> items;
    private OnActivityRouteClickListener clickListener;

    public ListActivitiesRoutesAdapter(List<ActivityRouteJoin> items, OnActivityRouteClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ActivityRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_item, parent, false);
        return new ActivityRouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityRouteViewHolder holder, int position) {
        ActivityRouteJoin item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public void updateItems(List<ActivityRouteJoin> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public class ActivityRouteViewHolder extends RecyclerView.ViewHolder {
        private final TextView activityName, activityText;
        private final ImageView activityIcon;


        public ActivityRouteViewHolder(@NonNull View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityName);
            activityText = itemView.findViewById(R.id.activityText);
            activityIcon = itemView.findViewById(R.id.activityIcon);
            activityText.setVisibility(VISIBLE);
        }

        public void bind(ActivityRouteJoin item) {
            activityName.setText(item.getRouteName());
            activityText.setText(item.getIdUser());

            ActivityTypes type = ActivityTypes.fromName(item.getIdUser(), itemView.getContext());
            activityIcon.setImageResource(type.getIcon());

            // Establecemos un listener para abrir la pantalla de visualizacion
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onActivityRouteClick(item);
            });
        }
    }
}
