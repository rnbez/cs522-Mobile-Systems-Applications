package edu.stevens.cs522.chat.oneway.server.activities.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmDialogFragment extends DialogFragment {

    public static final String DIALOG_KEY = "dialog_id";
    public static final String INFO_MESSAGE_KEY = "info_message_key";
    public static final String CONFIRM_TEXT_KEY = "confirm_text_key";
    public static final String CANCEL_TEXT_KEY = "cancel_text_key";


    public static void launch(Activity context, int dialogId, String tag, int messageRId) {
        launch(context, dialogId, tag, messageRId, R.string.dialog_confirm, R.string.dialog_cancel);
    }


    public static void launch(Activity context,
                              int dialogId,
                              String tag,
                              int infoMessageTextId,
                              int confirmTextId,
                              int cancelTextId
    ) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        Resources res = context.getResources();
        Bundle args = new Bundle();
        args.putInt(DIALOG_KEY, dialogId);
        args.putString(INFO_MESSAGE_KEY, res.getString(infoMessageTextId));
        if (confirmTextId != -1)
            args.putString(CONFIRM_TEXT_KEY, res.getString(confirmTextId));
        if (cancelTextId != -1)
            args.putString(CANCEL_TEXT_KEY, res.getString(cancelTextId));
        dialog.setArguments(args);
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        dialog.show(fm, tag);
    }

    private IConfirmDialogFragmentListener listener;

    private int dialogId;

    public interface IConfirmDialogFragmentListener {
        public void confirmDialogCallback(int dialogId, Dialog dialog, boolean confirm);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IConfirmDialogFragmentListener)) {
            throw new IllegalStateException("Activity must implement INewChatFragmentListener");
        }
        listener = (IConfirmDialogFragmentListener) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogId = getArguments().getInt(DIALOG_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__new_message, container, false);
        Bundle args = getArguments();

        TextView messageView = (TextView) view.findViewById(R.id.frag__confirm_dialog_message);
        Button confirmButton = (Button) view.findViewById(R.id.frag__confirm_dialog_confirm);
        Button cancelButton = (Button) view.findViewById(R.id.frag__confirm_dialog_cancel);

        messageView.setText(args.getString(INFO_MESSAGE_KEY));

        if (args.containsKey(CONFIRM_TEXT_KEY))
            confirmButton.setText(args.getString(CONFIRM_TEXT_KEY));
        else
            confirmButton.setVisibility(View.GONE);

        if (args.containsKey(CANCEL_TEXT_KEY))
            cancelButton.setText(args.getString(CANCEL_TEXT_KEY));
        else
            cancelButton.setVisibility(View.GONE);


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.confirmDialogCallback(dialogId, getDialog(), true);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.confirmDialogCallback(dialogId, getDialog(), false);
            }
        });


        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
