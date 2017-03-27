package org.driven_by_data.quizapp;

import java.util.List;

/**
 * Created by thoma on 3/18/2017.
 */


public class Quiz {
    private List<Question> questions;
    private int numberTotalQuestions;
    private int numberAnsweredQuestions = 0;
    private int numberCorrectQuestions = 0;
    private int currentQuestionID = 0;

    public int getNumberTotalQuestions() {
        return numberTotalQuestions;
    }

    public void setNumberTotalQuestions(int numberTotalQuestions) {
        this.numberTotalQuestions = numberTotalQuestions;
    }

    public int getNumberAnsweredQuestions() {
        return numberAnsweredQuestions;
    }

    public void setNumberAnsweredQuestions(int numberAnsweredQuestions) {
        this.numberAnsweredQuestions = numberAnsweredQuestions;
    }

    public int getNumberCorrectQuestions() {
        return numberCorrectQuestions;
    }

    public void setNumberCorrectQuestions(int numberCorrectQuestions) {
        this.numberCorrectQuestions = numberCorrectQuestions;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        setNumberTotalQuestions(questions.size());
        this.questions = questions;
    }

    public Question getCurrentQuestion() throws QuestionSetCompletedException{
        if (currentQuestionID > numberTotalQuestions){
            throw new QuestionSetCompletedException("All questions have already been answered. " +
                    "Start a new round.");
        }
        Question currentQuestion = questions.get(currentQuestionID);
        currentQuestionID += 1;
        return currentQuestion;
    }
}
