package com.rben23.valia.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rben23.valia.DAO.UsersDAO;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Users;

import java.util.List;

public class FriendsChallengesAdaper extends ArrayAdapter<Friends> {
    private Context context;
    private String currentUserUid;
    private UsersDAO usersDAO;

    public FriendsChallengesAdaper(@NonNull Context context, List<Friends> friendsList, String currentUserUid) {
        super(context, 0, friendsList);
        this.context = context;
        this.currentUserUid = currentUserUid;

        this.usersDAO = ValiaSQLiteHelper.getUsersDAO();
    }

    // Vista que mostramos cuando el spinner esta cerrado
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(getFriendDisplayName(getItem(position)));
        return convertView;
    }

    private String getFriendDisplayName(Friends friend) {
        if (friend == null) return context.getString(R.string.unknownUser);

        // Si currentUserUid es igual a idUser, el amigo es idFriend; si no, es idUser.
        String friendUid = currentUserUid.equals(friend.getIdUser()) ? friend.getIdFriend() : friend.getIdUser();
        String displayName = context.getString(R.string.unknownUser);

        Cursor cursor = usersDAO.getRecord(friendUid);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Users friendUser = usersDAO.toUsers(cursor);
                if (friendUser != null && friendUser.getUserId() != null) {
                    displayName = friendUser.getUserId();
                }
            }
            cursor.close();
        }
        return displayName;
    }
}
