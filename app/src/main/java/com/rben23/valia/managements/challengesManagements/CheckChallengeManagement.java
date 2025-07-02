package com.rben23.valia.managements.challengesManagements;

import android.content.Context;
import android.util.Log;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.models.ResultsChallenges;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;

import java.util.List;

public class CheckChallengeManagement {
    private ValiaSQLiteHelper vsql;
    private Users users;

    public CheckChallengeManagement(ValiaSQLiteHelper vsql) {
        this.vsql = vsql;
        this.users = vsql.getUsersDAO().getCurrentUser();
    }

    // Comprobamos si esta activo
    public void checkActiveChallenges() {
        List<Challenges> activeChallenges = vsql.getChallengesDAO().getActiveChallengesByUser(users.getUid());

        for (Challenges reto : activeChallenges) {
            double totalkm = vsql.getRoutesDAO().getKmForChallenge(reto.getDate());
            if (totalkm >= reto.getTotalKm()) {
                completeChallenge(reto);
            }
        }
    }

    // Completamos el reto
    private void completeChallenge(Challenges reto) {
        // Verificamos que el reto no se ha completado
        ResultsChallenges existingResult = vsql.getResultsChallengesDAO().getResultByChallengeId(reto.getIdChallenge());
        if (existingResult == null) {
            ResultsChallenges newResult = new ResultsChallenges();
            newResult.setIdChallenge(reto.getIdChallenge());
            newResult.setIdUser(users.getUid());
            long insertedId = vsql.getResultsChallengesDAO().insert(newResult);
            newResult.setIdResult((int) insertedId);

            FirebaseManager firebaseManager = FirebaseManager.getInstance(users.getUid());
            firebaseManager.getChallengesSyncManager().synchronizeToRemoteDatabase(() -> Log.d("SyncToRemote", "Reto sincronizado con Firebase"));
            firebaseManager.getResultsChallengesSyncManager().synchronizeToRemoteDatabase(() -> Log.d("SyncToRemote", "ResultadosRetos sincronizado con Firebase"));

        }
    }

}
