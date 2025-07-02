package com.rben23.valia.DAO;

import android.content.ContentValues;
import android.database.Cursor;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Activities;

public class ActivitiesDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Activities";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_ACTIVITY_ID = "ActivityId";
    private static final String COLUMN_ACTIVITY_USERID = "UserId";
    private static final String COLUMN_ACTIVITY_NAME = "Name";
    private static final String COLUMN_ACTIVITY_TYPEID = "TypeId";
    private static final String COLUMN_ACTIVITY_DATE = "Date";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_ACTIVITY_ID, COLUMN_ACTIVITY_USERID,
            COLUMN_ACTIVITY_NAME, COLUMN_ACTIVITY_TYPEID, COLUMN_ACTIVITY_DATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_ACTIVITY_DATE + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_ACTIVITY_ID + " = " + id;
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, null,
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Activities activities) {
        ContentValues contentValues = new ContentValues();
        if (activities.getIdActivity() > 0) {
            contentValues.put(COLUMN_ACTIVITY_ID, activities.getIdActivity());
        }
        contentValues.put(COLUMN_ACTIVITY_USERID, activities.getIdUser());
        contentValues.put(COLUMN_ACTIVITY_NAME, activities.getName());
        contentValues.put(COLUMN_ACTIVITY_TYPEID, activities.getIdType());
        if (activities.getDate() != null) {
            contentValues.put(COLUMN_ACTIVITY_DATE, activities.getDate());
        }

        return contentValues;
    }

    public Activities toActivities(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_ACTIVITY_ID);
        int idUsuario = cursor.getColumnIndex(COLUMN_ACTIVITY_USERID);
        int nombre = cursor.getColumnIndex(COLUMN_ACTIVITY_NAME);
        int idTipo = cursor.getColumnIndex(COLUMN_ACTIVITY_TYPEID);
        int fecha = cursor.getColumnIndex(COLUMN_ACTIVITY_DATE);

        Activities activities = new Activities();
        activities.setIdActivity(cursor.getInt(id));
        activities.setIdUser(cursor.getString(idUsuario));
        activities.setName(cursor.getString(nombre));
        activities.setIdType(cursor.getInt(idTipo));
        if (cursor.getString(fecha) != null) {
            activities.setDate(cursor.getString(fecha));
        }

        return activities;
    }

    // Insertar registros
    public long insert(Activities activities) {
        ContentValues reg = toContentValues(activities);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(Activities activities) {
        ContentValues reg = toContentValues(activities);
        long result = 0;

        if (reg.containsKey(COLUMN_ACTIVITY_ID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            long id = reg.getAsLong(COLUMN_ACTIVITY_ID);
            reg.remove(COLUMN_ACTIVITY_ID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_ACTIVITY_ID + " = " + id,
                    null);
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ACTIVITY_ID + " = ?";
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
    public long delete(int id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_ACTIVITY_ID + " = " + id, null);
    }

    // Buscar actividades unicas
    public Activities getActivityByUniqueFields(String nombre, int idTipo, String fecha, String idUsuario) {
        Activities activity = null;

        String selection = COLUMN_ACTIVITY_NAME + "= ? AND "
                + COLUMN_ACTIVITY_TYPEID + "= ? AND "
                + COLUMN_ACTIVITY_DATE + "= ? AND "
                + COLUMN_ACTIVITY_USERID + "= ?";

        String[] selectionArgs = new String[]{nombre, String.valueOf(idTipo), fecha, idUsuario};

        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // Si existe devolvemos un objeto
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                activity = toActivities(cursor);
            }
            cursor.close();
        }
        return activity;
    }
}
