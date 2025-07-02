package com.rben23.valia.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.FriendRequests;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "FriendRequests";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_FRIENDREQUESTS_ID = "RequestId";
    private static final String COLUMN_FRIENDREQUESTS_USERID = "UserId";
    private static final String COLUMN_FRIENDREQUESTS_ADDRESSEEID = "AddresseeId";
    private static final String COLUMN_FRIENDREQUESTS_STATEID = "StateId";
    private static final String COLUMN_FRIENDREQUESTS_REQUESTDATE = "RequestDate";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_FRIENDREQUESTS_ID, COLUMN_FRIENDREQUESTS_USERID,
            COLUMN_FRIENDREQUESTS_ADDRESSEEID, COLUMN_FRIENDREQUESTS_STATEID, COLUMN_FRIENDREQUESTS_REQUESTDATE
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, COLUMN_FRIENDREQUESTS_REQUESTDATE + " DESC ", null);
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_FRIENDREQUESTS_ID + " = " + id;
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, null,
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(FriendRequests friendRequests) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FRIENDREQUESTS_ID, friendRequests.getIdRequest());
        contentValues.put(COLUMN_FRIENDREQUESTS_USERID, friendRequests.getIdUser());
        contentValues.put(COLUMN_FRIENDREQUESTS_ADDRESSEEID, friendRequests.getIdAddressee());
        contentValues.put(COLUMN_FRIENDREQUESTS_STATEID, friendRequests.getIdState());
        if (friendRequests.getDateRequest() != null) {
            contentValues.put(COLUMN_FRIENDREQUESTS_REQUESTDATE, friendRequests.getDateRequest());
        }
        return contentValues;
    }

    public FriendRequests toFriendRequests(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_ID);
        int idUsuario = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_USERID);
        int idDestinatario = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_ADDRESSEEID);
        int idEstado = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_STATEID);
        int fecha = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_REQUESTDATE);

        FriendRequests friendRequests = new FriendRequests();
        friendRequests.setIdRequest(cursor.getInt(id));
        friendRequests.setIdUser(cursor.getString(idUsuario));
        friendRequests.setIdAddressee(cursor.getString(idDestinatario));
        friendRequests.setIdState(cursor.getInt(idEstado));
        friendRequests.setDateRequest(cursor.getString(fecha));

        return friendRequests;
    }

    // Insertar registros
    public long insert(FriendRequests friendRequests) {
        ContentValues reg = toContentValues(friendRequests);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(FriendRequests friendRequests) {
        ContentValues reg = toContentValues(friendRequests);
        long id = friendRequests.getIdRequest();

        long result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg,
                COLUMN_FRIENDREQUESTS_ID + " = ? ",
                new String[]{String.valueOf(id)});

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_FRIENDREQUESTS_ID + " = ?";
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
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_FRIENDREQUESTS_ID + " = " + id, null);
    }


    // Eliminar registros por idUsuario
    public long deleteAllByUserId(String id) {
        String whereClause = COLUMN_FRIENDREQUESTS_USERID + " = ? OR " + COLUMN_FRIENDREQUESTS_ADDRESSEEID + " = ?";
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, whereClause, new String[]{id, id});
    }

    // Devolvemos las solicitudes de amistad
    public List<FriendRequests> getFriendRequestsByAddressee(String addresseeId, int stateId) {
        List<FriendRequests> requestList = new ArrayList<>();

        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME, null,
                COLUMN_FRIENDREQUESTS_ADDRESSEEID + " = ? AND " + COLUMN_FRIENDREQUESTS_STATEID + " = ? ",
                new String[]{addresseeId, "0"}, null, null, COLUMN_FRIENDREQUESTS_REQUESTDATE + " ASC");
        Log.i("INFO-QUERY-SOLAMISTAD", "DESTINATARIO: " + addresseeId + " ESTADO: " + stateId);
        Log.i("INFO-QUERY-SOLAMISTAD", "FILAS ENCONTRADAS: " + cursor.getCount());

        // Si tenemos resultados los asignamos al objeto SolicitudesAmistad
        if (cursor.moveToFirst()) {
            int columnIdSolicitud = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_ID);
            int columnIdUsuario = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_USERID);
            int columnIdDestinatario = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_ADDRESSEEID);
            int columnIdEstado = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_STATEID);
            int columnFechaSolicitud = cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_REQUESTDATE);

            do {
                FriendRequests request = new FriendRequests();
                request.setIdRequest(cursor.getInt(columnIdSolicitud));
                request.setIdUser(cursor.getString(columnIdUsuario));
                request.setIdAddressee(cursor.getString(columnIdDestinatario));
                request.setIdState(cursor.getInt(columnIdEstado));
                request.setDateRequest(cursor.getString(columnFechaSolicitud));

                requestList.add(request);
            } while (cursor.moveToNext());
        }
        // Cerramos el cursor y devolvemos la lista
        cursor.close();

        return requestList;
    }

    // Devolver el estado de la solicitud
    @SuppressLint("Range")
    public int getFriendRequestStatus(String userUid, String addresseeUid) {
        String query = "SELECT " + COLUMN_FRIENDREQUESTS_STATEID + " FROM " + TABLE_NAME +
                " WHERE ( " + COLUMN_FRIENDREQUESTS_USERID + " = ? AND " + COLUMN_FRIENDREQUESTS_ADDRESSEEID + " = ? )" +
                " OR ( " + COLUMN_FRIENDREQUESTS_USERID + " = ? AND " + COLUMN_FRIENDREQUESTS_ADDRESSEEID + " = ? )";

        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{userUid, addresseeUid, addresseeUid, userUid});
        int status = -1;

        if (cursor.moveToFirst()) {
            status = cursor.getInt(cursor.getColumnIndex(COLUMN_FRIENDREQUESTS_STATEID)); // -1 No hay solicitudes | 0 Solicitud pendiente | 1 Solicitud aceptada
        }

        cursor.close();
        return status;
    }
}
