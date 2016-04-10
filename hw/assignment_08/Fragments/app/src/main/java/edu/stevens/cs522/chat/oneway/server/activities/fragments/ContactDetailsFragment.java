package edu.stevens.cs522.chat.oneway.server.activities.fragments;

import android.os.Bundle;
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
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

public class ContactDetailsFragment extends Fragment {

    private static final String TAG = ContactDetailsFragment.class.getCanonicalName();
    public static final String PEER_DETAILS_KEY = TAG + "peer_details";

    TextView nameView, latitudeView, longitudeView, isEmptyMsgView;
    ListView messagesListView;

    Peer contact;
    List<String> messageList;
    ArrayAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__contact_book_details, container, false);


        nameView = (TextView) view.findViewById(R.id.contact_book_details_name);
        latitudeView = (TextView) view.findViewById(R.id.contact_book_details_latitude);
        longitudeView = (TextView) view.findViewById(R.id.contact_book_details_longitude);
        isEmptyMsgView = (TextView) view.findViewById(R.id.contact_book_details_list_empty);
        messagesListView = (ListView) view.findViewById(R.id.contact_book_details_messages);



        Bundle args = getArguments();
        if (args != null && args.containsKey(PEER_DETAILS_KEY)) {
            contact = args.getParcelable(PEER_DETAILS_KEY);

            nameView.setText(contact.getName());
            latitudeView.setText("latitude");
            longitudeView.setText("longitude");

            messageList = new ArrayList<>();
            arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, messageList);
            messagesListView.setAdapter(arrayAdapter);
            isEmptyMsgView.setVisibility(View.GONE);
        }
        else{
            isEmptyMsgView.setVisibility(View.VISIBLE);

            nameView.setVisibility(View.GONE);
            latitudeView.setVisibility(View.GONE);
            longitudeView.setVisibility(View.GONE);
            messagesListView.setVisibility(View.GONE);
        }


        return view;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList.clear();
        for (Message m :
                messageList) {
            this.messageList.add(m.getMessageText());
        }
        this.arrayAdapter.notifyDataSetChanged();
    }
}
