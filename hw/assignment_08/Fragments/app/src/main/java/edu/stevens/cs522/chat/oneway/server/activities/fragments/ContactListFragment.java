package edu.stevens.cs522.chat.oneway.server.activities.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ContactAdapter;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

public class ContactListFragment extends Fragment {

    public interface IContactListFragmentListener {
        public void showContactDetails(Peer peer);
    }

    private static final String TAG = ContactListFragment.class.getCanonicalName();


    ListView contacts;
    TextView listEmptyView;
    ContactAdapter cursorAdapter;
    IContactListFragmentListener context;
    boolean isActivityCreated;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__contact_book_list, container, false);
        this.contacts = (ListView) view.findViewById(R.id.frag__contact_book_list_content);
        this.listEmptyView = (TextView) view.findViewById(R.id.frag__contact_book_list_empty);
        this.listEmptyView.setVisibility(View.VISIBLE);
        this.cursorAdapter = null;
        this.isActivityCreated = false;
        initList();
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = (IContactListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.contacts.toString()
                    + " must implement IContactListFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isActivityCreated = true;
//        initList();
    }

    public void setListCursor(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            listEmptyView.setVisibility(View.GONE);
        } else {
            listEmptyView.setVisibility(View.VISIBLE);
        }
        cursorAdapter.swapCursor(cursor);
    }

    public void initList() {
        this.cursorAdapter = new ContactAdapter(getContext(), null);
        this.contacts.setAdapter(this.cursorAdapter);
        this.contacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        this.contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "contact clicked");

//                Intent detailsIntent = new Intent(getnContext(), ContactDetailsActivity.class);
                if (isActivityCreated) {
                    Peer peer = new Peer((Cursor) cursorAdapter.getItem(position));
                    context.showContactDetails(peer);
                }

//                detailsIntent.putExtra(ContactDetailsActivity.PEER_DETAILS_KEY, peer);
//                startActivity(detailsIntent);
            }
        });


    }
}
