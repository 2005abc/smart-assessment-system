package com.studybuddy.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("last_login")
    private LocalDateTime lastLogin;

    @Field("is_active")
    private Boolean isActive = true;

    @Field("quiz_count")
    private Integer quizCount = 0;

    @Field("document_analysis_count")
    private Integer documentAnalysisCount = 0;

    @Field("total_chats")
    private Integer totalChats = 0;

    @Field("preferences")
    private UserPreferences preferences = new UserPreferences();

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.quizCount = 0;
        this.documentAnalysisCount = 0;
        this.totalChats = 0;
        this.preferences = new UserPreferences();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Integer quizCount) {
        this.quizCount = quizCount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDocumentAnalysisCount() {
        return documentAnalysisCount;
    }

    public void setDocumentAnalysisCount(Integer documentAnalysisCount) {
        this.documentAnalysisCount = documentAnalysisCount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTotalChats() {
        return totalChats;
    }

    public void setTotalChats(Integer totalChats) {
        this.totalChats = totalChats;
        this.updatedAt = LocalDateTime.now();
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementQuizCount() {
        this.quizCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementDocumentAnalysisCount() {
        this.documentAnalysisCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementTotalChats() {
        this.totalChats++;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", quizCount=" + quizCount +
                ", documentAnalysisCount=" + documentAnalysisCount +
                ", totalChats=" + totalChats +
                ", preferences=" + preferences +
                '}';
    }

    public static class UserPreferences {
        @Field("default_quiz_questions")
        private Integer defaultQuizQuestions = 5;

        @Field("preferred_language")
        private String preferredLanguage = "english";

        @Field("theme")
        private String theme = "dark";

        @Field("auto_save_chats")
        private Boolean autoSaveChats = true;

        @Field("max_file_size")
        private Integer maxFileSize = 5; // in MB

        public Integer getDefaultQuizQuestions() {
            return defaultQuizQuestions;
        }

        public void setDefaultQuizQuestions(Integer defaultQuizQuestions) {
            this.defaultQuizQuestions = defaultQuizQuestions;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public Boolean getAutoSaveChats() {
            return autoSaveChats;
        }

        public void setAutoSaveChats(Boolean autoSaveChats) {
            this.autoSaveChats = autoSaveChats;
        }

        public Integer getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(Integer maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        @Override
        public String toString() {
            return "UserPreferences{" +
                    "defaultQuizQuestions=" + defaultQuizQuestions +
                    ", preferredLanguage='" + preferredLanguage + '\'' +
                    ", theme='" + theme + '\'' +
                    ", autoSaveChats=" + autoSaveChats +
                    ", maxFileSize=" + maxFileSize +
                    '}';
        }
    }
}