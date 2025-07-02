package com.rben23.valia.syncDataBase.syncInterfaces;

public interface iSyncManager {
    void synchronizeToLocalDatabase(SyncCallback syncCallback);
    void synchronizeToRemoteDatabase(SyncCallback syncCallback);
}
