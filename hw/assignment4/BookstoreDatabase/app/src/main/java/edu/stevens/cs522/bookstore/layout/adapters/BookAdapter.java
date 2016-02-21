package edu.stevens.cs522.bookstore.layout.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;

/**
 * Created by Rafael on 2/21/2016.
 */
public class BookAdapter extends ResourceCursorAdapter {
    protected final	static	int ROW_LAYOUT =	android.R.layout.simple_list_item_2;
    public BookAdapter(Context	context,	Cursor	cursor)	{
        super(context,	ROW_LAYOUT,	cursor,	0);
    }
    @Override
    public View newView(Context context, Cursor cur,	ViewGroup parent)	{
        LayoutInflater inflater	=	(LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(ROW_LAYOUT,	parent,	false);
    }
    @Override
    public void bindView(View	view,	Context	context,	Cursor	cursor)	{
        TextView titleLine = (TextView) view.findViewById(R.id.cart_row_title);
        TextView authorLine = (TextView) view.findViewById(R.id.cart_row_author);
        titleLine.setText(BookContract.getTitle(cursor));
        authorLine.setText("Fake author");
    }
}