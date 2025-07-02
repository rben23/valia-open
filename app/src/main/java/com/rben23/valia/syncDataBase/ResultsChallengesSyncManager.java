package com.rben23.valia.syncDataBase;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.DAO.ResultsChallengesDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.ResultsChallenges;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

public class ResultsChallengesSyncManager implements iSyncManager {
    private final DatabaseReference databaseReference;
    private final ResultsChallengesDAO resultsChallengesDAO = ValiaSQLiteHelper.getResultsChallengesDAO();

    public ResultsChallengesSyncManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference("ResultsChallenges");
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
                    ResultsChallenges resultsChallenges = dataSnapshot.getValue(ResultsChallenges.class);
                    if (resultsChallenges != null) {
                        // Si no existe el usuario se inserta uno nuevo
                        if (resultsChallengesDAO.exists(resultsChallenges.getIdResult())) {
                            resultsChallengesDAO.update(resultsChallenges);
                        } else {
                            resultsChallengesDAO.insert(resultsChallenges);
                        }
                    }
                }
                if (syncCallback != null) {
                    syncCallback.onSyncCompleted();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncResRetos", "ERROR: No se ha podido sincronizar la tabla", error.toException());
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = resultsChallengesDAO.getCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                ResultsChallenges resultsChallenges = resultsChallengesDAO.toResultChallenges(cursor);

                // Subir a firebase
                databaseReference.child(Integer.toString(resultsChallenges.getIdResult())).setValue(resultsChallenges)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "ResultadosRetos sincronizado con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
