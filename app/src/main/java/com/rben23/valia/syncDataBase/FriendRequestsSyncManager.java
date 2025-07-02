package com.rben23.valia.syncDataBase;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.DAO.FriendRequestsDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.FriendRequests;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

public class FriendRequestsSyncManager implements iSyncManager {
    private final DatabaseReference databaseReference;
    private final FriendRequestsDAO friendRequestsDAO = ValiaSQLiteHelper.getFriendRequestsDAO();
    private String currentUserUid;

    public FriendRequestsSyncManager(String currentUserUid) {
        this.currentUserUid = currentUserUid;
        databaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests");
    }

    // Sincronizar con la base de datos local
    @Override
    public void synchronizeToLocalDatabase(SyncCallback syncCallback) {
        // Realizamos una consulta donde el idDestinatario sea igual al uid del usuario actual
        Query queryAddressee = databaseReference.orderByChild("idAddressee").equalTo(currentUserUid);
        Query querySender = databaseReference.orderByChild("idUser").equalTo(currentUserUid);

        // Escuchar cambios en la base de datos
        queryAddressee.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si hay un cambio, se actualiza
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FriendRequests friendRequests = dataSnapshot.getValue(FriendRequests.class);
                    if (friendRequests != null) {
                        Log.d("SyncSolicitudes", "queryAddressee - Procesando solicitud id: " + friendRequests.getIdRequest() +
                                " estado: " + friendRequests.getIdState());
                        // Si no existe la solicitud, se inserta una nueva
                        if (friendRequestsDAO.exists(friendRequests.getIdRequest())) {
                            friendRequestsDAO.update(friendRequests);
                        } else {
                            friendRequestsDAO.insert(friendRequests);
                        }
                    }
                }

                querySender.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FriendRequests friendRequests = dataSnapshot.getValue(FriendRequests.class);
                            if (friendRequests != null) {
                                Log.d("SyncSolicitudes", "querySender - Procesando solicitud id: " + friendRequests.getIdRequest() +
                                        " estado: " + friendRequests.getIdState());
                                if (friendRequestsDAO.exists(friendRequests.getIdRequest())) {
                                    friendRequestsDAO.update(friendRequests);
                                } else {
                                    friendRequestsDAO.insert(friendRequests);
                                }
                            }
                        }
                        if (syncCallback != null) {
                            syncCallback.onSyncCompleted();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("SyncSolicitudes", "ERROR en querySender", error.toException());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncSolicitudes", "ERROR en queryAddressee", error.toException());
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = friendRequestsDAO.getCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                FriendRequests friendRequests = friendRequestsDAO.toFriendRequests(cursor);

                // Subir a firebase
                databaseReference.child(Integer.toString(friendRequests.getIdRequest())).setValue(friendRequests)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "Solicitud sincronizada con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}
