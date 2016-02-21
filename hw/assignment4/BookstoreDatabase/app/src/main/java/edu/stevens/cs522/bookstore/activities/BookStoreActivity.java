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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

//import edu.stevens.cs522.bookstore.databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.providers.BookProvider;

public class BookStoreActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    static final private int CURSOR_LOADER_ID = 1;

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
    private SimpleCursorAdapter cursorAdapter;

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

        LoaderManager lm = getLoaderManager();
        lm.initLoader(CURSOR_LOADER_ID, null, this);
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
                        try {
                            ContentValues values = new ContentValues();
                            BookContract.putAll(values, newBook);
                            getContentResolver().insert(BookProvider.CONTENT_URI, values);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        /**
         * Called when action mode is first created. The menu supplied will be used to
         * generate action buttons for the action mode.
         *
         * @param mode ActionMode being created
         * @param menu Menu used to populate action buttons
         * @return true if the action mode should be created, false if entering this
         * mode should be aborted.
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            String title = selectedItem != null ? selectedItem.getTitle() : "Edit";
            mode.setTitle(title);
            MenuInflater inf = mode.getMenuInflater();
            inf.inflate(R.menu.bookstore_delete_item, menu);
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated.
         *
         * @param mode ActionMode being prepared
         * @param menu Menu used to populate action buttons
         * @return true if the menu or action mode was updated, false otherwise.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * Called to report a user click on an action button.
         *
         * @param mode The current ActionMode
         * @param item The item that was clicked
         * @return true if this callback handled the event, false if the standard MenuItem
         * invocation should continue.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean result = onOptionsItemSelected(item);
            selectedItem = null;
            mode.finish();
            return result;
        }

        /**
         * Called when an action mode is about to be exited and destroyed.
         *
         * @param mode The current ActionMode being destroyed
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedItemIndex = -1;
        }
    };

    public void initList(final Cursor c) {


        String[] from = new String[]{
                BookContract.TITLE,
//                BookContract.AUTHORS
        };
        int[] to = new int[]{
                R.id.cart_row_title,
//                R.id.cart_row_author
        };


        this.cursorAdapter = new SimpleCursorAdapter(this, R.layout.cart_row, c, from, to);
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
                if(checked){
                    selectedItemIds.add(id);
                }
                else {
                    selectedItemIds.remove(selectedItemIds.indexOf(id));
                }

                int count = selectedItemIds.size();
                String title = String.valueOf(count);
                if(count == 1){
                    title += " book selected";
                }
                else{
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CURSOR_LOADER_ID:
                String[] projection = new String[]{
                        BookContract.ID_FULL,
                        BookContract.TITLE,
                        BookContract.ISBN,
                        BookContract.PRICE,
                };
                return new CursorLoader(this,
                        BookProvider.CONTENT_URI,
                        projection,
                        null, null, null);
            default:
                throw new IllegalArgumentException("Unexpected loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.cursorAdapter.swapCursor(data);
        if (data.getCount() > 0) {
            lblListIsEmpty.setVisibility(View.GONE);
        } else {
            lblListIsEmpty.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.cursorAdapter.swapCursor(null);
    }


}