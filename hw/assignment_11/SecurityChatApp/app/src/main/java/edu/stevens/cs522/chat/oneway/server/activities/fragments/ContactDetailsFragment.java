package edu.stevens.cs522.chat.oneway.server.activities.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.activities.base.BaseFragmentActivity;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;

public class ContactDetailsFragment extends Fragment {



    public interface IContactDetailsFragmentListener {
        public void getContactMessagesAsync(long peerId);
        public char[] getDatabaseKey();
    }


    private static final String TAG = ContactDetailsFragment.class.getCanonicalName();
    public static final String PEER_DETAILS_KEY = TAG + "peer_details";

    TextView nameView, locationView, latitudeView, longitudeView, isEmptyMsgView;
    ListView messagesListView;

    Peer contact;
    List<String> messageList;
    ArrayAdapter arrayAdapter;
    IContactDetailsFragmentListener listener;
    boolean isActivityCreated;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__contact_book_details, container, false);


        nameView = (TextView) view.findViewById(R.id.contact_book_details_name);
//        latitudeView = (TextView) view.findViewById(R.id.contact_book_details_latitude);
//        longitudeView = (TextView) view.findViewById(R.id.contact_book_details_longitude);
        locationView = (TextView) view.findViewById(R.id.contact_book_details_location);
        isEmptyMsgView = (TextView) view.findViewById(R.id.contact_book_details_list_empty);
        messagesListView = (ListView) view.findViewById(R.id.contact_book_details_messages);


        Bundle args = getArguments();
        if (args != null && args.containsKey(PEER_DETAILS_KEY)) {
            contact = args.getParcelable(PEER_DETAILS_KEY);

            nameView.setText(contact.getName());
            locationView.setText(String.valueOf(contact.getAddress()));
//            latitudeView.setText(String.valueOf(contact.getLatitute()));
//            longitudeView.setText(String.valueOf(contact.getLongitude()));


            messageList = new ArrayList<>();
            arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, messageList);
            messagesListView.setAdapter(arrayAdapter);
            isEmptyMsgView.setVisibility(View.GONE);
        } else {
            isEmptyMsgView.setVisibility(View.VISIBLE);

            if (view.findViewById(R.id.contact_book_details_container) != null){
                view.findViewById(R.id.contact_book_details_container).setVisibility(View.GONE);
            }
        }


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (IContactDetailsFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.listener.toString()
                    + " must implement IContactListFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isActivityCreated = true;
        if (this.contact != null) {
//            this.listener.getContactMessagesAsync(this.contact.getId());
            final Activity activity = getActivity();

            SimpleQueryBuilder.executeQuery(activity,
                    PeerContract.withDatabaseKeyUri(listener.getDatabaseKey(),PeerContract.getMessagesUri(this.contact.getId())),
                    MessageContract.DEFAULT_ENTITY_CREATOR,
                    new ISimpleQueryListener<Message>() {
                        @Override
                        public void handleResults(List<Message> results) {
                                setMessageList(results);
                        }
                    });
        }


    }

    public void setMessageList(List<Message> messageList) {
        if (this.messageList != null)
            this.messageList.clear();
        else {
            this.messageList = new ArrayList<>();
            this.arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, messageList);
            this.messagesListView.setAdapter(arrayAdapter);
        }

        for (Message m :
                messageList) {
            this.messageList.add(m.getMessageText());
        }
        this.arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = new IContactDetailsFragmentListener() {
            @Override
            public void getContactMessagesAsync(long peerId) {
            }

            @Override
            public char[] getDatabaseKey() {
                return new char[0];
            }
        };

    }
}
