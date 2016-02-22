package edu.stevens.cs522.bookstore.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//import edu.stevens.cs522.bookstore.databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.layout.adapters.BookAdapter;
import edu.stevens.cs522.bookstore.managers.BookManager;
import edu.stevens.cs522.bookstore.managers.IContinue;
import edu.stevens.cs522.bookstore.managers.IEntityCreator;
import edu.stevens.cs522.bookstore.managers.IQueryListener;
import edu.stevens.cs522.bookstore.managers.QueryBuilder;
import edu.stevens.cs522.bookstore.managers.SimpleQueryBuilder;
import edu.stevens.cs522.bookstore.managers.TypedCursor;
import edu.stevens.cs522.bookstore.providers.BookProvider;

public class BookStoreActivity extends Activity {

    // Use this when logging errors and warnings.
    @SuppressWarnings("unused")
    private static final String TAG = BookStoreActivity.class.getCanonicalName();

    private static final String STATE_KEY = BookStoreActivity.class.getCanonicalName() + "_State";

    public static final String SHOPPING_CART_KEY = "shopping_cart";
    public static final String SHOPPING_CART_COUNT_KEY = "shopping_cart_count";
    public static final String BOOK_DETAILS_KEY = "book_details";

    // These are request codes for subactivity request calls
    static final private int ADD_REQUEST = 1;
    static final private int EDIT_REQUEST = ADD_REQUEST + 1;
    static final private int CHECKOUT_REQUEST = EDIT_REQUEST + 1;
    static final private int SEARCH_REQUEST = CHECKOUT_REQUEST + 1;
    static final private int DETAILS_REQUEST = SEARCH_REQUEST + 1;
    static final private IEntityCreator<Book> DEFAULT_ENTITY_CREATOR = new IEntityCreator<Book>() {
        @Override
        public Book create(Cursor cursor) {
            return new Book(cursor);
        }
    };


    static final private int BOOK_LOADER_ID = 1;

    private ArrayList<Book> shoppingCart;
    protected Object actionMode;
    private static Book selectedItem = null;
    private static ArrayList<Book> selectedItems = null;
    private ArrayList<Long> selectedItemIds = new ArrayList<>();
    private static int selectedItemIndex = -1;

    TextView lblListIsEmpty;
    ListView cartList;

    private Context context = this;
    //    private CartDbAdapter dbAdapter;
    private BookAdapter cursorAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            shoppingCart = savedInstanceState.getParcelableArrayList(STATE_KEY);
        }
        selectedItems = new ArrayList<>();

        setContentView(R.layout.cart);

        lblListIsEmpty = (TextView) findViewById(R.id.lblEmptyList);

        cartList = (ListView) findViewById(R.id.cartList);
        initList(null);


        QueryBuilder.executeQuery(TAG,
                this,
                BookContract.CONTENT_URI,
                BOOK_LOADER_ID,
                DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Book>() {

                    @Override
                    public void handleResults(TypedCursor<Book> results) {
                        cursorAdapter.swapCursor(results.getCursor());
                        if (results.getCount() > 0) {
                            lblListIsEmpty.setVisibility(View.GONE);
                        } else {
                            lblListIsEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });

//        LoaderManager lm = getLoaderManager();
//        lm.initLoader(BOOK_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.search:
                Log.i(TAG, "search button clicked");
                Intent searchIntent = new Intent(this, SearchBookActivity.class);
                startActivityForResult(searchIntent, SEARCH_REQUEST);
                return true;
            case R.id.checkout:
                Log.i(TAG, "checkout button clicked");
                Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
                Log.d("Cart count", String.valueOf(this.cursorAdapter.getCount()));
                checkoutIntent.putExtra(SHOPPING_CART_COUNT_KEY, this.cursorAdapter.getCount());
                startActivityForResult(checkoutIntent, CHECKOUT_REQUEST);
                return true;
            case R.id.delete:
                for (long id :
                        selectedItemIds) {
                    try {
                        Uri uri = BookContract.withExtendedPath(id);
                        getContentResolver().delete(uri, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // TODO Handle results from the Search and Checkout activities.

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SEARCH_REQUEST:
                    if (intent.hasExtra(SearchBookActivity.BOOK_RESULT_KEY)) {
                        Book newBook = (Book) intent.getParcelableExtra(SearchBookActivity.BOOK_RESULT_KEY);
//                        try {
//                            ContentValues values = new ContentValues();
//                            BookContract.putAll(values, newBook);
//                            getContentResolver().insert(BookProvider.CONTENT_URI, values);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        BookManager manager = new BookManager(this, BOOK_LOADER_ID, DEFAULT_ENTITY_CREATOR);
                        manager.persistAsync(newBook, new IContinue<Uri>() {
                            @Override
                            public void kontinue(Uri uri) {
                                getContentResolver().notifyChange(uri, null);
                            }
                        });
                    }
                    break;
                case CHECKOUT_REQUEST:
                    try {
                        getContentResolver().delete(BookContract.CONTENT_URI, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO save the shopping cart contents (which should be a list of parcelables).
        savedInstanceState.putParcelableArrayList(STATE_KEY, shoppingCart);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void initList(final Cursor c) {


        String[] from = new String[]{
                BookContract.TITLE,
//                BookContract.AUTHORS
        };
        int[] to = new int[]{
                R.id.cart_row_title,
//                R.id.cart_row_author
        };


//        this.cursorAdapter = new SimpleCursorAdapter(this, R.layout.cart_row, c, from, to);
        this.cursorAdapter = new BookAdapter(this, c);
        this.cartList.setAdapter(this.cursorAdapter);
        this.cartList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        this.cartList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailsIntent = new Intent(getApplicationContext(), BookDetailsActivity.class);
                Book book = new Book((Cursor) cartList.getAdapter().getItem(position));
                detailsIntent.putExtra(BOOK_DETAILS_KEY, book);
                startActivityForResult(detailsIntent, DETAILS_REQUEST);
            }
        });

        this.cartList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selectedItemIds.add(id);
                } else {
                    selectedItemIds.remove(selectedItemIds.indexOf(id));
                }

                int count = selectedItemIds.size();
                String title = String.valueOf(count);
                if (count == 1) {
                    title += " book selected";
                } else {
                    title += " books selected";
                }
                mode.setTitle(title);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.bookstore_delete_item, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean result = onOptionsItemSelected(item);
                mode.finish();
                return result;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedItemIds.clear();
            }
        });

    }


}