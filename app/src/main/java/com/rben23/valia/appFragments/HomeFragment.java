package com.rben23.valia.appFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.models.Challenges;
import com.rben23.valia.models.Users;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private ValiaSQLiteHelper vsql;
    private Users users;

    private TextView txvKmWeek;
    private TextView txvHoursWeek;
    private TextView txvChallengesActive;
    private TextView txvChallengesWin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Vista
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initDatabase();
        initViews(view);
        setValues();

        // Mostrar vista
        return view;
    }

    private void initDatabase() {
        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(this.getContext());
        users = vsql.getUsersDAO().getCurrentUser();
    }

    private void initViews(View view) {
        // Elementos de la vista
        txvKmWeek = view.findViewById(R.id.txvKmWeek);
        txvHoursWeek = view.findViewById(R.id.txvHoursWeek);
        txvChallengesActive = view.findViewById(R.id.txvChallengesActive);
        txvChallengesWin = view.findViewById(R.id.txvChallengesWin);
    }

    private void setValues() {
        // Km realizados esta semana
        String kmWeek = String.format(Locale.ENGLISH, "%.1f", vsql.getRoutesDAO().getKmThisWeek(users.getUid()));
        txvKmWeek.setText(kmWeek);

        // Horas de actividad semanales
        txvHoursWeek.setText(vsql.getRoutesDAO().getHoursThisWeek(users.getUid()));

        // Retos activos
        List<Challenges> activeChallenges = vsql.getChallengesDAO().getActiveChallengesByUser(users.getUid());
        String activeChallengeToText = Integer.toString(activeChallenges.size());
        txvChallengesActive.setText(activeChallengeToText);

        // Retos ganados esta semana
        String winChallengesToText = Integer.toString(vsql.getResultsChallengesDAO().getWinChallengesThisWeek(users.getUid()));
        txvChallengesWin.setText(winChallengesToText);
    }

}