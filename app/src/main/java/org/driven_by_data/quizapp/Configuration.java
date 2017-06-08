package org.driven_by_data.quizapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by thoma on 3/25/2017.
 */

class Configuration implements Parcelable {
    private int numberRounds;
    private ArrayList<Integer> selectedCategories;
    private String difficulty;
    private ArrayList<String> urls;

    private ArrayList<QuestionCategory> possibleCategories;

    public Configuration() {
        numberRounds = 10;
        difficulty = "any";
        selectedCategories = new ArrayList<>();
        urls = new ArrayList<>();
        buildEndpointUrl();
    }

    public Configuration(Parcel in) {
        numberRounds = in.readInt();
        selectedCategories = new ArrayList<>();
        in.readList(selectedCategories, null);
        difficulty = in.readString();
        urls = new ArrayList<>();
        in.readStringList(urls);
        possibleCategories = new ArrayList<>();
        in.readList(possibleCategories, null);
    }

    public int getNumberRounds() {
        return numberRounds;
    }

    public void setNumberRounds(int numberRounds) {
        this.numberRounds = numberRounds;
    }

    public List<Integer> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategory(ArrayList<Integer> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public void setCategoriesFromStrings(List<String> categories) {

        for (final String category : categories) {
            Predicate<QuestionCategory> predicate = new Predicate<QuestionCategory>() {
                @Override
                public boolean apply(QuestionCategory cat) {
                    return cat.getName().equals(category);
                }
            };
            selectedCategories.add(Iterables.find(possibleCategories, predicate).getId());
        }

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

    public ArrayList<String> getPossibleCategoriesAsStrings() {
        ArrayList<String> stringCategories = new ArrayList<>();
        for (QuestionCategory qc : possibleCategories) {
            stringCategories.add(qc.getName());
        }
        return stringCategories;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setPossibleCategories(ArrayList<QuestionCategory> pc) {
        possibleCategories = new ArrayList<>(pc.size() + 1);

        QuestionCategory anyCat = new QuestionCategory();
        anyCat.setId(0);
        anyCat.setName("Any Category");
        possibleCategories.add(anyCat);

        possibleCategories.addAll(pc);
    }

    public ArrayList<String> getCategoryNameArray() {
        ArrayList<String> cat = new ArrayList<>(possibleCategories.size());
        for (QuestionCategory c : possibleCategories
                ) {
            cat.add(c.getName());
        }
        return cat;
    }

    private SparseIntArray chooseCategories() {
        int filledCount = numberRounds;
        SparseIntArray categoryList = new SparseIntArray();
        ArrayList<Integer> remainingCategories = selectedCategories;
        while (remainingCategories.size() > 1) {
            int catid = (int) (Math.random() * remainingCategories.size());
            int c = remainingCategories.get(catid);
            int count = (int) (Math.random() * filledCount);
            categoryList.put(c, count);
            filledCount -= count;
            remainingCategories.remove(catid);
        }

        categoryList.put(remainingCategories.get(0), filledCount);

        return categoryList;
    }

    public void buildEndpointUrl() {
        this.urls.clear();
        String base_url = "https://opentdb.com/api.php?";
        if (!difficulty.equals("any")) {
            base_url += String.format("&difficulty=%s", difficulty);
        }
        if (selectedCategories.isEmpty() | selectedCategories.contains(0)) {
            this.urls.add(base_url + String.format("&amount=%d", numberRounds));
        } else {
            SparseIntArray chosenCategories = chooseCategories();
            for (int i = 0; i < chosenCategories.size(); i++) {
                this.urls.add(base_url + String.format("&amount=%d&category=%d",
                        chosenCategories.valueAt(i), chosenCategories.keyAt(i)));
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(numberRounds);
        dest.writeList(selectedCategories);
        dest.writeString(difficulty);
        dest.writeStringList(urls);
        dest.writeList(possibleCategories);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Configuration createFromParcel(Parcel in) {
            return new Configuration(in);
        }

        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
}
