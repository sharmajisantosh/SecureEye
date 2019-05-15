package com.example.secureEye.Fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Interface.UpdatedVerifiedNumber;
import com.example.secureEye.Interface.UpdatedVerifiedPassword;
import com.example.secureEye.R;
import com.example.secureEye.Utils.ImageConversion;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.TypefaceSpan;
import com.example.secureEye.Utils.UpdateMobile;
import com.example.secureEye.Utils.UpdatePassword;
import com.hbb20.CountryCodePicker;
import com.somesh.permissionmadeeasy.enums.Permission;
import com.somesh.permissionmadeeasy.helper.PermissionHelper;
import com.somesh.permissionmadeeasy.intefaces.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.example.secureEye.Utils.ImageConversion.selectedImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class NavEditAdminProfile extends Fragment implements PermissionListener, View.OnClickListener {

    private String TAG = getClass().getSimpleName();
    private CircleImageView circleProfilePic;
    private EditText etProfileName, etMobile, etNewPassword;
    private CountryCodePicker countryCode;
    private Button btnUpdate;
    private PermissionHelper permissionHelper;
    private static final int REQUEST_MULTIPLE_PERMISSION = 101;
    private Uri choosenImage;
    private Bitmap bp;
    private AwesomeValidation validation;
    private SessionManager session;
    private String currentUserName, currentFullMobile, currentSimpleMobile, currentCCode;
    private String sqlQuery;

    public NavEditAdminProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_edit_admin_profile, container, false);
        SpannableString str = new SpannableString("Edit Profile");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        etProfileName = view.findViewById(R.id.profileName);
        etMobile = view.findViewById(R.id.editMobile);
        countryCode = view.findViewById(R.id.editCountryCode);
        btnUpdate = view.findViewById(R.id.btnProfileUpdate);
        circleProfilePic = view.findViewById(R.id.profilePic);
        etNewPassword = view.findViewById(R.id.editOldPass);

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);

        session = new SessionManager(getActivity());

        HashMap<String, String> user = session.getUserDetails();
        currentUserName = user.get(SessionManager.KEY_NAME);
        currentSimpleMobile = user.get(SessionManager.KEY_SIMPLE_PHONE);
        currentCCode = user.get(SessionManager.KEY_C_CODE);
        currentFullMobile = user.get(SessionManager.KEY_PHONE);
        String encodedImage = user.get(SessionManager.KEY_IMAGE);
        Bitmap profilePic = ImageConversion.convertToBitmap(encodedImage);
        sqlQuery = "UPDATE LoginInfo Set ";

        circleProfilePic.setImageBitmap(profilePic);
        etProfileName.setText(currentUserName);
        countryCode.setCountryForPhoneCode(91);
        etMobile.setText(currentSimpleMobile);
        btnUpdate.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.profileName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.editMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        circleProfilePic.setOnClickListener(this);
        etMobile.setOnClickListener(this);
        etNewPassword.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.profilePic:
                selectProfilePic();
                break;

            case R.id.editMobile:
                etMobile.setText("");
                ChangeMobileNumber();
                break;

            case R.id.editOldPass:
                ChangePassword();
                break;

            case R.id.btnProfileUpdate:
                updateData();
                break;
        }
    }

    private void updateData() {

        if (validation.validate()) {

            updateDataOnServer();

        } else {
            Toast.makeText(getActivity(), "Some Error Occured.", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateDataOnServer() {

    }

    private void selectProfilePic() {
        if (checkPermissionForCameraAndStorage()) {

            selectPhoto();

        } else {
            requestCameraStoragePermission();
        }
    }

    private void requestCameraStoragePermission() {
        permissionHelper = PermissionHelper.Builder()
                .with(getActivity())
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(this)
                .askFor(Permission.CAMERA, Permission.STORAGE)
                .rationalMessage("Permissions are required for app to work properly")
                .build();

        permissionHelper.requestPermissions();
    }

    private boolean checkPermissionForCameraAndStorage() {
        boolean havePermission = false;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            havePermission = true;
        }
        return havePermission;
    }

    @Override
    public void onPermissionsGranted(int i, ArrayList<String> arrayList) {
        selectPhoto();
    }

    @Override
    public void onPermissionsDenied(int i, ArrayList<String> arrayList) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void selectPhoto() {
        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ((CircleImageView) getActivity().findViewById(R.id.profilePic)).setImageURI(result.getUri());
                Toast.makeText(getActivity(), "" + result.getUri(), Toast.LENGTH_SHORT).show();
                circleProfilePic.setImageURI(result.getUri());
                choosenImage = result.getUri();
                if (choosenImage != null) {
                    bp = ImageConversion.decodeUri(choosenImage, 200, getActivity());
                    final String encodedImage = Base64.encodeToString(selectedImage(bp), Base64.DEFAULT);
                    sqlQuery = sqlQuery + " User_Pic = '" + encodedImage + "'";
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(getActivity(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void ChangePassword() {
        UpdatePassword updatePassword = new UpdatePassword(currentUserName, currentFullMobile, getActivity());
        updatePassword.updatePassword(new UpdatedVerifiedPassword() {
            @Override
            public void onVerified(String password) {
                if (password.isEmpty()) {
                    btnUpdate.setEnabled(false);
                } else {
                    btnUpdate.setEnabled(true);
                    etNewPassword.setText(password);
                    sqlQuery = sqlQuery + " Password = '" + password + "'";
                }
            }

            @Override
            public void onFailed(String result) {

            }
        });
    }

    private void ChangeMobileNumber() {

        UpdateMobile updateMobile = new UpdateMobile(getActivity());
        updateMobile.showNewMobileDialog(new UpdatedVerifiedNumber() {

            @Override
            public void onVerified(String fullPhone, String cCode, String simplePhone) {
                if (currentSimpleMobile.equalsIgnoreCase(simplePhone)) {
                    btnUpdate.setEnabled(false);
                } else {
                    btnUpdate.setEnabled(true);
                    etMobile.setText(simplePhone);
                    countryCode.registerCarrierNumberEditText(etMobile);
                    countryCode.setCountryForPhoneCode(Integer.parseInt(cCode));
                    sqlQuery = sqlQuery + " C_Code = '" + cCode + "' Simple_Number = '" + simplePhone + "' Full_Mobile_No = '" + countryCode.getFullNumberWithPlus() + "'";

                }
            }

            @Override
            public void onFailed(String result) {

            }
        });
    }
}
