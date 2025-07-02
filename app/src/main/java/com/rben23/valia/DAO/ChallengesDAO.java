package com.rben23.valia.DAO;

import android.content.ContentValues;
import android.database.Cursor;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Challenges;

import java.util.ArrayList;
import java.util.List;

public class ChallengesDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Challenges";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_CHALLENGE_ID = "ChallengeId";
    private static final String COLUMN_CHALLENGE_USERID = "UserId";
    private static final String COLUMN_CHALLENGE_ADDRESSEEID = "AddresseeId";
    private static final String COLUMN_CHALLENGE_DESCRIPTION = "Description";
    private static final String COLUMN_CHALLENGE_TOTALKM = "TotalKm";
    private static final String COLUMN_CHALLENGE_DATE = "Date";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_CHALLENGE_ID, COLUMN_CHALLENGE_USERID,
            COLUMN_CHALLENGE_ADDRESSEEID, COLUMN_CHALLENGE_DESCRIPTION, COLUMN_CHALLENGE_TOTALKM, COLUMN_CHALLENGE_DATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_CHALLENGE_DATE + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_CHALLENGE_ID + " = " + id;
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, null,
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Challenges challenges) {
        ContentValues contentValues = new ContentValues();
        if (challenges.getIdChallenge() > 0) {
            contentValues.put(COLUMN_CHALLENGE_ID, challenges.getIdChallenge());
        }
        contentValues.put(COLUMN_CHALLENGE_USERID, challenges.getIdUser());
        contentValues.put(COLUMN_CHALLENGE_ADDRESSEEID, challenges.getIdAddressee());
        contentValues.put(COLUMN_CHALLENGE_DESCRIPTION, challenges.getDescription());
        contentValues.put(COLUMN_CHALLENGE_TOTALKM, challenges.getTotalKm());
        if (challenges.getDate() != null) {
            contentValues.put(COLUMN_CHALLENGE_DATE, challenges.getDate());
        }
        return contentValues;
    }

    public Challenges toChallenges(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_CHALLENGE_ID);
        int idUsuario = cursor.getColumnIndex(COLUMN_CHALLENGE_USERID);
        int idDestinatario = cursor.getColumnIndex(COLUMN_CHALLENGE_ADDRESSEEID);
        int descripcion = cursor.getColumnIndex(COLUMN_CHALLENGE_DESCRIPTION);
        int kmTotales = cursor.getColumnIndex(COLUMN_CHALLENGE_TOTALKM);
        int fecha = cursor.getColumnIndex(COLUMN_CHALLENGE_DATE);

        Challenges challenges = new Challenges();
        challenges.setIdChallenge(cursor.getInt(id));
        challenges.setIdUser(cursor.getString(idUsuario));
        challenges.setIdAddressee(cursor.getString(idDestinatario));
        challenges.setDescription(cursor.getString(descripcion));
        challenges.setTotalKm(cursor.getDouble(kmTotales));
        if (cursor.getString(fecha) != null) {
            challenges.setDate(cursor.getString(fecha));
        }
        return challenges;
    }

    // Insertar registros
    public long insert(Challenges challenges) {
        ContentValues reg = toContentValues(challenges);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(Challenges challenges) {
        ContentValues reg = toContentValues(challenges);
        long result = 0;

        if (reg.containsKey(COLUMN_CHALLENGE_ID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            long id = reg.getAsLong(COLUMN_CHALLENGE_ID);
            reg.remove(COLUMN_CHALLENGE_ID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_CHALLENGE_ID + " = ?",
                    new String[]{Long.toString(id)});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_CHALLENGE_ID + " = ?";
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

    // Devolvemos retos por usuario
    public List<Challenges> getChallengesByUser(String userUid) {
        List<Challenges> challengeList = new ArrayList<>();

        String selection = COLUMN_CHALLENGE_USERID + " = ? OR " + COLUMN_CHALLENGE_ADDRESSEEID + " = ?";
        String[] selectionArgs = new String[]{userUid, userUid};

        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_CHALLENGE_DATE + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Challenges challenges = toChallenges(cursor);
                    challengeList.add(challenges);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return challengeList;
    }

    // Devolvemos los retos activos por usuario
    public List<Challenges> getActiveChallengesByUser(String userUid) {
        List<Challenges> activeChallenges = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE (" +
                COLUMN_CHALLENGE_USERID + " = ? OR " + COLUMN_CHALLENGE_ADDRESSEEID + " = ?) " +
                "AND " + COLUMN_CHALLENGE_ID + " NOT IN (SELECT " + COLUMN_CHALLENGE_ID + " FROM ResultsChallenges)";

        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{userUid, userUid});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Challenges challenge = toChallenges(cursor);
                activeChallenges.add(challenge);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return activeChallenges;
    }

    // Eliminar registros por id
    public long delete(long id) {
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_CHALLENGE_ID + " = " + id, null);
    }

    // Eliminar registros por idUsuario
    public long deleteAllByUserId(String id) {
        String whereClause = COLUMN_CHALLENGE_USERID + " = ? OR " + COLUMN_CHALLENGE_ADDRESSEEID + " = ?";
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, whereClause , new String[]{id, id});
    }
}
