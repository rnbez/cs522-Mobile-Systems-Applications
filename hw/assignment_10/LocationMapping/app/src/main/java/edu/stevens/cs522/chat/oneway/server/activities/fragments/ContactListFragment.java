package edu.stevens.cs522.chat.oneway.server.activities.fragments;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ContactRowAdapter;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

public class ContactListFragment extends Fragment {

    public interface IContactListFragmentListener {
        public void showContactDetails(Peer peer);

        public Cursor getContactListCursor();
    }

    private static final String TAG = ContactListFragment.class.getCanonicalName();


    ListView listView;
    TextView listEmptyView;
    ContactRowAdapter cursorAdapter;
    List<String> contactList;
    ArrayAdapter arrayAdapter;
    IContactListFragmentListener listener;
    Activity activity;
    boolean isActivityCreated;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__contact_book_list, container, false);
        this.listView = (ListView) view.findViewById(R.id.frag__contact_book_list_content);
        this.listEmptyView = (TextView) view.findViewById(R.id.frag__contact_book_list_empty);
        this.listEmptyView.setVisibility(View.VISIBLE);
        this.cursorAdapter = null;

//        contactList = new ArrayList<>();
//        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, contactList);
//        this.listView.setAdapter(arrayAdapter);

        this.cursorAdapter = new ContactRowAdapter(getActivity(), null);
        this.listView.setAdapter(this.cursorAdapter);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "contact clicked");
                if (isActivityCreated) {
                    Peer peer = new Peer((Cursor) cursorAdapter.getItem(position));
                    listener.showContactDetails(peer);
                }
            }
        });

//        SimpleQueryBuilder.executeQuery(getActivity(),
//                PeerContract.CONTENT_URI,
//                PeerContract.DEFAULT_ENTITY_CREATOR,
//                new ISimpleQueryListener<Peer>() {
//                    @Override
//                    public void handleResults(List<Peer> results) {
//                        if (results != null && results.size() > 0) {
//                            listEmptyView.setVisibility(View.GONE);
//                        } else {
//                            listEmptyView.setVisibility(View.VISIBLE);
//                        }
//                        if (contactList != null)
//                            contactList.clear();
//                        for (Peer p :
//                                results) {
//                            contactList.add(p.getName());
//                        }
//                        arrayAdapter.notifyDataSetChanged();
//                    }
//                });

//        QueryBuilder.executeQuery(TAG,
//                getActivity(),
//                PeerContract.CONTENT_URI,
//                PeerContract.CURSOR_LOADER_ID,
//                PeerContract.DEFAULT_ENTITY_CREATOR,
//                new IQueryListener<Peer>() {
//
//                    @Override
//                    public void handleResults(TypedCursor<Peer> typedCursor) {
//                        setListCursor(typedCursor.getCursor());
//                    }
//
//                    @Override
//                    public void closeResults() {
//                        setListCursor(null);
//                    }
//                });

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (IContactListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.listener.toString()
                    + " must implement IContactListFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isActivityCreated = true;
        this.setListCursor(this.listener.getContactListCursor());
    }

    public void setListCursor(Cursor cursor) {
        if (listEmptyView != null)
            if (cursor != null && cursor.getCount() > 0)
                listEmptyView.setVisibility(View.GONE);
            else
                listEmptyView.setVisibility(View.VISIBLE);

        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = new IContactListFragmentListener() {
            @Override
            public void showContactDetails(Peer peer) {
            }

            @Override
            public Cursor getContactListCursor() {
                return null;
            }
        };
    }
}
