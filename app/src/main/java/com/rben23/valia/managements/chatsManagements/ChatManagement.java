package com.rben23.valia.managements.chatsManagements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rben23.valia.adapters.ChatAdapter;
import com.rben23.valia.R;
import com.rben23.valia.ValiaSQLiteHelper;
import com.rben23.valia.managements.UserManagement;
import com.rben23.valia.models.Messages;
import com.rben23.valia.models.Users;
import com.rben23.valia.syncDataBase.FirebaseManager;
import com.rben23.valia.managements.ProfileImageManagement;
import com.rben23.valia.syncDataBase.MessagesSyncManager;

import java.util.ArrayList;
import java.util.List;

public class ChatManagement extends AppCompatActivity {
    private FirebaseManager firebaseManager;
    private ValiaSQLiteHelper vsql;
    private Vibrator vibrator;
    private ChatAdapter chatAdapter;
    private List<Messages> messagesList;

    private TextView txvFrameTitle;
    private ImageView goBack, imgProfileImage;
    private EditText edtSendMessage;
    private ImageButton imgSendMessage, imgSelectRoute;
    private RecyclerView allMessagesRecycler;

    private String userUid;
    private String addresseeUid;
    private String userId;
    private String userName;
    private String userEmail;

    private String conversationUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_management);

        // Inicializamos y creamos
        initSystem();
        initDatabase();
        retrieveIntentExtras();
        initViews();
        asignData();
        initListeners();
        createAdapter();
        refreshMessages();
    }

    private void initSystem() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initDatabase() {
        vsql = ValiaSQLiteHelper.getInstance(this);
        Users user = vsql.getUsersDAO().getCurrentUser();
        this.userUid = user.getUid();
        firebaseManager = FirebaseManager.getInstance(userUid);
    }

    private void retrieveIntentExtras() {
        Intent intent = getIntent();
        addresseeUid = intent.getStringExtra("userUid");
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        txvFrameTitle = findViewById(R.id.txvFrameTitle);
        imgProfileImage = findViewById(R.id.imgProfileImage);
        edtSendMessage = findViewById(R.id.edtSendMessage);
        imgSendMessage = findViewById(R.id.imgSendMessage);
        imgSelectRoute = findViewById(R.id.imgSelectRoute);
        allMessagesRecycler = findViewById(R.id.allMessagesRecycler);
    }

    private void asignData() {
        // Asignar datos usuario
        txvFrameTitle.setText(userId);
        ProfileImageManagement profileImageManagement = new ProfileImageManagement(this, addresseeUid);
        profileImageManagement.getRemoteUserImageProfile(imgProfileImage);

        // Calculamos un IdUnico de conversacion para guardarlo en firebase
        conversationUid = (userUid.compareTo(addresseeUid) < 0) ? userUid + "_" + addresseeUid
                : addresseeUid + "_" + userUid;
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());

        // Ir al perfil si hacemos click en el nombre
        txvFrameTitle.setOnClickListener(view -> {
            Intent intGoToUserManagement = new Intent(ChatManagement.this, UserManagement.class);
            intGoToUserManagement.putExtra("userUid", addresseeUid);
            intGoToUserManagement.putExtra("userId", userId);
            intGoToUserManagement.putExtra("userName", userName);
            intGoToUserManagement.putExtra("userEmail", userEmail);
            startActivity(intGoToUserManagement);
        });

        imgSelectRoute.setOnClickListener(view -> {
            Intent goToSelectionRoute = new Intent(ChatManagement.this, RouteSendSelectionManagement.class);
            goToSelectionRoute.putExtra("userUid", userUid);
            goToSelectionRoute.putExtra("addresseeUid", addresseeUid);
            goToSelectionRoute.putExtra("conversationUid", conversationUid);
            Log.e("ConversationUid", conversationUid);
            startActivity(goToSelectionRoute);
        });

        // Guardamos el mensaje en base de datos al pulsar sobre enviar
        imgSendMessage.setOnClickListener(view -> {
            sendTextMessage();
            edtSendMessage.setText(""); // Reiniciamos el mensaje
            vibrator.vibrate(50);
        });

        // Comprobamos si hay texto en el campo de mensaje
        edtSendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    imgSelectRoute.setVisibility(View.GONE); // Ocultamos el boton cuando hay texto
                } else {
                    imgSelectRoute.setVisibility(View.VISIBLE); // Mostramos el boton cuando no hay texto
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void createAdapter() {
        // Creamos el adaptador con la lista
        allMessagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        messagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messagesList, userUid);
        allMessagesRecycler.setAdapter(chatAdapter);
    }

    // Escuchamos nuevos mensajes
    private ValueEventListener messagesValueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Messages> newMessages = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                Messages message = child.getValue(Messages.class);
                if (message != null) {
                    newMessages.add(message);
                }
            }
            // Actualizamos la UI limpiando y cargando los nuevos mensajes
            messagesList.clear();
            messagesList.addAll(newMessages);
            chatAdapter.notifyDataSetChanged();
            allMessagesRecycler.scrollToPosition(messagesList.size() - 1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("ChatManagement", "Error al leer mensajes: ", error.toException());
        }
    };

    // Enviamos un mensaje
    private void sendTextMessage() {
        String message = edtSendMessage.getText().toString();
        if (message.isEmpty()) return;

        Messages messages = new Messages();
        messages.setIdSender(userUid);
        messages.setIdAddressee(addresseeUid);
        messages.setMessage(message);
        messages.setIdType(0);

        vsql.getMessagesDAO().insert(messages);
        firebaseManager.getMessagesSyncManager(conversationUid).synchronizeToRemoteDatabase(() ->
                Log.d("SyncToRemote", "Mensaje sincronizado con Firebase"));
        refreshMessages();
    }

    // Refrescamos la lista
    private void refreshMessages() {
        List<Messages> newMessages = vsql.getMessagesDAO().getAllMessages(conversationUid);
        messagesList.clear();
        messagesList.addAll(newMessages);
        chatAdapter.notifyDataSetChanged();

        // Hacemos autoscroll al ultimo mensaje
        allMessagesRecycler.scrollToPosition(messagesList.size() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualizaciones en tiempo real
        MessagesSyncManager messagesSyncManager = firebaseManager.getMessagesSyncManager(conversationUid);
        messagesSyncManager.getDatabaseReference().addValueEventListener(messagesValueEventListener);

        // Sincronizamos las bases de datos con los mensajes remotos
        messagesSyncManager.synchronizeToLocalDatabase(() ->
                Log.d("SyncToLocal", "Mensajes sincronizados con la base de datos local"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseManager.getMessagesSyncManager(conversationUid)
                .getDatabaseReference().removeEventListener(messagesValueEventListener);
    }
}