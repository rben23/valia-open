package com.rben23.valia.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.managements.chatsManagements.ChatManagement;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Users;
import com.rben23.valia.managements.ProfileImageManagement;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {
    private List<Friends> friendsList;
    private String currentUser;
    private Context context;

    public FriendListAdapter(Context context, List<Friends> friendsList, String currentUser) {
        this.friendsList = friendsList;
        this.currentUser = currentUser;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendListAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.FriendViewHolder holder, int position) {
        Friends amigo = friendsList.get(position);
        holder.bind(amigo, currentUser, context);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }


    public void updateItems(List<Friends> newItems) {
        this.friendsList = newItems;
        notifyDataSetChanged();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView userName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIcon = itemView.findViewById(R.id.profileIcon);
            userName = itemView.findViewById(R.id.userName);
        }

        public void bind(Friends friend, String currentUser, Context context) {
            String friendId = friend.getIdUser().equals(currentUser) ? friend.getIdFriend() : friend.getIdUser();
            Cursor cursor = ValiaSQLiteHelper.getUsersDAO().getRecord(friendId);
            Users user = ValiaSQLiteHelper.getUsersDAO().toUsers(cursor);

            // Cerramos el cursor despues de obtener los datos
            if (cursor != null) {
                cursor.close();
            }

            if (user == null) {
                userName.setText(context.getString(R.string.unknownUser));
                profileIcon.setImageResource(R.drawable.img_default_user_image);
            } else {
                userName.setText(user.getUserId());

                // Pasar la imagen a Bitmap
                ProfileImageManagement profileImageManagement = new ProfileImageManagement(context, user.getUid());
                profileImageManagement.getRemoteUserImageProfile(profileIcon);
            }

            // Establecemos un listener para ir a la pantalla de visualizacion de la actividad
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChatManagement.class);
                intent.putExtra("userUid", user.getUid());
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("userName", user.getName());
                intent.putExtra("userEmail", user.getEmail());
                context.startActivity(intent);
            });
        }
    }
}
