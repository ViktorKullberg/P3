package se.mau.ah0987.redditp3.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.R;

public class RedditLoginDialog extends DialogFragment {
    private Controller controller;
    private TextView tvHeader;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnLogin;
    private String header;

    public RedditLoginDialog() {
        // Empty constructor required for DialogFragment
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login, container);
        //setCancelable(false);
        if (savedInstanceState != null) {
            header = savedInstanceState.getString("header");
        }
        InitializeComponents(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (header != null)
            tvHeader.setText(header);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("header", header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    private void InitializeComponents(View view) {
        tvHeader = view.findViewById(R.id.tvHeader);
        etUserName = view.findViewById(R.id.etUserName);
        etPassWord = view.findViewById(R.id.etPassWord);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new ButtonListener());
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.btnLogin:
                    if (true) {
                        //controller.redditLoginDialog(etUserName.getText().toString(), etPassWord.getText().toString());
                        controller.loginReddit();
                        clearDialog();
                        dismiss();
                    }
                    break;
            }
        }
    }

    private boolean inputCheck() {
        etUserName.clearFocus();
        etPassWord.clearFocus();
        if (TextUtils.isEmpty(etUserName.getText().toString())) {
            etUserName.requestFocus();
            etUserName.requestFocusFromTouch();
            etUserName.setError(getString(R.string.enter_user));
            return false;
        } else if (TextUtils.isEmpty(etPassWord.getText().toString())) {
            etPassWord.requestFocus();
            etPassWord.requestFocusFromTouch();
            etPassWord.setError(getString(R.string.enter_pass));
            return false;
        }
        return true;
    }

    private void clearDialog() {
        etUserName.setText("");
        etPassWord.setText("");
    }
}
