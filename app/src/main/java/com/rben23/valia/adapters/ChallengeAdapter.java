package com.rben23.valia.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.DAO.ResultsChallengesDAO;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.managements.challengesManagements.ChallengeManagement;
import com.rben23.valia.models.ResultsChallenges;
import com.rben23.valia.models.Challenges;

import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {
    private ValiaSQLiteHelper vsql;
    private ResultsChallengesDAO resultsChallengesDAO;

    private Context context;
    private List<Challenges> challengeList;
    private String currentUserUid;

    public ChallengeAdapter(Context context, List<Challenges> challengeList, String currentUserUid) {

        // Creamos la conexion si no existe
        vsql = ValiaSQLiteHelper.getInstance(context);

        this.context = context;
        this.challengeList = challengeList;
        this.currentUserUid = currentUserUid;
        this.resultsChallengesDAO = vsql.getResultsChallengesDAO();
    }

    @NonNull
    @Override
    public ChallengeAdapter.ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout para cada item
        View itemView = LayoutInflater.from(context).inflate(R.layout.challenge_list_item, parent, false);
        return new ChallengeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeAdapter.ChallengeViewHolder holder, int position) {
        // Obtenemos el reto a partir de la posición
        Challenges challenge = challengeList.get(position);

        // Asignamos los datos a cada vista del layout
        holder.challengeName.setText(challenge.getDescription());
        String km = " " + context.getString(R.string.txv_kmText);
        holder.challengeKm.setText(challenge.getTotalKm() + km);

        // Consultamos si existe algun resultado para el reto
        ResultsChallenges resultsChallenges = resultsChallengesDAO.getResultByChallengeId(challenge.getIdChallenge());
        if (resultsChallenges == null || TextUtils.isEmpty(resultsChallenges.getIdUser())) {
            holder.challengeIcon.setImageResource(R.drawable.img_chll_challenge_active);
        } else {
            if (resultsChallenges.getIdUser().equals(currentUserUid)) {
                holder.challengeIcon.setImageResource(R.drawable.img_chll_challenge_win);
            } else {
                holder.challengeIcon.setImageResource(R.drawable.img_chll_challenge_lost);
            }
        }

        // Abrimos el reto
        holder.itemView.setOnClickListener(view -> {
            Intent goToChallengeManagement = new Intent(context, ChallengeManagement.class);
            goToChallengeManagement.putExtra("idChallenge", challenge.getIdChallenge());
            context.startActivity(goToChallengeManagement);
        });
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public void updateItems(List<Challenges> newItems) {
        this.challengeList = newItems;
        notifyDataSetChanged();
    }

    public class ChallengeViewHolder extends RecyclerView.ViewHolder {
        ImageView challengeIcon;
        TextView challengeName, challengeKm;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            challengeIcon = itemView.findViewById(R.id.challengeIcon);
            challengeName = itemView.findViewById(R.id.challengeName);
            challengeKm = itemView.findViewById(R.id.challengeKm);
        }
    }
}
