package com.rben23.valia.appFragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.FriendListAdapter;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Friends;
import com.rben23.valia.models.Users;

import java.util.List;

public class SocialFragment extends Fragment {
    private ValiaSQLiteHelper vsql;

    private FriendListAdapter friendListAdapter;
    private List<Friends> friendsList;
    private String currentUser;

    private RecyclerView recyclerFriends;
    private ConstraintLayout friendsListEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Mostrar vista
        View view = inflater.inflate(R.layout.fragment_social, container, false);

        initDatabase(view);
        initViews(view);
        initListAdapter(view);
        updateUI();

        return view;
    }

    private void initDatabase(View view) {
        // Iniciamos la conexion y Obtenemos el usuario actual
        vsql = ValiaSQLiteHelper.getInstance(view.getContext());
        Users users = vsql.getUsersDAO().getCurrentUser();
        currentUser = users.getUid();
    }

    private void initViews(View view) {
        // Elementos de la vista
        recyclerFriends = view.findViewById(R.id.recyclerFriends);
        friendsListEmpty = view.findViewById(R.id.friendsListEmpty);
    }

    private void initListAdapter(View view) {
        // Inicializamos la lista y el adaptador
        friendsList = vsql.getFriendsDAO().getFriendsByUser(currentUser);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(view.getContext()));
        friendListAdapter = new FriendListAdapter(this.getContext(), friendsList, currentUser);
        recyclerFriends.setAdapter(friendListAdapter);
    }

    // Obtenemos la lista y mostramos u ocultamos los views
    private void updateUI() {
        new Thread(() -> {
            List<Friends> data = vsql.getFriendsDAO().getFriendsByUser(currentUser);

            if (getView() != null) {
                // Comprobamos que hay datos para mostrar, si hay se muestran y si no se muestra layout empty
                getView().post(() -> {
                    if (data != null && !data.isEmpty()) {
                        recyclerFriends.setVisibility(VISIBLE);
                        friendsListEmpty.setVisibility(GONE);

                        friendListAdapter.updateItems(data);
                    } else {
                        recyclerFriends.setVisibility(GONE);
                        friendsListEmpty.setVisibility(VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}