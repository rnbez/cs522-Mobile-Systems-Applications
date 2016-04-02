//package edu.stevens.cs522.bookstore.databases;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.DatabaseErrorHandler;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.text.StaticLayout;
//import android.util.Log;
//
//import java.io.FileOutputStream;
//import java.sql.SQLException;
//import java.util.Objects;
//import java.util.regex.Pattern;
//
//import edu.stevens.cs522.bookstore.contracts.AuthorContract;
//import edu.stevens.cs522.bookstore.contracts.BookContract;
//import edu.stevens.cs522.bookstore.entities.Author;
//import edu.stevens.cs522.bookstore.entities.Book;
//
///**
// * Created by Rafael on 2/13/2016.
// */
//public class CartDbAdapter {
//
//    public static final String BOOK_TABLE = "Books";
//    public static final String AUTHOR_TABLE = "Authors";
//    //---------------------------------------------------
//    private static final String[] DATABASE_CREATE = {
//            "CREATE TABLE Books(" +
//                    "_id INTEGER PRIMARY KEY," +
//                    "title TEXT NOT NULL," +
//                    "price REAL NOT NULL," +
//                    "isbn TEXT NOT NULL" +
//                    ");",
//            "CREATE TABLE Authors (" +
//                    "_id INTEGER PRIMARY KEY," +
//                    "first_name TEXT NOT NULL," +
//                    "mid_name TEXT," +
//                    "last_name TEXT NOT NULL," +
//                    "book_fk INTEGER NOT NULL," +
//                    "FOREIGN KEY(book_fk) REFERENCES Books(_id) ON DELETE CASCADE" +
//                    ");",
//            "CREATE INDEX AuthorsBookIndex ON Authors(book_fk);",
//            "PRAGMA	foreign_keys=ON;"
//    };
//    private static final String DATABASE_NAME = "books.db";
//    private static final int DATABASE_VERSION = 10;
//    //---------------------------------------------------
//    private SQLiteDatabase _db;
//    private final Context _context;
//    private DatabaseHelper _dbHelper;
//
//    // SHOULD THIS BE STATIC?
//    private static class DatabaseHelper extends SQLiteOpenHelper {
//
//
//        public DatabaseHelper(Context context) {
//            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        }
//
//        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//            super(context, name, factory, version);
//        }
//
//        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
//            super(context, name, factory, version, errorHandler);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) {
//            for (String createStatement :
//                    DATABASE_CREATE) {
//                db.execSQL(createStatement);
//            }
//            Log.d("database", "tables created");
//        }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//            Log.w("TaskDBAdapter",
//                    "Upgrading from version " + oldVersion
//                            + "	to	" + newVersion);
//
//            db.execSQL("DROP TABLE IF EXISTS " + BOOK_TABLE);
//            db.execSQL("DROP TABLE IF EXISTS " + AUTHOR_TABLE);
//
//            onCreate(db);
//        }
//    }
//
//    public CartDbAdapter(Context _context) {
//        this._context = _context;
//        this._dbHelper = new DatabaseHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
//        try {
//            this.open();
//            Log.d("database", "checking tables");
//            Log.d("database", "querying books: " + String.valueOf(_db.rawQuery("SELECT * FROM " + BOOK_TABLE, null)));
//            Log.d("database", "querying authors: " + String.valueOf(_db.rawQuery("SELECT * FROM " + AUTHOR_TABLE, null)));
//            this.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public CartDbAdapter open() throws SQLException {
//        _db = _dbHelper.getWritableDatabase();
//        return this;
//    }
//
//    public Cursor fetchAllBooks() {
//        String[] projection = {BookContract.ID, BookContract.TITLE, BookContract.AUTHORS};
////        return _db.query(BOOK_TABLE,
////                projection,
////                null, null, null, null, null);
//        final String q =
//                "SELECT Books._id, title, price, isbn, GROUP_CONCAT(last_name,', ') as authors " +
//                        "FROM Books LEFT OUTER JOIN Authors ON Books._id = Authors.book_fk " +
//                        "GROUP BY Books._id, title, price, isbn ";
//        return _db.rawQuery(q, null);
//    }
//
//    public Book fetchBook(long id) {
//        String[] projection = {BookContract.ID, BookContract.TITLE, BookContract.AUTHORS};
//        String selection = BookContract.ID + "=?";
//        String[] selectionArgs = {String.valueOf(id)};
////        Cursor c = _db.query(BOOK_TABLE,
////                projection,
////                selection,
////                selectionArgs,
////                null, null, null);
//        final String q =
//                "SELECT Books._id, title, price, isbn, GROUP_CONCAT(last_name,', ') as authors " +
//                        "FROM Books LEFT OUTER JOIN Authors " +
//                        "ON Books._id = Authors.book_fk AND Books._id = ? " +
//                        "GROUP BY Books._id, title, price, isbn";
//        Cursor c = _db.rawQuery(q, selectionArgs);
//        return new Book(c);
//    }
//
//    public long persist(Book book) throws SQLException {
//        ContentValues contentValues = new ContentValues();
//        BookContract.putTitle(contentValues, book.getTitle());
//        BookContract.putIsbn(contentValues, book.getIsbn());
////        BookContract.putAuthors(contentValues, book.getAuthors());
//        BookContract.putPrice(contentValues, book.getPrice());
//
//        long insertBookResult = _db.insert(BOOK_TABLE, null, contentValues);
//        Log.d("database", "Book " + book.getTitle() + " inserted");
////        Cursor cursor = _db.rawQuery("SELECT MAX(_id) as last_id FROM " + BOOK_TABLE, null);
////        Log.d("database", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("last_id"))));
////
////        for (int i = 0; i < cursor.getColumnCount(); i++) {
////            String columnName = cursor.getColumnName(i);
////            Log.d("database", columnName);
////            Log.d("database", String.valueOf(cursor.getInt(i)));
////        }
////        int bookId = cursor.getInt(0);
//        int bookId = -1;
//        Cursor c = this.fetchAllBooks();
//        if (c.moveToLast()){
//            Book lastInsertion = new Book(c);
//            bookId = lastInsertion.getId();
//            Log.d("database", "Last id is: " + String.valueOf(bookId));
//        }
//
//        if (insertBookResult > 0) {
//            for (Author aut :
//                    book.getAuthors()) {
//                contentValues = new ContentValues();
//                AuthorContract.putBookId(contentValues, bookId);
//                AuthorContract.putFirstName(contentValues, aut.getFirstName());
//                AuthorContract.putLastName(contentValues, aut.getLastName());
//
//                if(aut.getMiddleInitial() != null && !aut.getMiddleInitial().isEmpty()){
//                    AuthorContract.putMiddleName(contentValues, " " + aut.getMiddleInitial() + " ");
//                }
//                else{
//                    AuthorContract.putMiddleName(contentValues, " ");
//                }
//
//
//                _db.insert(AUTHOR_TABLE, null, contentValues);
//                Log.d("database", "Author " + aut.toString() + " inserted");
//            }
//        }
//
//        return insertBookResult;
//    }
//
//    public boolean delete(Book book) {
//        return _db.delete(BOOK_TABLE,
//                BookContract.ID + "=" + book.getId(), null) > 0;
//    }
//
//    public boolean deleteAll() {
//        return _db.delete(BOOK_TABLE,
//                BookContract.ID + ">= 0", null) > 0;
//    }
//
//    public void close() {
//        _db.close();
//    }
//
//}
//
//
