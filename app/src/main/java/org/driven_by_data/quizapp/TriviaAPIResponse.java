package org.driven_by_data.quizapp;

/**
 * Created by tdraebing on 3/24/2017.
 */

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TriviaAPIResponse {

    private int response_code;
    @SerializedName("results")
    private List<Question> questions;

    public int getResponse_code() {
        return response_code;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
