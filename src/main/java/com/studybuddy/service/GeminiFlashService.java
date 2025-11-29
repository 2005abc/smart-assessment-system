package com.studybuddy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;

@Service
public class GeminiFlashService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiFlashService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String prompt) {
        return generateResponse(prompt, "general");
    }

    public String generateResponse(String prompt, String queryType) {
        try {
            System.out.println("Processing: " + prompt);
            System.out.println("Query Type: " + queryType);

            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("${")) {
                return "API Key not configured properly. Please check your application.properties file.";
            }
            String enhancedPrompt = enhancePrompt(prompt, queryType);

            String requestBody = String.format("""
                {
                    "contents": [{
                        "parts": [{"text": "%s"}]
                    }],
                    "generationConfig": {
                        "temperature": %f,
                        "topK": 40,
                        "topP": 0.95,
                        "maxOutputTokens": %d
                    },
                    "safetySettings": [
                        {
                            "category": "HARM_CATEGORY_HARASSMENT",
                            "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                            "category": "HARM_CATEGORY_HATE_SPEECH",
                            "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                            "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                            "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                            "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                            "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        }
                    ]
                }
                """,
                    enhancedPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
                    getTemperature(queryType),
                    getMaxTokens(queryType)
            );

            System.out.println("Sending request to Gemini API...");

            String response = webClient.post()
                    .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Raw Response received");
            return extractTextFromResponse(response);

        } catch (Exception e) {
            System.out.println("Error in Gemini API call: " + e.getMessage());
            e.printStackTrace();
            return getErrorMessage(e, queryType);
        }
    }

    public String generateQuiz(String topic, int questionCount, String difficulty) {
        try {
            String prompt = String.format("""
                Generate a %d-question multiple choice quiz on the topic: "%s".
                Difficulty level: %s.
                
                Format requirements:
                - Each question should be clear and concise
                - Provide exactly 4 options (A, B, C, D) for each question
                - Mark the correct answer clearly
                - Questions should test understanding, not just memorization
                - Make options plausible but distinct
                
                Format each question like this:
                Question: [question text]
                A) [option A]
                B) [option B]
                C) [option C]
                D) [option D]
                Correct: [A/B/C/D]
                
                Generate exactly %d questions.
                """, questionCount, topic, difficulty, questionCount);

            return generateResponse(prompt, "quiz");

        } catch (Exception e) {
            System.out.println("Error generating quiz: " + e.getMessage());
            return "Failed to generate quiz. Please try again with a different topic.";
        }
    }

    public String analyzeDocument(MultipartFile file, String instructions) {
        try {
            System.out.println("========== ENHANCED DOCUMENT ANALYSIS ==========");
            System.out.println("File: " + file.getOriginalFilename());
            System.out.println("Size: " + file.getSize() + " bytes");
            System.out.println("Type: " + file.getContentType());
            System.out.println("Instructions: " + instructions);

            String fileContent = extractTextFromFile(file);

            String prompt;

            if (fileContent != null && !fileContent.trim().isEmpty() && fileContent.length() > 50) {
                System.out.println("Using extracted text content for AI analysis");
                System.out.println("Content preview: " + fileContent.substring(0, Math.min(200, fileContent.length())) + "...");

                prompt = buildContentAnalysisPrompt(file, instructions, fileContent);
            } else {
                System.out.println("Using enhanced fallback analysis (text extraction failed)");
                prompt = buildFallbackAnalysisPrompt(file, instructions);
            }

            System.out.println("Sending enhanced prompt to AI...");
            String analysisResult = generateResponse(prompt, "document");
            System.out.println("Enhanced analysis completed");

            return analysisResult;

        } catch (Exception e) {
            System.out.println("Error in enhanced document analysis: " + e.getMessage());
            e.printStackTrace();
            return "I apologize, but I encountered an error while analyzing your document. Please try again with a different file or contact support if the issue persists. Error: " + e.getMessage();
        }
    }

    private String buildContentAnalysisPrompt(MultipartFile file, String instructions, String fileContent) {
        return String.format("""
            You are an expert document analyzer. Please analyze this document and provide detailed feedback based on the user's specific instructions.
            
            ===== USER INSTRUCTIONS =====
            "%s"
            
            ===== DOCUMENT CONTENT =====
            Filename: %s
            Filetype: %s
            Filesize: %d bytes
            
            CONTENT:
            %s
            
            ===== ANALYSIS REQUEST =====
            Please provide a comprehensive analysis with:
            
            1. CONTENT SUMMARY:
               - Main topics and key points covered
               - Overall purpose and primary message
               - Target audience assessment
            
            2. QUALITY ASSESSMENT:
               - Key strengths and effective elements
               - Areas needing improvement
               - Clarity, organization, and structure
            
            3. SPECIFIC FEEDBACK:
               - Direct responses to user instructions: "%s"
               - Actionable recommendations with examples
               - Concrete improvement suggestions
            
            4. PROFESSIONAL RECOMMENDATIONS:
               - Best practices implementation
               - Industry standards alignment
               - Enhancement opportunities
            
            Focus on being specific, constructive, and providing concrete examples. If suggesting changes, show exactly how they should be implemented.
            """,
                instructions != null ? instructions : "Provide general analysis and feedback",
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                fileContent.length() > 12000 ? fileContent.substring(0, 12000) + "\n\n[Content truncated for length...]" : fileContent,
                instructions != null ? instructions : "Provide general analysis and feedback"
        );
    }

    private String buildFallbackAnalysisPrompt(MultipartFile file, String instructions) {
        return String.format("""
            You are an expert document analyzer. Please provide detailed feedback on the document based on the user's instructions.
            
            ===== DOCUMENT INFORMATION =====
            - Filename: %s
            - Filetype: %s
            - Size: %d bytes
            - User Instructions: "%s"
            
            ===== ANALYSIS REQUEST =====
            Since the document content cannot be directly accessed, please provide comprehensive guidance on:
            
            1. DOCUMENT TYPE BEST PRACTICES:
               - Industry standards for %s files
               - Common structures and formats expected
               - Professional presentation requirements
            
            2. CONTENT STRATEGY:
               - Key elements that should be included
               - Optimal information organization
               - Audience engagement techniques
            
            3. QUALITY IMPROVEMENT:
               - Common issues to avoid for this document type
               - Enhancement opportunities
               - Professional presentation standards
            
            4. SPECIFIC RECOMMENDATIONS:
               - Actionable improvement steps
               - Tools and resources for enhancement
               - Best practice examples
            
            5. USER INSTRUCTION FOCUS:
               - Direct advice based on: "%s"
               - Step-by-step implementation guide
               - Expected outcomes and benefits
            
            Provide specific, actionable advice that the user can implement immediately.
            """,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                instructions != null ? instructions : "Provide general document feedback",
                file.getContentType(),
                instructions != null ? instructions : "Provide general document feedback"
        );
    }

    public String summarizeText(String text, String summaryType) {
        try {
            String lengthInstruction = "";
            switch (summaryType) {
                case "brief":
                    lengthInstruction = "Provide a very brief summary (2-3 sentences).";
                    break;
                case "bullet":
                    lengthInstruction = "Provide a summary in bullet points.";
                    break;
                case "detailed":
                    lengthInstruction = "Provide a detailed summary with key points.";
                    break;
                default:
                    lengthInstruction = "Provide a concise summary.";
            }

            String prompt = String.format("""
                Please summarize the following text. %s
                
                Text to summarize:
                %s
                
                Focus on:
                - Main ideas and key points
                - Important details and facts
                - Overall purpose or conclusion
                """, lengthInstruction, text);

            return generateResponse(prompt, "summary");

        } catch (Exception e) {
            System.out.println("Error summarizing text: " + e.getMessage());
            return "Failed to generate summary. Please try again.";
        }
    }

    private String extractTextFromFile(MultipartFile file) {
        try {
            System.out.println("Attempting text extraction for: " + file.getContentType());

            if (file.getContentType().equals("text/plain")) {
                String text = new String(file.getBytes());
                System.out.println("Text file extracted: " + text.length() + " characters");
                return text;
            }

            if (file.getContentType().equals("application/pdf")) {
                try {
                    PDDocument document = PDDocument.load(file.getInputStream());
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    document.close();

                    System.out.println("PDF text extracted: " + text.length() + " characters");
                    if (text.length() > 0) {
                        System.out.println("Sample content: " + text.substring(0, Math.min(200, text.length())) + "...");
                    }

                    return text.trim().isEmpty() ? null : text;
                } catch (Exception e) {
                    System.out.println("PDF extraction error: " + e.getMessage());
                    return null;
                }
            }

            if (file.getContentType().startsWith("image/")) {
                System.out.println("Image file - text extraction not supported yet");
                return null;
            }

            System.out.println("Unsupported file type for text extraction: " + file.getContentType());
            return null;

        } catch (Exception e) {
            System.out.println("Text extraction failed: " + e.getMessage());
            return null;
        }
    }

    private String enhancePrompt(String prompt, String queryType) {
        switch (queryType) {
            case "code":
                return "You are an expert programming assistant. Provide clear, concise code help with explanations. " +
                        "If there are errors, explain what's wrong and how to fix them. " +
                        "If it's a concept, explain it with examples.\n\n" + prompt;

            case "explain":
                return "You are a patient teacher. Explain this concept in simple, easy-to-understand terms. " +
                        "Use analogies and real-world examples. Break down complex ideas into smaller parts.\n\n" + prompt;

            case "quiz":
                return "You are a quiz master. Create clear, fair, and educational questions. " +
                        "Ensure questions test understanding and options are well-distracted.\n\n" + prompt;

            case "document":
                return "You are a professional document analyst. Provide thorough, constructive, and actionable feedback. " +
                        "Be specific about strengths and areas for improvement with concrete examples.\n\n" + prompt;

            case "summary":
                return "You are a summarization expert. Extract key information and present it clearly. " +
                        "Maintain the original meaning while being concise.\n\n" + prompt;

            default:
                return "You are a helpful AI study buddy. Provide accurate, educational, and engaging responses. " +
                        "Be clear and supportive in your explanations.\n\n" + prompt;
        }
    }

    private double getTemperature(String queryType) {
        switch (queryType) {
            case "code":
            case "quiz":
                return 0.2;
            case "creative":
                return 0.8;
            default:
                return 0.7;
        }
    }

    private int getMaxTokens(String queryType) {
        switch (queryType) {
            case "summary":
            case "document":
                return 2048;
            case "quiz":
                return 4096;
            default:
                return 1024;
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return "Empty response from API. Please try again.";
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return text.trim();
                }
            }

            if (rootNode.has("error")) {
                String errorMsg = rootNode.path("error").path("message").asText();
                return "API Error: " + errorMsg;
            }

            return "Could not extract text from response. Raw response: " + jsonResponse;

        } catch (Exception e) {
            System.out.println("Error parsing response: " + e.getMessage());

            return extractTextSimple(jsonResponse);
        }
    }

    private String extractTextSimple(String jsonResponse) {
        try {
            // Simple text extraction as fallback
            int textIndex = jsonResponse.indexOf("\"text\"");
            if (textIndex == -1) {
                return "No 'text' field found in response. Please check the API response format.";
            }

            int colonIndex = jsonResponse.indexOf(":", textIndex);
            int firstQuote = jsonResponse.indexOf("\"", colonIndex);
            int secondQuote = jsonResponse.indexOf("\"", firstQuote + 1);

            if (firstQuote == -1 || secondQuote == -1) {
                return "Could not find text content in response.";
            }

            String extractedText = jsonResponse.substring(firstQuote + 1, secondQuote);

            return extractedText
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t")
                    .trim();

        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage() + "\n\nRaw response for debugging:\n" + jsonResponse;
        }
    }

    private String getErrorMessage(Exception e, String queryType) {
        String baseError = "I apologize, but I'm having trouble connecting to the AI service. ";

        switch (queryType) {
            case "quiz":
                return baseError + "Please try generating the quiz again in a moment.";
            case "document":
                return baseError + "Please try uploading your document again.";
            case "code":
                return baseError + "Please try your programming question again.";
            default:
                return baseError + "Please try your question again in a moment. Error: " + e.getMessage();
        }
    }
}