package com.rben23.valia.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.ResultsChallenges;

public class ResultsChallengesDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "ResultsChallenges";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_RESULTCHALLENGE_ID = "ResultId";
    private static final String COLUMN_RESULTCHALLENGE_CHALLENGEID = "ChallengeId";
    private static final String COLUMN_RESULTCHALLENGE_USERID = "UserId";
    private static final String COLUMN_RESULTCHALLENGE_TIMEDURATION = "TimeDuration";
    private static final String COLUMN_RESULTCHALLENGE_DATE = "Date";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_RESULTCHALLENGE_ID, COLUMN_RESULTCHALLENGE_CHALLENGEID,
            COLUMN_RESULTCHALLENGE_USERID, COLUMN_RESULTCHALLENGE_TIMEDURATION, COLUMN_RESULTCHALLENGE_DATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_RESULTCHALLENGE_DATE + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_RESULTCHALLENGE_ID + " = ?";
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, new String[]{String.valueOf(id)},
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(ResultsChallenges resultsChallenges) {
        ContentValues contentValues = new ContentValues();
        if (resultsChallenges.getIdResult() > 0) {
            contentValues.put(COLUMN_RESULTCHALLENGE_ID, resultsChallenges.getIdResult());
        }
        contentValues.put(COLUMN_RESULTCHALLENGE_CHALLENGEID, resultsChallenges.getIdChallenge());
        contentValues.put(COLUMN_RESULTCHALLENGE_USERID, resultsChallenges.getIdUser());
        contentValues.put(COLUMN_RESULTCHALLENGE_TIMEDURATION, resultsChallenges.getTimeDuration());
        if (resultsChallenges.getDate() != null) {
            contentValues.put(COLUMN_RESULTCHALLENGE_DATE, resultsChallenges.getDate());
        }
        return contentValues;
    }

    public ResultsChallenges toResultChallenges(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_ID);
        int idReto = cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_CHALLENGEID);
        int idUsuario = cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_USERID);
        int tiempoDuracion = cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_TIMEDURATION);
        int fecha = cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_DATE);

        ResultsChallenges resultsChallenges = new ResultsChallenges();
        resultsChallenges.setIdResult(cursor.getInt(id));
        resultsChallenges.setIdChallenge(cursor.getInt(idReto));
        resultsChallenges.setIdUser(cursor.getString(idUsuario));
        resultsChallenges.setTimeDuration(cursor.getString(tiempoDuracion));
        if (cursor.getString(fecha) != null) {
            resultsChallenges.setDate(cursor.getString(fecha));
        }
        return resultsChallenges;
    }

    // Insertar registros
    public long insert(ResultsChallenges resultsChallenges) {
        ContentValues reg = toContentValues(resultsChallenges);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(ResultsChallenges resultsChallenges) {
        ContentValues reg = toContentValues(resultsChallenges);
        long result = 0;

        if (reg.containsKey(COLUMN_RESULTCHALLENGE_ID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            long id = reg.getAsLong(COLUMN_RESULTCHALLENGE_ID);
            reg.remove(COLUMN_RESULTCHALLENGE_ID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_RESULTCHALLENGE_ID + " = ?",
                    new String[]{String.valueOf(id)});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_RESULTCHALLENGE_ID + " = ?";
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

    // Buscamos si hay algun resultado para el reto y devolvemos ResultadosRetos
    @SuppressLint("Range")
    public ResultsChallenges getResultByChallengeId(int challengeId) {
        ResultsChallenges resultsChallenges = null;

        String selection = COLUMN_RESULTCHALLENGE_CHALLENGEID + " = ? AND " + COLUMN_RESULTCHALLENGE_CHALLENGEID + " > ?";
        String[] selectionArgs = new String[]{String.valueOf(challengeId), "0"};

        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String userId = cursor.getString(cursor.getColumnIndex(COLUMN_RESULTCHALLENGE_USERID));
                Log.d("DAO", "Para challengeId " + challengeId + " el IdUsuario es: " + userId);

                if (userId != null && !userId.trim().isEmpty()) {
                    resultsChallenges = toResultChallenges(cursor);
                } else {
                    Log.d("DAO", "Entrada vacía en challengeId " + challengeId);
                }
            }
            cursor.close();
        }
        return resultsChallenges;
    }

    // Devolvemos los retos ganados dutante esta semana
    public int getWinChallengesThisWeek(String currentUserUid) {
        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery("SELECT COALESCE(COUNT(*), 0) AS WinChallenges " +
                        "FROM " + TABLE_NAME + " WHERE " + COLUMN_RESULTCHALLENGE_USERID + " = ? " +
                        "AND " + COLUMN_RESULTCHALLENGE_DATE + " >= date('now', 'localtime', 'weekday 1', '-7 days') " +
                        "AND " + COLUMN_RESULTCHALLENGE_DATE + " < date('now', 'localtime', 'weekday 1')",
                new String[]{currentUserUid});

        int winChallenges = 0;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("WinChallenges");
            if (index != -1) {
                winChallenges = cursor.getInt(index);
            }
        }
        cursor.close();
        return winChallenges;
    }

    // Eliminar registros por id
    public long delete(long id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_RESULTCHALLENGE_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Eliminar registros por idUsuario
    public long deleteAllByUserId(String id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_RESULTCHALLENGE_USERID + " = ?", new String[]{id});
    }
}