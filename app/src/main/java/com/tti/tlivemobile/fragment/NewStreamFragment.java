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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.CreatePlatformRequest;
import com.tti.tlivelibrary.tliveservice.request.EditPlatformByPhotoRequeset;
import com.tti.tlivelibrary.tliveservice.request.QueryCategoryRequest;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformByAccountRequest;
import com.tti.tlivelibrary.tliveservice.response.CreatePlatformResponse;
import com.tti.tlivelibrary.tliveservice.response.EditPlatformResponse;
import com.tti.tlivelibrary.tliveservice.response.QueryCategoryResponse;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.activity.PublishActivity;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Category;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.mime.TypedFile;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class NewStreamFragment extends Fragment implements BackFragment {
    private static final String TAG = "NewStreamFragment";

    private View view;
    private LinearLayout mainLayout;
    private FrameLayout progressLayout;
    private ProgressBar progressBar;
    private ImageView streamImageView;
    private TextView titleText, imageHint;
    private EditText titleEdit;
    private Spinner categorySpinner;
    private ImageButton backButton;
    private Button startButton;

    private SpiceManager spiceManager;
    private QueryCategoryRequest queryCategoryRequest;
    private QueryPlatformByAccountRequest queryPlatformByAccountRequest;
    private EditPlatformByPhotoRequeset editPlatformByPhotoRequeset;
    private CreatePlatformRequest createPlatformRequest;

    private List<Category> categoryList;
    private List<String> categoryNameList;

    private Platform platform;
    private String platformTitle;
    private int categoryId;
    private File photoFile;
    private Uri photoUri;

    private boolean isEditPhoto;
    private boolean isBack;
    private boolean isNewAccount;

    public static Fragment newInstance() {
        return new NewStreamFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_stream, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isEditPhoto = false;
        isBack = false;
        isNewAccount = false;

        initView();
        queryCategory();
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(
                queryCategoryRequest,
                queryPlatformByAccountRequest,
                editPlatformByPhotoRequeset,
                createPlatformRequest
        );
    }

    private void initView() {
        mainLayout = (LinearLayout) view.findViewById(R.id.main_layout);

        progressLayout = (FrameLayout) view.findViewById(R.id.progress_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        titleText = (TextView) view.findViewById(R.id.title_text);
        if (!Utils.isPad(getActivity())) {
            KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
                @Override
                public void onVisibilityChanged(boolean isOpen) {
                    if (!isOpen)
                        titleText.setVisibility(View.VISIBLE);
                    else
                        titleText.setVisibility(View.GONE);
                }
            });
        }
        
        imageHint = (TextView) view.findViewById(R.id.image_hint);
        streamImageView = (ImageView) view.findViewById(R.id.stream_image);
        streamImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhotoDialog();
            }
        });

        titleEdit = (EditText) view.findViewById(R.id.title_edit);
        categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);

        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        startButton = (Button) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlatform();
                Utils.hideKeyView(getActivity(), view);
                mainLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setPhotoDialog() {
        AlertDialog.Builder chooseDialog = new AlertDialog.Builder(getActivity());
        chooseDialog.setTitle(getResources().getString(R.string.change_platoform_image));
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
            if (isNewAccount)
                imageHint.setVisibility(View.GONE);
            startButton.setEnabled(false);
            streamImageView.setEnabled(false);
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
            editPlatformByPhoto();
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

    private void setCategory() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, categoryNameList);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                categoryId = categoryList.get(i).getCategoryId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setPlatformInfo(QueryPlatformResponse.Platform platformInfo) {
        platform = new Platform();
        platform.setAccountId(platformInfo.accountId);
        platform.setPlatformState(platformInfo.platformState);
        platform.setUserName(platformInfo.userName);
        platform.setPlatformTitle(platformInfo.platformTitle);
        platform.setCategoryId(platformInfo.categoryId);
        platform.setCategoryName(platformInfo.categoryName);
        platform.setStreamUrl(platformInfo.streamUrl);
        platform.setPlatformImagePath(platformInfo.platformImagePath);

        setUserInfo();
    }

    private void setUserInfo() {
        if (!isEditPhoto) {
            titleEdit.setText(platform.getPlatformTitle());
            categoryId = platform.getCategoryId();
            categorySpinner.setSelection(categoryId - 1);
        }

        String platformImagePath = platform.getPlatformImagePath();
        if (streamImageView.isAttachedToWindow() && platformImagePath != null) {
            Glide.with(getActivity())
                    .load(platformImagePath)
                    .asBitmap()
                    .error(R.drawable.stream_l)
                    .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            streamImageView.setEnabled(true);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            streamImageView.setEnabled(true);
                            return false;
                        }
                    })
                    .into(streamImageView);
        }
        else {
            streamImageView.setImageResource(R.drawable.stream_l);
            progressBar.setVisibility(View.GONE);
            streamImageView.setEnabled(true);
        }
    }

    private void setNewImage() {
        Glide.with(getActivity())
                .load(photoFile.getPath())
                .asBitmap()
                .error(R.drawable.stream_l)
                .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        streamImageView.setEnabled(true);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        streamImageView.setEnabled(true);
                        return false;
                    }
                })
                .into(streamImageView);
    }

    private void finishEditPlatform() {
        progressBar.setVisibility(View.GONE);
        if (isNewAccount)
            imageHint.setVisibility(View.VISIBLE);
        startButton.setEnabled(true);
        streamImageView.setEnabled(true);
    }

    private void finishProcess() {
        if (isEditPhoto) {
            startButton.setEnabled(true);
            streamImageView.setEnabled(true);
        }
        else {
            mainLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
        }
    }

    private void back() {
        if (!isBack) {
            isBack = true;
            AppConstants.USER_TYPE = AppConstants.USER_MAIN;
            if (getActivity() != null)
                ((MainActivity) getActivity()).updateFragment();
        }
    }

    private void queryCategory() {
        categoryList = new ArrayList<>();
        categoryNameList = new ArrayList<>();

        queryCategoryRequest = new QueryCategoryRequest();
        spiceManager.execute(queryCategoryRequest, QueryCategoryListener);
    }

    private RequestListener<QueryCategoryResponse> QueryCategoryListener = new RequestListener<QueryCategoryResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            back();
            Utils.requestFailure("QueryCategory");
        }

        @Override
        public void onRequestSuccess(QueryCategoryResponse response) {
            if (response == null) {
                back();
                Utils.requestFailure("QueryCategory");
                return;
            }

            if (response.Code == Constants.QUERY_SUCCESS) {
                for (int i = 0; i < response.categoryList.size(); i++) {
                    Category category = new Category();
                    category.setCategoryId(response.categoryList.get(i).categoryId);
                    category.setCategoryName(response.categoryList.get(i).categoryName);
                    categoryList.add(category);
                    categoryNameList.add(response.categoryList.get(i).categoryName);
                }
                if (getActivity() != null) {
                    setCategory();
                    queryPlatformByAccount();
                }
                else
                    back();
            }
            else
                back();
        }
    };

    private void queryPlatformByAccount() {
        queryPlatformByAccountRequest = new QueryPlatformByAccountRequest(
                (Utils.Preferences.getAccount().getAccountId()));
        spiceManager.execute(queryPlatformByAccountRequest, QueryPlatformListener);
    }

    private RequestListener<QueryPlatformResponse> QueryPlatformListener = new RequestListener<QueryPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishProcess();
            Utils.requestFailure("QueryPlatform");
        }

        @Override
        public void onRequestSuccess(QueryPlatformResponse response) {
            if (response == null) {
                finishProcess();
                Utils.requestFailure("QueryPlatform");
                return;
            }

            finishProcess();
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0)
                    setPlatformInfo(response.platformList.get(0));
                else {
                    imageHint.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    isNewAccount = true;
                }
            }
        }
    };

    private void editPlatformByPhoto() {
        TypedFile imageFile = null;
        if (photoFile != null)
            imageFile = new TypedFile(getResources().getString(R.string.type_file_jpg), photoFile);

        editPlatformByPhotoRequeset = new EditPlatformByPhotoRequeset(Utils.Preferences.getSessionToken(), imageFile);
        spiceManager.execute(editPlatformByPhotoRequeset , EditPlatformListener);
    }

    RequestListener<EditPlatformResponse> EditPlatformListener = new RequestListener<EditPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishEditPlatform();
            Utils.requestFailure("EditPlatform");
        }

        @Override
        public void onRequestSuccess(EditPlatformResponse response) {
            if (response == null) {
                finishEditPlatform();
                Utils.requestFailure("EditPlatform");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS) {
                if (isNewAccount) {
                    setNewImage();
                    startButton.setEnabled(true);
                }
                else
                    queryPlatformByAccount();
                isEditPhoto = true;
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response))
                    finishEditPlatform();
            }
            else {
                finishEditPlatform();
            }
        }
    };

    private void createPlatform() {
        platformTitle = titleEdit.getText().toString();

        createPlatformRequest = new CreatePlatformRequest(
                Utils.Preferences.getSessionToken(), platformTitle, categoryId);
        spiceManager.execute(createPlatformRequest, CreatePlatformListener);
    }

    private RequestListener<CreatePlatformResponse> CreatePlatformListener = new RequestListener<CreatePlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            mainLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            Utils.requestFailure("CreatePlatform");
        }

        @Override
        public void onRequestSuccess(CreatePlatformResponse response) {
            if (response == null) {
                mainLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                Utils.requestFailure("CreatePlatform");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS) {
                Intent intent = new Intent(getActivity(), PublishActivity.class);
                intent.putExtra(AppConstants.STRING_ITEM, platformTitle);
                startActivity(intent);
                getActivity().finish();
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    mainLayout.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.GONE);
                }
            }
            else {
                mainLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
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
