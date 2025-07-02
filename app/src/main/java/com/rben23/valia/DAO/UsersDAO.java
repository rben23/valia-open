package com.rben23.valia.DAO;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Users;


public class UsersDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Users";

    // Nombre de las columnas de la tabla
    public static final String COLUMN_USER_UID = "Uid";
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_USER_PROFILEIMAGE = "ProfileImage";
    public static final String COLUMN_USER_NAME = "Name";
    public static final String COLUMN_USER_EMAIL = "Email";
    public static final String COLUMN_USER_IDROLE = "RoleId";
    public static final String COLUMN_USER_CREATIONDATE = "CreationDate";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_USER_UID, COLUMN_USER_ID, COLUMN_USER_PROFILEIMAGE, COLUMN_USER_NAME,
            COLUMN_USER_EMAIL, COLUMN_USER_IDROLE, COLUMN_USER_CREATIONDATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_USER_CREATIONDATE + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(String id) {
        String condition = COLUMN_USER_UID + " = ?";
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, new String[]{id},
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Users users) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_UID, users.getUid());
        contentValues.put(COLUMN_USER_ID, users.getUserId());
        contentValues.put(COLUMN_USER_PROFILEIMAGE, users.getProfileImage());
        contentValues.put(COLUMN_USER_NAME, users.getName());
        contentValues.put(COLUMN_USER_EMAIL, users.getEmail());
        contentValues.put(COLUMN_USER_IDROLE, users.getRoleId());
        if (users.getCreationDate() != null) {
            contentValues.put(COLUMN_USER_CREATIONDATE, users.getCreationDate());
        }

        return contentValues;
    }

    public Users toUsers(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        int uid = cursor.getColumnIndex(COLUMN_USER_UID);
        int idUser = cursor.getColumnIndex(COLUMN_USER_ID);
        int fotoPerfil = cursor.getColumnIndex(COLUMN_USER_PROFILEIMAGE);
        int nombre = cursor.getColumnIndex(COLUMN_USER_NAME);
        int email = cursor.getColumnIndex(COLUMN_USER_EMAIL);
        int idRol = cursor.getColumnIndex(COLUMN_USER_IDROLE);
        int fechaCreacion = cursor.getColumnIndex(COLUMN_USER_CREATIONDATE);

        Users users = new Users();
        users.setUid(cursor.getString(uid));
        users.setUserId(cursor.getString(idUser));
        users.setProfileImage(cursor.getString(fotoPerfil));
        users.setName(cursor.getString(nombre));
        users.setEmail(cursor.getString(email));
        users.setRoleId(cursor.getInt(idRol));
        users.setCreationDate(cursor.getString(fechaCreacion));

        return users;
    }

    public Users getCurrentUser() {
        // Inicializar Users
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Log.e("UserDAO", "No hay un usuario autenticado en Firebase");
            return null;
        }

        String currentUserUID = firebaseUser.getUid();
        try (Cursor cursor = getRecord(currentUserUID)) {
            if (cursor != null && cursor.moveToFirst()) {
                return toUsers(cursor);
            } else {
                Log.e("UserDAO", "No se ha encontrado ningún registro en base de datos");
            }
        }

        return null;
    }

    // Insertar registros
    public long insert(Users users) {
        ContentValues reg = toContentValues(users);
        return ValiaSQLiteHelper.getDB().insertWithOnConflict(TABLE_NAME, null, reg,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Actualizar registros por id
    public long update(Users users) {
        ContentValues reg = toContentValues(users);
        long result = 0;

        if (reg.containsKey(COLUMN_USER_UID)) {
            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_USER_UID + " = ?",
                    new String[]{users.getUid()});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(String id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor checkExists = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{id});

        boolean exists = false;
        if (checkExists != null) {
            // Mover el cursor a la primera columna
            if (checkExists.moveToFirst()) {
                // Coger el resultado de la consulta y evaluar si es mayor o igual a 0
                exists = checkExists.getInt(0) >= 0;
                if (!exists) {
                    Log.e("UPDATE: ", "No existe el registro");
                }

            }
            checkExists.close();
        } else {
            Log.e("UPDATE: ", "Cursor nulo");
        }

        return exists;
    }

    // Eliminar registros por id
    public long delete(String id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_USER_ID + " = ?", new String[]{id});
    }
}
