package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookDetailsActivity extends Activity {

    Book book;
    TextView details_title, details_isbn, details_price;
    ListView details_authorsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_details);

        Intent callingIntent = getIntent();
        book = getIntent().getParcelableExtra(BookStoreActivity.BOOK_DETAILS_KEY);

        details_title = (TextView) findViewById(R.id.details_title);
        details_isbn = (TextView) findViewById(R.id.details_isbn);
        details_price = (TextView) findViewById(R.id.details_price);
        details_authorsList = (ListView) findViewById(R.id.details_authorsList);

        details_title.setText((CharSequence) book.getTitle());
        details_isbn.setText((CharSequence) book.getIsbn());
        details_price.setText((CharSequence) "$" + String.valueOf(book.getPrice()));

        ArrayList<String> list = new ArrayList<>();
        for (Author a :
                book.getAuthors()) {
            list.add(a.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        details_authorsList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO display ORDER and CANCEL options.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
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

}
