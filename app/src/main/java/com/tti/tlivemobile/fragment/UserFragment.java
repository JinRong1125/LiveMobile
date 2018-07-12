package com.tti.tlivemobile.fragment;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.EditAccountRequest;
import com.tti.tlivelibrary.tliveservice.request.LogoutRequest;
import com.tti.tlivelibrary.tliveservice.request.PhotoUploadRequest;
import com.tti.tlivelibrary.tliveservice.request.QueryAccountRequest;
import com.tti.tlivelibrary.tliveservice.response.EditAccountResponse;
import com.tti.tlivelibrary.tliveservice.response.LogoutResponse;
import com.tti.tlivelibrary.tliveservice.response.PhotoUploadResponse;
import com.tti.tlivelibrary.tliveservice.response.QueryAccountResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Account;
import com.tti.tlivemobile.utils.Utils;

import java.io.File;

import retrofit.mime.TypedFile;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class UserFragment extends Fragment {
    private static final String TAG = "UserFragment";

    private View view;
    private LinearLayout mainLayout;
    private FrameLayout progressLayout;
    private ProgressBar progressBar;
    private ImageView avatarView;
    private TextView userNameView, subscribingView, subscribersView, historyView,
            subscribingValue, subscribersValue, historyValue;
    private ImageButton editUserButton, streamButton, settingButton, introButton;

    private ViewPager viewPager;

    private SpiceManager spiceManager;
    private QueryAccountRequest queryAccountRequest;
    private PhotoUploadRequest photoUploadRequest;
    private EditAccountRequest editAccountRequest;
    private LogoutRequest logoutRequest;

    private Account account;
    private File photoFile;
    private Uri photoUri;

    public static Fragment newInstance() {
        return new UserFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        initView();
        queryAccount();
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(
                queryAccountRequest,
                photoUploadRequest,
                editAccountRequest,
                logoutRequest
        );
    }

    private void initView() {
        viewPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
        mainLayout = (LinearLayout) view.findViewById(R.id.main_layout);
        mainLayout.setVisibility(View.GONE);

        progressLayout = (FrameLayout) view.findViewById(R.id.progress_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        avatarView = (ImageView) view.findViewById(R.id.avatar_image);
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvatarDialog();
            }
        });
        userNameView = (TextView) view.findViewById(R.id.user_name);
        userNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        subscribingValue = (TextView) view.findViewById(R.id.subscribing_value);
        subscribersValue = (TextView) view.findViewById(R.id.subscribers_value);
        historyValue = (TextView) view.findViewById(R.id.history_value);

        subscribingView = (TextView) view.findViewById(R.id.subscribing);
        subscribingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.USER_TYPE = AppConstants.USER_SUBSCRIBING;
                if (getActivity() != null)
                    ((MainActivity) getActivity()).updateFragment();
            }
        });

        subscribersView = (TextView) view.findViewById(R.id.subscribers);
        subscribersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.USER_TYPE = AppConstants.USER_SUBSCRIBERS;
                if (getActivity() != null)
                    ((MainActivity) getActivity()).updateFragment();
            }
        });

        historyView = (TextView) view.findViewById(R.id.history);
        historyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingButton = (ImageButton) view.findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), settingButton);
                popupMenu.getMenuInflater()
                        .inflate(R.menu.menu_setting, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String itemString = item.getTitle().toString();
                        if (itemString.equals(getResources().getString(R.string.logout))) {
                            logout();
                            progressLayout.setVisibility(View.VISIBLE);
                            settingButton.setEnabled(false);
                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        editUserButton = (ImageButton) view.findViewById(R.id.edit_user);
        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserNameDialog();
            }
        });

        introButton = (ImageButton) view.findViewById(R.id.intro_button);
        introButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.USER_TYPE = AppConstants.USER_INTRODUCTION;
                if (getActivity() != null)
                    ((MainActivity) getActivity()).updateFragment();
            }
        });

        streamButton = (ImageButton) view.findViewById(R.id.stream_button);
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.USER_TYPE = AppConstants.USER_STREAM;
                if (getActivity() != null)
                    ((MainActivity) getActivity()).updateFragment();
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setAccountInfo(QueryAccountResponse.AccountInfo accountInfo) {
        account = new Account();
        account.setAccountId(accountInfo.accountId);
        account.setUserId(accountInfo.userId);
        account.setEmail(accountInfo.email);
        account.setUserName(accountInfo.userName);
        account.setAvatarPath(accountInfo.avatarPath);
        account.setSubscribingValue(accountInfo.subscribingValue);
        account.setSubscribersValue(accountInfo.subscribersValue);
        account.setChannnelsValue(accountInfo.channnelsValue);
        Utils.Preferences.setAccount(account);

        setUserInfo();
    }

    private void setUserInfo() {
        userNameView.setText(account.getUserName());
        subscribingValue.setText(String.valueOf(account.getSubscribingValue()));
        subscribersValue.setText(String.valueOf(account.getSubscribersValue()));

        if (avatarView.isAttachedToWindow()) {
            Glide.with(getActivity())
                    .load(account.getAvatarPath())
                    .asBitmap()
                    .error(R.drawable.user_l)
                    .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(avatarView);
        }
    }

    private void setAvatarDialog() {
        AlertDialog.Builder chooseDialog = new AlertDialog.Builder(getActivity());
        chooseDialog.setTitle(getResources().getString(R.string.change_avatar));
        chooseDialog.setMessage(getResources().getString(R.string.photo_message));
        chooseDialog.setNeutralButton(getResources().getString(R.string.cancel), null);

        chooseDialog.setPositiveButton(getResources().getString(R.string.camera), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, getResources().getString(R.string.type_file_jpg));
                photoUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePhoto, AppConstants.CAMERA);
            }
        });

        chooseDialog.setNegativeButton(getResources().getString(R.string.gallery), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent takePicture = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(takePicture, AppConstants.GALLERY);
            }
        });

        final AlertDialog alertDialog = chooseDialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imagePathIntent) {
        super.onActivityResult(requestCode, resultCode, imagePathIntent);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.GALLERY)
                photoUri = imagePathIntent.getData();
            covertPhotoPath();
            progressBar.setVisibility(View.VISIBLE);
            avatarView.setEnabled(false);
        }
    }

    private void covertPhotoPath() {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getActivity().getContentResolver().query(photoUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            photoFile = new File(cursor.getString(column_index));
            photoUpload();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setUserNameDialog() {
        AlertDialog.Builder editDialog = new AlertDialog.Builder(getActivity());
        editDialog.setTitle(getResources().getString(R.string.change_name));
        editDialog.setMessage(getResources().getString(R.string.name_message));
        editDialog.setNegativeButton(getResources().getString(R.string.cancel), null);

        final EditText editText = new EditText(getActivity());
        editText.setSingleLine();
        editText.setMaxLines(1);
        editDialog.setView(editText);
        editDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editAccount(editText.getText().toString());
                progressBar.setVisibility(View.VISIBLE);
                editUserButton.setEnabled(false);
            }
        });

        final AlertDialog alertDialog = editDialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void queryAccount() {
        queryAccountRequest = new QueryAccountRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(queryAccountRequest, QueryAccountListener);
    }

    private RequestListener<QueryAccountResponse> QueryAccountListener = new RequestListener<QueryAccountResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            if (viewPager.getCurrentItem() == AppConstants.USER_PAGE)
                Utils.backLogin(getActivity());
            Utils.requestFailure("QueryAccount");
        }

        @Override
        public void onRequestSuccess(QueryAccountResponse response) {
            if (response == null) {
                if (viewPager.getCurrentItem() == AppConstants.USER_PAGE)
                    Utils.backLogin(getActivity());
                Utils.requestFailure("QueryAccount");
                return;
            }

            if (response.Code == Constants.QUERY_SUCCESS) {
                mainLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                Utils.enableDisableView(view, true);
                setAccountInfo(response.AccountInfo);
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (viewPager.getCurrentItem() == AppConstants.USER_PAGE)
                    Utils.isTokenMessage(getActivity(), response);
                Utils.responseMessage(response.Message);
            }
        }
    };

    private void photoUpload() {
        TypedFile imageFile = null;
        if (photoFile != null)
            imageFile = new TypedFile(getResources().getString(R.string.type_file_jpg), photoFile);

        photoUploadRequest = new PhotoUploadRequest(Utils.Preferences.getSessionToken(), imageFile);
        spiceManager.execute(photoUploadRequest, PhotoUploadListener);
    }

    RequestListener<PhotoUploadResponse> PhotoUploadListener = new RequestListener<PhotoUploadResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressBar.setVisibility(View.GONE);
            avatarView.setEnabled(true);
            Utils.requestFailure("PhotoUpload");
        }

        @Override
        public void onRequestSuccess(PhotoUploadResponse response) {
            if (response == null) {
                progressBar.setVisibility(View.GONE);
                avatarView.setEnabled(true);
                Utils.requestFailure("PhotoUpload");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS)
                queryAccount();
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    progressBar.setVisibility(View.GONE);
                    avatarView.setEnabled(true);
                }
            }
            else {
                progressBar.setVisibility(View.GONE);
                avatarView.setEnabled(true);
            }
        }
    };

    private void editAccount(String userName) {
        editAccountRequest = new EditAccountRequest(Utils.Preferences.getSessionToken(), userName);
        spiceManager.execute(editAccountRequest, EditAccountListener);
    }

    RequestListener<EditAccountResponse> EditAccountListener = new RequestListener<EditAccountResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressBar.setVisibility(View.GONE);
            editUserButton.setEnabled(true);
            Utils.requestFailure("EditAccount");
        }

        @Override
        public void onRequestSuccess(EditAccountResponse response) {
            if (response == null) {
                progressBar.setVisibility(View.GONE);
                editUserButton.setEnabled(true);
                Utils.requestFailure("EditAccount");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS)
                queryAccount();
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    progressBar.setVisibility(View.GONE);
                    editUserButton.setEnabled(true);
                }
            }
            else {
                progressBar.setVisibility(View.GONE);
                editUserButton.setEnabled(true);
            }
        }
    };

    private void logout() {
        logoutRequest = new LogoutRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(logoutRequest, LogoutListener);
    }

    private RequestListener<LogoutResponse> LogoutListener = new RequestListener<LogoutResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressLayout.setVisibility(View.GONE);
            settingButton.setEnabled(true);
            Utils.requestFailure("Logout");
        }

        @Override
        public void onRequestSuccess(LogoutResponse response) {
            if (response == null) {
                progressLayout.setVisibility(View.GONE);
                settingButton.setEnabled(true);
                Utils.requestFailure("Logout");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS)
                Utils.backLogin(getActivity());
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    progressBar.setVisibility(View.GONE);
                    editUserButton.setEnabled(true);
                }
            }
            else {
                progressLayout.setVisibility(View.GONE);
                settingButton.setEnabled(true);
            }
        }
    };
}
