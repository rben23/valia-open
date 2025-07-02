package com.rben23.valia.adapters;

import android.content.Context;

import com.rben23.valia.R;

public enum ActivityTypes {
    CAMINATA(R.string.list_routeWalk, R.drawable.img_rut_walk),
    SENDERISMO(R.string.list_routeHiking, R.drawable.img_rut_hiking),
    RUNNING(R.string.list_routeRunning, R.drawable.img_rut_running),
    CICLISMO(R.string.list_routeCycling, R.drawable.img_rut_cycling),
    CICLISMO_MONTANYA(R.string.list_routeMountainBike, R.drawable.img_rut_mountain_bike),
    A_CABALLO(R.string.list_routeHorseRiding, R.drawable.img_rut_horse_riding),
    PATINAJE(R.string.list_routeSkating, R.drawable.img_rut_skating),
    RAFTING(R.string.list_routeRafting, R.drawable.img_rut_rafting),
    KAYAK(R.string.list_routeKayak, R.drawable.img_rut_kayak),
    MIXTO(R.string.list_routeMix, R.drawable.img_rut_mix);

    private final int showName;
    private final int iconResource;


    ActivityTypes(int showName, int iconResource) {
        this.showName = showName;
        this.iconResource = iconResource;
    }

    public int getName() {
        return showName;
    }

    public int getIcon() {
        return iconResource;
    }

    // Obtenemos el ActivityTypes a partir del nombre otorgado
    public static ActivityTypes fromName(String name, Context context) {
        // Recorremos todas las constantes
        for (ActivityTypes types : ActivityTypes.values()) {
            // Obtenemos el string asociado
            String typeName = context.getString(types.getName());
            if (typeName.equalsIgnoreCase(name)) {
                return types;
            }
        }
        // Si no se encuentra nada entonces mixto
        return MIXTO;
    }
}
