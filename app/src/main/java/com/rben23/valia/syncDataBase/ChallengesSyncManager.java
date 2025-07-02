package com.rben23.valia.syncDataBase;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.DAO.ChallengesDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

public class ChallengesSyncManager implements iSyncManager {
    private final DatabaseReference databaseReference;
    private final ChallengesDAO challengesDAO = ValiaSQLiteHelper.getChallengesDAO();
    private String currentUserUid;

    public ChallengesSyncManager(String currentUserUid) {
        this.currentUserUid = currentUserUid;
        databaseReference = FirebaseDatabase.getInstance().getReference("Challenges");
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
                    Challenges challenges = dataSnapshot.getValue(Challenges.class);
                    if (challenges != null) {

                        Log.d("SyncRetos", "Procesando reto: idChallenge=" + challenges.getIdChallenge() +
                                " idUser=" + challenges.getIdUser() +
                                " idAddressee=" + challenges.getIdAddressee());
                        // Filtramos los retos para guardar solo los que aparezca el usuario actual
                        if (challenges.getIdUser().equals(currentUserUid)
                                || challenges.getIdAddressee().equals(currentUserUid)) {
                            // Si no existe el reto, se inserta uno nuevo
                            if (challengesDAO.exists(challenges.getIdChallenge())) {
                                challengesDAO.update(challenges);
                            } else {
                                challengesDAO.insert(challenges);
                            }
                        }

                    }

                }
                if (syncCallback != null) {
                    syncCallback.onSyncCompleted();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncRetos", "ERROR: No se ha podido sincronizar la tabla", error.toException());
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = challengesDAO.getCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                Challenges challenges = challengesDAO.toChallenges(cursor);

                // Subir a firebase
                databaseReference.child(Integer.toString(challenges.getIdChallenge())).setValue(challenges)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "Reto sincronizado con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public void deleteInRemoteDatabase(long challengeId) {
        databaseReference.child(Long.toString(challengeId)).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("RetosSyncManager", "Reto eliminado de firebase: " + challengeId);
            } else {
                Log.d("RetosSyncManager", "ERROR al borrar el reto de firebase: " + challengeId);
            }
        });
    }
}
