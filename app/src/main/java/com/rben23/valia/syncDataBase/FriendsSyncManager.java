package com.rben23.valia.syncDataBase;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.DAO.FriendsDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

public class FriendsSyncManager implements iSyncManager {
    private final DatabaseReference databaseReference;
    private final FriendsDAO friendsDAO = ValiaSQLiteHelper.getFriendsDAO();
    private String uid;

    public FriendsSyncManager(String uid) {
        this.uid = uid;
        databaseReference = FirebaseDatabase.getInstance().getReference("Friends");
    }

    // Sincronizar con la base de datos local
    @Override
    public void synchronizeToLocalDatabase(SyncCallback syncCallback) {
        // Escuchar cambios en la base de datos
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si hay un cambio, se actualiza
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Friends friends = dataSnapshot.getValue(Friends.class);
                    if (friends != null) {
                        // Solo procesamos el registro si el usuario aparece en la relación
                        if (!uid.equals(friends.getIdUser()) && !uid.equals(friends.getIdFriend())) {
                            continue;
                        }

                        // Detectamos los uids desde firebase
                        String uid1 = friends.getIdUser();
                        String uid2 = friends.getIdFriend();

                        // Ordenamos los uids
                        String orderedUser, orderedFriend;
                        if (uid1.compareTo(uid2) <= 0) {
                            orderedUser = uid1;
                            orderedFriend = uid2;
                        } else {
                            orderedUser = uid2;
                            orderedFriend = uid1;
                        }

                        friends.setIdUser(orderedUser);
                        friends.setIdFriend(orderedFriend);

                        Log.i("USER ID:", friends.getIdUser());
                        Log.i("Amigo ID:", friends.getIdFriend());

                        // Si no existe la solicitud, se inserta una nueva
                        if (friendsDAO.exists(orderedUser, orderedFriend)) {
                            friendsDAO.update(friends);
                        } else {
                            friendsDAO.insert(friends);
                        }
                    }
                }
                if (syncCallback != null) {
                    syncCallback.onSyncCompleted();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncAmigos", "ERROR: No se ha podido sincronizar la tabla", error.toException());
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = friendsDAO.getCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                Friends friends = friendsDAO.toFriends(cursor);

                String compositeKey = (friends.getIdUser().compareTo(friends.getIdFriend())<=0)
                        ? friends.getIdUser() + "_" + friends.getIdFriend()
                        : friends.getIdFriend() + "_" + friends.getIdUser();

                // Subir a firebase
                databaseReference.child(compositeKey).setValue(friends)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "Solicitud sincronizada con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}
