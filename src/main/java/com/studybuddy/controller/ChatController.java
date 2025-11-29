package com.studybuddy.controller;
import com.studybuddy.service.GeminiFlashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private GeminiFlashService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String userEmail = request.get("userEmail");
        String queryType = request.get("queryType");

        System.out.println("Chat request - User: " + userEmail + ", Type: " + queryType);

        Map<String, Object> response = new HashMap<>();

        try {
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            String prompt = buildPrompt(message, queryType);
            String aiResponse = geminiService.generateResponse(prompt);

            response.put("success", true);
            response.put("response", aiResponse);
            response.put("queryType", queryType);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Error in chat: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Sorry, I encountered an error. Please try again later.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/generate-quiz")
    public ResponseEntity<Map<String, Object>> generateQuiz(@RequestBody Map<String, String> request) {
        String topic = request.get("topic");
        String questionCount = request.get("questionCount");
        String difficulty = request.get("difficulty");
        String userEmail = request.get("userEmail");

        System.out.println("Quiz request - Topic: " + topic + ", Questions: " + questionCount + ", User: " + userEmail);

        Map<String, Object> response = new HashMap<>();

        try {
            if (topic == null || topic.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Quiz topic is required");
                return ResponseEntity.badRequest().body(response);
            }

            int count;
            try {
                count = Integer.parseInt(questionCount);
                if (count < 1 || count > 20) {
                    response.put("success", false);
                    response.put("message", "Question count must be between 1 and 20");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (NumberFormatException e) {
                response.put("success", false);
                response.put("message", "Invalid question count");
                return ResponseEntity.badRequest().body(response);
            }

            String prompt = buildQuizPrompt(topic, count, difficulty);
            String aiResponse = geminiService.generateResponse(prompt);
            Map<String, Object> quizData = parseQuizResponse(aiResponse, topic, count);
            System.out.println("✅ Quiz generated successfully");
            response.put("success", true);
            response.put("quiz", quizData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error generating quiz: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to generate quiz. Please try again.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/analyze-document")
    public ResponseEntity<Map<String, Object>> analyzeDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "instructions", required = false) String instructions,
            @RequestParam("userEmail") String userEmail) {

        System.out.println("========== ENHANCED DOCUMENT ANALYSIS ==========");
        System.out.println("File: " + file.getOriginalFilename());
        System.out.println("Instructions: " + instructions);
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file");
                return ResponseEntity.badRequest().body(response);
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            String contentType = file.getContentType();
            if (!isSupportedFileType(contentType)) {
                response.put("success", false);
                response.put("message", "Unsupported file type. Please upload PDF, image, or text files.");
                return ResponseEntity.badRequest().body(response);
            }

            String analysisResult = geminiService.analyzeDocument(file, instructions);

            System.out.println("ENHANCED AI ANALYSIS COMPLETED");

            response.put("success", true);
            response.put("analysis", analysisResult);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("fileType", contentType);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Error in enhanced document analysis: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Failed to analyze document: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "AI Study Buddy API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    private String buildPrompt(String message, String queryType) {
        switch (queryType) {
            case "code":
                return "You are an expert programming assistant. Provide clear, concise code help:\n\n" + message;
            case "explain":
                return "Explain this concept in simple, easy-to-understand terms with examples:\n\n" + message;
            case "quiz":
                return "Create learning questions about this topic:\n\n" + message;
            case "summarize":
                return "Provide a clear and concise summary of:\n\n" + message;
            default:
                return message;
        }
    }
    private String buildQuizPrompt(String topic, int questionCount, String difficulty) {
        return String.format(
                "Generate a %d-question multiple choice quiz on the topic: '%s'. " +
                        "Difficulty: %s. " +
                        "Format: Question, then 4 options (A, B, C, D), then 'Correct: X' where X is A,B,C, or D. " +
                        "Make questions clear and options distinct.",
                questionCount, topic, difficulty != null ? difficulty : "medium"
        );
    }
    private String buildSummaryPrompt(String text, String summaryType) {
        String typeInstruction = "";
        if ("brief".equals(summaryType)) {
            typeInstruction = "Provide a very brief summary (2-3 sentences).";
        } else if ("bullet".equals(summaryType)) {
            typeInstruction = "Provide a summary in bullet points.";
        } else {
            typeInstruction = "Provide a detailed summary.";
        }
        return String.format(
                "Please summarize the following text. %s Focus on key points and main ideas:\n\n%s",
                typeInstruction, text
        );
    }
    private Map<String, Object> parseQuizResponse(String aiResponse, String topic, int count) {
        // Simple parsing - in real implementation, you'd want more robust parsing
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("topic", topic);
        quizData.put("totalQuestions", count);
        quizData.put("questions", aiResponse);
        System.out.println("Quiz response (needs parsing): " + aiResponse);
        return quizData;
    }
    private String simulateDocumentAnalysis(MultipartFile file, String instructions) {
        String baseAnalysis = String.format(
                "Analysis of document: %s (%d bytes)\n\n",
                file.getOriginalFilename(), file.getSize()
        );
        if (instructions != null && !instructions.trim().isEmpty()) {
            return baseAnalysis + "Based on your instructions: \"" + instructions + "\"\n\n" +
                    "This is a simulated analysis. In a full implementation, the AI would analyze the document content and provide specific feedback based on your instructions.";
        } else {
            return baseAnalysis + "This is a simulated analysis. In a full implementation, the AI would extract and analyze the content of your document to provide detailed insights and feedback.";
        }
    }

    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
                contentType.startsWith("image/") ||
                        contentType.equals("application/pdf") ||
                        contentType.equals("text/plain") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
}