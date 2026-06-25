package fpt.org.inblue.service;

public interface PasswordResetService {
    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);
}
