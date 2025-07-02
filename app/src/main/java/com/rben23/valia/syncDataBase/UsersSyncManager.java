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
import com.rben23.valia.DAO.UsersDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.syncInterfaces.OnUserRetrievedListener;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UsersSyncManager implements iSyncManager {
    private final DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users");
    private final DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("Friends");
    private final UsersDAO usersDAO = ValiaSQLiteHelper.getUsersDAO();
    private String uid;

    private List<Users> lastSearchResult = new ArrayList<>();

    public UsersSyncManager(String uid) {
        this.uid = uid;
    }

    // Getter lastSearchResult
    public List<Users> getLastSearchResult() {
        return lastSearchResult;
    }

    // Sincronizar con la base de datos local
    @Override
    public void synchronizeToLocalDatabase(SyncCallback syncCallback) {
        // Escuchar cambios en la base de datos
        usersReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si hay un cambio, se actualiza
                Users users = snapshot.getValue(Users.class);
                if (users != null && users.getUid() != null) {
                    // Si no existe el usuario, se inserta uno nuevo
                    if (usersDAO.exists(users.getUid())) {
                        usersDAO.update(users);
                    } else {
                        usersDAO.insert(users);
                    }
                }
                // Sincronizamos los amigos con la base de datos local
                syncFriendUsers(syncCallback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncUsuarios", "ERROR: No se ha podido sincronizar la tabla", error.toException());
                // Llamamos a sincronizar amigos para incrementar el contador y no bloquear
                syncFriendUsers(syncCallback);
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = usersDAO.getCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                Users users = usersDAO.toUsers(cursor);

                // Subir a Firebase
                usersReference.child(users.getUid()).setValue(users)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "Usuario sincronizado con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    // Sincronizamos solamente el usuario actual
    public void synchronizeCurrentUserToRemoteDatabase() {
        Users user = usersDAO.getCurrentUser();
        if (user != null) {
            usersReference.child(user.getUid()).setValue(user)
                    .addOnSuccessListener(s -> Log.d("SyncToRemote", "Usuario sincronizado con Firebase"))
                    .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
        }
    }

    // Sincronizamos los amigos a la base de datos local como Usuarios
    public void syncFriendUsers(SyncCallback syncCallback) {
        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Detectamos cuando se han sincronizado todos los amigos
                    final int totalFriends = (int) snapshot.getChildrenCount();
                    final AtomicInteger friendCounter = new AtomicInteger(0);

                    // Sincronizamos cada registro del amigo
                    for (DataSnapshot friendData : snapshot.getChildren()) {
                        // Obtenemos el objeto Amigos completo
                        Friends friend = friendData.getValue(Friends.class);
                        if (friend != null) {
                            // Verificamos si el usuario actual aparece en la amistad
                            if (uid.equals(friend.getIdUser()) || uid.equals(friend.getIdFriend())) {
                                String friendUid = uid.equals(friend.getIdUser())
                                        ? friend.getIdFriend() : friend.getIdUser();

                                syncFriendToLocalDatabase(friendUid, friendCounter, totalFriends, syncCallback);
                            } else {
                                // Si el usuairo no corresponde incrementeamos el contador
                                if (friendCounter.incrementAndGet() == totalFriends) {
                                    if (syncCallback != null) {
                                        syncCallback.onSyncCompleted();
                                    }
                                }
                            }
                        } else {
                            if (friendCounter.incrementAndGet() == totalFriends) {
                                if (syncCallback != null) {
                                    syncCallback.onSyncCompleted();
                                }
                            }
                        }

                    }
                } else {
                    // Si no hay registros llamammos al callback
                    if (syncCallback != null) {
                        syncCallback.onSyncCompleted();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncUsuarios", "ERROR: No se ha podido sincronizar la tabla", error.toException());
                if (syncCallback != null) {
                    syncCallback.onSyncCompleted();
                }
            }
        });
    }

    private void syncFriendToLocalDatabase(String friendUid, AtomicInteger friendCounter, int totalFriends, SyncCallback syncCallback) {
        usersReference.child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users friendUser = snapshot.getValue(Users.class);
                if (friendUser != null && friendUser.getUid() != null) {
                    if (usersDAO.exists(friendUser.getUid())) {
                        usersDAO.update(friendUser);
                    } else {
                        usersDAO.insert(friendUser);
                    }
                }

                // Incrementamos el contador
                if (friendCounter.incrementAndGet() == totalFriends) {
                    if (syncCallback != null) {
                        syncCallback.onSyncCompleted();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncUsuarios", "ERROR: No se ha podido sincronizar la tabla", error.toException());
                // Incrementamos el contador para evitar bloqueos
                if (friendCounter.incrementAndGet() == totalFriends) {
                    if (syncCallback != null) {
                        syncCallback.onSyncCompleted();
                    }
                }
            }
        });
    }

    public void getUserByUid(String uid, final OnUserRetrievedListener listener) {
        usersReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users retrieveUser = snapshot.getValue(Users.class);
                listener.onUserRetrieved(retrieveUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UsuariosSyncManager", "ERROR al recuperar el usuario " + error.getMessage());
                listener.onUserRetrieved(null);
            }
        });
    }

    public void searchUsers(String query, SyncCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            lastSearchResult.clear();  // Si la búsqueda es vacía, limpiamos la lista
            callback.onSyncCompleted();
            return;
        }

        Query firebaseQuery = usersReference.orderByChild("userId")
                .startAt(query)
                .endAt(query + "\uf8ff");  // Limite superior con caracter de rango privado

        firebaseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastSearchResult.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Users userResult = ds.getValue(Users.class);
                    if (userResult != null) {
                        lastSearchResult.add(userResult);
                    }
                }
                callback.onSyncCompleted();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Puedes registrar el error si lo deseas
                callback.onSyncCompleted();
            }
        });
    }
}
