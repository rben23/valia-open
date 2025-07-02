package com.rben23.valia.appFragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.ChallengeAdapter;
import com.rben23.valia.DAO.ChallengesDAO;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.managements.challengesManagements.NewChallengeManagement;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.models.Users;

import java.util.List;

public class ChallengeFragment extends Fragment {
    private ValiaSQLiteHelper vsql;
    private ChallengesDAO challengesDAO;
    private String currentUserUid;

    private RecyclerView recyclerChallenges;
    private ConstraintLayout savedChallengesEmpty;
    private ChallengeAdapter challengeAdapter;
    private List<Challenges> challengeList;

    private Button btnNewChallenge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Mostrar vista
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        initDatabase();
        initViews(view);
        initListeners();
        inflateChallengesList();
        updateUI();

        return view;
    }

    private void initDatabase() {
        // Iniciamos la conexion y DAO's
        vsql = ValiaSQLiteHelper.getInstance(getContext());
        challengesDAO = vsql.getChallengesDAO();
        Users currentUser = vsql.getUsersDAO().getCurrentUser();
        currentUserUid = currentUser.getUid();
    }

    private void initViews(View view) {
        // Elementos de la vista
        btnNewChallenge = view.findViewById(R.id.btnNewChallenge);
        recyclerChallenges = view.findViewById(R.id.recyclerChallenges);
        savedChallengesEmpty = view.findViewById(R.id.savedChallengesEmpty);
    }

    private void initListeners() {
        // Crear nuevo reto
        btnNewChallenge.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity().getBaseContext(), NewChallengeManagement.class);
            startActivity(intent);
        });
    }

    private void inflateChallengesList() {
        // Inflamos lista de retos
        recyclerChallenges.setLayoutManager(new LinearLayoutManager(getContext()));
        challengeList = challengesDAO.getChallengesByUser(currentUserUid);
        challengeAdapter = new ChallengeAdapter(getContext(), challengeList, currentUserUid);
        recyclerChallenges.setAdapter(challengeAdapter);
    }

    // Obtenemos la lista y mostramos u ocultamos los views
    private void updateUI() {
        new Thread(() -> {
            List<Challenges> data = new ChallengesDAO().getChallengesByUser(currentUserUid);

            if (getView() != null) {
                // Comprobamos que hay datos para mostrar, si hay se muestran y si no se muestra layout empty
                getView().post(() -> {
                    if (data != null && !data.isEmpty()) {
                        recyclerChallenges.setVisibility(VISIBLE);
                        savedChallengesEmpty.setVisibility(GONE);

                        challengeAdapter.updateItems(data);
                    } else {
                        recyclerChallenges.setVisibility(GONE);
                        savedChallengesEmpty.setVisibility(VISIBLE);
                    }
                });
            }
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}