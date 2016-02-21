package edu.stevens.cs522.bookstore.providers;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Rafael on 2/19/2016.
 */
public class BookProvider extends ContentProvider {

    public  static final Uri CONTENT_URI = BookContract.CONTENT_URI;

    private static final String[] DATABASE_CREATE = {
            BookContract.CREATE_TABLE,
            AuthorContract.CREATE_TABLE
    };
    private static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 2;
    private static final String DEFAULT_SORT = BookContract.ID + " ASC";
    //---------------------------------------------------
    private SQLiteDatabase _db;
//    private Context _context;
    private DBHelper _dbHelper;

    // SHOULD THIS BE STATIC?
    private static class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String createStatement :
                    DATABASE_CREATE) {
                db.execSQL(createStatement);
            }
            Log.d("database", "tables created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w("TaskDBAdapter",
                    "Upgrading from version " + oldVersion
                            + "	to	" + newVersion);

            db.execSQL("DROP TABLE IF EXISTS " + BookContract.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AuthorContract.TABLE_NAME);

            onCreate(db);
        }
    }

    private final static int ALL_ROWS = 1;
    private final static int SINGLE_ROW = 2;
    private static final UriMatcher uriMatcher;
    static	{
        String path = CONTENT_URI.getPath().substring(1);
        uriMatcher = new	UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BookContract.AUTHORITY, "books", ALL_ROWS);
        uriMatcher.addURI(BookContract.AUTHORITY, "books/#", SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        this._dbHelper = new DBHelper(getContext());
        _db = _dbHelper.getWritableDatabase();
        return (_db == null) ? false : true;
    }

    public BookProvider open(){
        _db = _dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        _db.close();
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        String joinStat = BookContract.TABLE_NAME
                + " LEFT OUTER JOIN " + AuthorContract.TABLE_NAME
                + " ON (" + BookContract.TABLE_NAME + "." + BookContract.ID
                + " = " + AuthorContract.TABLE_NAME + "." +  AuthorContract.BOOK_ID + ")";
        builder.setTables(joinStat);

        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
//                builder.setProjectionMap(new HashMap<String, String>());
                break;
            case SINGLE_ROW:
                builder.appendWhere(BookContract.ID + " = " + BookContract.getId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = builder.query(_db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
                return BookContract.CONTENT_TYPE;
            case SINGLE_ROW:
                return BookContract.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values){
        long row = _db.insert(BookContract.TABLE_NAME, null, values);

        if (row > 0){
            Uri instanceUri = BookContract.withExtendedPath(row);
//            ContentResolver resolver = this._context.getContentResolver();
//            resolver.notifyChange(instanceUri, null);
            getContext().getContentResolver().notifyChange(instanceUri, null);
            return instanceUri;
        }
        throw new SQLException("Insertion failed");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(BookContract.TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
//                builder.setProjectionMap(new HashMap<String, String>());
                break;
            case SINGLE_ROW:
                builder.appendWhere(BookContract.ID + " = " + BookContract.getId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = builder.query(_db, null, selection, selectionArgs, null, null, DEFAULT_SORT);
        int rowsChanged = cursor.getCount();
        if(rowsChanged > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsChanged;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(BookContract.TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
//                builder.setProjectionMap(new HashMap<String, String>());
                break;
            case SINGLE_ROW:
                builder.appendWhere(BookContract.ID + " = " + BookContract.getId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = builder.query(_db, null, selection, selectionArgs, null, null, DEFAULT_SORT);
        return cursor.getCount();
    }
}
