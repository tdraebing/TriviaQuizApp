package org.driven_by_data.quizapp;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;


/**
 * Created by thoma on 3/25/2017.
 */

class Configuration {
    private int numberRounds;
    private int selectedCategory;
    private String difficulty;
    private String url;

    private ArrayList<QuestionCategory> possibleCategories;

    public Configuration(){
        numberRounds = 10;
        difficulty = "any";
        selectedCategory = 0;
        buildEndpointUrl();
    }

    public int getNumberRounds() {
        return numberRounds;
    }

    public void setNumberRounds(int numberRounds) {
        this.numberRounds = numberRounds;
    }

    public int getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(int selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public void setCategoryFromString(final String category) {
        Predicate<QuestionCategory> predicate = new Predicate<QuestionCategory>() {
            @Override
            public boolean apply(QuestionCategory cat) {
                return cat.getName().equals(category);
            }
        };
        QuestionCategory result = Iterables.find(possibleCategories, predicate);
        selectedCategory = result.getId();

    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public ArrayList<QuestionCategory> getPossibleCategories() {
        return possibleCategories;
    }

    public String getUrl() {
        return url;
    }

    public void setPossibleCategories(ArrayList<QuestionCategory> pc) {
        possibleCategories = new ArrayList<>(pc.size()+1);

        QuestionCategory anyCat = new QuestionCategory();
        anyCat.setId(0);
        anyCat.setName("Any Category");
        possibleCategories.add(anyCat);

        possibleCategories.addAll(pc);
    }

    public ArrayList<String> getCategoryNameArray(){
        ArrayList<String> cat = new ArrayList<>(possibleCategories.size());
        for (QuestionCategory c: possibleCategories
             ) {
            cat.add(c.getName());
        }
        return cat;
    }

    public void buildEndpointUrl(){
        url = String.format("https://opentdb.com/api.php?amount=%d", numberRounds);
        if(!difficulty.equals("any")){
            url += String.format("&difficulty=%s", difficulty);
        }
        if(selectedCategory != 0){
            url += String.format("&category=%d", selectedCategory);
        }
    }
}
