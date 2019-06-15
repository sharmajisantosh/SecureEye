package com.example.secureEye.Fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secureEye.Adapter.ChoosenImageAdapter;
import com.example.secureEye.Model.UserMessage;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Services.LocationHelper;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.SharedPrefManager;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UserMessagingFrag extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "UserMessagingFrag";
    public static final int RequestPermissionCode = 100;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;
    private static final int RESULT_CAPTURE_VIDEO = 3;
    private static final String IMAGE_DIRECTORY = "/SecureEye/Image";
    private static final String AUDIO_RECORDER_FOLDER = "/SecureEye/Audio";
    private static final String VIDEO_RECORDER_FOLDER = "/SecureEye/Video";
    private Button btnChooseImage, btnUploadNote, btnRecordAudio, btnRecordVideo;
    private CollectionReference adminProfileRef, userProfileRef;
    private ProgressBar audioProgressBar;
    private RecyclerView recycleImageList;
    private FirebaseAuth mAuth;
    private EditText etTextMessage;
    private TextView tvAudioFileName, tvVideoFileName;
    private List<String> fileNameList;
    private List<Uri> fileUriList;
    private List<String> storageImageUrlList;
    private String storageAudioUrl, storageVideoUrl, textMessage, userAdminDeviceToken;
    private ChoosenImageAdapter choosenImageAdapter;
    private PickImageDialog dialog;
    private MediaRecorder recorder = null;
    private String audioFullFileName, audioFileName, videoFullFileName, videoFileName;
    private int counter = 0, count = 0;
    private ProgressDialog progressDialog1;
    private Runnable r;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_messaging, container, false);

        SpannableString str = new SpannableString("Messaging");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        mAuth = FirebaseAuth.getInstance();
        btnChooseImage = view.findViewById(R.id.btnChooseImage);
        btnUploadNote = view.findViewById(R.id.btnUploadNote);
        btnRecordAudio = view.findViewById(R.id.btnRecordAudio);
        btnRecordVideo = view.findViewById(R.id.btnRecordVideo);
        recycleImageList = view.findViewById(R.id.recyclerChooseImage);
        tvAudioFileName = view.findViewById(R.id.tvAudioFileName);
        tvVideoFileName = view.findViewById(R.id.tvVideoFileName);
        audioProgressBar = view.findViewById(R.id.audioProgressBar);
        etTextMessage = view.findViewById(R.id.etTextMessage);

        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;

      /*  SpannableString btnText = new SpannableString("(PRESS AND HOLD)");
        btnText.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btnText.setSpan(new RelativeSizeSpan(1f),0,btnText.length(),0);//text size
        btnRecordAudio.setText("Record Audio\n"+ btnText);*/

        fileNameList = new ArrayList<>();
        fileUriList = new ArrayList<>();
        storageImageUrlList = new ArrayList<>();
        choosenImageAdapter = new ChoosenImageAdapter(fileNameList);
        audioProgressBar.setVisibility(View.GONE);

        recycleImageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycleImageList.setAdapter(choosenImageAdapter);

        btnChooseImage.setOnClickListener(this);
        btnUploadNote.setOnClickListener(this);
        btnRecordAudio.setOnTouchListener(this);
        btnRecordVideo.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChooseImage:
                ChooseImage();
                break;
            case R.id.btnUploadNote:
                UploadNote();
                break;
            case R.id.btnRecordVideo:
                RecordVideo();
                break;
        }

    }

    private void ChooseImage() {
        if (checkPermission()) {
            dialog = PickImageDialog.build(new PickSetup())
                    .setOnClick(new IPickClick() {
                        @Override
                        public void onGalleryClick() {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
                            dialog.dismiss();
                        }

                        @Override
                        public void onCameraClick() {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, RESULT_CAPTURE_IMAGE);
                            dialog.dismiss();

                        }
                    }).show(getActivity().getSupportFragmentManager());
        } else {
            requestPermissions();
        }

    }

    private void UploadNote() {

        count = fileNameList.size() + count;
        progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Uploading.... " + counter + "/" + count);
        progressDialog1.show();

        textMessage = etTextMessage.getText().toString().trim();
        StorageReference imgStorageRef = Constant_URLS.IMAGE_STORAGE_REF;

        if (fileUriList.size() > 0) {
            storageImageUrlList.clear();
            for (int i = 0; i < fileUriList.size(); i++) {

                StorageReference imageRef = imgStorageRef.child(mAuth.getUid()).child(System.currentTimeMillis()+".jpg");
                imageRef.putFile(fileUriList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Toast.makeText(getActivity(), "uploaded", Toast.LENGTH_SHORT).show();
                                storageImageUrlList.add(uri.toString());
                                Log.d(TAG, "onSuccess: image url is " + uri.toString());
                                counter++;
                            }
                        });
                    }
                });

            }
        }
            if (audioFullFileName != null) {
                StorageReference audioStorageRef = Constant_URLS.AUDIO_STORAGE_REF;
                StorageReference audioRef = audioStorageRef.child(mAuth.getUid()).child(audioFileName);
                Uri uriAudio = Uri.fromFile(new File(audioFullFileName));
                audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                storageAudioUrl = uri.toString();
                                Log.d(TAG, "onSuccess: audio url is " + uri.toString());
                                counter++;
                            }
                        });
                    }
                });
            }

            if (videoFullFileName != null) {
                StorageReference videoStorageRef = Constant_URLS.VIDEO_STORAGE_REF;
                StorageReference videoRef = videoStorageRef.child(mAuth.getUid()).child(videoFileName);
                Uri uriVideo = Uri.fromFile(new File(videoFullFileName));
                videoRef.putFile(uriVideo).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                storageVideoUrl = uri.toString();
                                Log.d(TAG, "onSuccess: video url is " + uri.toString());
                                counter++;
                            }
                        });
                    }
                });
            }

            Handler handler = new Handler();
            r = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 1000);
                    if (counter == count) {
                        JSONObject imgUrlJson=null;
                        progressDialog1.setMessage("Uploading.... " + counter + "/" + count);
                        if (storageImageUrlList.size()>0) {
                            imgUrlJson = new JSONObject();
                            for (int i = 0; i < storageImageUrlList.size(); i++) {
                                try {
                                    imgUrlJson.put("imgUrl" + i, storageImageUrlList.get(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        String userAdminId=SharedPrefManager.getInstance(getActivity()).getUserAdminId();
                        Log.d(TAG, "admin id "+userAdminId);
                        JSONObject finalImgUrlJson = imgUrlJson;
                        adminProfileRef.document(userAdminId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isComplete()) {
                                    DocumentSnapshot user = task.getResult();
                                    user.toObject(UserProfile.class);
                                    userAdminDeviceToken = user.get("deviceToken").toString();
                                    String userAdminMail = SharedPrefManager.getInstance(getActivity()).getUserAdminMail();
                                    Log.d(TAG, "onSuccess:abcde "+userAdminDeviceToken);
                                    UserMessage userMessage = new UserMessage();
                                    userMessage.setAdminMail(userAdminMail);
                                    userMessage.setToAdminDevice(userAdminDeviceToken);
                                    userMessage.setFromId(mAuth.getUid());
                                    userMessage.setFromName(mAuth.getCurrentUser().getDisplayName());
                                    userMessage.setMessage(textMessage);
                                    if (finalImgUrlJson!=null) {
                                        userMessage.setImgUrl(finalImgUrlJson.toString());
                                    }else {
                                        userMessage.setImgUrl("");
                                    }
                                    userMessage.setAudUrl(storageAudioUrl);
                                    userMessage.setVidUrl(storageVideoUrl);
                                    userMessage.setTimeStamp(LocationHelper.getGMTTime());
                                    userMessage.setRead(false);

                                    Log.d(TAG, "inside admin profile");
                                    handler.removeCallbacks(r);
                                    progressDialog1.dismiss();

                                    String userAdminId = SharedPrefManager.getInstance(getActivity()).getUserAdminId();
                                    adminProfileRef.document(userAdminId).collection("usersMessage").document(String.valueOf(System.currentTimeMillis()))
                                            .set(userMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess:all uploaded ");

                                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                                            UserNavDashboard navFrag1 = new UserNavDashboard();
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                            ft.replace(R.id.NavFrameLayout1, navFrag1);
                                            ft.commit();
                                            handler.removeCallbacks(r);
                                            progressDialog1.dismiss();
                                        }
                                    });
                                }
                            }
                        });

                    } else {
                        progressDialog1.setMessage("Uploading.... " + counter + "/" + count);
                    }
                }
            };

            handler.postDelayed(r, 1000);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Log.d(TAG, "in galaery result");
            if (data.getClipData() != null) {

                int totalItemsSelected = data.getClipData().getItemCount();

                for (int i = 0; i < totalItemsSelected; i++) {

                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    fileUriList.add(fileUri);
                    String fileName = getFileName(fileUri);

                    fileNameList.add(fileName);
                    choosenImageAdapter.notifyDataSetChanged();
                }
                //Toast.makeText(getActivity(), "Selected multiple files", Toast.LENGTH_SHORT).show();
            } else if (data.getData() != null) {
                //Toast.makeText(getActivity(), "selected single file", Toast.LENGTH_SHORT).show();

                Uri fileUri = data.getData();
                fileUriList.add(fileUri);
                String fileName = getFileName(fileUri);

                fileNameList.add(fileName);
                choosenImageAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            Log.d(TAG, "incamera result");
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            Uri fileUri = saveImage(thumbnail);
            if (fileUri != null) {
                fileUriList.add(fileUri);
                String fileName = getFileName(fileUri);

                fileNameList.add(fileName);
                choosenImageAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Image not saved", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == RESULT_CAPTURE_VIDEO && resultCode == RESULT_OK) {
            Uri contentURI = data.getData();
            String recordedVideoPath = getPath(contentURI);
            Log.d("frrr", recordedVideoPath);
            saveVideoToInternalStorage(recordedVideoPath);
        } else {
            Log.d(TAG, "in result else");
        }
    }

    private Uri saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, "IMG_" + SessionManager.getDateTime() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getActivity(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return Uri.fromFile(f);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (checkPermission()) {
                    startRecording();
                } else {
                    requestPermissions();
                }
                return true;

            case MotionEvent.ACTION_UP:
                stopRecording();
                break;
        }
        return false;
    }


    private void startRecording() {

        btnRecordAudio.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        audioProgressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Recording start", Toast.LENGTH_SHORT).show();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioFullFileName = getAudioFilename();
        recorder.setOutputFile(audioFullFileName);
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        btnRecordAudio.setBackgroundTintList(getResources().getColorStateList(R.color.bg_main));
        audioProgressBar.setVisibility(View.GONE);
        if (null != recorder) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            Toast.makeText(getActivity(), "Recording stop", Toast.LENGTH_SHORT).show();
            tvAudioFileName.setText(audioFileName);
            count++;
        }
    }

    private String getAudioFilename() {
        audioFileName = "A_" + SessionManager.getDateTime() + ".mp3";
        File file = new File(Environment.getExternalStorageDirectory() + AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        return (Environment.getExternalStorageDirectory() + AUDIO_RECORDER_FOLDER + audioFileName);
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d(TAG, "Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.d(TAG, "Warning: " + what + ", " + extra);
        }
    };

    private void RecordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, RESULT_CAPTURE_VIDEO);
    }

    private void saveVideoToInternalStorage(String filePath) {

        File newfile;

        try {

            File currentFile = new File(filePath);
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + VIDEO_RECORDER_FOLDER);
            videoFileName = "V_" + SessionManager.getDateTime() + ".mp4";

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }
            newfile = new File(Environment.getExternalStorageDirectory() + VIDEO_RECORDER_FOLDER + videoFileName);
            count++;
            tvVideoFileName.setText(videoFileName);
            videoFullFileName = Environment.getExternalStorageDirectory() + VIDEO_RECORDER_FOLDER + videoFileName;

            if (currentFile.exists()) {

                InputStream in = new FileInputStream(currentFile);
                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.d(TAG, "Video file saved successfully.");
            } else {
                Log.d(TAG, "Video saving failed. Source file missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {

                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission && CameraPermission) {

                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }
}
