package com.example.secureEye.Interface;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import androidx.annotation.NonNull;

public interface OTP_Verify_Result {
    void onSuccess(@NonNull Task<AuthResult> task);
    void onFailed(String result);
}
