package com.example.secureEye.Interface;

import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public interface UpdatePhoneResult {

    void onSuccess(@NonNull Task<Void> task);
    void onFailed(String result);
}
