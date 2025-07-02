package com.rben23.valia.managements.chatsManagements;

import android.text.TextUtils;
import android.util.Log;

import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Activities;
import com.rben23.valia.models.Routes;

public class RouteMessageProcessor {

    public RouteMessageProcessor() {
    }

    public String parseAndStoreRoute(String routeDataString) {
        if (TextUtils.isEmpty(routeDataString)) return null;
        Log.e("STRING: ", routeDataString);

        // Separamos el string con el formato definido
        String[] parts = routeDataString.split("!");
        if (parts.length < 2) return null;

        String[] activityData = parts[0].split("#");
        if (activityData.length < 4) return null;

        String[] routeData = parts[1].split("#");
        if (routeData.length < 4) return null;

        long activityId = getActivityId(parts);
        long routeId = getRouteId(parts, (int) activityId);

        return String.valueOf(routeId);
    }

    // Recogemos la actividad
    private static long getActivityId(String[] parts) {
        // Parseamos la parte de las actividades
        String[] activityData = parts[0].split("#");

        String actividadNombre = activityData[0];
        int actividadIdTipo = Integer.parseInt(activityData[1]);
        String actividadFecha = activityData[2];
        String actividadIdUsuario = activityData[3];

        Activities checkActivity = ValiaSQLiteHelper.getActivitiesDAO()
                .getActivityByUniqueFields(actividadNombre, actividadIdTipo, actividadFecha, actividadIdUsuario);

        long activityId;
        if (checkActivity != null) {
            activityId = checkActivity.getIdActivity();
        } else {
            Activities newActivity = new Activities();
            newActivity.setName(actividadNombre);
            newActivity.setIdType(actividadIdTipo);
            newActivity.setDate(actividadFecha);
            newActivity.setIdUser(actividadIdUsuario);
            activityId = ValiaSQLiteHelper.getActivitiesDAO().insert(newActivity);
        }
        return activityId;
    }

    // Recogemos la ruta
    private static long getRouteId(String[] parts, int activityId) {
        // Parseamos la parte de las rutas
        String[] routeData = parts[1].split("#");

        String rutaNombre = routeData[0];
        String recorrido = routeData[1];
        String tiempoDuracion = routeData[2];
        String kmTotales = routeData[3];

        Routes checkRoutes = ValiaSQLiteHelper.getRoutesDAO()
                .getRouteByUniqueFields(rutaNombre, recorrido, tiempoDuracion, kmTotales);

        long routeId;
        if (checkRoutes != null) {
            routeId = checkRoutes.getIdRoute();
        } else {
            Routes nuevaRuta = new Routes();
            nuevaRuta.setIdActivity(activityId);
            nuevaRuta.setName(rutaNombre);
            nuevaRuta.setPathRoute(recorrido);
            nuevaRuta.setTiemDuration(tiempoDuracion);
            nuevaRuta.setTotalKm(Float.parseFloat(kmTotales));
            routeId = (int) ValiaSQLiteHelper.getRoutesDAO().insert(nuevaRuta);
        }
        return routeId;
    }
}
