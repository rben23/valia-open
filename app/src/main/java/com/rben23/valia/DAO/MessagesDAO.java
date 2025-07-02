package com.rben23.valia.DAO;

import android.content.ContentValues;
import android.database.Cursor;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Messages;

import java.util.ArrayList;
import java.util.List;

public class MessagesDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Messages";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_MESSAGE_ID = "MessageId";
    private static final String COLUMN_MESSAGE_SENDERID = "SenderId";
    private static final String COLUMN_MESSAGE_CONVERSATIONUID = "ConversationUid";
    private static final String COLUMN_MESSAGE_ADDRESSEEID = "AddresseeId";
    private static final String COLUMN_MESSAGE_MESSAGE = "Message";
    private static final String COLUMN_MESSAGE_DATE = "Date";
    private static final String COLUMN_MESSAGE_TYPEID = "TypeId";
    private static final String COLUMN_MESSAGE_FILEPATH = "FilePath";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_MESSAGE_ID, COLUMN_MESSAGE_CONVERSATIONUID, COLUMN_MESSAGE_SENDERID,
            COLUMN_MESSAGE_ADDRESSEEID, COLUMN_MESSAGE_MESSAGE, COLUMN_MESSAGE_DATE, COLUMN_MESSAGE_TYPEID,
            COLUMN_MESSAGE_FILEPATH
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_MESSAGE_DATE + " DESC ", null);
    }

    public Cursor getCursorForConversation(String conversationUid) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_MESSAGE_CONVERSATIONUID + " = ?";
        return ValiaSQLiteHelper.getDB().rawQuery(sql, new String[]{conversationUid});
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_MESSAGE_ID + " = ?";
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, new String[]{String.valueOf(id)},
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Messages messages) {
        ContentValues contentValues = new ContentValues();
        if (messages.getIdMessage() > 0) {
            contentValues.put(COLUMN_MESSAGE_ID, messages.getIdMessage());
        }

        String conversationUid = (messages.getIdSender().compareTo(messages.getIdAddressee()) < 0)
                ? messages.getIdSender() + "_" + messages.getIdAddressee()
                : messages.getIdAddressee() + "_" + messages.getIdSender();

        contentValues.put(COLUMN_MESSAGE_CONVERSATIONUID, conversationUid);
        contentValues.put(COLUMN_MESSAGE_SENDERID, messages.getIdSender());
        contentValues.put(COLUMN_MESSAGE_ADDRESSEEID, messages.getIdAddressee());
        contentValues.put(COLUMN_MESSAGE_MESSAGE, messages.getMessage());

        if (messages.getDate() != null) {
            contentValues.put(COLUMN_MESSAGE_DATE, messages.getDate());
        }

        contentValues.put(COLUMN_MESSAGE_TYPEID, messages.getIdType());
        contentValues.put(COLUMN_MESSAGE_FILEPATH, messages.getPathFile());

        return contentValues;
    }

    public Messages toMessages(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_MESSAGE_ID);
        int idConversacion = cursor.getColumnIndex(COLUMN_MESSAGE_CONVERSATIONUID);
        int idRemitente = cursor.getColumnIndex(COLUMN_MESSAGE_SENDERID);
        int idDestinatario = cursor.getColumnIndex(COLUMN_MESSAGE_ADDRESSEEID);
        int message = cursor.getColumnIndex(COLUMN_MESSAGE_MESSAGE);
        int fecha = cursor.getColumnIndex(COLUMN_MESSAGE_DATE);
        int idTipo = cursor.getColumnIndex(COLUMN_MESSAGE_TYPEID);
        int rutaArchivo = cursor.getColumnIndex(COLUMN_MESSAGE_FILEPATH);

        Messages messages = new Messages();
        messages.setIdMessage(cursor.getInt(id));
        if (idConversacion >= 0) {
            messages.setConversationUid(cursor.getString(idConversacion));
        }

        messages.setIdSender(cursor.getString(idRemitente));
        messages.setIdAddressee(cursor.getString(idDestinatario));
        messages.setMessage(cursor.getString(message));
        if (cursor.getString(fecha) != null) {
            messages.setDate(cursor.getString(fecha));
        }
        messages.setIdType(cursor.getInt(idTipo));
        messages.setPathFile(cursor.getString(rutaArchivo));

        return messages;
    }

    // Insertar registros
    public long insert(Messages messages) {
        ContentValues reg = toContentValues(messages);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(Messages messages) {
        ContentValues reg = toContentValues(messages);
        long result = 0;

        if (reg.containsKey(COLUMN_MESSAGE_ID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            long id = reg.getAsLong(COLUMN_MESSAGE_ID);
            reg.remove(COLUMN_MESSAGE_ID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_MESSAGE_ID + " = ?",
                    new String[]{String.valueOf(id)});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_MESSAGE_ID + " = ?";
        Cursor checkExists = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{Integer.toString(id)});

        boolean exists = false;
        if (checkExists != null) {
            // Mover el cursor a la primera columna
            if (checkExists.moveToFirst()) {
                // Coger el resultado de la consulta y evaluar si es mayor o igual a 0
                exists = checkExists.getInt(0) >= 0;
            }
            checkExists.close();
        }

        return exists;
    }

    // Eliminar registros por id
    public long delete(long id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_MESSAGE_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Eliminar registros por idUsuario
    public long deleteAllByUserId(String id) {
        String whereClause = COLUMN_MESSAGE_SENDERID + " = ? OR " + COLUMN_MESSAGE_ADDRESSEEID + " = ?";
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, whereClause, new String[]{id, id});
    }

    // Recoger todos los messages enviados y recibidos
    public List<Messages> getAllMessages(String conversationUid) {
        String query = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + COLUMN_MESSAGE_CONVERSATIONUID + " = ?"
                + " ORDER BY " + COLUMN_MESSAGE_DATE + " ASC";

        String[] args = new String[]{conversationUid};
        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(query, args);

        List<Messages> messagesList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Messages message = toMessages(cursor);
                messagesList.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return messagesList;
    }
}
