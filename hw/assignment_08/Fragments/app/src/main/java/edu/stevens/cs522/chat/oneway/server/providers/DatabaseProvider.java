package edu.stevens.cs522.chat.oneway.server.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

/**
 * Created by Rafael on 2/22/2016.
 */
public class DatabaseProvider extends ContentProvider {


    public static final Uri CHATROOM_CONTENT_URI = ChatroomContract.CONTENT_URI;
    public static final Uri PEER_CONTENT_URI = PeerContract.CONTENT_URI;
    public static final Uri MESSAGE_CONTENT_URI = MessageContract.CONTENT_URI;

    private static final String[] DATABASE_CREATE;
    private static final String DATABASE_NAME = "chat_server.db";
    private static final int DATABASE_VERSION = 10;

    private SQLiteDatabase _db;
    private DBHelper _dbHelper;

    private static class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String query :
                    DATABASE_CREATE) {
                Log.d("creating database", query);
                db.execSQL(query);
            }
            Log.d("database", "tables created");
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys=ON;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w("TaskDBAdapter",
                    "Upgrading from version " + oldVersion
                            + "	to	" + newVersion);

            db.execSQL("DROP TABLE IF EXISTS " + ChatroomContract.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PeerContract.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MessageContract.TABLE_NAME);

            onCreate(db);
        }
    }

    private final static int PEER_ALL_ROWS = 1;
    private final static int PEER_SINGLE_ROW = PEER_ALL_ROWS + 1;
    private final static int PEER_QUERY_NAME = PEER_SINGLE_ROW + 1;
    private final static int PEER_ALL_MESSAGES = PEER_QUERY_NAME + 1;

    private final static int MESSAGE_ALL_ROWS = PEER_ALL_ROWS + 100;
    private final static int MESSAGE_SINGLE_ROW = MESSAGE_ALL_ROWS + 1;

    private final static int CHATROOM_ALL_ROWS = MESSAGE_ALL_ROWS + 100;
    private final static int CHATROOM_SINGLE_ROW = CHATROOM_ALL_ROWS + 1;
    private final static int CHATROOM_ALL_MESSAGES = CHATROOM_SINGLE_ROW + 1;
    private final static int CHATROOM_QUERY_NAME = CHATROOM_ALL_MESSAGES + 1;
    private final static UriMatcher uriMatcher;

    static {
        String peer_path = PEER_CONTENT_URI.getLastPathSegment();
        String message_path = MESSAGE_CONTENT_URI.getLastPathSegment();
        String chatroom_path = CHATROOM_CONTENT_URI.getLastPathSegment();
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


        uriMatcher.addURI(ChatroomContract.AUTHORITY, chatroom_path, CHATROOM_ALL_ROWS);
        uriMatcher.addURI(ChatroomContract.AUTHORITY, chatroom_path + "/#", CHATROOM_SINGLE_ROW);
        uriMatcher.addURI(ChatroomContract.AUTHORITY, chatroom_path + "/*", CHATROOM_QUERY_NAME);
        uriMatcher.addURI(ChatroomContract.AUTHORITY, chatroom_path + "/#/messages", PEER_ALL_MESSAGES);

        uriMatcher.addURI(PeerContract.AUTHORITY, peer_path, PEER_ALL_ROWS);
        uriMatcher.addURI(PeerContract.AUTHORITY, peer_path + "/#", PEER_SINGLE_ROW);
        uriMatcher.addURI(PeerContract.AUTHORITY, peer_path + "/*", PEER_QUERY_NAME);
        uriMatcher.addURI(PeerContract.AUTHORITY, peer_path + "/#/messages", PEER_ALL_MESSAGES);

        uriMatcher.addURI(MessageContract.AUTHORITY, message_path, MESSAGE_ALL_ROWS);
        uriMatcher.addURI(MessageContract.AUTHORITY, message_path + "/#", MESSAGE_SINGLE_ROW);
        DATABASE_CREATE = new String[]{
                ChatroomContract.CREATE_TABLE,
                PeerContract.CREATE_TABLE,
                MessageContract.CREATE_TABLE
        };
    }

    @Override
    public boolean onCreate() {
        this._dbHelper = new DBHelper(getContext());
        _db = _dbHelper.getWritableDatabase();
        return (_db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        HashMap<String, String> map;
        String groupby = null;
        Log.d("query", uri.toString());

        projection = getProjection(uri);

        switch (uriMatcher.match(uri)){
            case PEER_ALL_ROWS:
                builder.setTables(PeerContract.TABLE_NAME);
                break;
            case PEER_SINGLE_ROW:
                builder.setTables(PeerContract.TABLE_NAME);
                builder.appendWhere(PeerContract.ID + " = " + PeerContract.getId(uri));
                break;
            case PEER_QUERY_NAME:
                builder.setTables(PeerContract.TABLE_NAME);
                Log.d("query", uri.getLastPathSegment());
                builder.appendWhere(PeerContract.NAME + " = ");
                builder.appendWhereEscapeString(uri.getLastPathSegment());
                break;
            case PEER_ALL_MESSAGES:
                builder.setTables(MessageContract.TABLE_NAME
                        + " LEFT JOIN " + PeerContract.TABLE_NAME
                        + " ON (" + MessageContract.PEER_ID
                        + " = " + PeerContract.ID_FULL + ")"
                        + " LEFT JOIN " + ChatroomContract.TABLE_NAME
                        + " ON (" + MessageContract.CHATROOM_ID
                        + " = " + ChatroomContract.ID_FULL + ")");
                map = new HashMap<>();
                map.put(MessageContract.SENDER, PeerContract.NAME_FULL + " as " + MessageContract.SENDER);
                map.put(MessageContract.CHATROOM_NAME, ChatroomContract.NAME_FULL + " as " + MessageContract.CHATROOM_NAME);
                for (String field :
                        projection) {
                    if(!map.containsKey(field)) map.put(field, field);
                }
                builder.setProjectionMap(map);
                List<String> peerPathSegments = uri.getPathSegments();
                long peerId = Long.valueOf(peerPathSegments.get(peerPathSegments.size() - 2));
                builder.appendWhere(MessageContract.PEER_ID_FULL + " = " + peerId);
                break;
            case CHATROOM_ALL_MESSAGES:
                builder.setTables(MessageContract.TABLE_NAME
                        + " LEFT JOIN " + PeerContract.TABLE_NAME
                        + " ON (" + MessageContract.PEER_ID
                        + " = " + PeerContract.ID_FULL + ")"
                        + " LEFT JOIN " + ChatroomContract.TABLE_NAME
                        + " ON (" + MessageContract.CHATROOM_ID
                        + " = " + ChatroomContract.ID_FULL + ")");
                map = new HashMap<>();
                map.put(MessageContract.SENDER, PeerContract.NAME_FULL + " as " + MessageContract.SENDER);
                map.put(MessageContract.CHATROOM_NAME, ChatroomContract.NAME_FULL + " as " + MessageContract.CHATROOM_NAME);
                for (String field :
                        projection) {
                    if(!map.containsKey(field)) map.put(field, field);
                }
                builder.setProjectionMap(map);
                List<String> chatRoomPathSegments = uri.getPathSegments();
                long chatroomId = Long.valueOf(chatRoomPathSegments.get(chatRoomPathSegments.size() - 2));
                builder.appendWhere(MessageContract.CHATROOM_ID_FULL + " = " + chatroomId);
                break;
            case CHATROOM_ALL_ROWS:
                builder.setTables(ChatroomContract.TABLE_NAME);
                break;
            case CHATROOM_SINGLE_ROW:
                builder.setTables(ChatroomContract.TABLE_NAME);
                builder.appendWhere(ChatroomContract.ID + " = " + ChatroomContract.getId(uri));
                break;
            case CHATROOM_QUERY_NAME:
                builder.setTables(ChatroomContract.TABLE_NAME);
                Log.d("query", uri.getLastPathSegment());
                builder.appendWhere(ChatroomContract.NAME + " = ");
                builder.appendWhereEscapeString(uri.getLastPathSegment());
                break;
            case MESSAGE_ALL_ROWS:
                builder.setTables(MessageContract.TABLE_NAME
                        + " LEFT JOIN " + PeerContract.TABLE_NAME
                        + " ON (" + MessageContract.PEER_ID
                        + " = " + PeerContract.ID_FULL + ")"
                        + " LEFT JOIN " + ChatroomContract.TABLE_NAME
                        + " ON (" + MessageContract.CHATROOM_ID
                        + " = " + ChatroomContract.ID_FULL + ")");
                map = new HashMap<>();
                map.put(MessageContract.SENDER, PeerContract.NAME_FULL + " as " + MessageContract.SENDER);
                map.put(MessageContract.CHATROOM_NAME, ChatroomContract.NAME_FULL + " as " + MessageContract.CHATROOM_NAME);
                for (String field :
                        projection) {
                    if(!map.containsKey(field)) map.put(field, field);
                }
                builder.setProjectionMap(map);
                break;
            case MESSAGE_SINGLE_ROW:
                builder.setTables(MessageContract.TABLE_NAME);
                builder.appendWhere(MessageContract.ID + " = " + MessageContract.getId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Log.d("query", builder.buildQuery(projection, selection, groupby, null, null, null));
        Cursor cursor = builder.query(_db, projection, selection, selectionArgs, groupby, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private String[] getProjection(Uri uri){
        switch (uriMatcher.match(uri)){
            case PEER_ALL_ROWS:
            case PEER_SINGLE_ROW:
            case PEER_QUERY_NAME:
                return new String[]{
                        PeerContract.ID_FULL,
                        PeerContract.NAME,
                        PeerContract.LATITUDE,
                        PeerContract.LONGITUDE
//                        PeerContract.ADDRESS,
//                        PeerContract.PORT
                };
            case CHATROOM_ALL_ROWS:
            case CHATROOM_SINGLE_ROW:
            case CHATROOM_QUERY_NAME:
                return new String[]{
                        ChatroomContract.ID_FULL,
                        ChatroomContract.NAME
                };
            case MESSAGE_ALL_ROWS:
            case MESSAGE_SINGLE_ROW:
            case PEER_ALL_MESSAGES:
            case CHATROOM_ALL_MESSAGES:
                return new String[]{
                        MessageContract.ID_FULL,
                        MessageContract.SEQ_NUM,
                        MessageContract.MESSAGE_TEXT,
                        MessageContract.TIMESTAMP,
                        MessageContract.PEER_ID,
                        MessageContract.SENDER,
                        MessageContract.CHATROOM_ID,
                        MessageContract.CHATROOM_NAME,
                };
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PEER_ALL_ROWS:
                return PeerContract.CONTENT_TYPE;
            case PEER_SINGLE_ROW:
                return PeerContract.CONTENT_TYPE_ITEM;
            case CHATROOM_ALL_ROWS:
                return ChatroomContract.CONTENT_TYPE;
            case CHATROOM_SINGLE_ROW:
                return ChatroomContract.CONTENT_TYPE_ITEM;
            case MESSAGE_ALL_ROWS:
                return MessageContract.CONTENT_TYPE;
            case MESSAGE_SINGLE_ROW:
                return MessageContract.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = 0;
        Uri instanceUri = null;
        switch (uriMatcher.match(uri)) {
            case CHATROOM_ALL_ROWS:
                rowId = _db.insert(ChatroomContract.TABLE_NAME, null, values);
                instanceUri = ChatroomContract.withExtendedPath(rowId);
                break;
            case PEER_ALL_ROWS:
                rowId = _db.insert(PeerContract.TABLE_NAME, null, values);
                instanceUri = PeerContract.withExtendedPath(rowId);
                break;
            case MESSAGE_ALL_ROWS:
                rowId = _db.insert(MessageContract.TABLE_NAME, null, values);
                instanceUri = MessageContract.withExtendedPath(rowId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowId > 0 && instanceUri != null) {
            getContext().getContentResolver().notifyChange(instanceUri, null);
            return instanceUri;
        }
        throw new SQLException("Insertion failed");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int rowsDeleted = 0;
        Uri notifyUri = null;
        switch (uriMatcher.match(uri)) {
            case CHATROOM_ALL_ROWS:
                builder.setTables(ChatroomContract.TABLE_NAME);
                rowsDeleted = _db.delete(ChatroomContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = ChatroomContract.CONTENT_URI;
                break;
            case CHATROOM_SINGLE_ROW:
                builder.setTables(ChatroomContract.TABLE_NAME);
                selection = ChatroomContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsDeleted = _db.delete(ChatroomContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = ChatroomContract.CONTENT_URI;
                break;
            case PEER_ALL_ROWS:
                builder.setTables(PeerContract.TABLE_NAME);
                rowsDeleted = _db.delete(PeerContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = PeerContract.CONTENT_URI;
                break;
            case PEER_SINGLE_ROW:
                builder.setTables(PeerContract.TABLE_NAME);
                selection = PeerContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsDeleted = _db.delete(PeerContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = PeerContract.CONTENT_URI;
                break;
            case MESSAGE_ALL_ROWS:
                builder.setTables(MessageContract.TABLE_NAME);
                rowsDeleted = _db.delete(MessageContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = MessageContract.CONTENT_URI;
                break;
            case MESSAGE_SINGLE_ROW:
                builder.setTables(MessageContract.TABLE_NAME);
                selection = MessageContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsDeleted = _db.delete(MessageContract.TABLE_NAME, selection, selectionArgs);
                notifyUri = MessageContract.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(notifyUri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int rowsUpdated = 0;
        Uri notifyUri = null;
        switch (uriMatcher.match(uri)) {
            case CHATROOM_ALL_ROWS:
                builder.setTables(ChatroomContract.TABLE_NAME);
                rowsUpdated = _db.update(ChatroomContract.TABLE_NAME, values, selection, selectionArgs);
                notifyUri = ChatroomContract.CONTENT_URI;
                break;
            case CHATROOM_SINGLE_ROW:
                builder.setTables(ChatroomContract.TABLE_NAME);
                selection = ChatroomContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsUpdated = _db.update(ChatroomContract.TABLE_NAME, values, selection, selectionArgs);
                notifyUri = ChatroomContract.CONTENT_URI;
                break;
            case PEER_ALL_ROWS:
                builder.setTables(PeerContract.TABLE_NAME);
                rowsUpdated = _db.update(PeerContract.TABLE_NAME, values, selection, selectionArgs);
                notifyUri = PeerContract.CONTENT_URI;
                break;
            case PEER_SINGLE_ROW:
                builder.setTables(PeerContract.TABLE_NAME);
                selection = PeerContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsUpdated = _db.update(PeerContract.TABLE_NAME, values, selection, selectionArgs);
                notifyUri = PeerContract.CONTENT_URI;
                break;
            case MESSAGE_ALL_ROWS:
                builder.setTables(MessageContract.TABLE_NAME);
                rowsUpdated = _db.update(MessageContract.TABLE_NAME, values, selection, selectionArgs);
                notifyUri = MessageContract.CONTENT_URI;
                break;
            case MESSAGE_SINGLE_ROW:
                builder.setTables(MessageContract.TABLE_NAME);
                selection = MessageContract.ID + " = ?";
                selectionArgs = new String[]{
                        uri.getLastPathSegment()
                };
                rowsUpdated = _db.update(MessageContract.TABLE_NAME,values, selection, selectionArgs);
                notifyUri = MessageContract.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(notifyUri, null);
        }
        return rowsUpdated;
    }
}
