package org.driven_by_data.quizapp;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by thoma on 3/18/2017.
 */

class Question {
    private String category;
    private String type;
    private String difficulty;
    private String question;
    @SerializedName("correct_answer")
    private String correctAnswer;
    @SerializedName("incorrect_answers")
    private List<String> incorrectAnswers;

    private List<String> allAnswers;

    public void combineAnswers(){
        allAnswers = new ArrayList<String>();
        allAnswers.addAll(incorrectAnswers);
        allAnswers.add(correctAnswer);
        Collections.shuffle(allAnswers);
    }

    public List<String> getAllAnswers() {
        if (allAnswers == null){
            combineAnswers();
        }
        return allAnswers;
    }

    public String getCategory(){
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return StringEscapeUtils.unescapeHtml4(question);
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }
}
