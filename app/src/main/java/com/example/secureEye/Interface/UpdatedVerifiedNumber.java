package com.example.secureEye.Interface;

public interface UpdatedVerifiedNumber {
    void onVerified(String fullPhone, String cCode, String simplePhone);
    void onFailed(String result);
}
