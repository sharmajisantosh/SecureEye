package com.example.secureEye.Interface;

public interface UpdatedVerifiedPassword {
    void onVerified(String newPassword);
    void onFailed(String result);
}
