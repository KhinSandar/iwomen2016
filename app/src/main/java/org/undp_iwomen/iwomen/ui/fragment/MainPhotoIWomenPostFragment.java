package org.undp_iwomen.iwomen.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.widget.LoginButton;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONObject;
import org.undp_iwomen.iwomen.CommonConfig;
import org.undp_iwomen.iwomen.R;
import org.undp_iwomen.iwomen.model.MyTypeFace;
import org.undp_iwomen.iwomen.model.parse.IwomenPost;
import org.undp_iwomen.iwomen.ui.activity.DrawerMainActivity;
import org.undp_iwomen.iwomen.ui.widget.ResizableImageView;
import org.undp_iwomen.iwomen.utils.Connection;
import org.undp_iwomen.iwomen.utils.ShowKeyboardListener;
import org.undp_iwomen.iwomen.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;


/**
 * Created by khinsandar on 4/12/15.
 */
public class MainPhotoIWomenPostFragment extends Fragment implements ImageChooserListener {


    /*LostReport lostReport;
    FoundReport foundReport;*/

    IwomenPost postParse;
    View progressbackground;

    ProgressWheel progress_wheel;
    private EditText postEditText;


    TextView txt_rule_E, txt_rule_U, txt_rule_2_U;

    public LoginButton loginButton;

    String name;


    private JSONObject user;
    String crop_absolute_path;
    String str_report_type;

    private int maxCharacterCount = 1000;
    private TextView characterCountTextView;
    private Button postButton;


    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";

    private static final String REQUEST_FIELDS =
            TextUtils.join(",", new String[]{ID, NAME, PICTURE});


    CallbackManager callbackManager;
    private PendingAction pendingAction = PendingAction.NONE;


    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private final String PENDING_ACTION_BUNDLE_KEY =
            "com.facebook.samples.hellofacebook:PendingAction";


    //private final Context Dcontext;
    private SharedPreferences mSharedPreferences;
    private String user_name, user_obj_id, user_nrc;
    private String userprofile_Image_path;

    //For Image Chooser
    private ImageChooserManager imageChooserManager;
    String crop_file_name, crop_file_path;
    private int chooserType;
    private String filePath;
    private String capture_filePath;
    File croppedImageFile = null;
    private ChosenImage chosenImage;

    private static int REQUEST_PICTURE = 1;
    private static int REQUEST_CROP_PICTURE = 2;

    ResizableImageView img_photo;
    TextView txt_camera;

    TextView txt_camera_text;

    TextView txt_img_upload_icon;
    TextView txt_img_upload;

    TextView txt_audio_upload;
    TextView txt_video_upload;

    SharedPreferences sharePrefLanguageUtil;
    String mstr_lang;
    private Context mContext;
    private EditText new_post_et_title;
    private EditText new_post_et_story;

    private LinearLayout ly_title;
    private LinearLayout ly_body;

    public MainPhotoIWomenPostFragment() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Bundle bundleArgs = getArguments();
        if (bundleArgs != null) {


            str_report_type = bundleArgs.getString("ReportType");
        }*/
        View rootView = inflater.inflate(R.layout.new_post_fragment_new_ui, container, false);

        callbackManager = CallbackManager.Factory.create();
        //loginButton = (LoginButton) rootView.findViewById(R.id.login_button_1);

        //commfig
        mSharedPreferences = getActivity().getSharedPreferences(CommonConfig.SHARE_PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        if (!mSharedPreferences.getBoolean(CommonConfig.IS_LOGIN, false)) {

        } else {
            user_name = mSharedPreferences.getString(CommonConfig.USER_NAME, null);
            user_obj_id = mSharedPreferences.getString(CommonConfig.USER_OBJ_ID, null);

        }
        userprofile_Image_path = mSharedPreferences.getString(CommonConfig.USER_IMAGE_PATH, null);

        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        mContext = getActivity().getApplicationContext();
        sharePrefLanguageUtil = getActivity().getSharedPreferences(Utils.PREF_SETTING, Context.MODE_PRIVATE);
        mstr_lang = sharePrefLanguageUtil.getString(com.parse.utils.Utils.PREF_SETTING_LANG, com.parse.utils.Utils.ENG_LANG);

        img_photo = (ResizableImageView) rootView.findViewById(R.id.new_post_img);
        new_post_et_title = (EditText) rootView.findViewById(R.id.new_post_et_title);


        //img_camera = (ImageView)rootView.findViewById(R.id.img_camera);
        postEditText = (EditText) rootView.findViewById(R.id.new_post_et_story);
        postEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });


        characterCountTextView = (TextView) rootView.findViewById(R.id.character_count_textview);

        postButton = (Button) rootView.findViewById(R.id.new_post_btn);

        txt_img_upload_icon = (TextView) rootView.findViewById(R.id.new_post_photo_img_upload);
        txt_img_upload = (TextView) rootView.findViewById(R.id.new_post_txt_img_upload);


        txt_camera = (TextView) rootView.findViewById(R.id.new_post_camera_img_camera);
        txt_camera_text = (TextView) rootView.findViewById(R.id.new_post_txt_camera_upload);

        txt_audio_upload = (TextView) rootView.findViewById(R.id.new_post_txt_audio_upload);
        txt_video_upload = (TextView) rootView.findViewById(R.id.new_post_txt_video_upload);


        txt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        txt_camera_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });


        txt_img_upload_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        txt_img_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        //Set TypeFace
        /*postEditText.setTypeface(MyTypeFace.get(getActivity().getApplicationContext(), MyTypeFace.NORMAL));
        postButton.setTypeface(MyTypeFace.get(getActivity().getApplicationContext(), MyTypeFace.NORMAL));
        characterCountTextView.setTypeface(MyTypeFace.get(getActivity().getApplicationContext(), MyTypeFace.NORMAL));*/

        progress_wheel = (ProgressWheel) rootView.findViewById(R.id.new_post_photo_progress_wheel);
        progressbackground = rootView.findViewById(R.id.new_post_progresswheel_background);
        ly_title = (LinearLayout) rootView.findViewById(R.id.new_post_ly_title);
        ly_body = (LinearLayout) rootView.findViewById(R.id.new_post_ly_body);


        ly_title.setOnClickListener(new ShowKeyboardListener(getActivity()));
        ly_body.setOnClickListener(new ShowKeyboardListener(getActivity()));

        new_post_et_title.setVisibility(View.GONE);


        progressbackground.bringToFront();
        progress_wheel.bringToFront();
        progress_wheel.spin();
        //progress_wheel.setBarColor(Color.RED);
        progress_wheel.setRimColor(Color.LTGRAY);
        progress_wheel.setVisibility(View.GONE);

        //Set Type Face and Chnage text
        if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {

            //new_post_et_title.setHint(R.string.new_post_hint_title_eng);
            postEditText.setHint(R.string.new_iwomen_post_hint_title_eng);//new_post_hint_body_eng
            txt_img_upload.setText(R.string.new_post_upload_photo_eng);
            postButton.setText(R.string.new_post_eng);
            txt_camera_text.setText(R.string.new_post_take_photo_eng);
            txt_audio_upload.setText(R.string.new_post_audio_eng);
            txt_video_upload.setText(R.string.new_post_video_eng);

            new_post_et_title.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            postEditText.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            txt_img_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            txt_camera_text.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            postButton.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            txt_video_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));
            txt_audio_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.NORMAL));

        } else {
            //new_post_et_title.setHint(R.string.new_post_hint_title_mm);
            postEditText.setHint(R.string.new_iwomen_post_hint_title_mm);//new_post_hint_body_mm
            txt_img_upload.setText(R.string.new_post_upload_photo_mm);
            postButton.setText(R.string.new_post_mm);
            txt_camera_text.setText(R.string.new_post_take_photo_mm);

            txt_audio_upload.setText(R.string.new_post_audio_mm);
            txt_video_upload.setText(R.string.new_post_video_mm);


            new_post_et_title.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            postEditText.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            txt_img_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            txt_camera_text.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            postButton.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            txt_video_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));
            txt_audio_upload.setTypeface(MyTypeFace.get(mContext, MyTypeFace.ZAWGYI));

        }

        txt_audio_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mstr_lang.equals(Utils.ENG_LANG)) {
                    Utils.doToastEng(mContext, getResources().getString(R.string.resource_coming_soon_eng));
                } else {

                    Utils.doToastMM(mContext, getResources().getString(R.string.resource_coming_soon_mm));
                }
            }
        });

        txt_video_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mstr_lang.equals(Utils.ENG_LANG)) {
                    Utils.doToastEng(mContext, getResources().getString(R.string.resource_coming_soon_eng));
                } else {

                    Utils.doToastMM(mContext, getResources().getString(R.string.resource_coming_soon_mm));
                }
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Connection.isOnline(getActivity())) {

                    progress_wheel.setVisibility(View.VISIBLE);
                    progressbackground.setVisibility(View.VISIBLE);


                    //Session session = Session.getActiveSession();
                    if (user_obj_id != null) {
                        /*Toast.makeText(getActivity().getApplicationContext(),
                                "Already Log In stage !",
                                Toast.LENGTH_LONG).show();*/

                        if (new_post_et_title.length() != 0 || postEditText.length() != 0) {
                            progress_wheel.setVisibility(View.VISIBLE);
                            progressbackground.setVisibility(View.VISIBLE);

                            uploadReportToParse();

                        } else {

                            progress_wheel.setVisibility(View.GONE);

                            if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {
                                Utils.doToastEng(mContext, getResources().getString(R.string.upload_post_warning_eng));
                            } else {
                                Utils.doToastMM(mContext, getResources().getString(R.string.upload_post_warning_mm));

                            }


                        }


                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Please Log In !",
                                Toast.LENGTH_LONG).show();
                        /*Intent intent = new Intent(getActivity(), SplashActivity.class);
                        startActivity(intent);*/


                    }


                } else {
                    /*Toast.makeText(getActivity().getApplicationContext(),
                            "Please Open Internet Connection!",
                            Toast.LENGTH_LONG).show();*/
                    if (mstr_lang.equals(Utils.ENG_LANG)) {
                        Utils.doToastEng(mContext, getResources().getString(R.string.open_internet_warning_eng));
                    } else {

                        Utils.doToastMM(mContext, getActivity().getResources().getString(R.string.open_internet_warning_mm));
                    }
                }
            }
        });


    }


    private void uploadReportToParse() {


        //File photo = new File(chosenImage.getFilePathOriginal());
        if (crop_file_path != null) {

            File photo = new File(crop_file_path);

            FileInputStream fileInputStream = null;


            byte[] bFile = new byte[(int) photo.length()];

            try {
                //convert file into array of bytes
                fileInputStream = new FileInputStream(photo);
                fileInputStream.read(bFile);
                fileInputStream.close();

                for (int i = 0; i < bFile.length; i++) {
                    //System.out.print((char)bFile[i]);
                }

                //System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }


            //TypedFile typedFile = new TypedFile("multipart/form-data", photo);//croppedImageFile
            /*Log.e("///File//",""+chosenImage.getFilePathOriginal());
            byte[] data = crop_file_path.getBytes();*/


            ParseFile photoFile = new ParseFile("photo.jpg", bFile);


            postParse = new IwomenPost();


            //postParse.setContentTypes("Story");

            //TODO FROM SERVER SIDE FOR REVIEW 2
            postParse.setUserId(user_obj_id);
            if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {

                if (postEditText.length() != 0) {
                    postParse.setContent(postEditText.getText().toString());
                }
                if (new_post_et_title.length() != 0) {
                    postParse.setTitle(new_post_et_title.getText().toString());
                }


            } else {

                if (postEditText.length() != 0) {
                    postParse.setContentMm(postEditText.getText().toString());
                }
                if (new_post_et_title.length() != 0) {
                    postParse.setTitleMm(new_post_et_title.getText().toString());
                }

            }

            //TODO FROM SERVER SIDE FOR REVIEW 1
            postParse.setIsAllow(false);

            postParse.setLikes(0);
            postParse.setCommentCount(0);
            postParse.setShareCount(0);
            if(userprofile_Image_path != null) {
                postParse.setPostUploadUserImgPath(userprofile_Image_path);
            }
            postParse.setContentTypes("Story");
            postParse.setPostUploadedDate(new Date());

            postParse.setPostUploadAuthorName(user_name);
            //postParse.setPostUploadAuthorImgFile(ParseUser.getCurrentUser().getParseFile("profileimage"));

            //TODO images
            postParse.setImageFile(photoFile);
            //postParse.setPhotoFile(photoFile);


            /**Very Important */
            ParseACL groupACL = new ParseACL();
            // userList is an Iterable<ParseUser> with the users we are sending this message to.
                /*for (ParseUser user : userList) {
                    groupACL.setReadAccess(user, true);
                    //groupACL.setWriteAccess(user, true);
                }*/
            //groupACL.setRoleReadAccess("");

            //groupACL.setRoleWriteAccess("");


            groupACL.setPublicReadAccess(true);

            postParse.setACL(groupACL);
            postParse.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    /*if (getActivity().isFinishing()) {
                        return;
                    }*/
                    if (e == null) {
                        progress_wheel.setVisibility(View.INVISIBLE);
                        progressbackground.setVisibility(View.INVISIBLE);


                        if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {
                            Utils.doToastEng(mContext, getResources().getString(R.string.iwomen_postupload_success_toast_eng));
                        } else {
                            Utils.doToastMM(mContext, getResources().getString(R.string.iwomen_postupload_success_toast_mm));

                        }
                        Intent intent = new Intent(getActivity(), DrawerMainActivity.class);


                        startActivity(intent);

                    } else {
                        progress_wheel.setVisibility(View.INVISIBLE);
                        progressbackground.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Error saving: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                    }
                }
            });

        } else { //If didn't chose Photo


            postParse = new IwomenPost();
            //TODO FROM SERVER SIDE FOR REVIEW 1

            postParse.setUserId(user_obj_id);
            if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {

                if (postEditText.length() != 0) {
                    postParse.setContent(postEditText.getText().toString());
                }
                if (new_post_et_title.length() != 0) {
                    postParse.setTitle(new_post_et_title.getText().toString());
                }

            } else {

                if (postEditText.length() != 0) {
                    postParse.setContentMm(postEditText.getText().toString());
                }
                if (new_post_et_title.length() != 0) {
                    postParse.setTitleMm(new_post_et_title.getText().toString());
                }

            }


            //TODO FROM SERVER SIDE FOR REVIEW 2

            postParse.setIsAllow(false);

            postParse.setLikes(0);
            postParse.setCommentCount(0);
            postParse.setShareCount(0);
            postParse.setContentTypes("Story");

            if(userprofile_Image_path != null) {
                postParse.setPostUploadUserImgPath(userprofile_Image_path);
            }
            postParse.setPostUploadedDate(new Date());

            postParse.setPostUploadAuthorName(user_name);
            //postParse.setPostUploadAuthorImgFile(ParseUser.getCurrentUser().getParseFile("profileimage"));

            //TODO images null case allow post
            //postParse.setImageFile(photoFile);


            //postUploadImgPath
            /**Very Important */
            ParseACL groupACL = new ParseACL();
            // userList is an Iterable<ParseUser> with the users we are sending this message to.
                /*for (ParseUser user : userList) {
                    groupACL.setReadAccess(user, true);
                    //groupACL.setWriteAccess(user, true);
                }*/
            //groupACL.setRoleReadAccess("");

            //groupACL.setRoleWriteAccess("");


            groupACL.setPublicReadAccess(true);

            postParse.setACL(groupACL);
            postParse.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (getActivity().isFinishing()) {
                        return;
                    }
                    if (e == null) {
                        progress_wheel.setVisibility(View.INVISIBLE);
                        progressbackground.setVisibility(View.INVISIBLE);


                        if (mstr_lang.equals(com.parse.utils.Utils.ENG_LANG)) {
                            Utils.doToastEng(mContext, getResources().getString(R.string.iwomen_postupload_success_toast_eng));
                        } else {
                            Utils.doToastMM(mContext, getResources().getString(R.string.iwomen_postupload_success_toast_mm));

                        }
                        Intent intent = new Intent(getActivity(), DrawerMainActivity.class);


                        startActivity(intent);

                    } else {
                        progress_wheel.setVisibility(View.INVISIBLE);
                        progressbackground.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Error saving: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                    }
                }
            });


        }


    }


    @Override
    public void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(getActivity());

        updateUI();

    }


    @Override
    public void onPause() {
        super.onPause();


        AppEventsLogger.deactivateApp(getActivity());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
        super.onSaveInstanceState(outState);


    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        //postStatusUpdateButton.setEnabled(enableButtons || canPresentShareDialog);
        //postPhotoButton.setEnabled(enableButtons || canPresentShareDialogWithPhotos);
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            user = me;
                            //updateUI();


                            //Toast.makeText(getActivity().getApplicationContext(), "ID" + user.optString("id") + "Name" + user.optString("name") + "Email " +user.optString("email"), Toast.LENGTH_LONG).show();

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString(FIELDS, REQUEST_FIELDS);
            request.setParameters(parameters);
            GraphRequest.executeBatchAsync(request);
        } else {
            //Toast.makeText(getActivity().getApplicationContext(), "accessToken Null Case ", Toast.LENGTH_LONG).show();

            user = null;
        }


    }

    /**
     * *************Editext Control********************
     */
    private String getPostEditTextText() {
        return postEditText.getText().toString().trim();
    }

    private void updatePostButtonState() {
        int length = getPostEditTextText().length();
        boolean enabled = length > 0 && length < maxCharacterCount;
        postButton.setEnabled(enabled);
    }

    private void updateCharacterCountTextViewText() {
        String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
        characterCountTextView.setText(characterCountString);
    }
    /********************Editext Control*****************/

    /**
     * *****************Image Chooser***************
     */
    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            //progress_wheel.setVisibility(View.VISIBLE);
            capture_filePath = imageChooserManager.choose();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            //progress_wheel.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == getActivity().RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
            //startActivityForResult(MediaStoreUtils.getPickImageIntent(getActivity().getApplicationContext()),REQUEST_PICTURE );
        } else {
            progress_wheel.setVisibility(View.GONE);
        }
        if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == getActivity().RESULT_OK)) {
            // When we are done cropping, display it in the ImageView.

            img_photo.setVisibility(View.VISIBLE);
            img_photo.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
            //img_job.setMaxWidth(300);
            img_photo.setMaxHeight(400);
            crop_file_name = Uri.fromFile(croppedImageFile).getLastPathSegment().toString();
            crop_file_path = Uri.fromFile(croppedImageFile).getPath();


        }


        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);


        /*if(crop_file_path != null ){

            Intent intent = new Intent(MainReportActivity.this, MainPhotoReportActivity.class);
            intent.putExtra("ImageName" ,crop_file_name);
            intent.putExtra("ImagePath",crop_file_path);
            intent.putExtra("ImageAbsolutePath" ,croppedImageFile.getAbsolutePath());


            startActivity(intent);*//*

            *//*MainReportFragment mainReportFragment = new MainReportFragment();
            Bundle b = new Bundle();

            b.putString("ImageName",crop_file_name);
            b.putString("ImagePath",crop_file_path);
            b.putString("ImageAbsolutePath",croppedImageFile.getAbsolutePath());

            mainReportFragment.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainReportFragment)
                    .commit();
        }*/
    }


    @Override
    public void onImageChosen(final ChosenImage image) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progress_wheel.setVisibility(View.GONE);
                if (image != null) {
                    //textViewFile.setText(image.getFilePathOriginal());
                    croppedImageFile = new File(image.getFilePathOriginal());


                    // When the user is done picking a picture, let's start the CropImage Activity,
                    // setting the output image file and size to 200x200 pixels square.


                    Uri croppedImage = Uri.fromFile(croppedImageFile);
                    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(512, 512, croppedImage);
                    cropImage.setSourceImage(croppedImage);
                    startActivityForResult(cropImage.getIntent(getActivity().getApplicationContext()), REQUEST_CROP_PICTURE);


                    chosenImage = image;


                }
            }
        });
    }

    @Override
    public void onError(final String reason) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progress_wheel.setVisibility(View.GONE);
                Toast.makeText(getActivity().getApplicationContext(), reason,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    //Check Image Rotate

    public static Bitmap decodeFile(String path) {

        int orientation;

        try {

            if (path == null) {

                return null;
            }
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 1024;
            //int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int width_tmp = 512, height_tmp = 512;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bm = BitmapFactory.decodeFile(path, o2);


            Bitmap bitmap = bm;

            ExifInterface exif = new ExifInterface(path);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.e("orientation", "" + orientation);
            Matrix m = new Matrix();

            if ((orientation == 3)) {

                m.postRotate(180);
                m.postScale((float) bm.getWidth(), (float) bm.getHeight());

//               if(m.preRotate(90)){
                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == 6) {

                m.postRotate(90);

                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == 8) {

                m.postRotate(270);

                Log.e("in orientation", "" + orientation);

                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            }
            return bitmap;
        } catch (Exception e) {
        }
        return null;
    }

    public static Bitmap checkifImageRotated(File file) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;

            Log.e("checkifImageRotated", "==>" + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = -90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;


            }
            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(file), null, null);
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
                return bmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
