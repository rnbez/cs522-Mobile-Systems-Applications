package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Book;

public class CheckoutActivity extends Activity {
    private int shoppingCartSize = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        shoppingCart = getIntent().getParcelableArrayListExtra(BookStoreActivity.SHOPPING_CART_KEY);
        shoppingCartSize = getIntent().getIntExtra(BookStoreActivity.SHOPPING_CART_COUNT_KEY, -1);
        Log.d("Cart count", String.valueOf(shoppingCartSize));

        setContentView(R.layout.checkout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO display ORDER and CANCEL options.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_checkout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // TODO

        // ORDER: display a toast message of how many books have been ordered and return

        // CANCEL: just return with REQUEST_CANCELED as the result code

        switch (item.getItemId()) {
            case R.id.order: {
                showToast();
                Intent resultData = new Intent();
                setResult(Activity.RESULT_OK, resultData);
                finish();
                return true;
            }
            case R.id.cancel: {
                Intent resultData = new Intent();
                setResult(Activity.RESULT_CANCELED, resultData);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast() {
        Context context = getApplicationContext();
        CharSequence text = "You have ordered " + shoppingCartSize + " books!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}