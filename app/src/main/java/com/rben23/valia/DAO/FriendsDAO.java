package com.rben23.valia.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;

import java.util.ArrayList;
import java.util.List;

public class FriendsDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Friends";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_FRIEND_USERID = "UserId";
    private static final String COLUMN_FRIEND_FRIENDID = "FriendId";
    private static final String COLUMN_FRIEND_FRIENDSHIPDATE = "FriendshipDate";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_FRIEND_USERID, COLUMN_FRIEND_FRIENDID,
            COLUMN_FRIEND_FRIENDSHIPDATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_FRIEND_FRIENDID + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(String id) {
        String condition = COLUMN_FRIEND_FRIENDID + " = ?";
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, new String[]{id},
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Friends friends) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FRIEND_USERID, friends.getIdUser());
        contentValues.put(COLUMN_FRIEND_FRIENDID, friends.getIdFriend());
        contentValues.put(COLUMN_FRIEND_FRIENDSHIPDATE, friends.getFriendshipDate());

        return contentValues;
    }

    @SuppressLint("Range")
    public Friends toFriends(Cursor cursor) {
        Friends friends = new Friends();

        String userId = cursor.getString(cursor.getColumnIndex(COLUMN_FRIEND_USERID));
        String friendId = cursor.getString(cursor.getColumnIndex(COLUMN_FRIEND_FRIENDID));
        String date = cursor.getString(cursor.getColumnIndex(COLUMN_FRIEND_FRIENDSHIPDATE));

        friends.setIdUser(userId);
        friends.setIdFriend(friendId);
        friends.setFriendshipDate(date);

        return friends;
    }

    // Insertar registros
    public long insert(Friends friends) {
        ContentValues reg = toContentValues(friends);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(Friends friends) {
        ContentValues reg = toContentValues(friends);
        long result = 0;

        if (reg.containsKey(COLUMN_FRIEND_USERID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            String id = reg.getAsString(COLUMN_FRIEND_USERID);
            Log.i("UID USER: ", id);
            reg.remove(COLUMN_FRIEND_USERID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_FRIEND_USERID + " = ?",
                    new String[]{id});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(String userUid, String friendUid) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_FRIEND_USERID + " = ? AND " +
                COLUMN_FRIEND_FRIENDID + " = ?";
        Cursor checkExists = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{userUid, friendUid});

        boolean exists = false;
        if (checkExists != null) {
            // Mover el cursor a la primera columna
            exists = checkExists.moveToFirst();
            checkExists.close();
        } else {
            Log.e("UPDATE: ", "Cursor nulo");
        }

        return exists;
    }


    // Eliminar registros por id
    public long delete(String id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_FRIEND_USERID + " = ?", new String[]{id});
    }

    // Eliminar registros por idUsuario
    public long deleteAllByUserId(String id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_FRIEND_USERID + " = ?", new String[]{id});
    }

    // Devolver amigos por usuario
    public List<Friends> getFriendsByUser(String currentUser) {
        List<Friends> friendsList = new ArrayList<>();

        if (currentUser == null) {
            throw new IllegalArgumentException("El userUid no puede ser null");
        }

        // Obtenemos los registros donde el usuario es el amigo o el usuario (IdAmigo, IdUsuario)
        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME, null,
                COLUMN_FRIEND_USERID + " = ? OR " + COLUMN_FRIEND_FRIENDID + " = ?",
                new String[]{currentUser, currentUser}, null, null,
                COLUMN_FRIEND_FRIENDSHIPDATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Friends friends = toFriends(cursor);
                friendsList.add(friends);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return friendsList;
    }
}
