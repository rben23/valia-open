package com.rben23.valia.syncDataBase;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.DAO.MessagesDAO;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Messages;
import com.rben23.valia.syncDataBase.syncInterfaces.SyncCallback;
import com.rben23.valia.syncDataBase.syncInterfaces.iSyncManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesSyncManager implements iSyncManager {
    private final DatabaseReference databaseReference;
    private final MessagesDAO messagesDAO = ValiaSQLiteHelper.getMessagesDAO();
    private String conversationUid;

    public MessagesSyncManager(String conversationUid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Messages").child(conversationUid);
        this.conversationUid = conversationUid;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    // Sincronizar con la base de datos local
    @Override
    public void synchronizeToLocalDatabase(SyncCallback syncCallback) {
        // Escuchar cambios en la base de datos
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Mapeamos los mensajes remotos
                Map<String, Messages> remoteMessagesMap = new HashMap<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if (messages != null) {
                        remoteMessagesMap.put(Integer.toString(messages.getIdMessage()), messages);
                    }
                }

                // Obtenemos los mensajes locales
                List<Messages> localMessages = messagesDAO.getAllMessages(conversationUid);

                // Recorremos los mensajes locales
                for (Messages localMessage : localMessages) {
                    // Si el mensaje remoto no existe, se elimina
                    if (!remoteMessagesMap.containsKey(Integer.toString(localMessage.getIdMessage()))) {
                        messagesDAO.delete(localMessage.getIdMessage());
                    }
                }

                // Si hay un cambio, se actualiza
                for (Messages remoteMessage : remoteMessagesMap.values()) {
                    // Si no existe el mensaje se inserta uno nuevo
                    if (messagesDAO.exists(remoteMessage.getIdMessage())) {
                        messagesDAO.update(remoteMessage);
                    } else {
                        messagesDAO.insert(remoteMessage);
                    }
                }
                if (syncCallback != null) {
                    syncCallback.onSyncCompleted();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncMensajes", "ERROR: No se ha podido sincronizar la tabla", error.toException());
            }
        });
    }

    // Sincronizar con la base de datos en red
    @Override
    public void synchronizeToRemoteDatabase(SyncCallback syncCallback) {
        Cursor cursor = messagesDAO.getCursorForConversation(conversationUid);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Convertir cursor a model
                Messages messages = messagesDAO.toMessages(cursor);

                // Subir a firebase
                databaseReference.child(Integer.toString(messages.getIdMessage())).setValue(messages)
                        .addOnSuccessListener(s -> Log.d("SyncToRemote", "Mensaje sincronizado con Firebase"))
                        .addOnFailureListener(e -> Log.e("SyncToRemote", "ERROR: No se ha podido sincronizar con Firebase", e));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}
