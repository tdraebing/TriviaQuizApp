package org.driven_by_data.quizapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by thoma on 3/18/2017.
 */

class Question implements Parcelable {
    private String category;
    private String type;
    private String difficulty;
    private String question;
    @SerializedName("correct_answer")
    private String correctAnswer;
    @SerializedName("incorrect_answers")
    private List<String> incorrectAnswers;

    private List<String> allAnswers;
    private double probabilityTextAnswer;

    private String givenAnswer = null;

    public String getGivenAnswer() {
        return givenAnswer;
    }

    public void setGivenAnswer(String givenAnswer) {
        this.givenAnswer = givenAnswer;
    }

    public Question(Parcel in) {
        category = in.readString();
        type = in.readString();
        difficulty = in.readString();
        question = in.readString();

        correctAnswer = in.readString();
        incorrectAnswers = new ArrayList<String>();
        in.readStringList(incorrectAnswers);
        allAnswers = new ArrayList<String>();
        in.readStringList(allAnswers);
        probabilityTextAnswer = in.readDouble();
        givenAnswer = in.readString();
    }

    public void processQuestion() {
        if (!type.equals("boolean")) {
            probabilityTextAnswer = Math.random();
            if (probabilityTextAnswer > 0.8) {
                type = "text";
            }
        }
        incorrectAnswers = decodeAnswer(incorrectAnswers);
        correctAnswer = decodeAnswer(correctAnswer);
        combineAnswers();
    }

    public void combineAnswers() {
        allAnswers = new ArrayList<String>();
        allAnswers.addAll(incorrectAnswers);
        allAnswers.add(correctAnswer);
        Collections.shuffle(allAnswers);
    }

    private String decodeAnswer(String encodedAnswer) {
        return Html.fromHtml(encodedAnswer).toString();
    }

    private List<String> decodeAnswer(List<String> encodedAnswer) {
        ArrayList<String> decoded = new ArrayList<>();
        for (String s : encodedAnswer) {
            decoded.add(Html.fromHtml(s).toString());
        }
        return decoded;
    }

    public List<String> getAllAnswers() {
        return allAnswers;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return Html.fromHtml(this.question).toString();
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(type);
        dest.writeString(difficulty);
        dest.writeString(question);
        dest.writeString(correctAnswer);
        dest.writeStringList(incorrectAnswers);
        dest.writeStringList(allAnswers);
        dest.writeDouble(probabilityTextAnswer);
        dest.writeString(givenAnswer);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}
