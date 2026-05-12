package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.client.QuizServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizServiceClient quizServiceClient;
    private final HomeController homeController;

    @GetMapping("/public")
    public String publicList(@RequestParam(required = false) String search,
                             HttpServletRequest request,
                             Model model) {
        String token = homeController.extractToken(request);
        try {
            if (search != null && !search.isBlank()) {
                model.addAttribute("quizzes", quizServiceClient.searchQuizzes(search));
                model.addAttribute("search", search);
            } else {
                model.addAttribute("quizzes", quizServiceClient.getPublishedQuizzes());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить квизы");
        }
        if (token != null) {
            try {
                model.addAttribute("currentUsername", quizServiceClient.getCurrentUser(token).getUsername());
            } catch (Exception ignored) {
                // non-blocking for public page
            }
        }
        return "quiz/public-list";
    }

    @GetMapping("/create")
    public String createPage(HttpServletRequest request) {
        if (homeController.extractToken(request) == null) {
            return "redirect:/auth/login";
        }
        return "quiz/create";
    }

    @PostMapping("/create")
    public String createQuiz(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String category,
                             HttpServletRequest request,
                             Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            var quiz = quizServiceClient.createQuiz(title, description, category, token);
            return "redirect:/quizzes/" + quiz.getId() + "/questions";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка создания квиза");
            return "quiz/create";
        }
    }

    @GetMapping("/{id}/questions")
    public String questionsPage(@PathVariable Long id, HttpServletRequest request, Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            model.addAttribute("quiz", quizServiceClient.getQuiz(id, token));
            model.addAttribute("token", token);
        } catch (Exception e) {
            model.addAttribute("error", "Квиз не найден");
        }
        return "quiz/questions";
    }

    @GetMapping("/{id}/add-question")
    public String addQuestionPage(@PathVariable Long id, HttpServletRequest request, Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        model.addAttribute("quizId", id);
        return "quiz/add-question";
    }

    @PostMapping("/{id}/add-question")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam String text,
                              @RequestParam int timeLimitSeconds,
                              @RequestParam int points,
                              @RequestParam List<String> optionTexts,
                              @RequestParam(required = false) List<Integer> correctIndexes,
                              HttpServletRequest request,
                              Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            List<Map<String, Object>> options = buildAnswerOptions(optionTexts, correctIndexes);
            quizServiceClient.addQuestion(id, text, timeLimitSeconds, points, options, token);
            return "redirect:/quizzes/" + id + "/questions";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка добавления вопроса");
            model.addAttribute("quizId", id);
            return "quiz/add-question";
        }
    }

    @GetMapping("/{quizId}/questions/{questionId}/edit")
    public String editQuestionPage(@PathVariable Long quizId,
                                   @PathVariable Long questionId,
                                   HttpServletRequest request,
                                   Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            var quiz = quizServiceClient.getQuiz(quizId, token);
            var questionOpt = quiz.getQuestions().stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst();
            if (questionOpt.isEmpty()) {
                return "redirect:/quizzes/" + quizId + "/questions?error=questionNotFound";
            }
            var question = questionOpt.get();
            List<Integer> correctIndexes = new ArrayList<>();
            for (int i = 0; i < question.getAnswerOptions().size(); i++) {
                if (question.getAnswerOptions().get(i).isCorrect()) {
                    correctIndexes.add(i);
                }
            }
            model.addAttribute("quizId", quizId);
            model.addAttribute("questionId", questionId);
            model.addAttribute("question", question);
            model.addAttribute("correctIndexes", correctIndexes);
            return "quiz/edit-question";
        } catch (Exception e) {
            return "redirect:/quizzes/" + quizId + "/questions?error=questionLoad";
        }
    }

    @PostMapping("/{quizId}/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId,
                               @PathVariable Long questionId,
                               @RequestParam String text,
                               @RequestParam int timeLimitSeconds,
                               @RequestParam int points,
                               @RequestParam List<String> optionTexts,
                               @RequestParam(required = false) List<Integer> correctIndexes,
                               HttpServletRequest request,
                               Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            List<Map<String, Object>> options = buildAnswerOptions(optionTexts, correctIndexes);
            quizServiceClient.updateQuestion(questionId, text, timeLimitSeconds, points, options, token);
            return "redirect:/quizzes/" + quizId + "/questions";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка обновления вопроса");
            model.addAttribute("quizId", quizId);
            model.addAttribute("questionId", questionId);
            try {
                var quiz = quizServiceClient.getQuiz(quizId, token);
                quiz.getQuestions().stream()
                        .filter(q -> q.getId().equals(questionId))
                        .findFirst()
                        .ifPresent(question -> model.addAttribute("question", question));
            } catch (Exception ignored) {
            }
            model.addAttribute("correctIndexes", correctIndexes != null ? correctIndexes : List.of());
            return "quiz/edit-question";
        }
    }

    @PostMapping("/{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId,
                                 @PathVariable Long questionId,
                                 HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            quizServiceClient.deleteQuestion(questionId, token);
            return "redirect:/quizzes/" + quizId + "/questions?questionDeleted=true";
        } catch (Exception e) {
            return "redirect:/quizzes/" + quizId + "/questions?questionDeleted=false";
        }
    }

    @PostMapping("/{id}/publish")
    public String publishQuiz(@PathVariable Long id, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            quizServiceClient.publishQuiz(id, token);
        } catch (Exception ignored) {}
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable Long id,
                             @RequestParam(required = false) String from,
                             HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        String redirectBase = "public".equals(from) ? "/quizzes/public" : "/dashboard";
        try {
            quizServiceClient.deleteQuiz(id, token);
            return "redirect:" + redirectBase + "?quizDeleted=true";
        } catch (Exception ignored) {
            return "redirect:" + redirectBase + "?quizDeleted=false";
        }
    }

    private List<Map<String, Object>> buildAnswerOptions(List<String> optionTexts, List<Integer> correctIndexes) {
        List<Map<String, Object>> options = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String text = optionTexts.get(i) == null ? "" : optionTexts.get(i).trim();
            if (text.isEmpty()) {
                continue;
            }
            Map<String, Object> opt = new HashMap<>();
            opt.put("text", text);
            opt.put("correct", correctIndexes != null && correctIndexes.contains(i));
            opt.put("orderIndex", options.size());
            options.add(opt);
        }
        if (options.size() < 2) {
            throw new IllegalArgumentException("Нужно минимум 2 варианта ответа");
        }
        boolean hasCorrect = options.stream()
                .anyMatch(opt -> Boolean.TRUE.equals(opt.get("correct")));
        if (!hasCorrect) {
            throw new IllegalArgumentException("Отметьте хотя бы один правильный вариант");
        }
        return options;
    }
}
