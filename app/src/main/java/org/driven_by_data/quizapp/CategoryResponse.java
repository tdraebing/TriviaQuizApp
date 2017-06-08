package org.driven_by_data.quizapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by thoma on 3/26/2017.
 */
class QuestionCategory implements Parcelable {
    private int id;
    private String name;

    public QuestionCategory() {

    }

    public QuestionCategory(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QuestionCategory createFromParcel(Parcel in) {
            return new QuestionCategory(in);
        }

        public QuestionCategory[] newArray(int size) {
            return new QuestionCategory[size];
        }
    };
}

class CategoryResponse {
    private QuestionCategory[] trivia_categories;

    public QuestionCategory[] getTrivia_categories() {
        return trivia_categories;
    }
}
