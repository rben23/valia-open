package com.rben23.valia;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

import com.rben23.valia.DAO.ActivitiesDAO;
import com.rben23.valia.DAO.FriendsDAO;
import com.rben23.valia.DAO.MessagesDAO;
import com.rben23.valia.DAO.ResultsChallengesDAO;
import com.rben23.valia.DAO.ChallengesDAO;
import com.rben23.valia.DAO.RoutesDAO;
import com.rben23.valia.DAO.FriendRequestsDAO;
import com.rben23.valia.DAO.UsersDAO;

public class ValiaSQLiteHelper extends SQLiteOpenHelper {
    private static int dbVersion = 1;
    private static String dbName = "ValiaDatabase";
    private static ValiaSQLiteHelper instance = null;
    private static SQLiteDatabase db;

    // DAO'S
    private static UsersDAO usersDAO;
    private static FriendRequestsDAO friendRequestsDAO;
    private static FriendsDAO friendsDAO;
    private static MessagesDAO messagesDAO;
    private static ActivitiesDAO activitiesDAO;
    private static RoutesDAO routesDAO;
    private static ChallengesDAO challengesDAO;
    private static ResultsChallengesDAO resultsChallengesDAO;

    // Constructor
    public ValiaSQLiteHelper(Context contexto) {
        super(contexto, dbName, null, dbVersion);
    }

    // Crear tabla Usuarios
    private String sqlCreateTableRolesUsers = "CREATE TABLE UserRoles(" +
            "RoleId INTEGER PRIMARY KEY," + // 0 - Usuaeio | 1 - Admin
            "Name VARCHAR(50))";

    private String sqlCreateTableUsers = "CREATE TABLE Users(" +
            "Uid VARCHAR(50) PRIMARY KEY," +
            "UserId VARCHAR(50) UNIQUE," +
            "ProfileImage TEXT," +
            "Name VARCHAR(50) NOT NULL," +
            "Email VARCHAR(50) UNIQUE NOT NULL," +
            "RoleId INTEGER DEFAULT 0," +
            "CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (RoleId) REFERENCES UserRoles(RoleId))";

    // Crear tabla SolicitudesAmistad
    private String sqlCreateTableStatesRequests = "CREATE TABLE StatesRequests(" +
            "StateId INTEGER PRIMARY KEY," + // 0 - Pendiente | 1 - Aceptado | 2 - Rechazado
            "Name VARCHAR(50))";

    private String sqlCreateTableRequestsFriendship = "CREATE TABLE FriendRequests(" +
            "RequestId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserId VARCHAR(50)," +
            "AddresseeId VARCHAR(50)," +
            "StateId INTEGER," +
            "RequestDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (UserId) REFERENCES Users(Uid) ON DELETE CASCADE ON UPDATE CASCADE," +
            "FOREIGN KEY (AddresseeId) REFERENCES Users(Uid) ON DELETE CASCADE ON UPDATE CASCADE," +
            "FOREIGN KEY (StateId) REFERENCES StatesRequests(StateId))";

    private String sqlCreateIndexUser = "CREATE INDEX user_requests ON FriendRequests(UserId)";
    private String sqlCreateIndexAddressee = "CREATE INDEX requests_addressee ON FriendRequests(AddresseeId)";

    // Crear tabla Amigos
    private String sqlCreateTableFriends = "CREATE TABLE Friends(" +
            "UserId VARCHAR(50)," +
            "FriendId VARCHAR(50)," +
            "FriendshipDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "PRIMARY KEY (UserId, FriendId)," +
            "FOREIGN KEY (UserId) REFERENCES Users(Uid) ON DELETE CASCADE ON UPDATE CASCADE," +
            "FOREIGN KEY (FriendId) REFERENCES Users(Uid) ON DELETE CASCADE ON UPDATE CASCADE)";

    // Crear tabla Mensajes
    private String sqlCreateTableTypesMessages = "CREATE TABLE TypesMessages(" +
            "TypeId INTEGER PRIMARY KEY," + // 0 - Texto | 1 - Rutas
            "Name VARCHAR(50))";

    private String sqlCreateTableMessages = "CREATE TABLE Messages(" +
            "MessageId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ConversationUid VARCHAR(255)," +
            "SenderId VARCHAR(50)," +
            "AddresseeId VARCHAR(50)," +
            "Message TEXT," +
            "Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "TypeId INTEGER," +
            "FilePath VARCHAR(255) DEFAULT NULL," +
            "FOREIGN KEY (SenderId) REFERENCES Users(Uid)," +
            "FOREIGN KEY (AddresseeId) REFERENCES Users(Uid)," +
            "FOREIGN KEY (TypeId) REFERENCES TypesMessages(TypeId))";


    private String sqlCreateIndexConversationMessages = "CREATE INDEX idx_conversation_uid ON Messages(ConversationUid)";

    // Crear tabla Actividades
    private String sqlCreateTableTypesActivities = "CREATE TABLE ActivitiesTypes(" +
            "TypeId INTEGER PRIMARY KEY," + // 0 - Ruta | Se puede ampliar a mas actividades en un futuro
            "Name VARCHAR(50))";

    private String sqlCreateTableActivities = "CREATE TABLE Activities(" +
            "ActivityId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserId VARCHAR(50)," +
            "Name VARCHAR(80)," + // Correr, Paseo, Bicicleta...
            "TypeId INTEGER," +
            "Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (UserId) REFERENCES Users(Uid)," +
            "FOREIGN KEY (TypeId) REFERENCES ActivitiesTypes(TypeId))";

    // Crear tabla Rutas
    private String sqlCreateTableRoutes = "CREATE TABLE Routes(" +
            "RouteId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ActivityId INTEGER," +
            "Name VARCHAR(80)," +
            "PathRoute TEXT," + // Recorrido (coordenadas geográficas) de cada punto de la ruta para pintar en el mapa
            "TimeDuration TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "TotalKm DECIMAL(10,2)," +
            "FOREIGN KEY (ActivityId) REFERENCES Activities(ActivityId))";

    // Crear tabla Retos
    private String sqlCreateTableChallenges = "CREATE TABLE Challenges(" +
            "ChallengeId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserId VARCHAR(50)," +
            "AddresseeId VARCHAR(50)," +
            "Description VARCHAR(80)," +
            "TotalKm DECIMAL(10,2)," +
            "Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (UserId) REFERENCES Users(Uid)," +
            "FOREIGN KEY (AddresseeId) REFERENCES Users(Uid))";

    // Crear tabla ResultadosRetos
    private String sqlCreateTableResultsChallenges = "CREATE TABLE ResultsChallenges(" +
            "ResultId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ChallengeId INTEGER," +
            "UserId VARCHAR(50)," +
            "TimeDuration TIME," +
            "Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (ChallengeId) REFERENCES Challenges(ChallengeId)," +
            "FOREIGN KEY (UserId) REFERENCES Users(Uid))";

    // Insertar Roles Usuarios
    private String[] sqlInsertRolesUsers = {
            "INSERT INTO UserRoles(RoleId, Name) " +
                    "VALUES(0,'User')",
            "INSERT INTO UserRoles(RoleId, Name) " +
                    "VALUES(1,'Admin')"
    };

    // Insertar Estados Solicitudes
    private String[] sqlInsertStatesRequests = {
            "INSERT INTO StatesRequests(StateId, Name) " +
                    "VALUES(0,'Pending')",
            "INSERT INTO StatesRequests(StateId, Name) " +
                    "VALUES(1,'Accepted')",
            "INSERT INTO StatesRequests(StateId, Name) " +
                    "VALUES(2,'Refused')"
    };

    // Insertar Tipos Mensajes
    private String[] sqlInsertTypesMessages = {
            "INSERT INTO TypesMessages(TypeId, Name) " +
                    "VALUES(0,'Text')",
            "INSERT INTO TypesMessages(TypeId, Name) " +
                    "VALUES(1,'Route')"
    };

    // Insertar Tipos Actividades
    private String[] sqlInsertTypesActivities = {
            "INSERT INTO ActivitiesTypes(TypeId, Name) " +
                    "VALUES(0,'Route')"
    };

    // Getters
    public static ValiaSQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ValiaSQLiteHelper(context);
            db = instance.getWritableDatabase();

            // Instanciar DAO's
            usersDAO = new UsersDAO();
            friendRequestsDAO = new FriendRequestsDAO();
            friendsDAO = new FriendsDAO();
            messagesDAO = new MessagesDAO();
            activitiesDAO = new ActivitiesDAO();
            routesDAO = new RoutesDAO();
            challengesDAO = new ChallengesDAO();
            resultsChallengesDAO = new ResultsChallengesDAO();
        }
        return instance;
    }

    public static UsersDAO getUsersDAO() {return usersDAO;}

    public static FriendRequestsDAO getFriendRequestsDAO() {
        return friendRequestsDAO;
    }

    public static FriendsDAO getFriendsDAO() {
        return friendsDAO;
    }

    public static MessagesDAO getMessagesDAO() {
        return messagesDAO;
    }

    public static ActivitiesDAO getActivitiesDAO() {
        return activitiesDAO;
    }

    public static RoutesDAO getRoutesDAO() {
        return routesDAO;
    }

    public static ChallengesDAO getChallengesDAO() {
        return challengesDAO;
    }

    public static ResultsChallengesDAO getResultsChallengesDAO() {
        return resultsChallengesDAO;
    }

    public static SQLiteDatabase getDB() {
        return db;
    }

    // Cerrar base de datos
    public static void closeDB() {
        db.close();
    }

    // onCreate y onUpgrade
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Ejecutar la secuencia SQL de creacion de tablas
        executeCreateTables(sqLiteDatabase);

        // Cargas de datos iniciales en las tablas
        uploadRecords(sqLiteDatabase);
    }

    private void executeCreateTables(SQLiteDatabase sqLiteDatabase) {
        Log.i(this.getClass().toString(), "Creando tablas ...");

        sqLiteDatabase.execSQL(sqlCreateTableRolesUsers);
        sqLiteDatabase.execSQL(sqlCreateTableUsers);
        Log.i(this.getClass().toString(), "Tabla 'Usuarios' creada");

        sqLiteDatabase.execSQL(sqlCreateTableStatesRequests);
        sqLiteDatabase.execSQL(sqlCreateTableRequestsFriendship);
        sqLiteDatabase.execSQL(sqlCreateIndexUser);
        sqLiteDatabase.execSQL(sqlCreateIndexAddressee);
        Log.i(this.getClass().toString(), "Tabla 'SolicitudesAmistad' creada");

        sqLiteDatabase.execSQL(sqlCreateTableFriends);
        Log.i(this.getClass().toString(), "Tabla 'Amigos' creada");

        sqLiteDatabase.execSQL(sqlCreateTableTypesMessages);
        sqLiteDatabase.execSQL(sqlCreateTableMessages);
        sqLiteDatabase.execSQL(sqlCreateIndexConversationMessages);
        Log.i(this.getClass().toString(), "Tabla 'Mensajes' creada");

        sqLiteDatabase.execSQL(sqlCreateTableTypesActivities);
        sqLiteDatabase.execSQL(sqlCreateTableActivities);
        Log.i(this.getClass().toString(), "Tabla 'Actividades' creada");

        sqLiteDatabase.execSQL(sqlCreateTableRoutes);
        Log.i(this.getClass().toString(), "Tabla 'Rutas' creada");

        sqLiteDatabase.execSQL(sqlCreateTableChallenges);
        Log.i(this.getClass().toString(), "Tabla 'Retos' creada");

        sqLiteDatabase.execSQL(sqlCreateTableResultsChallenges);
        Log.i(this.getClass().toString(), "Tabla 'ResultadosRetos' creada");
    }

    private void uploadRecords(SQLiteDatabase sqLiteDatabase) {
        Log.i(this.getClass().toString(), "Insertando datos iniciales ...");
        for (int i = 0; i < sqlInsertRolesUsers.length; i++) {
            sqLiteDatabase.execSQL(sqlInsertRolesUsers[i]);
        }
        Log.i(this.getClass().toString(), "'RolesUsuarios' insertado");

        for (int i = 0; i < sqlInsertStatesRequests.length; i++) {
            sqLiteDatabase.execSQL(sqlInsertStatesRequests[i]);
        }
        Log.i(this.getClass().toString(), "'EstadosSolicitudes' insertado");

        for (int i = 0; i < sqlInsertTypesMessages.length; i++) {
            sqLiteDatabase.execSQL(sqlInsertTypesMessages[i]);
        }
        Log.i(this.getClass().toString(), "'TiposMensajes' insertado");

        for (int i = 0; i < sqlInsertTypesActivities.length; i++) {
            sqLiteDatabase.execSQL(sqlInsertTypesActivities[i]);
        }
        Log.i(this.getClass().toString(), "'TiposActividades' insertado");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
