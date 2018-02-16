package com.boshanlu.mobile.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.utils.GetId;
import com.squareup.picasso.Picasso;

/**
 * Created by free2 on 16-3-14.
 * 添加好友diaog
 */
public class AddFriendDialog extends DialogFragment {
    private EditText content;
    private String userName = "";
    private String userImage = "";
    private AddFriendListener dialogListener;


    public static AddFriendDialog newInstance(AddFriendListener var, String name, String imhurl) {
        AddFriendDialog frag = new AddFriendDialog();
        frag.setUserName(name);
        frag.setUserImage(imhurl);
        frag.setListner(var);
        return frag;
    }


    private void setListner(AddFriendListener listener) {
        this.dialogListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_friend, null);
        builder.setView(view);

        builder.setTitle("添加好友");
        content = view.findViewById(R.id.message);
        TextView textView = view.findViewById(R.id.user_name);
        textView.setText(userName);
        ImageView imageView = view.findViewById(R.id.logo);
        Picasso.with(getActivity()).load(userImage).placeholder(R.drawable.image_placeholder).into(imageView);

        view.findViewById(R.id.btn_send).setOnClickListener(view1 -> {
            if (checkInput()) {
                dialogListener.OnAddFriendOkClick(
                        content.getText().toString(),
                        GetId.getId("uid=", userImage));
                AddFriendDialog.this.getDialog().cancel();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(view12 -> dismiss());

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dialogListener = (AddFriendListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private boolean checkInput() {
        String str = content.getText().toString();
        if (str.length() > 10) {
            content.setError("字数太多了,最多10个");
            return false;
        }
        return true;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public interface AddFriendListener {
        void OnAddFriendOkClick(String mes, String uid);
    }
}
