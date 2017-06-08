package org.driven_by_data.quizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.driven_by_data.quizapp.R.id.answers;


public class MainActivity extends AppCompatActivity {

    private ArrayList<String> QUESTION_ENDPOINTS;
    private static final String CATEGORY_ENDPOINT = "https://opentdb.com/api_category.php";

    private RequestQueue requestQueue;
    private Gson gson;

    ProgressDialog loadingDialog;

    boolean isGameActive = false;
    boolean isEnding = false;
    Quiz quiz;
    Configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Loading data ...");
        loadingDialog.setMessage("Just a moment, please!");
        loadingDialog.setIndeterminate(true);

        requestQueue = Volley.newRequestQueue(this);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        config = new Configuration();
        if (savedInstanceState == null) {
            fetchCategories();
        }

        final Button button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isGameActive) {
                            startGame();
                            button_start.setText("Quit game");
                        } else {
                            endGame();
                            button_start.setText("Start new game");
                        }
                    }
                });

        final Button button_config = (Button) findViewById(R.id.button_config);
        button_config.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isGameActive) {
                            showConfigDialog();
                        }
                    }
                });

        final Button button_next = (Button) findViewById(R.id.button_next);
        button_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isEnding) {
                            try {
                                emptyAnswerView();
                                populateQuestionView(quiz.advanceQuestion());
                            } catch (QuestionSetCompletedException e) {
                                endGame();
                            }
                        } else {
                            endGame();
                        }
                    }
                });
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        QUESTION_ENDPOINTS = savedInstanceState.getStringArrayList("endpoints");
        isGameActive = savedInstanceState.getBoolean("is_game_active");
        isEnding = savedInstanceState.getBoolean("is_ending");
        quiz = savedInstanceState.getParcelable("quiz");
        config = savedInstanceState.getParcelable("config");

        if (isGameActive) {
            populateQuestionView(quiz.getCurrentQuestion());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("endpoints", QUESTION_ENDPOINTS);
        outState.putBoolean("is_game_active", isGameActive);
        outState.putBoolean("is_ending", isEnding);
        outState.putParcelable("quiz", quiz);
        outState.putParcelable("config", config);
        super.onSaveInstanceState(outState);
    }

    private void emptyAnswerView() {
        LinearLayout questionView =
                (LinearLayout) findViewById(R.id.question_container);
        questionView.removeView(findViewById(answers));
    }

    private void fetchCategories() {
        loadingDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, CATEGORY_ENDPOINT,
                onCategoriesLoaded, onRequestError);

        requestQueue.add(request);
    }

    private void fetchQuestions() {
        loadingDialog.show();
        for (String endpoint : QUESTION_ENDPOINTS) {
            StringRequest request = new StringRequest(Request.Method.GET, endpoint,
                    onQuestionsLoaded, onRequestError);

            requestQueue.add(request);
        }
    }

    private final Response.Listener<String> onCategoriesLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            CategoryResponse json = gson.fromJson(response, CategoryResponse.class);
            ArrayList<QuestionCategory> q = new ArrayList<>(Arrays.asList(json.getTrivia_categories()));
            config.setPossibleCategories(q);
            loadingDialog.dismiss();
        }
    };

    private final Response.Listener<String> onQuestionsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            TriviaAPIResponse json = gson.fromJson(response, TriviaAPIResponse.class);
            quiz.addQuestions(json.getQuestions());
            try {
                if (quiz.getNumberTotalQuestions() == config.getNumberRounds()) {
                    populateQuestionView(quiz.advanceQuestion());
                    loadingDialog.dismiss();
                }
            } catch (QuestionSetCompletedException e) {
                Log.e("MainActivity", e.getMessage());
                loadingDialog.dismiss();
            }
        }
    };

    private final Response.ErrorListener onRequestError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("MainActivity", error.toString());
            loadingDialog.dismiss();
        }
    };

    private void displayResultMultipleAnswers(Question question,
                                              ListView list,
                                              boolean isOld) {
        quiz.setNumberAnsweredQuestions(quiz.getNumberAnsweredQuestions() + 1);
        final ArrayList<View> givenView = new ArrayList<View>();
        list.findViewsWithText(givenView, question.getGivenAnswer(),
                View.FIND_VIEWS_WITH_TEXT);
        TextView item = (TextView) givenView.get(0);
        if (question.getGivenAnswer().equals(question.getCorrectAnswer())) {
            item.setBackgroundColor(Color.rgb(15, 85, 30));
            quiz.setNumberCorrectQuestions(quiz.getNumberCorrectQuestions() + 1);
        } else {
            item.setBackgroundColor(Color.rgb(120, 15, 15));
            final ArrayList<View> outViews = new ArrayList<View>();
            list.findViewsWithText(outViews, question.getCorrectAnswer(),
                    View.FIND_VIEWS_WITH_TEXT);
            TextView correct = (TextView) outViews.get(0);
            correct.setTextColor(Color.rgb(15, 85, 30));
        }
        list.setEnabled(false);

        if (quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()) {
            isEnding = true;
        }

        Button buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setEnabled(true);
    }

    private void createMultipleAnswers(final Question question) {
        List<String> answers = question.getAllAnswers();

        final LinearLayout questionView = (LinearLayout) findViewById(R.id.question_container);
        final ListView answerList = (ListView) getLayoutInflater().inflate(R.layout.multiple_answers, null);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                answers);
        answerList.setTag(666);
        answerList.setAdapter(adapter);
        answerList.setEnabled(true);
        answerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                TextView item = (TextView) adapter.getChildAt(position);
                question.setGivenAnswer(item.getText().toString());
                displayResultMultipleAnswers(question,
                        (ListView) questionView.findViewWithTag(666), false);

            }
        });
        answerList.post(new Runnable() {
            @Override
            public void run() {
                if (question.getGivenAnswer() != null) {
                    displayResultMultipleAnswers(question,
                            (ListView) questionView.findViewWithTag(666), true);
                }
            }
        });
        questionView.addView(answerList);
    }

    private void displayResultBooleanAnswer(Question question,
                                            RadioButton checkedButton,
                                            RadioGroup group,
                                            boolean isOld) {
        final String answer = question.getCorrectAnswer();
        if (checkedButton.getTag().toString().equals(answer)) {
            checkedButton.setBackgroundColor(Color.rgb(15, 85, 30));
            if (!isOld) {
                quiz.setNumberCorrectQuestions(quiz.getNumberCorrectQuestions() + 1);
            }
        } else {
            checkedButton.setBackgroundColor(Color.rgb(120, 15, 15));
        }


        if (!isOld) {
            quiz.setNumberAnsweredQuestions(quiz.getNumberAnsweredQuestions() + 1);
        } else {
            checkedButton.setChecked(true);
        }

        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(false);
        }

        if (quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()) {
            isEnding = true;
        }

        Button buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setEnabled(true);
    }

    private void createBooleanAnswer(final Question question) {

        LinearLayout questionView = (LinearLayout) findViewById(R.id.question_container);
        LayoutInflater inflater = this.getLayoutInflater();
        final View radioList = inflater.inflate(R.layout.boolean_answer, null);
        final RadioGroup answers = (RadioGroup) radioList.findViewById(R.id.answers);

        answers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton checkedButton = (RadioButton) group.findViewById(checkedId);
                question.setGivenAnswer(checkedButton.getTag().toString());
                displayResultBooleanAnswer(question, checkedButton, group, false);
            }
        });

        if (question.getGivenAnswer() != null) {
            displayResultBooleanAnswer(question,
                    (RadioButton) answers.findViewWithTag(question.getGivenAnswer()),
                    answers, true);
        }
        questionView.addView(radioList);
    }


    private void displayResultTextAnswer(Question question,
                                         EditText answerEdit,
                                         View editTextAnswer,
                                         Button confirmAnswer,
                                         boolean isOld) {

        final String correctAnswer = question.getCorrectAnswer();

        if (correctAnswer.equals(question.getGivenAnswer())) {
            answerEdit.setBackgroundColor(Color.rgb(15, 85, 30));
            if (!isOld) {
                quiz.setNumberCorrectQuestions(quiz.getNumberCorrectQuestions() + 1);
            }
        } else {
            answerEdit.setBackgroundColor(Color.rgb(120, 15, 15));
            TextView correctAnswerView = (TextView)
                    editTextAnswer.findViewById(R.id.correct_answer);
            correctAnswerView.setText(String.format("The correct answer would have been: %s",
                    correctAnswer));
            correctAnswerView.setVisibility(View.VISIBLE);
        }

        if (!isOld) {
            quiz.setNumberAnsweredQuestions(quiz.getNumberAnsweredQuestions() + 1);
        } else {
            answerEdit.setText(question.getGivenAnswer());
        }

        answerEdit.setEnabled(false);
        confirmAnswer.setEnabled(false);

        if (quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()) {
            isEnding = true;
        }

        Button buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setEnabled(true);
    }

    private void createTextAnswer(final Question question) {

        LinearLayout questionView = (LinearLayout) findViewById(R.id.question_container);
        LayoutInflater inflater = this.getLayoutInflater();
        final View editTextAnswer = inflater.inflate(R.layout.text_answer, null);
        final Button confirmAnswer = (Button) editTextAnswer.findViewById(R.id.confirm_answer);
        final EditText answerEdit = (EditText) editTextAnswer.findViewById(R.id.answer_text);

        confirmAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String givenAnswer = answerEdit.getText().toString();
                question.setGivenAnswer(givenAnswer);
                displayResultTextAnswer(question, answerEdit, editTextAnswer, confirmAnswer, false);
            }
        });

        if (question.getGivenAnswer() != null) {
            displayResultTextAnswer(question, answerEdit, editTextAnswer, confirmAnswer, true);
        }
        questionView.addView(editTextAnswer);
    }


    private void populateQuestionView(final Question question) {
        Button buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setEnabled(false);
        if (quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()) {
            endGame();
        } else {
            TextView questionText = (TextView) findViewById(R.id.question);
            questionText.setText(question.getQuestion());

            if (question.getType().equals("multiple")) {
                createMultipleAnswers(question);
            } else if (question.getType().equals("boolean")) {
                createBooleanAnswer(question);
            } else if (question.getType().equals("text")) {
                createTextAnswer(question);
            }
        }

    }

    private void startGame() {
        isGameActive = true;
        QUESTION_ENDPOINTS = config.getUrls();
        quiz = new Quiz();
        fetchQuestions();
    }

    private void endGame() {
        isGameActive = false;
        showEndGameDialog();
    }

    private void createRoundNumberPicker(View dialogView) {
        final NumberPicker numberQuestions = (NumberPicker) dialogView.findViewById(R.id.num_questions);
        numberQuestions.setMinValue(1);
        numberQuestions.setMaxValue(100);
    }

    private void createDifficultySpinner(View dialogView) {
        class DifficultySpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                parent.setSelection(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        }

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_difficulty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulties_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new DifficultySpinnerActivity());
        spinner.setSelection(0);
    }

    private void createCategorySpinner(View dialogView) {
        MultiSelectSpinner spinner = (MultiSelectSpinner)
                dialogView.findViewById(R.id.spinner_category);
        spinner.setItems(config.getPossibleCategoriesAsStrings());
    }

    public void showConfigDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.config_dialog, null);
        dialogBuilder.setView(dialogView);

        createRoundNumberPicker(dialogView);
        createDifficultySpinner(dialogView);
        createCategorySpinner(dialogView);

        final NumberPicker numberQuestions = (NumberPicker) dialogView.findViewById(R.id.num_questions);
        final Spinner difficultySpinner = (Spinner) dialogView.findViewById(R.id.spinner_difficulty);
        final MultiSelectSpinner categorySpinner = (MultiSelectSpinner)
                dialogView.findViewById(R.id.spinner_category);


        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                config.setNumberRounds(numberQuestions.getValue());
                config.setDifficulty(difficultySpinner.getSelectedItem().toString());
                List<String> selectedCategories = categorySpinner.getSelectedStrings();
                config.setCategoriesFromStrings(selectedCategories);
                config.buildEndpointUrl();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showEndGameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.endgame_dialog, null);
        dialogBuilder.setView(dialogView);

        final TextView correct = (TextView) dialogView.findViewById(R.id.correct);
        final TextView wrong = (TextView) dialogView.findViewById(R.id.wrong);

        correct.setText(String.valueOf(quiz.getNumberCorrectQuestions()));
        wrong.setText(String.valueOf(quiz.getNumberTotalQuestions()));

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                TextView question = (TextView) findViewById(R.id.question);
                question.setText("");

                emptyAnswerView();

                final Button button_start = (Button) findViewById(R.id.button_start);
                button_start.setText("Start new game");

                final Button button_next = (Button) findViewById(R.id.button_next);
                button_next.setEnabled(false);
                isGameActive = false;
                isEnding = false;
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
