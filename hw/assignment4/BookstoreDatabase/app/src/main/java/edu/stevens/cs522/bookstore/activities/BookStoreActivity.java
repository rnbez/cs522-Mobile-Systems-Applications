package edu.stevens.cs522.bookstore.activities;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookStoreActivity extends ListActivity {

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

    private ArrayList<Book> shoppingCart;
    protected Object actionMode;
    private static Book selectedItem = null;
    private static int selectedItemIndex = -1;

    TextView lblListIsEmpty;

    private Context context = this;
    private CartDbAdapter dbAdapter;
    private SimpleCursorAdapter cursorAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO check if there is saved UI state, and if so, restore it (i.e. the cart contents)
        if (savedInstanceState != null) {
            shoppingCart = savedInstanceState.getParcelableArrayList(STATE_KEY);
        }

        if (shoppingCart == null) shoppingCart = new ArrayList<>();

        // TODO Set the layout (use cart.xml layout)
        setContentView(R.layout.cart);

        lblListIsEmpty = (TextView) findViewById(R.id.lblEmptyList);
        lblListIsEmpty.setVisibility(View.VISIBLE);

        // TODO use an array adapter to display the cart contents.

        ListView list = getListView();
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "item long clicked >> position:" + position);
                if (actionMode != null) {
                    return false;
                }

                selectedItemIndex = position;
                selectedItem = new Book((Cursor)getListView().getAdapter().getItem(selectedItemIndex));

                actionMode = BookStoreActivity.this.startActionMode(actionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

//        CartRowAdapter adapter = new CartRowAdapter(getApplicationContext(), R.layout.cart, shoppingCart);
//        this.setListAdapter(adapter);

        this.dbAdapter = new CartDbAdapter(context);
        this.cursorAdapter = getCursorAdapter(null);
        this.setListAdapter(cursorAdapter);

        try {
            this.dbAdapter.open();
            Cursor c = this.dbAdapter.fetchAllBooks();
            if (c.moveToFirst()){
                this.cursorAdapter.changeCursor(c);
                lblListIsEmpty.setVisibility(View.GONE);
            }
            this.dbAdapter.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SimpleCursorAdapter getCursorAdapter(Cursor cursor) {
//        final Uri data = Uri.parse("content://edu.stevens.cs522.bookstore/Books/");
//        final Cursor c = managedQuery(data, null, null, null, null);

        String[] from = new String[]{
                BookContract.TITLE,
                BookContract.AUTHORS
        };
        int[] to = new int[]{
                R.id.cart_row_title,
                R.id.cart_row_author
        };
        return new SimpleCursorAdapter(this, R.layout.cart_row, cursor, from, to);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO provide ADD, DELETE and CHECKOUT options
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
//                checkoutIntent.putExtra(SHOPPING_CART_KEY, shoppingCart);
                Log.d("Cart count", String.valueOf(this.cursorAdapter.getCount()));
                checkoutIntent.putExtra(SHOPPING_CART_COUNT_KEY, this.cursorAdapter.getCount());
                startActivityForResult(checkoutIntent, CHECKOUT_REQUEST);
                return true;
            case R.id.delete:
                try {
                    dbAdapter.open();
                    dbAdapter.delete(selectedItem);
                    Cursor c = dbAdapter.fetchAllBooks();
                    cursorAdapter.changeCursor(c);
                    if (c.moveToFirst()){
                        lblListIsEmpty.setVisibility(View.GONE);
                    }
                    else{
                        lblListIsEmpty.setVisibility(View.VISIBLE);
                    }
                    dbAdapter.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.details:
                Log.i(TAG, "details button clicked");
                Intent detailsIntent = new Intent(this, BookDetailsActivity.class);
                detailsIntent.putExtra(BOOK_DETAILS_KEY, selectedItem);
                startActivityForResult(detailsIntent, DETAILS_REQUEST);
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
//                        shoppingCart.add(newBook);
                        try {
                            this.dbAdapter.open();
                            long result = this.dbAdapter.persist(newBook);
                            Log.d("Insert result", String.valueOf(result));
                            this.cursorAdapter.changeCursor(this.dbAdapter.fetchAllBooks());
                            this.dbAdapter.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        lblListIsEmpty.setVisibility(View.GONE);
                    }
                    break;
                case CHECKOUT_REQUEST:
                    try {
                        this.dbAdapter.open();
                        this.dbAdapter.deleteAll();
                        this.cursorAdapter.changeCursor(this.dbAdapter.fetchAllBooks());
                        this.dbAdapter.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    lblListIsEmpty.setVisibility(View.VISIBLE);
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
            inf.inflate(R.menu.bookstore_item_selected, menu);
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
}