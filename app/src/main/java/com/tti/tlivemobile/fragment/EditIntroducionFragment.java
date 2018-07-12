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
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.EditChannelRequest;
import com.tti.tlivelibrary.tliveservice.response.EditChannelResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Channel;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

import java.io.File;

import retrofit.mime.TypedFile;

/**
 * Created by dylan_liang on 2017/6/27.
 */

public class EditIntroducionFragment extends Fragment implements BackFragment {
    private static final String TAG = "EditIntroduceFragment";

    private View view;
    private ProgressBar imageProgress, submitProgress;
    private ImageView contentImage;
    private EditText contentText;
    private ImageButton backButton;
    private Button submitButton;

    private SpiceManager spiceManager;
    private EditChannelRequest editChannelRequest;

    private Channel channel;
    private File photoFile;
    private Uri photoUri;

    private boolean isBack;

    public static Fragment newInstance() {
        return new EditIntroducionFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_introduction, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isBack = false;

        initView();
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(editChannelRequest);
    }

    private void initView() {
        channel = getActivity().getIntent().getParcelableExtra(AppConstants.CHANNEL_ITEM);

        imageProgress = (ProgressBar) view.findViewById(R.id.image_progress);
        submitProgress = (ProgressBar) view.findViewById(R.id.submit_progress);

        contentImage = (ImageView) view.findViewById(R.id.content_image);
        contentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentImageDialog();
            }
        });
        setContentImage(channel.getContentImagePath());

        contentText = (EditText) view.findViewById(R.id.content_text);
        contentText.setText(channel.getContentText());

        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        submitButton = (Button) view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editChannel();
                Utils.hideKeyView(getActivity(), view);
                Utils.enableDisableView(view, false);
                backButton.setEnabled(true);
                submitProgress.setVisibility(View.VISIBLE);
            }
        });
        submitButton.setEnabled(false);

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setContentImageDialog() {
        AlertDialog.Builder chooseDialog = new AlertDialog.Builder(getActivity());
        chooseDialog.setTitle(getResources().getString(R.string.add_content_image));
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
            imageProgress.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
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
            setContentImage(photoFile.getPath());
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

    private void setContentImage(String contentImagePath) {
        Glide.with(getActivity())
                .load(contentImagePath)
                .asBitmap()
                .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        imageProgress.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageProgress.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        return false;
                    }
                })
                .into(contentImage);
    }

    private void back() {
        if (!isBack) {
            isBack = true;
            AppConstants.USER_TYPE = AppConstants.USER_INTRODUCTION;
            if (getActivity() != null)
                ((MainActivity) getActivity()).updateFragment();
        }
    }

    private void editChannel() {
        TypedFile imageFile = null;
        if (photoFile != null)
            imageFile = new TypedFile(getResources().getString(R.string.type_file_jpg), photoFile);

        editChannelRequest = new EditChannelRequest(
                Utils.Preferences.getSessionToken(),
                channel.getContentId(),
                contentText.getText().toString(),
                imageFile);
        spiceManager.execute(editChannelRequest, EditChannelListener);
    }

    RequestListener<EditChannelResponse> EditChannelListener = new RequestListener<EditChannelResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.enableDisableView(view, true);
            submitProgress.setVisibility(View.GONE);
            Utils.requestFailure("EditChannel");
        }

        @Override
        public void onRequestSuccess(EditChannelResponse response) {
            if (response == null) {
                Utils.enableDisableView(view, true);
                submitProgress.setVisibility(View.GONE);
                Utils.requestFailure("EditChannel");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS) {
                back();
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    Utils.enableDisableView(view, true);
                    submitProgress.setVisibility(View.GONE);
                }
            }
            else {
                Utils.enableDisableView(view, true);
                submitProgress.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public boolean onBackPressed() {
        back();
        return true;
    }

    @Override
    public int getBackPriority() {
        return 0;
    }
}
