package org.driven_by_data.quizapp;

/**
 * Created by thoma on 3/26/2017.
 */
class QuestionCategory {
    private int id;
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class CategoryResponse {
    private QuestionCategory[] trivia_categories;

    public QuestionCategory[] getTrivia_categories() {
        return trivia_categories;
    }
}
