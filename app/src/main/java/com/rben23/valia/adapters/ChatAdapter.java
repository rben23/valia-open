package com.rben23.valia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.R;
import com.rben23.valia.managements.ActivityManagement;
import com.rben23.valia.managements.chatsManagements.RouteMessageProcessor;
import com.rben23.valia.models.Messages;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;
    private static final int TYPE_MESSAGE_ROUTE_SENT = 3;
    private static final int TYPE_MESSAGE_ROUTE_RECEIVED = 4;

    private final List<Messages> messages;
    private final String currentUserUid;

    public ChatAdapter(List<Messages> messages, String currentUserUid) {
        this.messages = messages;
        this.currentUserUid = currentUserUid;
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messages.get(position);
        // Comprobamos si el mensaje es una ruta
        if (message.getIdType() == 1) {
            return message.getIdSender().equals(currentUserUid) ? TYPE_MESSAGE_ROUTE_SENT : TYPE_MESSAGE_ROUTE_RECEIVED;
        } else {
            return message.getIdSender().equals(currentUserUid) ? TYPE_MESSAGE_SENT : TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_MESSAGE_SENT:
                View sentView = inflater.inflate(R.layout.message_sent, parent, false);
                return new SentMessageViewHolder(sentView);
            case TYPE_MESSAGE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);
            case TYPE_MESSAGE_ROUTE_SENT:
                View sentRouteView = inflater.inflate(R.layout.message_route_sent, parent, false);
                return new RouteSentMessageViewHolder(sentRouteView);
            case TYPE_MESSAGE_ROUTE_RECEIVED:
                View receivedRouteView = inflater.inflate(R.layout.message_route_received, parent, false);
                return new RouteReceivedMessageViewHolder(receivedRouteView);
            default:
                throw new IllegalArgumentException("Tipo de mensaje desconocido" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messages.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case TYPE_MESSAGE_ROUTE_SENT:
                ((RouteSentMessageViewHolder) holder).bind(message);
                break;
            case TYPE_MESSAGE_ROUTE_RECEIVED:
                ((RouteReceivedMessageViewHolder) holder).bind(message);
                break;
            default:
                throw new IllegalArgumentException("Tipo de mensaje desconocido");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txvMessageSent;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txvMessageSent = itemView.findViewById(R.id.txvMessageSent);
        }

        void bind(Messages message) {
            txvMessageSent.setText(message.getMessage());
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txvMessageReveived;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txvMessageReveived = itemView.findViewById(R.id.txvMessageReveived);
        }

        void bind(Messages message) {
            txvMessageReveived.setText(message.getMessage());
        }
    }

    class RouteSentMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRouteSent;

        public RouteSentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRouteSent = itemView.findViewById(R.id.imgRouteSent);
        }

        void bind(Messages message) {
            imgRouteSent.setImageResource(R.drawable.img_dec_sent_route);
            imgRouteSent.setOnClickListener(view -> handleRouteClick(view, message));
        }
    }

    class RouteReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRouteReceived;

        public RouteReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRouteReceived = itemView.findViewById(R.id.imgRouteReceived);
        }

        void bind(Messages message) {
            imgRouteReceived.setImageResource(R.drawable.img_dec_sent_route);
            imgRouteReceived.setOnClickListener(view -> handleRouteClick(view, message));
        }
    }

    private void handleRouteClick(View view, Messages message) {
        Context context = view.getContext();
        String pathFile = message.getPathFile();
        String processedId = pathFile;

        // Si la ruta contiene delimitadores, se procesa
        if (pathFile.contains("#") || pathFile.contains("!")) {
            RouteMessageProcessor processor = new RouteMessageProcessor();
            processedId = processor.parseAndStoreRoute(pathFile);

            if (processedId == null || processedId.isEmpty()) {
                Toast.makeText(context, R.string.tst_ProcesingRoute, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            int routeId = Integer.parseInt(processedId);
            Intent intent = new Intent(context, ActivityManagement.class);
            intent.putExtra("idRoute", routeId);
            context.startActivity(intent);
        } catch (NumberFormatException e) {
            Toast.makeText(context, R.string.tst_ProcesingRouteError, Toast.LENGTH_SHORT).show();
        }
    }
}
