package org.driven_by_data.quizapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thoma on 3/18/2017.
 */


public class Quiz implements Parcelable {
    private List<Question> questions;
    private int numberTotalQuestions;
    private int numberAnsweredQuestions = 0;
    private int numberCorrectQuestions = 0;
    private int nextQuestionID = 0;

    public Quiz() {

    }

    public Quiz(Parcel in) {
        questions = new ArrayList<>();
        in.readList(questions, null);
        numberTotalQuestions = in.readInt();
        numberAnsweredQuestions = in.readInt();
        numberCorrectQuestions = in.readInt();
        nextQuestionID = in.readInt();
    }

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

    public void addQuestions(List<Question> questions) {
        this.numberTotalQuestions += questions.size();
        if (this.questions == null) {
            this.questions = questions;
        } else {
            this.questions.addAll(questions);
        }
    }

    public Question advanceQuestion() throws QuestionSetCompletedException {
        if (nextQuestionID > numberTotalQuestions) {
            throw new QuestionSetCompletedException("All questions have already been answered. " +
                    "Start a new round.");
        }
        Question currentQuestion = questions.get(nextQuestionID);
        nextQuestionID += 1;
        return currentQuestion;
    }

    public Question getCurrentQuestion() {
        return questions.get(nextQuestionID - 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(questions);
        dest.writeInt(numberTotalQuestions);
        dest.writeInt(numberAnsweredQuestions);
        dest.writeInt(numberCorrectQuestions);
        dest.writeInt(nextQuestionID);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
}
