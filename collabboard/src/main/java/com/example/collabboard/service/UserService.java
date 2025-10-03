package com.example.collabboard.service;

import com.example.collabboard.model.User;
import com.example.collabboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // In-memory storage for OTPs. In a real-world production app, this should
    // be stored in a more persistent cache like Redis or a database table.
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiryStorage = new ConcurrentHashMap<>();

    // Using constructor injection is a best practice in Spring
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void registerUser(String username, String email, String password) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email address is already registered.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    // Returning an Optional is a cleaner way to handle a successful/failed login
    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Generates a 6-digit OTP, stores it, and sends it to the user's email.
     *
     * @param email The email address of the user who forgot their password.
     * @throws Exception if no user is found with the given email.
     */
    public void generateAndSendOtp(String email) throws Exception {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("No user found with this email address."));

        String otp = new Random().ints(1, 100000, 999999)
                .findFirst()
                .getAsInt() + "";

        otpStorage.put(email, otp);
        otpExpiryStorage.put(email, LocalDateTime.now().plusMinutes(10)); // OTP is valid for 10 minutes

        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Resets the user's password after verifying the OTP.
     *
     * @param email       The user's email.
     * @param otp         The OTP code entered by the user.
     * @param newPassword The new password to set.
     * @throws Exception if the OTP is invalid/expired or the user is not found.
     */
    public void resetPassword(String email, String otp, String newPassword) throws Exception {
        if (!verifyOtp(email, otp)) {
            throw new Exception("Invalid or expired OTP. Please try again.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("An unexpected error occurred. User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate the OTP after it has been successfully used
        otpStorage.remove(email);
        otpExpiryStorage.remove(email);
    }

    /**
     * A private helper method to check if an OTP is valid and not expired.
     */
    private boolean verifyOtp(String email, String otp) {
        // Check if the provided OTP matches the stored OTP
        boolean isOtpValid = otp != null && otp.equals(otpStorage.get(email));
        if (!isOtpValid) {
            return false;
        }

        // Check if the OTP has expired
        LocalDateTime expiryTime = otpExpiryStorage.get(email);
        return expiryTime != null && expiryTime.isAfter(LocalDateTime.now());
    }
}

