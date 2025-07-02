package com.rben23.valia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;
import com.rben23.valia.managements.UserManagement;
import com.rben23.valia.models.Users;
import com.rben23.valia.managements.ProfileImageManagement;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private final List<Users> users;

    public UsersAdapter(List<Users> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        Users user = users.get(position);
        holder.userName.setText(user.getUserId());

        // Pasar la imagen a Bitmap
        ProfileImageManagement profileImageManagement = new ProfileImageManagement(holder.itemView.getContext(), user.getUid());
        profileImageManagement.getRemoteUserImageProfile(holder.profileIcon);

        // Establecemos un listener para ir a la pantalla de visualizacion de la actividad
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, UserManagement.class);
            intent.putExtra("userUid", user.getUid());
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userName", user.getName());
            intent.putExtra("userEmail", user.getEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIcon = itemView.findViewById(R.id.profileIcon);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}