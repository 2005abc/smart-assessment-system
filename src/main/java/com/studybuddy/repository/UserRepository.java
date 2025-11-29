package com.studybuddy.repository;

import com.studybuddy.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();
    List<User> findByIsActiveFalse();

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findByQuizCountGreaterThan(Integer count);

    List<User> findByDocumentAnalysisCountGreaterThan(Integer count);

    @Query("{ 'email': ?0 }")
    @Update("{ '$inc': { 'quizCount': 1, 'totalChats': 1 }, '$set': { 'updatedAt': ?1 } }")
    void incrementQuizCount(String email, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$inc': { 'documentAnalysisCount': 1, 'totalChats': 1 }, '$set': { 'updatedAt': ?1 } }")
    void incrementDocumentAnalysisCount(String email, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$inc': { 'totalChats': 1 }, '$set': { 'updatedAt': ?1 } }")
    void incrementTotalChats(String email, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'lastLogin': ?1, 'updatedAt': ?1 } }")
    void updateLastLogin(String email, java.time.LocalDateTime lastLogin);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'preferences.defaultQuizQuestions': ?1, 'updatedAt': ?2 } }")
    void updateDefaultQuizQuestions(String email, Integer defaultQuestions, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'preferences.theme': ?1, 'updatedAt': ?2 } }")
    void updateTheme(String email, String theme, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'preferences.autoSaveChats': ?1, 'updatedAt': ?2 } }")
    void updateAutoSaveChats(String email, Boolean autoSave, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'isActive': false, 'updatedAt': ?1 } }")
    void deactivateUser(String email, java.time.LocalDateTime updatedAt);

    @Query("{ 'email': ?0 }")
    @Update("{ '$set': { 'isActive': true, 'updatedAt': ?1 } }")
    void activateUser(String email, java.time.LocalDateTime updatedAt);

    List<User> findTop5ByOrderByQuizCountDesc();

    List<User> findTop5ByOrderByDocumentAnalysisCountDesc();

    List<User> findByCreatedAtAfter(java.time.LocalDateTime date);

    long countByIsActiveTrue();

    List<User> findByPreferencesTheme(String theme);

    @Query("{ 'isActive': true, 'quizCount': { $gt: ?0 }, 'documentAnalysisCount': { $gt: ?1 } }")
    List<User> findActiveUsersWithHighUsage(Integer minQuizCount, Integer minDocAnalysisCount);

    List<User> findByEmailContainingIgnoreCase(String emailPattern);

    List<User> findByLastLoginBefore(java.time.LocalDateTime date);

    @Query("{ 'lastLogin': { $lt: ?0 }, 'isActive': true }")
    @Update("{ '$set': { 'isActive': false, 'updatedAt': ?1 } }")
    void deactivateInactiveUsers(java.time.LocalDateTime lastLoginThreshold, java.time.LocalDateTime updatedAt);
}