package com.ioansen.dailyapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.ioansen.dailyapp.database.TodayDatabaseHelper;
import com.ioansen.dailyapp.Goal;
import com.ioansen.dailyapp.Label;
import com.ioansen.dailyapp.R;
import com.ioansen.dailyapp.adapters.LabelsAdapter;
import com.ioansen.dailyapp.repositories.LabelsRepository;

import java.util.List;

public class AddGoalActivity extends AppCompatActivity
        implements ColorChooserDialog.ColorCallback {

    private long id = -1;
    private String description ;
    private int difficulty = 10;
    private int importance = 10;
    private boolean isRecursive = false;
    private boolean isDone = false;

    private ImageView dialogColorIcon;
    private int selectedColor;

    /*the labels entered with*/
    private List<Label> enteredLabels;

    private MaterialDialog labelsListDialog;
    private LabelsRepository goalsLabelRepository;
    private LabelsRepository dialogLabelsRepository;
    private LabelsAdapter goalLabelsAdapter;
    private LabelsAdapter dialogLabelsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);
        setupToolbar();
        setupExtras();
        setupDescription();
        setupImportance(importance);
        setupDifficulty(difficulty);
        setupRecursive();
        setupLabels();
    }


    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupExtras(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Goal goal = extras.getParcelable("Goal");

            id = goal.getId();
            description = goal.getDescription();
            difficulty = goal.getDifficulty();
            importance = goal.getImportance();
            isRecursive = goal.isRecursive();
            isDone = goal.isDone();
            enteredLabels = goal.getLabels();
            String name = goal.getName();
            EditText nameView = (EditText) findViewById(R.id.edit_name);
            nameView.setText(name);
            nameView.setSelection(name.length());
        }
    }

    private void setupDescription(){
        final TextView descText = (TextView) findViewById(R.id.desc_text);
        descText.setText(description);
        View descLayout = findViewById(R.id.description);
        descLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddGoalActivity.this)
                        .title(R.string.description)
                        .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                        .input("", description ,true, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                description = input.toString();
                                descText.setText(description);
                            }
                        })
                        .positiveText(R.string.ok)
                        .show();
            }
        });
    }

    private void setupImportance(final int imp){
        View impLayout = findViewById(R.id.importance);
        final TextView impText = (TextView) findViewById(R.id.importance_text);
        impText.setText(String.valueOf(imp));
        impLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               new MaterialDialog.Builder(AddGoalActivity.this)
                       .title(R.string.importance)
                       .items(R.array.chooseNumber)
                       .itemsCallbackSingleChoice(imp, new MaterialDialog.ListCallbackSingleChoice() {
                           @Override
                           public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                               /*
                                * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                * returning false here won't allow the newly selected radio button to actually be selected.
                                **/
                               importance = Integer.valueOf(text.toString());
                               impText.setText(text.toString());

                               return true;
                           }
                       })
                       .positiveText(R.string.choose)
                       .show();
           }
        });

    }

    private void setupDifficulty(final int diff){
        View diffLayout = findViewById(R.id.difficulty);
        final TextView diffText = (TextView) findViewById(R.id.difficulty_text);
        diffText.setText(String.valueOf(diff));
        diffLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddGoalActivity.this)
                        .title(R.string.difficulty)
                        .items(R.array.chooseNumber)
                        .itemsCallbackSingleChoice(diff, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                /*
                                 * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected radio button to actually be selected.
                                 **/
                                difficulty = Integer.valueOf(text.toString());
                                diffText.setText(text.toString());

                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });

    }

    private void setupRecursive(){
        final TextView recuriveText = (TextView) findViewById(R.id.is_recursive_text);
        if(isRecursive) { recuriveText.setText(R.string.yes); }
        else { recuriveText.setText(R.string.no); }
        View recursiveLayout = findViewById(R.id.is_recursive);
        recursiveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecursive = !isRecursive;
                if(isRecursive) { recuriveText.setText(R.string.yes); }
                else { recuriveText.setText(R.string.no); }
            }
        });
    }

    private void setupGoalLabelsRecyclerView(){
        goalsLabelRepository = new LabelsRepository().init();
        goalsLabelRepository.loadGoalLabels(id);

        /*setup view labels recycler*/
        RecyclerView goalLabelsRecycler = (RecyclerView) findViewById(R.id.labels_recycler);
        goalLabelsAdapter = new LabelsAdapter(goalsLabelRepository.getLabels(), R.layout.label_layout);
        goalLabelsRecycler.setAdapter(goalLabelsAdapter);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        goalLabelsRecycler.setLayoutManager(layoutManager);

        /*setup listener for onAdd/onRemove on repository*/
        goalsLabelRepository.setReadListener(new LabelsRepository.ReadListener() {
            @Override
            public void onAddLabel(int position, Label label) {
                goalLabelsAdapter.notifyItemInserted(position);
            }

            @Override
            public void onRemoveLabel(int position) {
                goalLabelsAdapter.notifyItemRemoved(position);
            }
        });

        /*setup the adapter to handle onLabelClickEvent*/
        goalLabelsAdapter.setOnLabelClickListener(new LabelsAdapter.OnLabelClickListener() {
            @Override
            public void onLabelClick(int position, Label label) {
                goalsLabelRepository.removeLabel(label);
                dialogLabelsRepository.addPreLastLabel(label);
            }
        });

        /*setup the adapter to handle onNewClickEvent*/
        goalLabelsAdapter.setOnInteractClickListener(new LabelsAdapter.OnInteractClickListener() {
            @Override
            public void onClearClick(int position, Label label) {

            }

            @Override
            public void onNewClick() {
                labelsListDialog.show();
            }
        });
        goalLabelsRecycler.setItemAnimator(null);
    }

    private void setupDialogLabelsRecyclerView(){
        dialogLabelsRepository = new LabelsRepository();
        dialogLabelsRepository.loadLabels();

        /*remove the already added labels*/
        dialogLabelsRepository.removeLabelRepository(goalsLabelRepository);
        dialogLabelsAdapter = new LabelsAdapter(dialogLabelsRepository.getLabels(), R.layout.card_label);

        /*setup listener for onAdd/onRemove on repository*/
        dialogLabelsRepository.setReadListener(new LabelsRepository.ReadListener() {
            @Override
            public void onAddLabel(int position, Label label) {
                dialogLabelsAdapter.notifyItemInserted(position);
            }

            @Override
            public void onRemoveLabel(int position) {
                dialogLabelsAdapter.notifyItemRemoved(position);
            }
        });

        /*setup the adapter to handle onLabelClickEvent*/
        dialogLabelsAdapter.setOnLabelClickListener(new LabelsAdapter.OnLabelClickListener() {
            @Override
            public void onLabelClick(int position, Label label) {
                goalsLabelRepository.addPreLastLabel(label);
                dialogLabelsRepository.removeLabel(label);
                //dialog.dismiss();
            }
        });

        /*setup the adapter to handle onNew and onClear*/
        dialogLabelsAdapter.setOnInteractClickListener(new LabelsAdapter.OnInteractClickListener() {

            @Override
            public void onClearClick(final int position, final Label toRemoveLabel) {

                //labelListDialog.dismiss();
                new MaterialDialog.Builder(AddGoalActivity.this)
                        .title("Remove label")
                        .content("Are you sure you want to remove this label and all of it's data?")
                        .positiveText(R.string.yes)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                dialogLabelsRepository.removeLabel(toRemoveLabel);
                                goalsLabelRepository.removeLabel(toRemoveLabel);
                                new TodayDatabaseHelper.RemoveLabelTask().execute(toRemoveLabel);

                            }
                        })
                        .negativeText(R.string.no)
                        .neutralText(R.string.cancel)
                        .show();

            }
            @Override
            public void onNewClick() {

                labelsListDialog.dismiss();
                MaterialDialog newLabelDialog = new MaterialDialog.Builder(AddGoalActivity.this)
                        .title(R.string.new_label)
                        .customView(R.layout.dialog_new_label, false)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                final EditText nameV = (EditText) dialog.getCustomView().findViewById(R.id.name_edit_text);
                                if ( nameV.getText().toString().equals("")){
                                    Toast.makeText(AddGoalActivity.this,"Cannot create no name", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                long id = TodayDatabaseHelper.addDatabaseLabel(new Label(nameV.getText().toString(), selectedColor));
                                Label label = new Label(id, nameV.getText().toString(), selectedColor);
                                dialogLabelsRepository.addPreLastLabel(label);
                                dialog.dismiss();
                                labelsListDialog.show();
                            }
                        })
                        .autoDismiss(false)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build();

                ImageView colorLens = (ImageView) newLabelDialog.getCustomView().findViewById(R.id.color_lens_icon);
                dialogColorIcon = (ImageView) newLabelDialog.getCustomView().findViewById(R.id.color_icon);

                colorLens.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ColorChooserDialog.Builder(AddGoalActivity.this, R.string.add_goal_name)
                                .titleSub(R.string.add_recursive_goal)  // title of dialog when viewing shades of a color
                                .accentMode(false)  // when true, will display accent palette instead of primary palette
                                .doneButton(R.string.accept)  // changes label of the done button
                                .cancelButton(R.string.cancel)  // changes label of the cancel button
                                .backButton(R.string.back)  // changes label of the back button
                                //.preselect(accent ? accentPreselect : primaryPreselect)  // optionally preselects a color
                                .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                                .show(AddGoalActivity.this); // an AppCompatActivity which implements ColorCallback
                    }
                });

                newLabelDialog.show();
            }
        });
    }

    private void setupLabelsListDialog(){
        labelsListDialog = new MaterialDialog.Builder(AddGoalActivity.this)
                .title(R.string.add_label)
                // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                .adapter(dialogLabelsAdapter, new LinearLayoutManager(AddGoalActivity.this))
                .negativeText(R.string.cancel)
                .positiveText(R.string.done)
                .autoDismiss(false)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();

        RecyclerView dialogLabelsRecycler = (RecyclerView) labelsListDialog.getRecyclerView();
        dialogLabelsRecycler.setItemAnimator(null);
    }

    private void setupLabels(){

        setupGoalLabelsRecyclerView();
        setupDialogLabelsRecyclerView();

        /*enteredLabels = new ArrayList<>();
        enteredLabels.addAll(goalsLabelRepository.getLabels());*/

        /*add fake labels to act as buttons*/
        dialogLabelsRepository.addLabel(new Label(-1, "New Label", 0xB3c0c0c0));
        goalsLabelRepository.addLabel(new Label(-1,"Add Label", 0xB3c0c0c0));

        setupLabelsListDialog();

        /*set listener on the labels layout to open the label list dialog*/
        View labelLayout = findViewById(R.id.label);
        labelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelsListDialog.show();
            }
        });
    }

    public void onClickDone(View view) {
        //Code that runs when the FAB is clicked
        EditText nameView = (EditText) findViewById(R.id.edit_name);
        String name = nameView.getText().toString();
        if ( name.equals("")){
            Snackbar.make(findViewById(R.id.root_coordinator), "Cannot create no name", Snackbar.LENGTH_SHORT).show();
        } else {

            Goal goal = new Goal.Builder(name)
                    .id(id)
                    .description(description)
                    .difficulty(difficulty)
                    .importance(importance)
                    .isRecursive(isRecursive)
                    .isDone(isDone)
                    .labels(goalsLabelRepository.getLabels())
                    .build();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("Goal", goal);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();

        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
            this.selectedColor = selectedColor;
            dialogColorIcon.setColorFilter(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }
}
