package edu.stevens.cs522.chat.oneway.server.activities.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ChatroomRowAdapter;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatroomListFragment extends Fragment {

    public interface IChatroomListFragmentListener {
        public void showChatroomDetails(Chatroom chatroom);

        public void getChatroomListAsync();

        public Cursor getChatroomListCursor();
    }

    TextView listEmptyView;
    LinearLayout layoutContainer;
    ListView listView;

    ChatroomRowAdapter cursorAdapter;
    IChatroomListFragmentListener listener;
    Activity activity;
    boolean isActivityCreated;
    IQueryListener queryListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag__chatroom_list, container, false);

        this.layoutContainer = (LinearLayout) view.findViewById(R.id.frag__chatroom_list_layout_container);
        this.listView = (ListView) view.findViewById(R.id.frag__chatroom_list_content);
        this.listEmptyView = (TextView) view.findViewById(R.id.frag__chatroom_list_empty);
//        this.listEmptyView.setVisibility(View.VISIBLE);
        this.cursorAdapter = null;

        this.cursorAdapter = new ChatroomRowAdapter(getActivity(), null);
        this.listView.setAdapter(this.cursorAdapter);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isActivityCreated) {
                    Chatroom chatroom = new Chatroom((Cursor) cursorAdapter.getItem(position));
                    listener.showChatroomDetails(chatroom);
                }
            }
        });

        queryListener = new IQueryListener<Chatroom>() {
            @Override
            public void handleResults(TypedCursor<Chatroom> typedCursor) {
                Cursor cursor = typedCursor.getCursor();
                cursorAdapter.swapCursor(cursor);
                if (listEmptyView != null)
                    if (cursor != null && cursor.getCount() > 0)
                        listEmptyView.setVisibility(View.GONE);
                    else
                        listEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        };

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (IChatroomListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.listener.toString()
                    + " must implement IChatroomListFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isActivityCreated = true;
//        this.listener.getChatroomListAsync();
        this.setListCursor(this.listener.getChatroomListCursor());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = new IChatroomListFragmentListener() {
            @Override
            public void showChatroomDetails(Chatroom chatroom) {
            }

            @Override
            public void getChatroomListAsync() {
            }

            @Override
            public Cursor getChatroomListCursor() {
                return null;
            }
        };
    }

    public IQueryListener getQueryListener() {
        return queryListener;
    }

    public void setListCursor(Cursor cursor) {
        if (cursorAdapter != null)
            cursorAdapter.swapCursor(cursor);
        if (listEmptyView != null)
            if (cursor != null && cursor.getCount() > 0) {
                listEmptyView.setVisibility(View.GONE);

//                if (cursor != null) {
//                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    Chatroom currentChatroom = new Chatroom(sharedPreferences.getString(App.PREF_KEY_CHATROOM, App.PREF_DEFAULT_CHATROOM));
//                    int pos = 0;
//                    if (cursor.moveToFirst()) {
//                        do {
//                            Chatroom cursorChat = new Chatroom(cursor);
//                            if (cursorChat.getId() == currentChatroom.getId()) {
//                                pos = cursor.getPosition();
//                                break;
//                            }
//                        } while (cursor.moveToNext());
//                        listView.setItemChecked(pos, true);
//                        listView.setSelection(pos);
//                        if (listView.getSelectedView() != null)
//                            listView.getSelectedView().setSelected(true);
//                    }
//                }
//                listener.showChatroomDetails(currentChatroom);
            }
            else
                listEmptyView.setVisibility(View.VISIBLE);
    }
}
