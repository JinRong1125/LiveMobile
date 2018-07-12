package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.LoginRequest;
import com.tti.tlivelibrary.tliveservice.request.NewAccountRequest;
import com.tti.tlivelibrary.tliveservice.response.LoginResponse;
import com.tti.tlivelibrary.tliveservice.response.NewAccountResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.LoginInformation;
import com.tti.tlivemobile.utils.Utils;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private View view, loginIndicator, signupIndicator;
    private ProgressBar progressBar;
    private LinearLayout loginLayout, signupLayout;
    private TextView loginText, signupText;
    private EditText loginAccount, loginPassword, signupAccount, signupPassword, signupUserName, signupEmail;
    private Button submitButton;

    private SpiceManager spiceManager;
    private LoginRequest loginRequest;
    private NewAccountRequest newAccountRequest;

    private String userId;
    private String password;
    private String userName;
    private String email;

    private int layoutType;

    private static final int LOGIN_TYPE = 0;
    private static final int SIGN_UP_TYPE = 1;

    public static Fragment newInstance() {
        return new LoginFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        initView();

        layoutType = LOGIN_TYPE;
        switchLayout();
    }

    private void initView() {
        loginLayout = (LinearLayout) getActivity().findViewById(R.id.login_layout);
        signupLayout = (LinearLayout) getActivity().findViewById(R.id.sign_up_Layout);
        loginIndicator = view.findViewById(R.id.login_indicator);
        signupIndicator = view.findViewById(R.id.sign_up_indicator);
        loginAccount = (EditText) view.findViewById(R.id.login_account);
        loginPassword = (EditText) view.findViewById(R.id.login_password);
        signupAccount = (EditText) view.findViewById(R.id.sign_up_account);
        signupPassword = (EditText) view.findViewById(R.id.sign_up_password);
        signupUserName = (EditText) view.findViewById(R.id.sign_up_user_name);
        signupEmail = (EditText) view.findViewById(R.id.sign_up_email);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        loginText = (TextView) view.findViewById(R.id.login_text);
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutType = LOGIN_TYPE;
                switchLayout();
            }
        });

        signupText = (TextView) view.findViewById(R.id.sign_up_text);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutType = SIGN_UP_TYPE;
                switchLayout();
            }
        });

        submitButton = (Button) view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (layoutType) {
                    case LOGIN_TYPE:
                        startLogin();
                        break;
                    case SIGN_UP_TYPE:
                        startSignUp();
                        break;
                }

                Utils.hideKeyView(getActivity(), view);
                Utils.enableDisableView(view, false);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();

        LoginInformation loginInformation = Utils.Preferences.getLoginInfo();
        if (loginInformation != null) {
            loginAccount.setText(loginInformation.getUserId());
            loginPassword.setText(loginInformation.getPassword());
            startLogin();

            Utils.hideKeyView(getActivity(), view);
            Utils.enableDisableView(view, false);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void switchLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        switch (layoutType) {
            case LOGIN_TYPE:
                loginLayout.setVisibility(View.VISIBLE);
                signupLayout.setVisibility(View.GONE);
                loginText.setTextColor(ContextCompat.getColor(getContext(), R.color.white_second));
                signupText.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_forth));
                loginIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white_second));
                signupIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_forth));

                params.addRule(RelativeLayout.BELOW, R.id.login_layout);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                submitButton.setLayoutParams(params);
                break;
            case SIGN_UP_TYPE:
                loginLayout.setVisibility(View.GONE);
                signupLayout.setVisibility(View.VISIBLE);
                loginText.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_forth));
                signupText.setTextColor(ContextCompat.getColor(getContext(), R.color.white_second));
                loginIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_forth));
                signupIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white_second));

                params.addRule(RelativeLayout.BELOW, R.id.sign_up_Layout);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                submitButton.setLayoutParams(params);
                break;
        }
    }

    private void enableButton() {
        Utils.enableDisableView(view, true);
        progressBar.setVisibility(View.GONE);
    }

    private void startLogin() {
        userId = loginAccount.getText().toString();
        password = loginPassword.getText().toString();

        loginRequest = new LoginRequest(userId, password, Constants.PLATFORM_ID);
        spiceManager.execute(loginRequest, LoginListener);
    }

    private RequestListener<LoginResponse> LoginListener = new RequestListener<LoginResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            enableButton();
            Utils.requestFailure("Login");
        }

        @Override
        public void onRequestSuccess(LoginResponse response) {
            if (response == null) {
                enableButton();
                Utils.requestFailure("Login");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS) {
                LoginInformation loginInformation = new LoginInformation();
                loginInformation.setUserId(userId);
                loginInformation.setPassword(password);
                Utils.Preferences.setLoginInfo(loginInformation);

                Utils.Preferences.setUUID(response.uuId);
                Utils.Preferences.setSessionToken(response.sessionToken);

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            else
                enableButton();
        }
    };

    private void startSignUp() {
        userId = signupAccount.getText().toString();
        password = signupPassword.getText().toString();
        userName = signupUserName.getText().toString();
        email = signupEmail.getText().toString();

        newAccountRequest = new NewAccountRequest(userId, email, password, userName);
        spiceManager.execute(newAccountRequest, NewAccountListener);
    }

    private RequestListener<NewAccountResponse> NewAccountListener = new RequestListener<NewAccountResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            enableButton();
            Utils.requestFailure("NewAccount");
        }

        @Override
        public void onRequestSuccess(NewAccountResponse response) {
            if (response == null) {
                enableButton();
                Utils.requestFailure("NewAccount");
                return;
            }
            Utils.responseMessage(response.Message);

            enableButton();
            if (response.Code == Constants.QUERY_SUCCESS) {
                layoutType = LOGIN_TYPE;
                switchLayout();
                signupAccount.setText("");
                signupPassword.setText("");
                signupUserName.setText("");
                signupEmail.setText("");
            }
        }
    };
}
