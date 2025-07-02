package com.rben23.valia.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Routes;
import com.rben23.valia.models.Users;
import com.rben23.valia.models.joins.ActivityRouteJoin;

import java.util.ArrayList;
import java.util.List;

public class RoutesDAO {
    // Nombre de la tabla
    public static final String TABLE_NAME = "Routes";

    // Nombre de las columnas de la tabla
    private static final String COLUMN_ROUTE_ID = "RouteId";
    private static final String COLUMN_ROUTE_ACTIVITYID = "ActivityId";
    private static final String COLUMN_ROUTE_NAME = "Name";
    private static final String COLUMN_ROUTE_PATHROUTE = "PathRoute";
    private static final String COLUMN_ROUTE_TIMEDURATION = "TimeDuration";
    private static final String COLUMN_ROUTE_TOTALKM = "TotalKm";

    // Lista de columnas para utilizar en las consultas
    private String[] columns = new String[]{COLUMN_ROUTE_ID, COLUMN_ROUTE_ACTIVITYID,
            COLUMN_ROUTE_NAME, COLUMN_ROUTE_PATHROUTE, COLUMN_ROUTE_TIMEDURATION, COLUMN_ROUTE_TOTALKM
    };

    // Cursor con todas las filas y columnas de la tabla
    public Cursor getCursor() {
        return ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, null, null,
                null, null, null, null);
    }

    // Obtener registro por Id
    public Cursor getRecord(long id) {
        String condition = COLUMN_ROUTE_ID + " = ?";
        Cursor cursor = ValiaSQLiteHelper.getDB().query(true, TABLE_NAME, columns, condition, new String[]{String.valueOf(id)},
                null, null, null, null);

        // Mover al primer registro
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private ContentValues toContentValues(Routes routes) {
        ContentValues contentValues = new ContentValues();
        if (routes.getIdRoute() > 0) {
            contentValues.put(COLUMN_ROUTE_ID, routes.getIdRoute());
        }
        contentValues.put(COLUMN_ROUTE_ACTIVITYID, routes.getIdActivity());
        contentValues.put(COLUMN_ROUTE_NAME, routes.getName());
        contentValues.put(COLUMN_ROUTE_PATHROUTE, routes.getPathRoute());
        contentValues.put(COLUMN_ROUTE_TIMEDURATION, routes.getTiemDuration());
        contentValues.put(COLUMN_ROUTE_TOTALKM, routes.getTotalKm());

        return contentValues;
    }

    public Routes toRoutes(Cursor cursor) {
        int id = cursor.getColumnIndex(COLUMN_ROUTE_ID);
        int idActividad = cursor.getColumnIndex(COLUMN_ROUTE_ACTIVITYID);
        int nombre = cursor.getColumnIndex(COLUMN_ROUTE_NAME);
        int recorrido = cursor.getColumnIndex(COLUMN_ROUTE_PATHROUTE);
        int tiempoDuracion = cursor.getColumnIndex(COLUMN_ROUTE_TIMEDURATION);
        int kmTotales = cursor.getColumnIndex(COLUMN_ROUTE_TOTALKM);

        Routes routes = new Routes();
        routes.setIdRoute(cursor.getInt(id));
        routes.setIdActivity(cursor.getInt(idActividad));
        routes.setName(cursor.getString(nombre));
        routes.setPathRoute(cursor.getString(recorrido));
        routes.setTiemDuration(cursor.getString(tiempoDuracion));
        routes.setTotalKm(cursor.getFloat(kmTotales));

        return routes;
    }

    // Insertar registros
    public long insert(Routes routes) {
        ContentValues reg = toContentValues(routes);
        return ValiaSQLiteHelper.getDB().insert(TABLE_NAME, null, reg);
    }

    // Actualizar registros por id
    public long update(Routes routes) {
        ContentValues reg = toContentValues(routes);
        long result = 0;

        if (reg.containsKey(COLUMN_ROUTE_ID)) {
            // Se obtiene el id y se elimina de los valores a actualizar
            long id = reg.getAsLong(COLUMN_ROUTE_ID);
            reg.remove(COLUMN_ROUTE_ID);

            // Se actualiza el registro con el id extraido
            result = ValiaSQLiteHelper.getDB().update(TABLE_NAME, reg, COLUMN_ROUTE_ID + " = ?",
                    new String[]{Long.toString(id)});
        }

        return result;
    }

    // Comprobar existencias
    public boolean exists(int id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ROUTE_ID + " = ?";
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
        return ValiaSQLiteHelper.getDB().delete(TABLE_NAME, COLUMN_ROUTE_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // == CONSULTAS PARA VISTAS ==

    // Consultamos 3 tablas para estructurar los datos
    @SuppressLint("Range")
    public List<ActivityRouteJoin> getActivityRoutes() {
        List<ActivityRouteJoin> list = new ArrayList<>();

        String joinQuery = "SELECT " +
                "r." + COLUMN_ROUTE_ID + ", " +
                "r." + COLUMN_ROUTE_ACTIVITYID + ", " +
                "r." + COLUMN_ROUTE_NAME + ", " +
                "r." + COLUMN_ROUTE_PATHROUTE + ", " +
                "r." + COLUMN_ROUTE_TIMEDURATION + ", " +
                "r." + COLUMN_ROUTE_TOTALKM + ", " +
                "a.Name AS ActivityName, " +
                "ta.TypeId AS TypeId, " +
                "ta.Name AS TypeName " +
                "FROM " + TABLE_NAME + " r " +
                "INNER JOIN Activities a ON r." + COLUMN_ROUTE_ACTIVITYID + " = a.ActivityId " +
                "INNER JOIN ActivitiesTypes ta ON a.TypeId = ta.TypeId " +
                "WHERE a.UserId = ? " +
                "ORDER BY a.Date DESC";

        Users currentUser = ValiaSQLiteHelper.getUsersDAO().getCurrentUser();

        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(joinQuery, new String[]{currentUser.getUid()});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ActivityRouteJoin activityRouteJoin = new ActivityRouteJoin();

                    // Extraemos los datos de las tablas (ROUTES)
                    activityRouteJoin.setIdRoute(cursor.getInt(cursor.getColumnIndex(COLUMN_ROUTE_ID)));
                    activityRouteJoin.setIdActivity(cursor.getInt(cursor.getColumnIndex(COLUMN_ROUTE_ACTIVITYID)));
                    activityRouteJoin.setRouteName(cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_NAME)));
                    activityRouteJoin.setPathRoute(cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_PATHROUTE)));
                    activityRouteJoin.setTiemDuration(cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_TIMEDURATION)));
                    activityRouteJoin.setTotalKm(cursor.getFloat(cursor.getColumnIndex(COLUMN_ROUTE_TOTALKM)));

                    // Extraemos los datos de las tablas (ACTIVIDADES Y TIPOS ACTIVIDADES)
                    activityRouteJoin.setIdUser(cursor.getString(cursor.getColumnIndex("ActivityName")));
                    activityRouteJoin.setIdType(cursor.getInt(cursor.getColumnIndex("TypeId")));
                    activityRouteJoin.setNameType(cursor.getString(cursor.getColumnIndex("TypeName")));

                    list.add(activityRouteJoin);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    // Obtenemos el total de KM de la semana actual
    public float getKmThisWeek(String userUid) {
        String sql =
                "SELECT COALESCE(SUM(" + COLUMN_ROUTE_TOTALKM + "), 0) AS weekKm " +
                        "FROM Routes r " +
                        "JOIN Activities a ON r.ActivityId = a.ActivityId " +
                        "WHERE a.Date >= date('now', 'localtime', 'weekday 1', '-7 days') " + // Desde el lunes de la semana pasada
                        "AND a.Date < date('now', 'localtime', 'weekday 1') " + // Hasta el lunes de la semana actual
                        "AND a.UserId = ?"; // Filtrar por el usuario

        float kmWeek = 0f;
        try (Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(sql, new String[]{userUid})) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("weekKm");
                if (index != -1) {
                    kmWeek = cursor.getFloat(index);
                }
            }
        }
        return kmWeek;
    }

    // Obtenemos el total de Horas de la semana actual
    public String getHoursThisWeek(String userUid) {
        String sql =
                "SELECT CAST(COALESCE(SUM(" + COLUMN_ROUTE_TIMEDURATION + ")/60, 0) AS INTEGER) AS weekTime " +
                        "FROM Routes r " +
                        "JOIN Activities a ON r.ActivityId = a.ActivityId " +
                        "WHERE a.Date >= date('now', 'localtime', 'weekday 1', '-7 days') " + // Desde el lunes de la semana pasada
                        "AND a.Date < date('now', 'localtime', 'weekday 1') " + // Hasta el lunes de la semana actual
                        "AND a.UserId = ?"; // Filtrar por el usuario

        String timeWeek = "0";
        try (Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(sql, new String[]{userUid})) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("weekTime");
                if (index != -1) {
                    timeWeek = cursor.getString(index);
                }
            }
        }
        return timeWeek;
    }

    // Devolvemos las rutas que coincidan con los campos únicos
    public Routes getRouteByUniqueFields(String nombre, String recorrido, String tiempoDuracion, String kmTotales) {
        Routes route = null;

        String selection = COLUMN_ROUTE_NAME + "= ? AND "
                + COLUMN_ROUTE_PATHROUTE + "= ? AND "
                + COLUMN_ROUTE_TIMEDURATION + "= ? AND "
                + COLUMN_ROUTE_TOTALKM + "= ?";

        String[] selectionArgs = new String[]{nombre, recorrido, tiempoDuracion, kmTotales};

        Cursor cursor = ValiaSQLiteHelper.getDB().query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // Si existe devolvemos un objeto
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                route = toRoutes(cursor);
            }
            cursor.close();
        }
        return route;
    }

    // Recogemos los Km recorridos desde la fecha de inicio del reto hasta la actual
    @SuppressLint("Range")
    public double getKmForChallenge(String dateIni) {
        double totalKm = 0;
        String query = "SELECT SUM(r." + COLUMN_ROUTE_TOTALKM + ") as TotalKm " +
                "FROM " + TABLE_NAME + " r " +
                "INNER JOIN Activities a ON r." + COLUMN_ROUTE_ACTIVITYID + " = a.ActivityId " +
                "WHERE a.Date >= ?";

        Cursor cursor = ValiaSQLiteHelper.getDB().rawQuery(query, new String[]{dateIni});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalKm = cursor.getDouble(cursor.getColumnIndex("TotalKm"));
            }
            cursor.close();
        }
        return totalKm;
    }
}
