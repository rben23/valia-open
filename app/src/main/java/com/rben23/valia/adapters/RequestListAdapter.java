package com.rben23.valia.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.FriendRequests;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;
import com.rben23.valia.managements.ProfileImageManagement;

import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.ViewHolder> {
    private List<FriendRequests> requests;
    private Context context;
    ValiaSQLiteHelper vsql;
    FirebaseManager firebaseManager;

    public RequestListAdapter(Context context, List<FriendRequests> requests) {
        this.requests = requests;
        this.context = context;

        vsql = ValiaSQLiteHelper.getInstance(context);

        // Recuperamos el usuario actual
        Users currentUser = vsql.getUsersDAO().getCurrentUser();
        firebaseManager = FirebaseManager.getInstance(currentUser.getUid());
    }

    @NonNull
    @Override
    public RequestListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_request_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestListAdapter.ViewHolder holder, int position) {
        final FriendRequests friendRequests = requests.get(position);
        final String senderUid = friendRequests.getIdUser();

        // Buscamos la información del usuario en firebase y la cargamos
        firebaseManager.getUsersSyncManager().getUserByUid(senderUid, user -> {
            if (user != null) {
                holder.userName.setText(user.getUserId());

                ProfileImageManagement profileImageManagement = new ProfileImageManagement(context, user.getUid());
                profileImageManagement.getRemoteUserImageProfile(holder.profileIcon);

            } else {
                holder.userName.setText(context.getString(R.string.unknownUser));
                holder.profileIcon.setImageResource(R.drawable.img_default_user_image);
            }
        });

        holder.btnAccept.setOnClickListener(view -> acceptRequest(holder));
        holder.btnDiscard.setOnClickListener(view -> discardRequest(holder));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }


    public void updateItems(List<FriendRequests> newItems) {
        this.requests = newItems;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView userName;
        Button btnAccept, btnDiscard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIcon = itemView.findViewById(R.id.profileIcon);
            userName = itemView.findViewById(R.id.userName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDiscard = itemView.findViewById(R.id.btnDiscart);
        }
    }

    private void acceptRequest(@NonNull ViewHolder holder) {
        // Se gestiona aquí la posicion para operar sobre el item correcto incluso si la lista cambia
        int currentPosition = holder.getAdapterPosition();

        if (currentPosition == RecyclerView.NO_POSITION) return;

        FriendRequests solicitud = requests.get(currentPosition);
        solicitud.setIdState(1); // Aceptado

        long affected = vsql.getFriendRequestsDAO().update(solicitud);
        Log.d("UPDATE_SOLICITUD", "Filas actualizadas: " + affected);

        // Ordenamos los UIDs para almacenar una única entrada
        String uid1 = solicitud.getIdUser();
        String uid2 = solicitud.getIdAddressee();

        // Verificamos que no sean iguales
        if (uid1.equals(uid2)) {
            Log.e("ErrorAmistad", "No se puede crear una amistad donde el usuario sea él mismo. - " + uid1 + " - " + uid2);
            return;
        }

        String orderedUid1, orderedUid2;
        if (uid1.compareTo(uid2) <= 0) {
            orderedUid1 = uid1;
            orderedUid2 = uid2;
        } else {
            orderedUid1 = uid2;
            orderedUid2 = uid1;
        }

        // Creamos un único registro de amistad utilizando los UIDs ordenados
        Friends friend = new Friends();
        friend.setIdUser(orderedUid1);
        friend.setIdFriend(orderedUid2);
        vsql.getFriendsDAO().insert(friend);

        firebaseManager.getFriendRequestsSyncManager().synchronizeToRemoteDatabase(() -> Log.i("Request List sync", "Sincronizacion completada"));
        firebaseManager.getFriendsSyncManager().synchronizeToRemoteDatabase(() -> Log.i("Request List sync", "Sincronizacion completada"));
        firebaseManager.getUsersSyncManager().syncFriendUsers(() ->
                Log.i("Request List sync", "Sincronizacion completada"));

        // Eliminamos el item de la lista
        requests.remove(currentPosition);
        notifyItemRemoved(currentPosition);
    }

    private void discardRequest(@NonNull ViewHolder holder) {
        // Se gestiona aquí la posicion para operar sobre el item correcto incluso si la lista cambia
        int currentPosition = holder.getAdapterPosition();

        if (currentPosition == RecyclerView.NO_POSITION) return;

        FriendRequests solicitud = requests.get(currentPosition);
        solicitud.setIdState(2); // Rechazado

        long affected = vsql.getFriendRequestsDAO().update(solicitud);
        Log.d("UPDATE_SOLICITUD", "Filas actualizadas: " + affected);

        firebaseManager.getFriendRequestsSyncManager().synchronizeToRemoteDatabase(() -> Log.i("Request List sync", "Sincronizacion completada"));

        // Actualizamos la lista
        requests.remove(currentPosition);
        notifyItemRemoved(currentPosition);

        Toast.makeText(holder.itemView.getContext(), context.getString(R.string.tst_requestRejected), Toast.LENGTH_SHORT).show();
    }
}
