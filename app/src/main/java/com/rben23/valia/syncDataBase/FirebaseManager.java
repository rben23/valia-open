package com.rben23.valia.syncDataBase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;

import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseManager {
    private static FirebaseManager instance;

    private final UsersSyncManager usersSyncManager;
    private final MessagesSyncManager messagesSyncManager;
    private final FriendRequestsSyncManager friendRequestsSyncManager;
    private final FriendsSyncManager friendsSyncManager;
    private final ChallengesSyncManager challengesSyncManager;
    private final ResultsChallengesSyncManager resultsChallengesSyncManager;

    // Constructor privado
    private FirebaseManager(String uid) {
        // Inicializamos cada uno de los sync managers
        usersSyncManager = new UsersSyncManager(uid);
        messagesSyncManager = new MessagesSyncManager(uid);
        friendRequestsSyncManager = new FriendRequestsSyncManager(uid);
        friendsSyncManager = new FriendsSyncManager(uid);
        challengesSyncManager = new ChallengesSyncManager(uid);
        resultsChallengesSyncManager = new ResultsChallengesSyncManager();
    }

    // Obtenemos una unica instancia de FirebaseManager
    public static synchronized FirebaseManager getInstance(String currentUserUid) {
        if (instance == null) {
            instance = new FirebaseManager(currentUserUid);
        }
        return instance;
    }

    // Restablecemos la instancia
    public static synchronized void resetInstance() {
        instance = null;
    }

    // Getters
    public UsersSyncManager getUsersSyncManager() {
        return usersSyncManager;
    }

    public FriendsSyncManager getFriendsSyncManager() {
        return friendsSyncManager;
    }

    public FriendRequestsSyncManager getFriendRequestsSyncManager() {
        return friendRequestsSyncManager;
    }

    public MessagesSyncManager getMessagesSyncManager(String conversationUid) {
        return new MessagesSyncManager(conversationUid);
    }

    public ChallengesSyncManager getChallengesSyncManager() {
        return challengesSyncManager;
    }

    public ResultsChallengesSyncManager getResultsChallengesSyncManager() {
        return resultsChallengesSyncManager;
    }

    // Sincronizamos todos los módulos
    public void syncAll(final SyncCallback syncCallback) {
        final int totalManager = 6;
        final AtomicInteger counter = new AtomicInteger(totalManager);

        SyncCallback internalCallback = () -> {
            if (counter.decrementAndGet() == 0) {
                syncCallback.onSyncCompleted();
            }
        };

        // To Local
        syncToLocalDatabase(internalCallback);
        // To Remote
        syncToRemoteDatabase(internalCallback);
    }

    // Sincronizamos los módulos hacia la base de datos local
    public void syncAllToLocalDatabase(final SyncCallback syncCallback) {
        final int totalManager = 6;
        final AtomicInteger counter = new AtomicInteger(totalManager);

        SyncCallback internalCallback = () -> {
            if (counter.decrementAndGet() == 0) {
                syncCallback.onSyncCompleted();
            }
        };

        // To Local
        syncToLocalDatabase(internalCallback);
    }

    private void syncToLocalDatabase(SyncCallback syncCallback) {
        usersSyncManager.synchronizeToLocalDatabase(syncCallback);
        messagesSyncManager.synchronizeToLocalDatabase(syncCallback);
        friendRequestsSyncManager.synchronizeToLocalDatabase(syncCallback);
        friendsSyncManager.synchronizeToLocalDatabase(syncCallback);
        challengesSyncManager.synchronizeToLocalDatabase(syncCallback);
        resultsChallengesSyncManager.synchronizeToLocalDatabase(syncCallback);
    }

    // Sincronizamos los módulos hacia la base de datos en red
    public void syncAllToRemoteDatabase(final SyncCallback syncCallback) {
        final int totalManager = 6;
        final AtomicInteger counter = new AtomicInteger(totalManager);

        SyncCallback internalCallback = () -> {
            if (counter.decrementAndGet() == 0) {
                syncCallback.onSyncCompleted();
            }
        };

        // To Remote
        syncToRemoteDatabase(internalCallback);
    }

    public void syncToRemoteDatabase(SyncCallback syncCallback) {
        usersSyncManager.synchronizeToRemoteDatabase(syncCallback);
        messagesSyncManager.synchronizeToRemoteDatabase(syncCallback);
        friendRequestsSyncManager.synchronizeToRemoteDatabase(syncCallback);
        friendsSyncManager.synchronizeToRemoteDatabase(syncCallback);
        challengesSyncManager.synchronizeToRemoteDatabase(syncCallback);
        resultsChallengesSyncManager.synchronizeToRemoteDatabase(syncCallback);

        // Es necesario notificar cuando actualizamos a remoto
        if (syncCallback != null) {
            syncCallback.onSyncCompleted();
        }
    }
}
