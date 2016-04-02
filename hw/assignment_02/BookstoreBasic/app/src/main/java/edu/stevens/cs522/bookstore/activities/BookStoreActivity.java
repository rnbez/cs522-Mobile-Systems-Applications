package edu.stevens.cs522.bookstore.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.adapters.CartRowAdapter;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookStoreActivity extends ListActivity {

    // Use this when logging errors and warnings.
    @SuppressWarnings("unused")
    private static final String TAG = BookStoreActivity.class.getCanonicalName();

    private static final String STATE_KEY = BookStoreActivity.class.getCanonicalName() + "_State";

    public static final String SHOPPING_CART_KEY = "shopping_cart";

    // These are request codes for subactivity request calls
    static final private int ADD_REQUEST = 1;
    static final private int EDIT_REQUEST = ADD_REQUEST + 1;
    static final private int CHECKOUT_REQUEST = EDIT_REQUEST + 1;
    static final private int SEARCH_REQUEST = CHECKOUT_REQUEST + 1;

    // There is a reason this must be an ArrayList instead of a List.
    @SuppressWarnings("unused")
    private ArrayList<Book> shoppingCart;
    protected Object actionMode;
    private static int selectedItem = -1;
    private String listItem = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO check if there is saved UI state, and if so, restore it (i.e. the cart contents)
        if (savedInstanceState != null) {
            shoppingCart = savedInstanceState.getParcelableArrayList(STATE_KEY);
        }
        if (shoppingCart == null) {
            Author[] authors = new Author[1];
            authors[0] = new Author("George", "RR", "Martin");
            shoppingCart = new ArrayList<>();
            shoppingCart.add(new Book(1, "Game of Thrones", authors, "12345678941", "$16.90"));
            shoppingCart.add(new Book(2, "A Dance with Dragons", authors, "12345678941", "$19.90"));
        }
        // TODO Set the layout (use cart.xml layout)
        setContentView(R.layout.cart);

        findViewById(R.id.lblEmptyList).setVisibility(shoppingCart.isEmpty() ? View.VISIBLE : View.GONE);

        // TODO use an array adapter to display the cart contents.
        CartRowAdapter adapter = new CartRowAdapter(getApplicationContext(), R.layout.cart, shoppingCart);
        ListView list = getListView();
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "item long clicked >> position:" + position);
                if(actionMode != null){
                    return false;
                }
                selectedItem = position;
                listItem =  shoppingCart.get(selectedItem).title;
                actionMode = BookStoreActivity.this.startActionMode(actionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

        this.setListAdapter(adapter);
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
        // TODO

        // ADD provide the UI for adding a book
        // Intent addIntent = new Intent(this, AddBookActivity.class);
        // startActivityForResult(addIntent, ADD_REQUEST);

        // DELETE delete the currently selected book

        // CHECKOUT provide the UI for checking out

        switch (item.getItemId()) {
            case R.id.add:
                Log.i(TAG, "add button clicked");
                Intent addIntent = new Intent(this, AddBookActivity.class);
                startActivityForResult(addIntent, ADD_REQUEST);
                return true;
            case R.id.checkout:
                Log.i(TAG, "checkout button clicked");
                Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
                checkoutIntent.putExtra(SHOPPING_CART_KEY, shoppingCart);
                startActivityForResult(checkoutIntent, CHECKOUT_REQUEST);
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

        // Use SEARCH_REQUEST and CHECKOUT_REQUEST codes to distinguish the cases.

        // SEARCH: add the book that is returned to the shopping cart.

        // CHECKOUT: empty the shopping cart.
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD_REQUEST:
                    if (intent.hasExtra(AddBookActivity.BOOK_RESULT_KEY)) {
                        Book newBook = (Book) intent.getParcelableExtra(AddBookActivity.BOOK_RESULT_KEY);
                        shoppingCart.add(newBook);
                    }
                    break;
                case CHECKOUT_REQUEST:
                    shoppingCart.clear();
                    break;

            }
            ((CartRowAdapter) getListView().getAdapter()).notifyDataSetChanged();
        }

//        findViewById(R.id.lblEmptyList).setVisibility(shoppingCart.isEmpty() ? View.GONE : View.VISIBLE);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO save the shopping cart contents (which should be a list of parcelables).
        savedInstanceState.putParcelableArrayList(STATE_KEY, shoppingCart);
        super.onSaveInstanceState(savedInstanceState);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback(){

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
            mode.setTitle(listItem);
            MenuInflater inf = mode.getMenuInflater();
            inf.inflate(R.menu.bookstore_delete, menu);
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
            if (item.getItemId() == R.id.delete) {
                shoppingCart.remove(selectedItem);
                ((CartRowAdapter) getListView().getAdapter()).notifyDataSetChanged();
                mode.finish();
                return true;
            }
            else return false;
        }

        /**
         * Called when an action mode is about to be exited and destroyed.
         *
         * @param mode The current ActionMode being destroyed
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedItem = -1;
        }
    };
}