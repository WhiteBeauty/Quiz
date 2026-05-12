
(function () {
    'use strict';

    // ---- State ----
    let totalQuestions = 0;
    let currentQuestionIndex = 0;

    const data = document.getElementById('game-data').dataset;
    const token = data.token;
    const roomCode = data.roomCode;
    const userId = parseInt(data.userId);
    const isHost = data.isHost === 'true';

    const baseUrl = window.location.origin;

    let stompClient = null;
    let currentQuestion = null;
    let timerInterval = null;
    let questionStartTime = null;
    let myScore = 0;
    let answeredThisRound = false;

    // ---- Screens ----
    const screens = {
        waiting: document.getElementById('screen-waiting'),
        game: document.getElementById('screen-game'),
        leaderboard: document.getElementById('screen-leaderboard'),
        finished: document.getElementById('screen-finished'),
    };

    function showScreen(name) {
        Object.values(screens).forEach(s => s && (s.style.display = 'none'));
        if (screens[name]) screens[name].style.display = 'flex';
    }

    // Show appropriate screen on load
    const initialStatus = data.status;
    if (initialStatus === 'IN_PROGRESS') showScreen('game');
    else if (initialStatus === 'FINISHED') showScreen('finished');
    else showScreen('waiting');

    // ---- WebSocket ----
    function connect() {
        const socket = new SockJS(baseUrl + '/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function () {
            stompClient.subscribe('/topic/game/' + roomCode, handleMessage);
            // Подписка на персональные уведомления
            stompClient.subscribe('/queue/game/' + roomCode + '/user/' + userId, handleMessage);
        }, function (err) {
            console.warn('WS error, retrying in 3s...', err);
            setTimeout(connect, 3000);
        });
    }

    function handleMessage(frame) {
        let msg;
        try { msg = JSON.parse(frame.body); } catch (e) { return; }

        switch (msg.type) {
            case 'PLAYER_JOINED':
                addPlayerChip(msg.payload);
                break;
            case 'GAME_STARTED':
                showScreen('game');
                break;
            case 'NEXT_QUESTION':
                currentQuestionIndex++;
                const qData = msg.payload.question || msg.payload;
                renderQuestion(qData, currentQuestionIndex, totalQuestions);
                break;
            case 'LEADERBOARD_UPDATE':
                renderMidLeaderboard(msg.payload);
                break;
            case 'GAME_FINISHED':
                renderFinalLeaderboard(msg.payload);
                showScreen('finished');
                clearInterval(timerInterval);
                break;
            case 'ANSWER_RESULT':
                handleAnswerResult(msg.payload);
                break;
        }
    }

    // Player chips
    function addPlayerChip(username) {
        const grid = document.getElementById('players-grid');
        if (!grid) return;
        const existing = Array.from(grid.querySelectorAll('.player-chip'))
            .find(c => c.textContent === username);
        if (!existing) {
            const chip = document.createElement('div');
            chip.className = 'player-chip';
            chip.textContent = username;
            grid.appendChild(chip);
        }
    }

    //Host controls
    window.startGame = function () {
        apiPost('/api/rooms/' + roomCode + '/start')
            .then(() => showScreen('game'))
            .catch(err => alert('Ошибка запуска: ' + err));
    };

    window.nextQuestion = function () {
        apiPost('/api/rooms/' + roomCode + '/next')
            .then(room => {
                answeredThisRound = false;
                showScreen('game');
                fetchCurrentQuestion(room.currentQuestionIndex);
            });
    };

    window.finishGame = function() {
        apiPost('/api/rooms/' + roomCode + '/finish')
            .then(() => {
                showScreen('finished');
            })
            .catch(err => console.error('Finish error:', err));
    };

    window.handleNextOrFinish = function() {
        if (currentQuestionIndex >= totalQuestions - 1) {
            finishGame();
        } else {
            nextQuestion();
        }
    };

    //Fetch current question from quiz
    function fetchCurrentQuestion(questionIndex) {
        apiGet('/api/rooms/' + roomCode)
            .then(room => {
                const quizId = room.quizId;
                return apiGetQuiz('/api/quizzes/' + quizId);
            })
            .then(quiz => {
                if (quiz && quiz.questions && quiz.questions[questionIndex]) {
                    renderQuestion(quiz.questions[questionIndex], questionIndex, quiz.questions.length);
                }
            })
            .catch(err => console.error('fetchQuestion error', err));
    }

    // Render question
    function renderQuestion(question, index, total) {
        if (!question) {
            console.error('Question payload is empty');
            return;
        }

        currentQuestion = question;
        answeredThisRound = false;
        questionStartTime = Date.now();

        if (total) totalQuestions = total;
        currentQuestionIndex = index;

        document.getElementById('q-current').textContent = index + 1;
        document.getElementById('q-total').textContent = totalQuestions;
        document.getElementById('question-text').textContent = question.text || 'Вопрос без текста';

        const letters = ['A', 'B', 'C', 'D', 'E', 'F'];
        const grid = document.getElementById('answers-grid');
        grid.innerHTML = '';

        const options = question.answerOptions || question.options || [];
        options.forEach((opt, i) => {
            const btn = document.createElement('button');
            btn.className = 'answer-btn opt-' + ['blue','red','yellow','green'][i % 4];
            btn.innerHTML = `<span class="opt-letter">${letters[i]}</span>${opt.text || ''}`;
            btn.onclick = () => submitAnswer(opt.id, btn);
            grid.appendChild(btn);
        });

        startTimer(question.timeLimitSeconds || question.timeLimit || 30);
        updateHostButton();
    }

    function updateHostButton() {
        const btn = document.getElementById('btn-action');
        const controls = document.getElementById('host-controls');
        if (!btn || !controls) return;

        // Кнопка видна только хосту
        if (!isHost) {
            controls.style.display = 'none';
            return;
        }
        controls.style.display = 'block';

        if (currentQuestionIndex >= totalQuestions - 1) {
            btn.textContent = '🏁 Завершить квиз';
            btn.className = 'btn btn-danger btn-lg';
            btn.onclick = finishGame;
        } else {
            btn.textContent = 'Следующий вопрос ▶';
            btn.className = 'btn btn-primary btn-lg';
            btn.onclick = handleNextOrFinish;
        }
    }

    // Timer
    function startTimer(seconds) {
        clearInterval(timerInterval);
        let remaining = seconds;
        const bar = document.getElementById('timer-bar');
        const val = document.getElementById('timer-value');

        bar.style.width = '100%';
        bar.classList.remove('danger');
        val.textContent = remaining;

        timerInterval = setInterval(() => {
            remaining--;
            val.textContent = remaining;
            const pct = (remaining / seconds) * 100;
            bar.style.width = pct + '%';

            if (remaining <= 5) {
                bar.classList.add('danger');
                val.style.color = 'var(--accent-2)';
            }

            if (remaining <= 0) {
                clearInterval(timerInterval);
                lockAnswers();
            }
        }, 1000);
    }

    function lockAnswers() {
        document.querySelectorAll('.answer-btn').forEach(btn => {
            btn.disabled = true;
        });
    }

    // Submit answer
    function submitAnswer(optionId, btn) {
        if (answeredThisRound) return;
        answeredThisRound = true;
        clearInterval(timerInterval);

        const responseTimeMs = Date.now() - questionStartTime;
        btn.classList.add('selected');
        lockAnswers();

        apiPost('/api/rooms/' + roomCode + '/answers', {
            questionId: currentQuestion.id,
            selectedOptionId: optionId,
            responseTimeMs: responseTimeMs,
        }).catch(err => console.warn('Submit answer error:', err));
    }

    function handleAnswerResult(payload) {
        const [correct, points] = Array.isArray(payload) ? payload : [false, 0];
        if (correct) {
            myScore += points;
            document.getElementById('my-score').textContent = myScore;

            const selected = document.querySelector('.answer-btn.selected');
            if (selected) selected.classList.replace('selected', 'correct');

            showToast('+' + points + ' очков!', 'success');
        } else {
            const selected = document.querySelector('.answer-btn.selected');
            if (selected) selected.classList.add('wrong');
            showToast('Неверно', 'error');
        }
    }

    // Leaderboards
    function renderMidLeaderboard(entries) {
        const container = document.getElementById('mid-leaderboard');
        renderLeaderboardEntries(container, entries);
        showScreen('leaderboard');
    }

    function renderFinalLeaderboard(entries) {
        const container = document.getElementById('final-leaderboard');
        renderLeaderboardEntries(container, entries);
    }

    function renderLeaderboardEntries(container, entries) {
        if (!container || !entries) return;
        const medals = ['🥇', '', '🥉'];
        container.innerHTML = entries.map((e, i) => `
            <div class="leaderboard-row ${i === 0 ? 'lb-first' : ''}">
                <span class="lb-rank">${i < 3 ? medals[i] : e.rank}</span>
                <span class="lb-name">${e.username}</span>
                <span class="lb-score">${e.score}</span>
            </div>
        `).join('');
    }

    // Toast notifications
    function showToast(message, type) {
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed; bottom: 2rem; right: 2rem;
            padding: 0.75rem 1.25rem;
            border-radius: 10px;
            font-weight: 700;
            font-size: 1rem;
            z-index: 999;
            animation: slideIn 0.3s ease;
            background: ${type === 'success' ? 'rgba(62,207,176,0.15)' : 'rgba(255,95,64,0.15)'};
            border: 1px solid ${type === 'success' ? 'rgba(62,207,176,0.4)' : 'rgba(255,95,64,0.4)'};
            color: ${type === 'success' ? '#3ecfb0' : '#ff5f40'};
        `;
        toast.textContent = message;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 2500);
    }

    const style = document.createElement('style');
    style.textContent = '@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }';
    document.head.appendChild(style);

    //  API helpers
    function apiPost(path, body) {
        return fetch(baseUrl + path, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token,
            },
            body: body ? JSON.stringify(body) : undefined,
        }).then(res => res.ok ? res.json().catch(() => ({})) : Promise.reject('HTTP ' + res.status));
    }

    function apiGet(path) {
        return fetch(baseUrl + path, {
            headers: { 'Authorization': 'Bearer ' + token },
        }).then(res => res.ok ? res.json() : Promise.reject('HTTP ' + res.status));
    }

    function apiGetQuiz(path) {
        return fetch(baseUrl + path, {
            headers: { 'Authorization': 'Bearer ' + token },
        }).then(res => res.ok ? res.json() : Promise.reject('HTTP ' + res.status));
    }

    //  Init
    connect();

    // Poll for players while waiting
    if (initialStatus === 'WAITING') {
        setInterval(() => {
            apiGet('/api/rooms/' + roomCode)
                .then(room => {
                    const grid = document.getElementById('players-grid');
                    if (!grid) return;
                    room.participants.forEach(p => addPlayerChip(p.username));
                    if (room.status === 'IN_PROGRESS') {
                        showScreen('game');
                        fetchCurrentQuestion(room.currentQuestionIndex);
                    }
                }).catch(() => {});
        }, 3000);
    }

})();