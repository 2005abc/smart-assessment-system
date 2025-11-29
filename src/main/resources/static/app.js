document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const authContainer = document.getElementById('auth-container');
    const mainApp = document.getElementById('main-app');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const switchButton = document.getElementById('switch-button');
    const switchText = document.getElementById('switch-text');
    const logoutBtn = document.getElementById('logout-btn');
    const userNameSpan = document.getElementById('user-name');
    const chatMessages = document.getElementById('chat-messages');
    const promptInput = document.getElementById('prompt-input');
    const sendButton = document.getElementById('send-button');
    const toolButtons = document.querySelectorAll('.tool-button');
    const clearButton = document.getElementById('clear-chat');
    const charCounter = document.getElementById('char-counter');
    const copyChatBtn = document.getElementById('copy-chat');
    const currentToolText = document.getElementById('current-tool-text');

    // Quiz Elements
    const quizContent = document.getElementById('quiz-content');
    const quizQuestions = document.getElementById('quiz-questions');
    const quizResults = document.getElementById('quiz-results');
    const generateQuizBtn = document.getElementById('generate-quiz');
    const quizTopic = document.getElementById('quiz-topic');
    const quizCount = document.getElementById('quiz-count');
    const submitQuizBtn = document.getElementById('submit-quiz');
    const restartQuizBtn = document.getElementById('restart-quiz');
    const quizControls = document.getElementById('quiz-controls');

    // Document Analyzer Elements
    const documentContent = document.getElementById('document-content');
    const uploadArea = document.getElementById('upload-area');
    const fileInput = document.getElementById('file-input');
    const browseBtn = document.getElementById('browse-btn');
    const filePreview = document.getElementById('file-preview');
    const analysisInstructions = document.getElementById('analysis-instructions');
    const analyzeDocumentBtn = document.getElementById('analyze-document');
    const analysisResults = document.getElementById('analysis-results');

    // Input Areas
    const generalInput = document.getElementById('general-input');
    const inputHintText = document.getElementById('input-hint-text');

    // Backend API Base URL
    const BASE_URL = 'http://localhost:8080/api';

    // State
    let currentUser = null;
    let currentTool = 'home';
    let isStreaming = false;
    let currentQuiz = null;
    let selectedFile = null;

    // Initialize
    initializeApp();

    function initializeApp() {
        setupEventListeners();
        checkExistingSession();
    }

    function setupEventListeners() {
        // Auth events
        loginForm.addEventListener('submit', handleLogin);
        registerForm.addEventListener('submit', handleRegister);
        switchButton.addEventListener('click', toggleAuthForms);
        logoutBtn.addEventListener('click', handleLogout);

        // Chat events
        promptInput.addEventListener('keypress', handleKeyPress);
        promptInput.addEventListener('input', handleInputChange);
        sendButton.addEventListener('click', sendMessage);

        // Tool buttons
        toolButtons.forEach(button => {
            button.addEventListener('click', () => handleToolChange(button));
        });

        // Clear chat
        clearButton.addEventListener('click', clearChat);

        // Copy chat
        copyChatBtn.addEventListener('click', copyChatToClipboard);

        // Quiz events
        generateQuizBtn.addEventListener('click', generateQuiz);
        submitQuizBtn.addEventListener('click', submitQuiz);
        restartQuizBtn.addEventListener('click', restartQuiz);

        // Document analyzer events
        browseBtn.addEventListener('click', () => fileInput.click());
        fileInput.addEventListener('change', handleFileSelect);
        uploadArea.addEventListener('dragover', handleDragOver);
        uploadArea.addEventListener('drop', handleFileDrop);
        analyzeDocumentBtn.addEventListener('click', analyzeDocument);

        // Tool cards on home page
        document.addEventListener('click', (e) => {
            if (e.target.closest('.tool-card')) {
                const toolCard = e.target.closest('.tool-card');
                const toolType = toolCard.getAttribute('data-type');
                const toolButton = document.querySelector(`.tool-button[data-type="${toolType}"]`);
                if (toolButton) {
                    handleToolChange(toolButton);
                }
            }
        });

        // Quick action buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.quick-btn')) {
                const quickBtn = e.target.closest('.quick-btn');
                const prompt = quickBtn.getAttribute('data-prompt');
                promptInput.value = prompt;
                promptInput.focus();
                handleInputChange();
            }
        });
    }

    function checkExistingSession() {
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            currentUser = JSON.parse(savedUser);
            showChatApp();
        }
    }

    // Auth Functions
    async function handleLogin(e) {
        e.preventDefault();

        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            showNotification('Please enter email and password', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

            if (data.success) {
                currentUser = data.user;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));
                showChatApp();
                showNotification('Login successful!', 'success');
            } else {
                showNotification(data.message, 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            showNotification('Login failed. Please try again.', 'error');
        }
    }

    async function handleRegister(e) {
        e.preventDefault();

        const name = document.getElementById('register-name').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;

        if (!name || !email || !password) {
            showNotification('Please fill all fields', 'error');
            return;
        }

        if (password.length < 6) {
            showNotification('Password must be at least 6 characters', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: name,
                    email: email,
                    password: password
                })
            });

            const data = await response.json();

            if (data.success) {
                showNotification('Registration successful! Please login.', 'success');
                toggleAuthForms();
                // Pre-fill login form
                document.getElementById('login-email').value = email;
                document.getElementById('login-password').value = password;
            } else {
                showNotification(data.message, 'error');
            }
        } catch (error) {
            console.error('Registration error:', error);
            showNotification('Registration failed. Please try again.', 'error');
        }
    }

    function toggleAuthForms() {
        const isLogin = loginForm.style.display !== 'none';

        if (isLogin) {
            // Switch to register
            loginForm.style.display = 'none';
            registerForm.style.display = 'block';
            switchText.textContent = 'Already have an account?';
            switchButton.textContent = 'Sign In';
        } else {
            // Switch to login
            registerForm.style.display = 'none';
            loginForm.style.display = 'block';
            switchText.textContent = 'Don\'t have an account?';
            switchButton.textContent = 'Sign Up';
        }
    }

    function handleLogout() {
        currentUser = null;
        localStorage.removeItem('currentUser');
        showAuth();
        showNotification('Logged out successfully', 'info');
    }

    function showAuth() {
        authContainer.style.display = 'flex';
        mainApp.style.display = 'none';
        // Reset forms
        loginForm.reset();
        registerForm.reset();
    }

    function showChatApp() {
        authContainer.style.display = 'none';
        mainApp.style.display = 'block';
        userNameSpan.textContent = currentUser.name;

        // Initialize with home view
        showHomeView();
    }

    // Tool Management
    function handleToolChange(button) {
        if (isStreaming) return;

        toolButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        currentTool = button.getAttribute('data-type');

        const toolTitle = button.querySelector('.tool-title').textContent;
        currentToolText.textContent = `${toolTitle} Mode`;

        const toolIcon = button.querySelector('.tool-icon i').className;
        document.querySelector('.current-tool i').className = toolIcon;

        // Show appropriate content for 4 tools only
        switch (currentTool) {
            case 'home':
                showHomeView();
                break;
            case 'quiz':
                showQuizView();
                break;
            case 'document':
                showDocumentView();
                break;
            default: // general, code
                showChatView();
                break;
        }

        updateInputHint();
    }


    function showHomeView() {
        hideAllViews();
        document.getElementById('home-content').style.display = 'block';
        generalInput.style.display = 'none';
        quizControls.style.display = 'none';
    }

    function showChatView() {
        hideAllViews();
        generalInput.style.display = 'flex';
        quizControls.style.display = 'none';
        updateInputPlaceholder();
    }

    function showQuizView() {
        hideAllViews();
        quizContent.style.display = 'block';
        generalInput.style.display = 'none';
        quizControls.style.display = 'none';

        // Reset quiz state
        resetQuiz();
    }

    function showDocumentView() {
        hideAllViews();
        documentContent.style.display = 'block';
        generalInput.style.display = 'none';
        quizControls.style.display = 'none';
    }

    function hideAllViews() {

        document.querySelectorAll('#home-content, #quiz-content, #document-content').forEach(el => {
            el.style.display = 'none';
        });


        quizQuestions.style.display = 'none';
        quizResults.style.display = 'none';
        analysisResults.style.display = 'none';
    }

  function updateInputPlaceholder() {
      const placeholders = {
          'general': 'Ask me anything... (Press Enter to send)',
          'code': 'Ask programming questions or paste your code...',
          'quiz': 'Enter a topic to create quiz questions...'
      };

      promptInput.placeholder = placeholders[currentTool] || 'Ask me anything...';
  }


   function updateInputHint() {
       const hints = {
           'general': 'Pro Tip: Use specific questions for better answers',
           'code': 'Pro Tip: Include code examples and error messages',
           'quiz': 'Pro Tip: Be specific about the topic and difficulty level',
           'document': 'Pro Tip: Provide clear instructions for document analysis'
       };

       inputHintText.textContent = hints[currentTool] || 'Pro Tip: Use specific questions for better answers';
   }


async function generateQuiz() {
    const topic = quizTopic.value.trim();
    const count = parseInt(quizCount.value);

    if (!topic) {
        showNotification('Please enter a quiz topic', 'error');
        return;
    }

    generateQuizBtn.disabled = true;
    generateQuizBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating...';

    try {
        const response = await fetch(`${BASE_URL}/generate-quiz`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                topic: topic,
                questionCount: count,
                userEmail: currentUser.email,
                difficulty: 'medium'
            })
        });

        const data = await response.json();
        console.log('🔍 BACKEND RESPONSE:', data); // Debug log

        if (data.success) {
            // ✅ USE REAL BACKEND DATA
            currentQuiz = parseRealQuizResponse(data.quiz, topic, count);
            displayQuizQuestions(currentQuiz);
            showNotification(`Real AI Quiz generated on "${topic}"`, 'success');
        } else {
            throw new Error(data.message || 'Failed to generate quiz');
        }

    } catch (error) {
        console.error('Quiz generation error:', error);
        showNotification('Failed to generate quiz: ' + error.message, 'error');
    } finally {
        generateQuizBtn.disabled = false;
        generateQuizBtn.innerHTML = '<i class="fas fa-play"></i> Generate Quiz';
    }
}

   function parseRealQuizResponse(quizData, topic, count) {
       console.log('🔍 PARSING QUIZ DATA:', quizData);


       if (typeof quizData.questions === 'string') {
           return parseQuizFromText(quizData.questions, topic, count);
       }


       return {
           topic: topic,
           questions: quizData.questions || [],
           totalQuestions: count
       };
   }

   function parseQuizFromText(text, topic, count) {
       const questions = [];
       const lines = text.split('\n').filter(line => line.trim());

       let currentQuestion = null;

       for (let i = 0; i < lines.length; i++) {
           const line = lines[i].trim();

           if (line.match(/^(\d+\.|\*\*Question \d+:\*\*)/) ||
               (line.includes('?') && !currentQuestion)) {

               if (currentQuestion) {
                   questions.push(currentQuestion);
               }

               currentQuestion = {
                   id: questions.length + 1,
                   question: line.replace(/^\d+\.\s*/, '')
                                .replace(/\*\*Question \d+:\*\*\s*/, '')
                                .replace(/\*\*/g, '')
                                .trim(),
                   options: [],
                   correctAnswer: -1,
                   type: 'multiple choice'
               };
           }

           else if (line.match(/^[ABCD]\)/) && currentQuestion) {
               const optionText = line.replace(/^[ABCD]\)\s*/, '').trim();
               currentQuestion.options.push(optionText);
           }

           else if (line.toLowerCase().includes('correct:') && currentQuestion) {
               const correctChar = line.split(':')[1].trim().toUpperCase();
               currentQuestion.correctAnswer = 'ABCD'.indexOf(correctChar);
           }

           else if (currentQuestion && !line.match(/^[ABCD]\)/) &&
                    !line.toLowerCase().includes('correct:') &&
                    currentQuestion.options.length === 0) {
               currentQuestion.question += ' ' + line.replace(/\*\*/g, '').trim();
           }
       }

       if (currentQuestion) {
           questions.push(currentQuestion);
       }

       questions.forEach(q => {
           while (q.options.length < 4) {
               q.options.push(`Option ${String.fromCharCode(65 + q.options.length)}`);
           }
           if (q.correctAnswer === -1) {
               q.correctAnswer = 0;
           }
       });

       console.log('✅ PARSED QUESTIONS:', questions);

       return {
           topic: topic,
           questions: questions.slice(0, count),
           totalQuestions: Math.min(questions.length, count)
       };
   }
    function createMockQuiz(topic, count) {
        const questions = [];
        const questionTypes = [
            'multiple choice',
            'true/false',
            'fill in the blank'
        ];

        for (let i = 0; i < count; i++) {
            const type = questionTypes[Math.floor(Math.random() * questionTypes.length)];
            questions.push({
                id: i + 1,
                question: `Sample ${type} question about ${topic} #${i + 1}?`,
                options: [
                    `Option A for question ${i + 1}`,
                    `Option B for question ${i + 1}`,
                    `Option C for question ${i + 1}`,
                    `Option D for question ${i + 1}`
                ],
                correctAnswer: Math.floor(Math.random() * 4),
                type: type
            });
        }

        return {
            topic: topic,
            questions: questions,
            totalQuestions: count
        };
    }

    function displayQuizQuestions(quiz) {
        quizQuestions.innerHTML = '';

        quiz.questions.forEach((q, index) => {
            const questionDiv = document.createElement('div');
            questionDiv.className = 'quiz-question';
            questionDiv.innerHTML = `
                <div class="question-text">${index + 1}. ${q.question}</div>
                <div class="quiz-options">
                    ${q.options.map((option, optIndex) => `
                        <label class="quiz-option">
                            <input type="radio" name="question-${index}" value="${optIndex}">
                            <span>${String.fromCharCode(65 + optIndex)}) ${option}</span>
                        </label>
                    `).join('')}
                </div>
            `;
            quizQuestions.appendChild(questionDiv);
        });

        quizQuestions.style.display = 'block';
        quizControls.style.display = 'flex';

        // Add event listeners to radio buttons
        document.querySelectorAll('.quiz-option input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', function() {
                const label = this.closest('.quiz-option');
                document.querySelectorAll(`.quiz-option[name="question-${this.name}"]`).forEach(opt => {
                    opt.classList.remove('selected');
                });
                label.classList.add('selected');
            });
        });


        quizQuestions.scrollIntoView({ behavior: 'smooth' });
    }

    function submitQuiz() {
        if (!currentQuiz) return;

        let score = 0;
        const results = [];

        currentQuiz.questions.forEach((q, index) => {
            const selectedOption = document.querySelector(`input[name="question-${index}"]:checked`);
            const isCorrect = selectedOption && parseInt(selectedOption.value) === q.correctAnswer;

            if (isCorrect) score++;

            results.push({
                question: q.question,
                selected: selectedOption ? parseInt(selectedOption.value) : null,
                correct: q.correctAnswer,
                isCorrect: isCorrect,
                options: q.options
            });
        });

        displayQuizResults(score, results);
    }

    function displayQuizResults(score, results) {
        const totalQuestions = currentQuiz.questions.length;
        const percentage = Math.round((score / totalQuestions) * 100);

        let resultHTML = `
            <div class="result-score">${score}/${totalQuestions}</div>
            <div class="result-text">You scored ${percentage}% on ${currentQuiz.topic}</div>
            <div class="result-details">
        `;

        results.forEach((result, index) => {
            const userAnswer = result.selected !== null ?
                String.fromCharCode(65 + result.selected) : 'Not answered';
            const correctAnswer = String.fromCharCode(65 + result.correct);

            resultHTML += `
                <div class="result-item ${result.isCorrect ? 'correct' : 'incorrect'}">
                    <strong>Q${index + 1}:</strong> ${result.question}<br>
                    <small>Your answer: ${userAnswer} - ${result.options[result.selected] || 'Not answered'}</small><br>
                    <small>Correct answer: ${correctAnswer} - ${result.options[result.correct]}</small>
                </div>
            `;
        });

        resultHTML += '</div>';
        quizResults.innerHTML = resultHTML;

        quizResults.style.display = 'block';
        quizQuestions.style.display = 'none';
        showNotification('Quiz submitted! Check your results.', 'success');

        // Scroll to results
        quizResults.scrollIntoView({ behavior: 'smooth' });
    }

    function restartQuiz() {
        resetQuiz();
        quizTopic.value = '';
        showNotification('Quiz reset. Enter a new topic to start.', 'info');
    }

    function resetQuiz() {
        quizQuestions.style.display = 'none';
        quizResults.style.display = 'none';
        quizControls.style.display = 'none';
        currentQuiz = null;
    }


    function handleFileSelect(e) {
        const file = e.target.files[0];
        if (file) {
            processSelectedFile(file);
        }
    }

    function handleDragOver(e) {
        e.preventDefault();
        uploadArea.style.borderColor = '#4c8bf5';
        uploadArea.style.background = 'rgba(76, 139, 245, 0.1)';
    }

    function handleFileDrop(e) {
        e.preventDefault();
        uploadArea.style.borderColor = '#444';
        uploadArea.style.background = 'transparent';

        const file = e.dataTransfer.files[0];
        if (file) {
            processSelectedFile(file);
        }
    }

    function processSelectedFile(file) {
        // Validate file type
        const validTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'text/plain', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];

        if (!validTypes.includes(file.type)) {
            showNotification('Please select a PDF, image, or text file', 'error');
            return;
        }


        if (file.size > 5 * 1024 * 1024) {
            showNotification('File size must be less than 5MB', 'error');
            return;
        }

        selectedFile = file;


        filePreview.innerHTML = `
            <div class="file-info">
                <div class="file-icon">
                    <i class="fas fa-file"></i>
                </div>
                <div class="file-details">
                    <h4>${file.name}</h4>
                    <p>${(file.size / 1024).toFixed(2)} KB • ${file.type}</p>
                </div>
                <button class="remove-file" onclick="removeSelectedFile()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        filePreview.style.display = 'block';

        analyzeDocumentBtn.disabled = false;
        showNotification('File selected successfully', 'success');
    }

    function removeSelectedFile() {
        selectedFile = null;
        filePreview.style.display = 'none';
        fileInput.value = '';
        analyzeDocumentBtn.disabled = true;
    }

   async function analyzeDocument() {
       if (!selectedFile) {
           showNotification('Please select a file first', 'error');
           return;
       }

       analyzeDocumentBtn.disabled = true;
       analyzeDocumentBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Analyzing...';

       try {
           const formData = new FormData();
           formData.append('file', selectedFile);
           formData.append('userEmail', currentUser.email);

           const instructions = analysisInstructions.value.trim();
           if (instructions) {
               formData.append('instructions', instructions);
           }

           console.log('Sending document for REAL AI analysis...');

           const response = await fetch(`${BASE_URL}/analyze-document`, {
               method: 'POST',
               body: formData
           });

           const data = await response.json();
           console.log('Backend Response:', data);

           if (data.success) {

               analysisResults.innerHTML = `
                   <div class="analysis-result">
                       <h4>AI Analysis Results - ${data.fileName}</h4>
                       <div class="file-meta">
                           <small>File Type: ${data.fileType} • Size: ${(data.fileSize / 1024).toFixed(2)} KB</small>
                       </div>
                       <div class="result-content">
                           ${formatResponse(data.analysis)}
                       </div>
                   </div>
               `;
               analysisResults.style.display = 'block';
               showNotification('AI Document analysis complete!', 'success');
           } else {
               throw new Error(data.message || 'Analysis failed');
           }

       } catch (error) {
           console.error('Analysis error:', error);

           showNotification('Analysis failed: ' + error.message, 'error');

           // Show error in results
           analysisResults.innerHTML = `
               <div class="analysis-result">
                   <h4>Analysis Failed</h4>
                   <div class="result-content">
                       <p>Sorry, we couldn't analyze your document at this time.</p>
                       <p><strong>Error:</strong> ${error.message}</p>
                       <p>Please try again later or contact support.</p>
                   </div>
               </div>
           `;
           analysisResults.style.display = 'block';
       } finally {
           analyzeDocumentBtn.disabled = false;
           analyzeDocumentBtn.innerHTML = '<i class="fas fa-search"></i> Analyze Document';
       }
   }


    function handleKeyPress(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    }

    function handleInputChange() {
        const length = promptInput.value.length;
        charCounter.textContent = length;

        promptInput.style.height = 'auto';
        promptInput.style.height = Math.min(promptInput.scrollHeight, 120) + 'px';
    }

    async function sendMessage() {
        if (isStreaming || !currentUser || currentTool === 'home') return;

        const userPrompt = promptInput.value.trim();
        if (!userPrompt) {
            showNotification('Please enter a message', 'error');
            return;
        }

        const originalButtonText = sendButton.innerHTML;
        promptInput.value = '';
        promptInput.style.height = 'auto';
        charCounter.textContent = '0';
        sendButton.disabled = true;
        sendButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        isStreaming = true;


        const welcomeMsg = document.querySelector('.welcome-message');
        if (welcomeMsg) {
            welcomeMsg.remove();
        }

        appendMessage(userPrompt, 'user');
        const botBubble = appendMessage('<div class="typing-indicator"><span></span><span></span><span></span></div>', 'bot');

        try {
            const response = await fetch(`${BASE_URL}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: userPrompt,
                    userEmail: currentUser.email,
                    queryType: currentTool
                })
            });

            const data = await response.json();

            if (data.success) {
                botBubble.innerHTML = formatResponse(data.response);
                showNotification('Response received', 'success');
            } else {
                throw new Error(data.message);
            }

        } catch (error) {
            console.error('API Error:', error);
            // Fallback to mock response
            const mockResponse = generateMockResponse(userPrompt, currentTool);
            botBubble.innerHTML = formatResponse(mockResponse);
            showNotification('Response generated (offline mode)', 'info');
        } finally {
            sendButton.disabled = false;
            sendButton.innerHTML = '<i class="fas fa-paper-plane"></i>';
            isStreaming = false;
            scrollToBottom();
        }
    }

    function generateMockResponse(prompt, toolType) {
        const responses = {
            general: `I understand you're asking about: "${prompt}". This is a mock response since the AI service is currently unavailable. In a real scenario, I would provide a detailed, helpful answer to your question.`,

            code: `Regarding your code question about: "${prompt}". I'd be happy to help you with this programming concept! Since I'm in offline mode, I recommend checking official documentation or trying the code in your development environment.`,

            explain: `You want me to explain: "${prompt}". This is an important concept! While I'm currently in offline mode, I suggest looking for reliable educational resources that can provide the detailed explanation you're looking for.`,

            quiz: `You mentioned: "${prompt}". For quiz-related questions, I can help generate practice questions or explain quiz concepts. Please try the Quiz Master tool for interactive quiz generation.`,

            summarize: `You asked me to summarize: "${prompt}". Summarization is one of my key features! In online mode, I would provide a concise summary highlighting the main points and key information.`
        };

        return responses[toolType] || responses.general;
    }

    function appendMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';

        if (sender === 'bot') {
            bubble.innerHTML = text;
        } else {
            bubble.textContent = text;
        }

        messageDiv.appendChild(bubble);
        chatMessages.appendChild(messageDiv);

        scrollToBottom();
        return bubble;
    }

    function scrollToBottom() {
        setTimeout(() => {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }, 100);
    }

    function formatResponse(text) {
        if (!text) return 'I received your message but have nothing to say.';

        let formatted = text;


        formatted = formatted.replace(/```(\w+)?\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>');

        formatted = formatted.replace(/`([^`]+)`/g, '<code>$1</code>');

        formatted = formatted.replace(/\n/g, '<br>');

        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');

        return formatted;
    }

    function clearChat() {
        if (isStreaming) return;

        if (currentTool === 'home') {
            showHomeView();
        } else {
            chatMessages.innerHTML = '';
            if (currentTool === 'general') {
                appendMessage('Hello! I\'m your AI Study Buddy. How can I help you today?', 'bot');
            }
        }

        showNotification('Chat cleared', 'info');
    }

    function copyChatToClipboard() {
        const messages = chatMessages.querySelectorAll('.message');
        let chatText = '';

        messages.forEach(message => {
            const isUser = message.classList.contains('user-message');
            const sender = isUser ? 'You' : 'AI Study Buddy';
            const bubble = message.querySelector('.message-bubble');
            let text = bubble.textContent || bubble.innerText;

            chatText += `${sender}: ${text}\n\n`;
        });

        if (!chatText.trim()) {
            showNotification('No messages to copy', 'warning');
            return;
        }

        navigator.clipboard.writeText(chatText).then(() => {
            showNotification('Chat copied to clipboard!', 'success');
        }).catch(err => {
            console.error('Copy failed:', err);
            showNotification('Failed to copy chat', 'error');
        });
    }

    function showNotification(message, type = 'info') {

        document.querySelectorAll('.notification').forEach(notif => notif.remove());

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${getNotificationIcon(type)}"></i>
            <span>${message}</span>
        `;

        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${getNotificationColor(type)};
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            z-index: 1000;
            display: flex;
            align-items: center;
            gap: 10px;
            animation: slideIn 0.3s ease;
        `;

        document.body.appendChild(notification);


        if (!document.querySelector('#notification-styles')) {
            const style = document.createElement('style');
            style.id = 'notification-styles';
            style.textContent = `
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes slideOut {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        }

        setTimeout(() => {
            if (notification.parentNode) {
                notification.style.animation = 'slideOut 0.3s ease';
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.parentNode.removeChild(notification);
                    }
                }, 300);
            }
        }, 3000);
    }

    function getNotificationIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    function getNotificationColor(type) {
        const colors = {
            success: '#4CAF50',
            error: '#f44336',
            warning: '#ff9800',
            info: '#2196F3'
        };
        return colors[type] || '#2196F3';
    }

    window.removeSelectedFile = removeSelectedFile;
});