package com.studybuddy.service;

import com.studybuddy.entity.User;
import com.studybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String registerUser(String name, String email, String password) {
        try {

            if (name == null || name.trim().isEmpty()) {
                return "Name is required";
            }

            if (email == null || email.trim().isEmpty()) {
                return "Email is required";
            }

            if (password == null || password.length() < 6) {
                return "Password must be at least 6 characters";
            }

            email = email.trim().toLowerCase();
            name = name.trim();

            if (userRepository.existsByEmail(email)) {
                return "User already exists with this email";
            }

            User user = new User(name, email, password);
            userRepository.save(user);

            return "Registration successful";

        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    public String loginUser(String email, String password) {
        try {

            if (email == null || email.trim().isEmpty()) {
                return "Email is required";
            }

            if (password == null || password.isEmpty()) {
                return "Password is required";
            }

            email = email.trim().toLowerCase();

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            User user = userOpt.get();

            if (!user.getIsActive()) {
                return "Account is deactivated. Please contact support.";
            }

            if (!user.getPassword().equals(password)) {
                return "Invalid password";
            }

            user.updateLastLogin();
            userRepository.save(user);

            return "Login successful - Welcome, " + user.getName();

        } catch (Exception e) {
            return "Login failed: " + e.getMessage();
        }
    }

    public Optional<User> getUserByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Optional.empty();
            }
            return userRepository.findByEmail(email.trim().toLowerCase());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String updateUserPassword(String email, String oldPassword, String newPassword) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            User user = userOpt.get();

            if (!user.getPassword().equals(oldPassword)) {
                return "Current password is incorrect";
            }

            if (newPassword.length() < 6) {
                return "New password must be at least 6 characters";
            }

            user.setPassword(newPassword);
            userRepository.save(user);

            return "Password updated successfully";

        } catch (Exception e) {
            return "Password update failed: " + e.getMessage();
        }
    }

    public String updateUserProfile(String email, String newName) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            if (newName == null || newName.trim().isEmpty()) {
                return "Name cannot be empty";
            }

            User user = userOpt.get();
            user.setName(newName.trim());
            userRepository.save(user);

            return "Profile updated successfully";

        } catch (Exception e) {
            return "Profile update failed: " + e.getMessage();
        }
    }

    public String deactivateUser(String email, String password) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            User user = userOpt.get();

            if (!user.getPassword().equals(password)) {
                return "Invalid password";
            }

            user.setIsActive(false);
            userRepository.save(user);

            return "Account deactivated successfully";

        } catch (Exception e) {
            return "Account deactivation failed: " + e.getMessage();
        }
    }

    public String reactivateUser(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            User user = userOpt.get();
            user.setIsActive(true);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return "Account reactivated successfully";

        } catch (Exception e) {
            return "Account reactivation failed: " + e.getMessage();
        }
    }

    public boolean validateUserCredentials(String email, String password) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            return user.getIsActive() && user.getPassword().equals(password);

        } catch (Exception e) {
            return false;
        }
    }

    public User.UserPreferences getUserPreferences(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return user.getPreferences() != null ? user.getPreferences() : new User.UserPreferences();
            }

            return new User.UserPreferences();

        } catch (Exception e) {
            return new User.UserPreferences();
        }
    }

    public String updateUserPreferences(String email, User.UserPreferences preferences) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return "User not found";
            }

            User user = userOpt.get();
            user.setPreferences(preferences);
            userRepository.save(user);

            return "Preferences updated successfully";

        } catch (Exception e) {
            return "Preferences update failed: " + e.getMessage();
        }
    }

    public void incrementUserStats(String email, String actionType) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                switch (actionType) {
                    case "quiz":
                        user.incrementQuizCount();
                        break;
                    case "document":
                        user.incrementDocumentAnalysisCount();
                        break;
                    case "chat":
                        user.incrementTotalChats();
                        break;
                }

                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("Error updating user stats: " + e.getMessage());
        }
    }
}