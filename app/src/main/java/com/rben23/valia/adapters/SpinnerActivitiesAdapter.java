package com.rben23.valia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rben23.valia.R;

public class SpinnerActivitiesAdapter extends ArrayAdapter<ActivityTypes> {
    private final Context context;
    private final ActivityTypes[] types;

    public SpinnerActivitiesAdapter(@NonNull Context context, ActivityTypes[] types) {
        super(context, 0, types);
        this.context = context;
        this.types = types;
    }

    // Establecemos la vista de la opcion seleccionada
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    // Establecemos la vista para la el desplegable
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_spinner_item, parent, false);
        }
        ActivityTypes type = types[position];

        ImageView activityIcon = convertView.findViewById(R.id.activityIcon);
        TextView activityName = convertView.findViewById(R.id.activityName);

        activityIcon.setImageResource(type.getIcon());
        activityName.setText(context.getString(type.getName()));

        return convertView;
    }
}
