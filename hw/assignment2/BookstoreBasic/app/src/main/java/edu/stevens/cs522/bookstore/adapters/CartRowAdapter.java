package edu.stevens.cs522.bookstore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Rafael on 2/8/2016.
 */
public class CartRowAdapter extends ArrayAdapter<Book> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public CartRowAdapter(Context context, int resource, List<Book> objects) {
        super(context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Book item = getItem(position);
        String title = item.title;
        Author author = item.authors[0];
        String authorName = author.firstName + " "
                + (author.middleInitial != null ? author.middleInitial + " " : "")
                + author.lastName;

        if (convertView == null){
            convertView = LayoutInflater.from(super.getContext()).inflate(R.layout.cart_row, parent, false);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.cart_row_title);
        TextView authorView = (TextView) convertView.findViewById(R.id.cart_row_author);
        titleView.setText(title);
        authorView.setText(authorName);
        return convertView;
    }
}
