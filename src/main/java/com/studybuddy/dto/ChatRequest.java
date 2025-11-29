package com.studybuddy.dto;

import org.springframework.web.multipart.MultipartFile;

public class ChatRequest {
    private String message;
    private String userEmail;
    private String queryType;
    private String quizTopic;
    private Integer questionCount;
    private String difficulty;
    private MultipartFile document;
    private String analysisInstructions;
    private String summaryType;

    public ChatRequest() {}
    public ChatRequest(String message, String userEmail, String queryType) {
        this.message = message;
        this.userEmail = userEmail;
        this.queryType = queryType;
    }

    public ChatRequest(String quizTopic, Integer questionCount, String difficulty, String userEmail) {
        this.quizTopic = quizTopic;
        this.questionCount = questionCount;
        this.difficulty = difficulty;
        this.userEmail = userEmail;
        this.queryType = "quiz";
    }

    public ChatRequest(MultipartFile document, String analysisInstructions, String userEmail) {
        this.document = document;
        this.analysisInstructions = analysisInstructions;
        this.userEmail = userEmail;
        this.queryType = "document";
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQuizTopic() {
        return quizTopic;
    }

    public void setQuizTopic(String quizTopic) {
        this.quizTopic = quizTopic;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public MultipartFile getDocument() {
        return document;
    }

    public void setDocument(MultipartFile document) {
        this.document = document;
    }

    public String getAnalysisInstructions() {
        return analysisInstructions;
    }

    public void setAnalysisInstructions(String analysisInstructions) {
        this.analysisInstructions = analysisInstructions;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    public boolean isQuizRequest() {
        return "quiz".equals(queryType) && quizTopic != null && !quizTopic.trim().isEmpty();
    }

    public boolean isDocumentRequest() {
        return "document".equals(queryType) && document != null && !document.isEmpty();
    }

    public boolean isSummaryRequest() {
        return "summarize".equals(queryType) && message != null && !message.trim().isEmpty();
    }

    public boolean isGeneralChat() {
        return message != null && !message.trim().isEmpty() &&
                !"quiz".equals(queryType) &&
                !"document".equals(queryType) &&
                !"summarize".equals(queryType);
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + (message != null ? message.substring(0, Math.min(message.length(), 50)) + "..." : "null") + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", queryType='" + queryType + '\'' +
                ", quizTopic='" + quizTopic + '\'' +
                ", questionCount=" + questionCount +
                ", difficulty='" + difficulty + '\'' +
                ", document=" + (document != null ? document.getOriginalFilename() : "null") +
                ", analysisInstructions='" + analysisInstructions + '\'' +
                ", summaryType='" + summaryType + '\'' +
                '}';
    }
}