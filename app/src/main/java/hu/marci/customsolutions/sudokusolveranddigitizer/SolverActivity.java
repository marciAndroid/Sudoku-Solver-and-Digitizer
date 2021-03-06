package hu.marci.customsolutions.sudokusolveranddigitizer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.IllegalFormatConversionException;
import java.util.Locale;

public class SolverActivity extends AppCompatActivity {

    private boolean[] showNumber;
    private int[] numbers;
    private int[] solution;
    private TextView[] texts;
    private EditText[] ets;

    private TextView solveInfo;
    private Button showAll;
    private Button edit;
    private boolean showAllSolutions;
    private boolean editing;

    private TextView solutionNumber;

    private Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solver_layout);

        locale = getResources().getConfiguration().locale;

        editing = false;

        solutionNumber = findViewById(R.id.solutionNumber);
        solveInfo = findViewById(R.id.solveInfo);

        showNumber = new boolean[81];
        for (int i = 0; i < 81; i++){
            showNumber[i] = true;
        }
        showAllSolutions = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            numbers = extras.getIntArray("numbers");
        }
        else {
            numbers = new int[81];
        }

        solution = new int[81];



        texts = new TextView[81];
        ets = new EditText[81];
        for (int i = 0; i < 81; i++) {
            // Get TextView
            texts[i] = findViewById(getResourceIDfromNumber(i, 0));
            // Get EditText
            ets[i] = findViewById(getResourceIDfromNumber(i, 3));
            ets[i].setVisibility(View.INVISIBLE);
            ets[i].setOnFocusChangeListener(myOnFocusChangeListener);
            ets[i].addTextChangedListener(new MyTextWatcher(ets[i],i));
        }

        showAll = findViewById(R.id.showall);
        showAll.setOnClickListener(showAllOnClickListener);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.back));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });

        edit = findViewById(R.id.edit);
        edit.setOnClickListener(myEditButtonOnClickListener);

    }

    boolean solved = false;

    private class SolveSudoku extends AsyncTask<Void,Void, Void> {

        private Activity activity;
        private Dialog dialog;

        SolveSudoku(Activity activity) {
            this.activity = activity;
            dialog = new Dialog(activity, R.style.Theme_AppCompat);
            View view = LayoutInflater.from(activity).inflate(R.layout.remove_border, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
            dialog.setContentView(view);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Solving sudoku may takes a few seconds...
            solved = solveSudoku(numbers, solution);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()){
                dialog.dismiss();
            }

            solutionNumber.setText("1/1");

            if (solved){
                solveInfo.setText(R.string.solveSuccess);
            }
            else{
                solveInfo.setText(R.string.solveFailure);
            }

            for (int i = 0; i < 81; i++){
                if (numbers != null && numbers[i] != 0) {
                    // Remove tile background from this cell
                    findViewById(getResourceIDfromNumber(i, 1)).setVisibility(View.INVISIBLE);
                    // Delete onClick listener from FrameLayout
                    findViewById(getResourceIDfromNumber(i, 2)).setOnClickListener(null);
                }
                if (texts[i] != null && numbers[i] != 0) {
                    // Set number
                    texts[i].setText(String.format(locale, "%d", numbers[i]));
                }
                if (numbers != null && numbers[i] == 0){

                    if (texts[i] != null) {
                        texts[i].setText(String.format(locale, "%d", solution[i]));
                    }

                    showNumber[i] = false;

                    ObjectAnimator anim;
                    anim = ObjectAnimator.ofFloat(texts[i],"alpha",0.f);
                    anim.setDuration(0);
                    anim.start();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        new SolveSudoku(SolverActivity.this).execute();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }


    public native boolean solveSudoku(int[] numbers, int[] sudoku);

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener showAllOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            flipAll();
        }
    };

    public void flipAll(){
        for (int i = 0; i < 81; i++) {
            if (!showAllSolutions) {
                if (numbers != null && numbers[i] == 0 && !showNumber[i]) {
                    ObjectAnimator anim;
                    ObjectAnimator anim2;
                    anim = ObjectAnimator.ofFloat(findViewById(getResourceIDfromNumber(i, 1)),
                            "rotationY", 180f, 0f);
                    anim.setDuration(10);
                    anim.start();
                    anim2 = ObjectAnimator.ofFloat(findViewById(getResourceIDfromNumber(i, 0)),
                            "alpha", 1.f);
                    anim2.setDuration(10);
                    anim2.start();

                    showNumber[i] = !showNumber[i];
                }
                showAll.setText(R.string.hideall);
            }
            else{
                if (numbers != null && numbers[i] == 0 && showNumber[i]) {
                    ObjectAnimator anim;
                    ObjectAnimator anim2;
                    anim = ObjectAnimator.ofFloat(findViewById(getResourceIDfromNumber(i, 1))
                            , "rotationY", 0, -180f);
                    anim.setDuration(10);
                    anim.start();
                    anim2 = ObjectAnimator.ofFloat(findViewById(getResourceIDfromNumber(i, 0)),
                            "alpha", 0.f);
                    anim2.setDuration(10);
                    anim2.start();

                    showNumber[i] = !showNumber[i];
                }
                showAll.setText(R.string.showall);
            }
        }
        showAllSolutions = !showAllSolutions;
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener myEditButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editAll();
        }
    };

    public void editAll(){
        editing = !editing;
        if (editing){

            edit.setText(R.string.edit2);
            for (int i = 0; i < 81; i++) {
                ets[i].setBackgroundResource(R.color.Totaltransparent);
                // Remove tile background from this cell
                findViewById(getResourceIDfromNumber(i, 1)).setVisibility(View.INVISIBLE);
                // Remove text
                findViewById(getResourceIDfromNumber(i, 0)).setVisibility(View.INVISIBLE);
                if (numbers[i] != 0) {
                    try{
                        ets[i].setText(String.format(locale, "%d", numbers[i]));
                    }
                    catch (IllegalFormatConversionException ifce){
                        ets[i].setText(" ");
                    }
                }
                else {
                    ets[i].setText(" ");
                    // Delete onClick listener from FrameLayout
                    findViewById(getResourceIDfromNumber(i, 2)).setOnClickListener(null);
                }
                ets[i].setVisibility(View.VISIBLE);
            }
        }
        else{
            edit.setText(R.string.edit);
            for (int i = 0; i < 81; i++) {
                ets[i].setVisibility(View.INVISIBLE);
                if (numbers[i] != 0){
                    texts[i].setText(String.format(locale, "%d", numbers[i]));
                    // Remove tile background from this cell
                    findViewById(getResourceIDfromNumber(i, 1)).setVisibility(View.INVISIBLE);
                    // Delete onClick listener from FrameLayout
                    findViewById(getResourceIDfromNumber(i, 2)).setOnClickListener(null);
                    // Show text
                    findViewById(getResourceIDfromNumber(i, 0)).setVisibility(View.VISIBLE);
                    ObjectAnimator anim;
                    anim = ObjectAnimator.ofFloat(texts[i],"alpha",1.f);
                    anim.setDuration(0);
                    anim.start();
                }
                else {
                    // Remove tile background from this cell
                    findViewById(getResourceIDfromNumber(i, 1)).setVisibility(View.VISIBLE);
                    // Show text
                    findViewById(getResourceIDfromNumber(i, 0)).setVisibility(View.VISIBLE);
                    // Delete onClick listener from FrameLayout
                    findViewById(getResourceIDfromNumber(i, 2)).setOnClickListener(mOnClickListener);
                }
            }
            new SolveSudoku(SolverActivity.this).execute();
        }
    }

    private View.OnFocusChangeListener myOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            changefocus(view, hasFocus);
        }

    };
    public void changefocus(View view, boolean hasFocus) {
        if (hasFocus) {
            view.setBackgroundResource(R.color.focusedET);
        } else {
            view.setBackgroundResource(R.color.Totaltransparent);
        }
    }


    private class MyTextWatcher implements TextWatcher {
        private EditText et;
        private int index;
        private String beforeString;
        boolean preventUpdate = false;

        public MyTextWatcher(EditText e, int i) {
            et = e;
            index = i;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeString = et.getText().toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String myNum = et.getText().toString();myNum = myNum.replace(" ","");
            if (!myNum.startsWith(beforeString)) {
                myNum = myNum.replace(beforeString,"");
            }
            int mynumber=0;
            try {
                mynumber = Integer.parseInt(myNum);
            }
            catch (NumberFormatException nfe){
            }
            if (mynumber > 9){
                mynumber = mynumber % 10;
            }
            numbers[index] = mynumber;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(preventUpdate){
                preventUpdate = false; // reset flag after calling
                return;
            }
            preventUpdate = true;
            if (numbers[index] > 0) {
                et.setText(String.format(locale, "%d", numbers[index]));
            }
            else {
                et.setText(" ");
            }
            et.setSelection(et.getText().length());
        }
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            flipCard(v);
        }
    };

    public void flipCard(View v) {
        Log.i("asdasd",String.valueOf("hi"));
        // showing number to not showing
        if (showNumber[getIDfromView(v)]) {
            FrameLayout fl = (FrameLayout) v;

            ObjectAnimator anim;
            ObjectAnimator anim2;
            for (int i = 0; i < fl.getChildCount(); i++) {
                View child = fl.getChildAt(i);
                if (child instanceof ImageView){
                    anim = ObjectAnimator.ofFloat(child, "rotationY", 0f, -180f);
                    anim.setDuration(500);
                    anim.start();
                }
                if (child instanceof TextView){
                    anim2 = ObjectAnimator.ofFloat(child,"alpha",0.f);
                    anim2.setDuration(250);
                    anim2.start();
                }
            }
        }
        // not showing number to showing
        else{
            FrameLayout fl = (FrameLayout) v;

            ObjectAnimator anim;
            ObjectAnimator anim2;
            for (int i = 0; i < fl.getChildCount(); i++) {
                View child = fl.getChildAt(i);
                if (child instanceof ImageView){
                    anim = ObjectAnimator.ofFloat(child, "rotationY", 180f, 0f);
                    anim.setDuration(500);
                    anim.start();
                }
                if (child instanceof TextView){
                    anim2 = ObjectAnimator.ofFloat(child,"alpha",1.f);
                    anim2.setDuration(250);
                    anim2.start();
                }
            }
        }
        showNumber[getIDfromView(v)] = !showNumber[getIDfromView(v)];
    }


    private int getIDfromView(View v){
        switch (v.getId()){
            case R.id.frame0: case R.id.text0: {
                return 0;
            }
            case R.id.frame1: case R.id.text1: {
                return 1;
            }
            case R.id.frame2: case R.id.text2: {
                return 2;
            }
            case R.id.frame3: case R.id.text3: {
                return 3;
            }
            case R.id.frame4: case R.id.text4: {
                return 4;
            }
            case R.id.frame5: case R.id.text5: {
                return 5;
            }
            case R.id.frame6: case R.id.text6: {
                return 6;
            }
            case R.id.frame7: case R.id.text7: {
                return 7;
            }
            case R.id.frame8: case R.id.text8: {
                return 8;
            }
            case R.id.frame9: case R.id.text9: {
                return 9;
            }
            case R.id.frame10: case R.id.text10: {
                return 10;
            }
            case R.id.frame11: case R.id.text11: {
                return 11;
            }
            case R.id.frame12: case R.id.text12: {
                return 12;
            }
            case R.id.frame13: case R.id.text13: {
                return 13;
            }
            case R.id.frame14: case R.id.text14: {
                return 14;
            }
            case R.id.frame15: case R.id.text15: {
                return 15;
            }
            case R.id.frame16: case R.id.text16: {
                return 16;
            }
            case R.id.frame17: case R.id.text17: {
                return 17;
            }
            case R.id.frame18: case R.id.text18: {
                return 18;
            }
            case R.id.frame19: case R.id.text19: {
                return 19;
            }
            case R.id.frame20: case R.id.text20: {
                return 20;
            }
            case R.id.frame21: case R.id.text21: {
                return 21;
            }
            case R.id.frame22: case R.id.text22: {
                return 22;
            }
            case R.id.frame23: case R.id.text23: {
                return 23;
            }
            case R.id.frame24: case R.id.text24: {
                return 24;
            }
            case R.id.frame25: case R.id.text25: {
                return 25;
            }
            case R.id.frame26: case R.id.text26: {
                return 26;
            }
            case R.id.frame27: case R.id.text27: {
                return 27;
            }
            case R.id.frame28: case R.id.text28: {
                return 28;
            }
            case R.id.frame29: case R.id.text29: {
                return 29;
            }
            case R.id.frame30: case R.id.text30: {
                return 30;
            }
            case R.id.frame31: case R.id.text31: {
                return 31;
            }
            case R.id.frame32: case R.id.text32: {
                return 32;
            }
            case R.id.frame33: case R.id.text33: {
                return 33;
            }
            case R.id.frame34: case R.id.text34: {
                return 34;
            }
            case R.id.frame35: case R.id.text35: {
                return 35;
            }
            case R.id.frame36: case R.id.text36: {
                return 36;
            }
            case R.id.frame37: case R.id.text37: {
                return 37;
            }
            case R.id.frame38: case R.id.text38: {
                return 38;
            }
            case R.id.frame39: case R.id.text39: {
                return 39;
            }
            case R.id.frame40: case R.id.text40: {
                return 40;
            }
            case R.id.frame41: case R.id.text41: {
                return 41;
            }
            case R.id.frame42: case R.id.text42: {
                return 42;
            }
            case R.id.frame43: case R.id.text43: {
                return 43;
            }
            case R.id.frame44: case R.id.text44: {
                return 44;
            }
            case R.id.frame45: case R.id.text45: {
                return 45;
            }
            case R.id.frame46: case R.id.text46: {
                return 46;
            }
            case R.id.frame47: case R.id.text47: {
                return 47;
            }
            case R.id.frame48: case R.id.text48: {
                return 48;
            }
            case R.id.frame49: case R.id.text49: {
                return 49;
            }
            case R.id.frame50: case R.id.text50: {
                return 50;
            }
            case R.id.frame51: case R.id.text51: {
                return 51;
            }
            case R.id.frame52: case R.id.text52: {
                return 52;
            }
            case R.id.frame53: case R.id.text53: {
                return 53;
            }
            case R.id.frame54: case R.id.text54: {
                return 54;
            }
            case R.id.frame55: case R.id.text55: {
                return 55;
            }
            case R.id.frame56: case R.id.text56: {
                return 56;
            }
            case R.id.frame57: case R.id.text57: {
                return 57;
            }
            case R.id.frame58: case R.id.text58: {
                return 58;
            }
            case R.id.frame59: case R.id.text59: {
                return 59;
            }
            case R.id.frame60: case R.id.text60: {
                return 60;
            }
            case R.id.frame61: case R.id.text61: {
                return 61;
            }
            case R.id.frame62: case R.id.text62: {
                return 62;
            }
            case R.id.frame63: case R.id.text63: {
                return 63;
            }
            case R.id.frame64: case R.id.text64: {
                return 64;
            }
            case R.id.frame65: case R.id.text65: {
                return 65;
            }
            case R.id.frame66: case R.id.text66: {
                return 66;
            }
            case R.id.frame67: case R.id.text67: {
                return 67;
            }
            case R.id.frame68: case R.id.text68: {
                return 68;
            }
            case R.id.frame69: case R.id.text69: {
                return 69;
            }
            case R.id.frame70: case R.id.text70: {
                return 70;
            }
            case R.id.frame71: case R.id.text71: {
                return 71;
            }
            case R.id.frame72: case R.id.text72: {
                return 72;
            }
            case R.id.frame73: case R.id.text73: {
                return 73;
            }
            case R.id.frame74: case R.id.text74: {
                return 74;
            }
            case R.id.frame75: case R.id.text75: {
                return 75;
            }
            case R.id.frame76: case R.id.text76: {
                return 76;
            }
            case R.id.frame77: case R.id.text77: {
                return 77;
            }
            case R.id.frame78: case R.id.text78: {
                return 78;
            }
            case R.id.frame79: case R.id.text79: {
                return 79;
            }
            case R.id.frame80: case R.id.text80: {
                return 80;
            }
            default:{
                return -1;
            }
        }
    }

    // Returns the resource id of text (returnTextID = 0) or tile (returnTextID = 1) or frame (returnTextID = 2)
            //  or edit text (returnTextID = 3)
    private int getResourceIDfromNumber(int num, int returnTextID){
        switch (num){
            case 0: {
                if (returnTextID == 0) {
                    return R.id.text0;
                }
                if (returnTextID == 1) {
                    return R.id.tile0;
                }
                if (returnTextID == 2) {
                    return R.id.frame0;
                }
                else {
                    return R.id.et0;
                }
            }
            case 1: {
                if (returnTextID == 0) {
                    return R.id.text1;
                }
                if (returnTextID == 1) {
                    return R.id.tile1;
                }
                if (returnTextID == 2) {
                    return R.id.frame1;
                }
                else {
                    return R.id.et1;
                }
            }
            case 2: {
                if (returnTextID == 0) {
                    return R.id.text2;
                }
                if (returnTextID == 1) {
                    return R.id.tile2;
                }
                if (returnTextID == 2) {
                    return R.id.frame2;
                }
                else {
                    return R.id.et2;
                }
            }
            case 3: {
                if (returnTextID == 0) {
                    return R.id.text3;
                }
                if (returnTextID == 1) {
                    return R.id.tile3;
                }
                if (returnTextID == 2) {
                    return R.id.frame3;
                }
                else {
                    return R.id.et3;
                }
            }
            case 4: {
                if (returnTextID == 0) {
                    return R.id.text4;
                }
                if (returnTextID == 1) {
                    return R.id.tile4;
                }
                if (returnTextID == 2) {
                    return R.id.frame4;
                }
                else {
                    return R.id.et4;
                }
            }
            case 5: {
                if (returnTextID == 0) {
                    return R.id.text5;
                }
                if (returnTextID == 1) {
                    return R.id.tile5;
                }
                if (returnTextID == 2) {
                    return R.id.frame5;
                }
                else {
                    return R.id.et5;
                }
            }
            case 6: {
                if (returnTextID == 0) {
                    return R.id.text6;
                }
                if (returnTextID == 1) {
                    return R.id.tile6;
                }
                if (returnTextID == 2) {
                    return R.id.frame6;
                }
                else {
                    return R.id.et6;
                }
            }
            case 7: {
                if (returnTextID == 0) {
                    return R.id.text7;
                }
                if (returnTextID == 1) {
                    return R.id.tile7;
                }
                if (returnTextID == 2) {
                    return R.id.frame7;
                }
                else {
                    return R.id.et7;
                }
            }
            case 8: {
                if (returnTextID == 0) {
                    return R.id.text8;
                }
                if (returnTextID == 1) {
                    return R.id.tile8;
                }
                if (returnTextID == 2) {
                    return R.id.frame8;
                }
                else {
                    return R.id.et8;
                }
            }
            case 9: {
                if (returnTextID == 0) {
                    return R.id.text9;
                }
                if (returnTextID == 1) {
                    return R.id.tile9;
                }
                if (returnTextID == 2) {
                    return R.id.frame9;
                }
                else {
                    return R.id.et9;
                }
            }
            case 10: {
                if (returnTextID == 0) {
                    return R.id.text10;
                }
                if (returnTextID == 1) {
                    return R.id.tile10;
                }
                if (returnTextID == 2) {
                    return R.id.frame10;
                }
                else {
                    return R.id.et10;
                }
            }
            case 11: {
                if (returnTextID == 0) {
                    return R.id.text11;
                }
                if (returnTextID == 1) {
                    return R.id.tile11;
                }
                if (returnTextID == 2) {
                    return R.id.frame11;
                }
                else {
                    return R.id.et11;
                }
            }
            case 12: {
                if (returnTextID == 0) {
                    return R.id.text12;
                }
                if (returnTextID == 1) {
                    return R.id.tile12;
                }
                if (returnTextID == 2) {
                    return R.id.frame12;
                }
                else {
                    return R.id.et12;
                }
            }
            case 13: {
                if (returnTextID == 0) {
                    return R.id.text13;
                }
                if (returnTextID == 1) {
                    return R.id.tile13;
                }
                if (returnTextID == 2) {
                    return R.id.frame13;
                }
                else {
                    return R.id.et13;
                }
            }
            case 14: {
                if (returnTextID == 0) {
                    return R.id.text14;
                }
                if (returnTextID == 1) {
                    return R.id.tile14;
                }
                if (returnTextID == 2) {
                    return R.id.frame14;
                }
                else {
                    return R.id.et14;
                }
            }
            case 15: {
                if (returnTextID == 0) {
                    return R.id.text15;
                }
                if (returnTextID == 1) {
                    return R.id.tile15;
                }
                if (returnTextID == 2) {
                    return R.id.frame15;
                }
                else {
                    return R.id.et15;
                }
            }
            case 16: {
                if (returnTextID == 0) {
                    return R.id.text16;
                }
                if (returnTextID == 1) {
                    return R.id.tile16;
                }
                if (returnTextID == 2) {
                    return R.id.frame16;
                }
                else {
                    return R.id.et16;
                }
            }
            case 17: {
                if (returnTextID == 0) {
                    return R.id.text17;
                }
                if (returnTextID == 1) {
                    return R.id.tile17;
                }
                if (returnTextID == 2) {
                    return R.id.frame17;
                }
                else {
                    return R.id.et17;
                }
            }
            case 18: {
                if (returnTextID == 0) {
                    return R.id.text18;
                }
                if (returnTextID == 1) {
                    return R.id.tile18;
                }
                if (returnTextID == 2) {
                    return R.id.frame18;
                }
                else {
                    return R.id.et18;
                }
            }
            case 19: {
                if (returnTextID == 0) {
                    return R.id.text19;
                }
                if (returnTextID == 1) {
                    return R.id.tile19;
                }
                if (returnTextID == 2) {
                    return R.id.frame19;
                }
                else {
                    return R.id.et19;
                }
            }
            case 20: {
                if (returnTextID == 0) {
                    return R.id.text20;
                }
                if (returnTextID == 1) {
                    return R.id.tile20;
                }
                if (returnTextID == 2) {
                    return R.id.frame20;
                }
                else {
                    return R.id.et20;
                }
            }
            case 21: {
                if (returnTextID == 0) {
                    return R.id.text21;
                }
                if (returnTextID == 1) {
                    return R.id.tile21;
                }
                if (returnTextID == 2) {
                    return R.id.frame21;
                }
                else {
                    return R.id.et21;
                }
            }
            case 22: {
                if (returnTextID == 0) {
                    return R.id.text22;
                }
                if (returnTextID == 1) {
                    return R.id.tile22;
                }
                if (returnTextID == 2) {
                    return R.id.frame22;
                }
                else {
                    return R.id.et22;
                }
            }
            case 23: {
                if (returnTextID == 0) {
                    return R.id.text23;
                }
                if (returnTextID == 1) {
                    return R.id.tile23;
                }
                if (returnTextID == 2) {
                    return R.id.frame23;
                }
                else {
                    return R.id.et23;
                }
            }
            case 24: {
                if (returnTextID == 0) {
                    return R.id.text24;
                }
                if (returnTextID == 1) {
                    return R.id.tile24;
                }
                if (returnTextID == 2) {
                    return R.id.frame24;
                }
                else {
                    return R.id.et24;
                }
            }
            case 25: {
                if (returnTextID == 0) {
                    return R.id.text25;
                }
                if (returnTextID == 1) {
                    return R.id.tile25;
                }
                if (returnTextID == 2) {
                    return R.id.frame25;
                }
                else {
                    return R.id.et25;
                }
            }
            case 26: {
                if (returnTextID == 0) {
                    return R.id.text26;
                }
                if (returnTextID == 1) {
                    return R.id.tile26;
                }
                if (returnTextID == 2) {
                    return R.id.frame26;
                }
                else {
                    return R.id.et26;
                }
            }
            case 27: {
                if (returnTextID == 0) {
                    return R.id.text27;
                }
                if (returnTextID == 1) {
                    return R.id.tile27;
                }
                if (returnTextID == 2) {
                    return R.id.frame27;
                }
                else {
                    return R.id.et27;
                }
            }
            case 28: {
                if (returnTextID == 0) {
                    return R.id.text28;
                }
                if (returnTextID == 1) {
                    return R.id.tile28;
                }
                if (returnTextID == 2) {
                    return R.id.frame28;
                }
                else {
                    return R.id.et28;
                }
            }
            case 29: {
                if (returnTextID == 0) {
                    return R.id.text29;
                }
                if (returnTextID == 1) {
                    return R.id.tile29;
                }
                if (returnTextID == 2) {
                    return R.id.frame29;
                }
                else {
                    return R.id.et29;
                }
            }
            case 30: {
                if (returnTextID == 0) {
                    return R.id.text30;
                }
                if (returnTextID == 1) {
                    return R.id.tile30;
                }
                if (returnTextID == 2) {
                    return R.id.frame30;
                }
                else {
                    return R.id.et30;
                }
            }
            case 31: {
                if (returnTextID == 0) {
                    return R.id.text31;
                }
                if (returnTextID == 1) {
                    return R.id.tile31;
                }
                if (returnTextID == 2) {
                    return R.id.frame31;
                }
                else {
                    return R.id.et31;
                }
            }
            case 32: {
                if (returnTextID == 0) {
                    return R.id.text32;
                }
                if (returnTextID == 1) {
                    return R.id.tile32;
                }
                if (returnTextID == 2) {
                    return R.id.frame32;
                }
                else {
                    return R.id.et32;
                }
            }
            case 33: {
                if (returnTextID == 0) {
                    return R.id.text33;
                }
                if (returnTextID == 1) {
                    return R.id.tile33;
                }
                if (returnTextID == 2) {
                    return R.id.frame33;
                }
                else {
                    return R.id.et33;
                }
            }
            case 34: {
                if (returnTextID == 0) {
                    return R.id.text34;
                }
                if (returnTextID == 1) {
                    return R.id.tile34;
                }
                if (returnTextID == 2) {
                    return R.id.frame34;
                }
                else {
                    return R.id.et34;
                }
            }
            case 35: {
                if (returnTextID == 0) {
                    return R.id.text35;
                }
                if (returnTextID == 1) {
                    return R.id.tile35;
                }
                if (returnTextID == 2) {
                    return R.id.frame35;
                }
                else {
                    return R.id.et35;
                }
            }
            case 36: {
                if (returnTextID == 0) {
                    return R.id.text36;
                }
                if (returnTextID == 1) {
                    return R.id.tile36;
                }
                if (returnTextID == 2) {
                    return R.id.frame36;
                }
                else {
                    return R.id.et36;
                }
            }
            case 37: {
                if (returnTextID == 0) {
                    return R.id.text37;
                }
                if (returnTextID == 1) {
                    return R.id.tile37;
                }
                if (returnTextID == 2) {
                    return R.id.frame37;
                }
                else {
                    return R.id.et37;
                }
            }
            case 38: {
                if (returnTextID == 0) {
                    return R.id.text38;
                }
                if (returnTextID == 1) {
                    return R.id.tile38;
                }
                if (returnTextID == 2) {
                    return R.id.frame38;
                }
                else {
                    return R.id.et38;
                }
            }
            case 39: {
                if (returnTextID == 0) {
                    return R.id.text39;
                }
                if (returnTextID == 1) {
                    return R.id.tile39;
                }
                if (returnTextID == 2) {
                    return R.id.frame39;
                }
                else {
                    return R.id.et39;
                }
            }
            case 40: {
                if (returnTextID == 0) {
                    return R.id.text40;
                }
                if (returnTextID == 1) {
                    return R.id.tile40;
                }
                if (returnTextID == 2) {
                    return R.id.frame40;
                }
                else {
                    return R.id.et40;
                }
            }
            case 41: {
                if (returnTextID == 0) {
                    return R.id.text41;
                }
                if (returnTextID == 1) {
                    return R.id.tile41;
                }
                if (returnTextID == 2) {
                    return R.id.frame41;
                }
                else {
                    return R.id.et41;
                }
            }
            case 42: {
                if (returnTextID == 0) {
                    return R.id.text42;
                }
                if (returnTextID == 1) {
                    return R.id.tile42;
                }
                if (returnTextID == 2) {
                    return R.id.frame42;
                }
                else {
                    return R.id.et42;
                }
            }
            case 43: {
                if (returnTextID == 0) {
                    return R.id.text43;
                }
                if (returnTextID == 1) {
                    return R.id.tile43;
                }
                if (returnTextID == 2) {
                    return R.id.frame43;
                }
                else {
                    return R.id.et43;
                }
            }
            case 44: {
                if (returnTextID == 0) {
                    return R.id.text44;
                }
                if (returnTextID == 1) {
                    return R.id.tile44;
                }
                if (returnTextID == 2) {
                    return R.id.frame44;
                }
                else {
                    return R.id.et44;
                }
            }
            case 45: {
                if (returnTextID == 0) {
                    return R.id.text45;
                }
                if (returnTextID == 1) {
                    return R.id.tile45;
                }
                if (returnTextID == 2) {
                    return R.id.frame45;
                }
                else {
                    return R.id.et45;
                }
            }
            case 46: {
                if (returnTextID == 0) {
                    return R.id.text46;
                }
                if (returnTextID == 1) {
                    return R.id.tile46;
                }
                if (returnTextID == 2) {
                    return R.id.frame46;
                }
                else {
                    return R.id.et46;
                }
            }
            case 47: {
                if (returnTextID == 0) {
                    return R.id.text47;
                }
                if (returnTextID == 1) {
                    return R.id.tile47;
                }
                if (returnTextID == 2) {
                    return R.id.frame47;
                }
                else {
                    return R.id.et47;
                }
            }
            case 48: {
                if (returnTextID == 0) {
                    return R.id.text48;
                }
                if (returnTextID == 1) {
                    return R.id.tile48;
                }
                if (returnTextID == 2) {
                    return R.id.frame48;
                }
                else {
                    return R.id.et48;
                }
            }
            case 49: {
                if (returnTextID == 0) {
                    return R.id.text49;
                }
                if (returnTextID == 1) {
                    return R.id.tile49;
                }
                if (returnTextID == 2) {
                    return R.id.frame49;
                }
                else {
                    return R.id.et49;
                }
            }
            case 50: {
                if (returnTextID == 0) {
                    return R.id.text50;
                }
                if (returnTextID == 1) {
                    return R.id.tile50;
                }
                if (returnTextID == 2) {
                    return R.id.frame50;
                }
                else {
                    return R.id.et50;
                }
            }
            case 51: {
                if (returnTextID == 0) {
                    return R.id.text51;
                }
                if (returnTextID == 1) {
                    return R.id.tile51;
                }
                if (returnTextID == 2) {
                    return R.id.frame51;
                }
                else {
                    return R.id.et51;
                }
            }
            case 52: {
                if (returnTextID == 0) {
                    return R.id.text52;
                }
                if (returnTextID == 1) {
                    return R.id.tile52;
                }
                if (returnTextID == 2) {
                    return R.id.frame52;
                }
                else {
                    return R.id.et52;
                }
            }
            case 53: {
                if (returnTextID == 0) {
                    return R.id.text53;
                }
                if (returnTextID == 1) {
                    return R.id.tile53;
                }
                if (returnTextID == 2) {
                    return R.id.frame53;
                }
                else {
                    return R.id.et53;
                }
            }
            case 54: {
                if (returnTextID == 0) {
                    return R.id.text54;
                }
                if (returnTextID == 1) {
                    return R.id.tile54;
                }
                if (returnTextID == 2) {
                    return R.id.frame54;
                }
                else {
                    return R.id.et54;
                }
            }
            case 55: {
                if (returnTextID == 0) {
                    return R.id.text55;
                }
                if (returnTextID == 1) {
                    return R.id.tile55;
                }
                if (returnTextID == 2) {
                    return R.id.frame55;
                }
                else {
                    return R.id.et55;
                }
            }
            case 56: {
                if (returnTextID == 0) {
                    return R.id.text56;
                }
                if (returnTextID == 1) {
                    return R.id.tile56;
                }
                if (returnTextID == 2) {
                    return R.id.frame56;
                }
                else {
                    return R.id.et56;
                }
            }
            case 57: {
                if (returnTextID == 0) {
                    return R.id.text57;
                }
                if (returnTextID == 1) {
                    return R.id.tile57;
                }
                if (returnTextID == 2) {
                    return R.id.frame57;
                }
                else {
                    return R.id.et57;
                }
            }
            case 58: {
                if (returnTextID == 0) {
                    return R.id.text58;
                }
                if (returnTextID == 1) {
                    return R.id.tile58;
                }
                if (returnTextID == 2) {
                    return R.id.frame58;
                }
                else {
                    return R.id.et58;
                }
            }
            case 59: {
                if (returnTextID == 0) {
                    return R.id.text59;
                }
                if (returnTextID == 1) {
                    return R.id.tile59;
                }
                if (returnTextID == 2) {
                    return R.id.frame59;
                }
                else {
                    return R.id.et59;
                }
            }
            case 60: {
                if (returnTextID == 0) {
                    return R.id.text60;
                }
                if (returnTextID == 1) {
                    return R.id.tile60;
                }
                if (returnTextID == 2) {
                    return R.id.frame60;
                }
                else {
                    return R.id.et60;
                }
            }
            case 61: {
                if (returnTextID == 0) {
                    return R.id.text61;
                }
                if (returnTextID == 1) {
                    return R.id.tile61;
                }
                if (returnTextID == 2) {
                    return R.id.frame61;
                }
                else {
                    return R.id.et61;
                }
            }
            case 62: {
                if (returnTextID == 0) {
                    return R.id.text62;
                }
                if (returnTextID == 1) {
                    return R.id.tile62;
                }
                if (returnTextID == 2) {
                    return R.id.frame62;
                }
                else {
                    return R.id.et62;
                }
            }
            case 63: {
                if (returnTextID == 0) {
                    return R.id.text63;
                }
                if (returnTextID == 1) {
                    return R.id.tile63;
                }
                if (returnTextID == 2) {
                    return R.id.frame63;
                }
                else {
                    return R.id.et63;
                }
            }
            case 64: {
                if (returnTextID == 0) {
                    return R.id.text64;
                }
                if (returnTextID == 1) {
                    return R.id.tile64;
                }
                if (returnTextID == 2) {
                    return R.id.frame64;
                }
                else {
                    return R.id.et64;
                }
            }
            case 65: {
                if (returnTextID == 0) {
                    return R.id.text65;
                }
                if (returnTextID == 1) {
                    return R.id.tile65;
                }
                if (returnTextID == 2) {
                    return R.id.frame65;
                }
                else {
                    return R.id.et65;
                }
            }
            case 66: {
                if (returnTextID == 0) {
                    return R.id.text66;
                }
                if (returnTextID == 1) {
                    return R.id.tile66;
                }
                if (returnTextID == 2) {
                    return R.id.frame66;
                }
                else {
                    return R.id.et66;
                }
            }
            case 67: {
                if (returnTextID == 0) {
                    return R.id.text67;
                }
                if (returnTextID == 1) {
                    return R.id.tile67;
                }
                if (returnTextID == 2) {
                    return R.id.frame67;
                }
                else {
                    return R.id.et67;
                }
            }
            case 68: {
                if (returnTextID == 0) {
                    return R.id.text68;
                }
                if (returnTextID == 1) {
                    return R.id.tile68;
                }
                if (returnTextID == 2) {
                    return R.id.frame68;
                }
                else {
                    return R.id.et68;
                }
            }
            case 69: {
                if (returnTextID == 0) {
                    return R.id.text69;
                }
                if (returnTextID == 1) {
                    return R.id.tile69;
                }
                if (returnTextID == 2) {
                    return R.id.frame69;
                }
                else {
                    return R.id.et69;
                }
            }
            case 70: {
                if (returnTextID == 0) {
                    return R.id.text70;
                }
                if (returnTextID == 1) {
                    return R.id.tile70;
                }
                if (returnTextID == 2) {
                    return R.id.frame70;
                }
                else {
                    return R.id.et70;
                }
            }
            case 71: {
                if (returnTextID == 0) {
                    return R.id.text71;
                }
                if (returnTextID == 1) {
                    return R.id.tile71;
                }
                if (returnTextID == 2) {
                    return R.id.frame71;
                }
                else {
                    return R.id.et71;
                }
            }
            case 72: {
                if (returnTextID == 0) {
                    return R.id.text72;
                }
                if (returnTextID == 1) {
                    return R.id.tile72;
                }
                if (returnTextID == 2) {
                    return R.id.frame72;
                }
                else {
                    return R.id.et72;
                }
            }
            case 73: {
                if (returnTextID == 0) {
                    return R.id.text73;
                }
                if (returnTextID == 1) {
                    return R.id.tile73;
                }
                if (returnTextID == 2) {
                    return R.id.frame73;
                }
                else {
                    return R.id.et73;
                }
            }
            case 74: {
                if (returnTextID == 0) {
                    return R.id.text74;
                }
                if (returnTextID == 1) {
                    return R.id.tile74;
                }
                if (returnTextID == 2) {
                    return R.id.frame74;
                }
                else {
                    return R.id.et74;
                }
            }
            case 75: {
                if (returnTextID == 0) {
                    return R.id.text75;
                }
                if (returnTextID == 1) {
                    return R.id.tile75;
                }
                if (returnTextID == 2) {
                    return R.id.frame75;
                }
                else {
                    return R.id.et75;
                }
            }
            case 76: {
                if (returnTextID == 0) {
                    return R.id.text76;
                }
                if (returnTextID == 1) {
                    return R.id.tile76;
                }
                if (returnTextID == 2) {
                    return R.id.frame76;
                }
                else {
                    return R.id.et76;
                }
            }
            case 77: {
                if (returnTextID == 0) {
                    return R.id.text77;
                }
                if (returnTextID == 1) {
                    return R.id.tile77;
                }
                if (returnTextID == 2) {
                    return R.id.frame77;
                }
                else {
                    return R.id.et77;
                }
            }
            case 78: {
                if (returnTextID == 0) {
                    return R.id.text78;
                }
                if (returnTextID == 1) {
                    return R.id.tile78;
                }
                if (returnTextID == 2) {
                    return R.id.frame78;
                }
                else {
                    return R.id.et78;
                }
            }
            case 79: {
                if (returnTextID == 0) {
                    return R.id.text79;
                }
                if (returnTextID == 1) {
                    return R.id.tile79;
                }
                if (returnTextID == 2) {
                    return R.id.frame79;
                }
                else {
                    return R.id.et79;
                }
            }
            case 80: {
                if (returnTextID == 0) {
                    return R.id.text80;
                }
                if (returnTextID == 1) {
                    return R.id.tile80;
                }
                if (returnTextID == 2) {
                    return R.id.frame80;
                }
                else {
                    return R.id.et80;
                }
            }
            default:{
                return -1;
            }
        }
    }



}
