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

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Objects;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Rafael on 2/19/2016.
 */
public class BookProvider extends ContentProvider {

    public  static final Uri CONTENT_URI = BookContract.CONTENT_URI;

    private static final String[] DATABASE_CREATE;
    private static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 3;
    private static final String DEFAULT_SORT = BookContract.ID + " ASC";
    private static final HashMap<String, String> projectionMap;
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
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BookContract.AUTHORITY, path, ALL_ROWS);
        uriMatcher.addURI(BookContract.AUTHORITY, path + "/#", SINGLE_ROW);
        projectionMap = new HashMap<String, String>();
        projectionMap.put(BookContract.AUTHORS, "GROUP_CONCAT((first_name || mid_name || last_name),'|') as " + BookContract.AUTHORS);
        DATABASE_CREATE = new String[]{
            BookContract.CREATE_TABLE,
            AuthorContract.CREATE_TABLE
        };
    }

    @Override
    public boolean onCreate() {
        this._dbHelper = new DBHelper(getContext());
        _db = _dbHelper.getWritableDatabase();
        return (_db == null) ? false : true;
    }

//    public BookProvider open(){
//        _db = _dbHelper.getWritableDatabase();
//        return this;
//    }

//    public void close() {
//        _db.close();
//    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        String joinStat = BookContract.TABLE_NAME
                + " LEFT OUTER JOIN " + AuthorContract.TABLE_NAME
                + " ON (" + BookContract.TABLE_NAME + "." + BookContract.ID
                + " = " + AuthorContract.TABLE_NAME + "." +  AuthorContract.BOOK_ID + ")";
        String groupby = "Books._id, title, price, isbn";
        builder.setTables(joinStat);
        HashMap<String, String> map = projectionMap;
        for (String field :
                projection) {
            if(!map.containsKey(field)) map.put(field, field);
        }
        builder.setProjectionMap(map);

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
        Log.d("query", builder.buildQuery(projection, selection, groupby, null, null, null));
        Cursor cursor = builder.query(_db, projection, selection, selectionArgs, groupby, null, null);
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
        Object objAuthors = values.get(BookContract.AUTHORS);
        values.remove(BookContract.AUTHORS);

        long rowId = _db.insert(BookContract.TABLE_NAME, null, values);

        if (rowId > 0){
            if(objAuthors != null) {
                Author[] authors = BookContract.getAuthorsFromString(objAuthors.toString(), BookContract.SEPARATOR_CHAR);
                for (Author author :
                        authors) {
                    ContentValues autContentValues = new ContentValues();
                    AuthorContract.putFirstName(autContentValues, author.getFirstName());
                    AuthorContract.putMiddleName(autContentValues, author.getMiddleInitial());
                    AuthorContract.putLastName(autContentValues, author.getLastName());
                    AuthorContract.putBookId(autContentValues, rowId);

                    _db.insert(AuthorContract.TABLE_NAME, null, autContentValues);
                }
            }

            Uri instanceUri = BookContract.withExtendedPath(rowId);
            getContext().getContentResolver().notifyChange(instanceUri, null);
            return instanceUri;
        }
        throw new SQLException("Insertion failed");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(BookContract.TABLE_NAME);
        int count = 0;


        switch (uriMatcher.match(uri)){
            case ALL_ROWS:
                count = _db.delete(BookContract.TABLE_NAME, selection, selectionArgs);
                break;
            case SINGLE_ROW:
                selection = BookContract.ID + " = ?";
                selectionArgs = new String[]{
                    uri.getLastPathSegment()
                };

                count = _db.delete(BookContract.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if(count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
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
