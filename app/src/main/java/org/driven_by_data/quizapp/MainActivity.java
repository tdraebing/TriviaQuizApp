package org.driven_by_data.quizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
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


public class MainActivity extends AppCompatActivity {

    private String QUESTION_ENDPOINT;
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
        fetchCategories();

        final Button button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isGameActive){
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
                        if (!isGameActive){
                            showConfigDialog();
                        }
                    }
                });

        final Button button_next = (Button) findViewById(R.id.button_next);
        button_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isEnding){
                            try{
                                populateQuestionView(quiz.getCurrentQuestion());
                            }catch (QuestionSetCompletedException e){
                                endGame();
                            }
                        }else{
                            endGame();
                        }
                    }
                });
    }

    private void fetchCategories() {
        loadingDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, CATEGORY_ENDPOINT,
                onCategoriesLoaded, onRequestError);

        requestQueue.add(request);
    }

    private void fetchQuestions() {
        loadingDialog.show();
        StringRequest request = new StringRequest(Request.Method.GET, QUESTION_ENDPOINT,
                onQuestionsLoaded, onRequestError);

        requestQueue.add(request);
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
            quiz.setQuestions(json.getQuestions());
            try{
                populateQuestionView(quiz.getCurrentQuestion());
                loadingDialog.dismiss();
            }catch (QuestionSetCompletedException e){
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


    private void populateQuestionView(final Question question){
        Button buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setEnabled(false);
        if(quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()){
            endGame();
        }else{
            TextView questionText = (TextView) findViewById(R.id.question);
            questionText.setText(question.getQuestion());

            List<String> answers = question.getAllAnswers();
            ListView answerList = (ListView) findViewById(R.id.answers);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    answers);
            answerList.setAdapter(adapter);
            answerList.setEnabled(true);
            answerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long arg){
                    TextView item = (TextView) adapter.getChildAt(position);
                    quiz.setNumberAnsweredQuestions(quiz.getNumberAnsweredQuestions()+1);

                    if (item.getText().equals(question.getCorrectAnswer())){
                        item.setBackgroundColor(Color.rgb(15,85,30));
                        quiz.setNumberCorrectQuestions(quiz.getNumberCorrectQuestions()+1);
                    }else{
                        item.setBackgroundColor(Color.rgb(120,15,15));
                        final ArrayList<View> outViews = new ArrayList<View>();
                        adapter.findViewsWithText(outViews, question.getCorrectAnswer(),
                                View.FIND_VIEWS_WITH_TEXT);
                        TextView correct = (TextView) outViews.get(0);
                        correct.setTextColor(Color.rgb(15,85,30));
                    }
                    adapter.setEnabled(false);

                    if (quiz.getNumberAnsweredQuestions() >= quiz.getNumberTotalQuestions()){
                        isEnding = true;
                    }

                    Button buttonNext = (Button) findViewById(R.id.button_next);
                    buttonNext.setEnabled(true);

                }
            });
        }

    }

    private void startGame(){
        isGameActive = true;
        QUESTION_ENDPOINT = config.getUrl();
        quiz = new Quiz();
        fetchQuestions();
    }

    private void  endGame(){
        isGameActive = false;
        showEndGameDialog();
    }

    private void createRoundNumberPicker(View dialogView){
        final NumberPicker numberQuestions = (NumberPicker) dialogView.findViewById(R.id.num_questions);
        numberQuestions.setMinValue(1);
        numberQuestions.setMaxValue(100);
    }

    private void createDifficultySpinner(View dialogView){
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

    private void createCategorySpinner(View dialogView){
        class CategorySpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                parent.setSelection(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        }

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_category);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, config.getCategoryNameArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new CategorySpinnerActivity());
        spinner.setSelection(0);
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
        final Spinner categorySpinner = (Spinner) dialogView.findViewById(R.id.spinner_category);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                config.setNumberRounds(numberQuestions.getValue());
                config.setDifficulty(difficultySpinner.getSelectedItem().toString());
                String selectedCat = categorySpinner.getSelectedItem().toString();
                config.setCategoryFromString(selectedCat);
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

                ListView answers = (ListView) findViewById(R.id.answers);
                answers.setAdapter(null);

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
