package com.ioansen.dailyapp.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ioansen.dailyapp.database.DatabaseManager;
import com.ioansen.dailyapp.database.TodayDatabaseHelper;
import com.ioansen.dailyapp.Day;
import com.ioansen.dailyapp.Goal;
import com.ioansen.dailyapp.Label;
import com.ioansen.dailyapp.R;
import com.ioansen.dailyapp.fragments.GoalListHomeFragment;
import com.ioansen.dailyapp.repositories.DayRepository;
import com.ioansen.dailyapp.repositories.GoalsRepository;
import com.ioansen.dailyapp.repositories.RecursiveGoalsRepository;
import com.ioansen.dailyapp.viewmodels.DayViewModel;
import com.ioansen.dailyapp.viewmodels.GoalsViewModel;


import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import java.time.format.DateTimeFormatter;
import java.util.List;


/**@since 1.0*/
public class DailyHomeActivity extends AppCompatActivity
    implements GoalListHomeFragment.OnFragmentInteractionListener {

    /*static variables to start addGoalActivity with
    * 1 - new goal
    * 2 - edit goal */
    private static final int PICK_ADDED_GOAL_REQUEST = 1;
    private static final int PICK_EDIT_GOAL_REQUEST = 2;

    private FloatingActionsMenu menuMultipleActions;
    private GoalListHomeFragment todayGoalsFrag;
    private GoalsViewModel goalsModel;

    /*this are the elements that need to update
    * on any updateProgress method so i think
    * its better if i only search for them once
    * instead on on very update*/
    private TextView textDone;
    private TextView textLeft;
    private ProgressBar doneProgressBar;

    /*this is the day object for the dailyapp day
    * provided by a dayViewModel*/
    private Day today;

    /**A boolean to check if a remove goal operation is in queue.*/
    private boolean remove;
    /**The goal to remove.
     * {@link #remove}*/
    private Goal removeGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_home);
        setupDatabase();
        setupViewModels();
        setupToolbar();
        setupFragment();
        setupFAB();
        setupNavigationView();

    }

    private void setupDatabase(){
        DatabaseManager.initializeInstance(new TodayDatabaseHelper(this));
    }

    private void setupViewModels(){
        goalsModel = ViewModelProviders.of(this).get(GoalsViewModel.class);
        DayViewModel dayModel = ViewModelProviders.of(this).get(DayViewModel.class);
        dayModel.setDayRepository(new DayRepository());
        dayModel.setToday();
        today = dayModel.getToday();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        TextView textDay = (TextView) findViewById(R.id.text_day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd,  LLL");
        textDay.setText(today.getDate().format(formatter));

        textDone = (TextView) findViewById(R.id.text_done);
        textLeft = (TextView) findViewById(R.id.text_left);
        doneProgressBar = (ProgressBar) findViewById(R.id.goals_progress_bar);
        updateProgress();
    }

    private void setupNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent;
                switch(id){
                    case R.id.nav_goals:
                        intent = new Intent(DailyHomeActivity.this, CollectionActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_calendar:
                        intent = new Intent(DailyHomeActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        break;
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void setupFragment() {
        todayGoalsFrag = (GoalListHomeFragment) getSupportFragmentManager().findFragmentById(R.id.goals_fragment);
    }

    private void setupFAB() {
        final RelativeLayout relativeLayout= (RelativeLayout)findViewById(R.id.fab_layout);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                fam_animator(true);
                //disable(rootLayout);
                relativeLayout.setVisibility(RelativeLayout.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                fam_animator(false);
                //relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                relativeLayout.setVisibility(RelativeLayout.INVISIBLE);

            }
        });
    }

    @Override
    public void onRemoveGoalFromToday(final int position, final Goal goal) {
        goalsModel.removeGoal(position, goal);
        today.descGoalsAttempted();
        if (goal.isDone()) today.descGoalsDone();
        if (remove) {
            TodayDatabaseHelper.removeDatabaseDayGoal(today.getId(), removeGoal.getId());
        }
        removeGoal = goal;
        remove = true;
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_root_layout), "\"" + goal.getName() + "\" removed from dailyapp" ,  Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goalsModel.addGoal(position, goal);
                remove = false;
                today.incGoalsAttempted();
                if (goal.isDone()) today.incGoalsDone();
                updateProgress();
            }
        });
        snackbar.show();
        updateProgress();
    }

    @Override
    public void updateProgress() {

        String displayDone = today.getGoalsDone() + " done";
        textDone.setText(displayDone);
        int goalsLeft = today.getGoalsAttempted() - today.getGoalsDone();
        String displayLeft = goalsLeft  + " left";
        textLeft.setText(displayLeft);

        doneProgressBar.setMax(today.getGoalsAttempted());
        doneProgressBar.setProgress(today.getGoalsDone());
    }

    @Override
    public void onGoalClick(int position, Goal goal) {
        Intent intent = new Intent(this, AddGoalActivity.class);
        intent.putExtra("Goal", goal);
        startActivityForResult(intent, PICK_EDIT_GOAL_REQUEST);

    }

    // Animates the screen when the FloatingActionMenu is opened/closed
    private void fam_animator(Boolean x) {

        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        if (x) {
            AlphaAnimation alpha = new AlphaAnimation(1F, 0.1F);
            alpha.setDuration(100);
            alpha.setFillAfter(true);
            rootLayout.startAnimation(alpha);
        }
        else {
            rootLayout.clearAnimation();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_today, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (remove) {
            TodayDatabaseHelper.removeDatabaseDayGoal(today.getId(), removeGoal.getId());
        }
    }

    /**
     * Called when a new quick qoal is requested by a view click.
     * A quick goal is a goal that besides name it's inheriting all the default values.
     * @param view the view that was clicked*/
    public void onClickQuick(View view) {

        new MaterialDialog.Builder(this)
                .title(R.string.add_today_goal)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(R.string.name_hint, R.string.input_prefill,false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        long id = TodayDatabaseHelper.addDatabaseGoal(new Goal.Builder(input.toString())
                                .build());

                        Goal goal = new Goal.Builder(input.toString())
                                .id(id)
                                .build();

                        goalsModel.addGoal(goal);
                        TodayDatabaseHelper.addDatabaseDayGoal(today.getId(), goal);
                        today.incGoalsAttempted();
                        updateProgress();
                    }
                })
                .positiveText(R.string.ok)
                .show();
        menuMultipleActions.collapse();
    }

    /**
     * Called when a new qoal is requested by a view click.
     * @param view the view that was clicked*/
    public void onClickNew(View view) {
        Intent intent = new Intent(this, AddGoalActivity.class);
        startActivityForResult(intent, PICK_ADDED_GOAL_REQUEST);
        menuMultipleActions.collapse();
    }

    /**
     * Called when a recursive qoal is requested by a view click.
     * A recursive goal is a goal that has it's isRecursive field set to true.
     * @param view the view that was clicked*/
    public void onClickRecursive(View view) {

        final GoalsRepository recursiveRepo = new RecursiveGoalsRepository();
        recursiveRepo.load();

        recursiveRepo.setOnLoadFinishedListener(new GoalsRepository.OnLoadFinishedListener() {
            @Override
            public void onLoadFinished(List<Goal> goals) {
                final long[] bannedIds = goalsModel.getIdsArray();
                String[] names = recursiveRepo.getNamesArray(bannedIds);
                if (names.length == 0){
                    Snackbar.make(findViewById(R.id.coordinator_root_layout), R.string.no_more_recursive_goal_left_to_add ,  Snackbar.LENGTH_LONG).show();
                    return;
                }
                new MaterialDialog.Builder(DailyHomeActivity.this)
                        .title(R.string.recursive_goals)
                        .items(names)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Goal goal = recursiveRepo.getGoals(bannedIds).get(which);
                                TodayDatabaseHelper.addDatabaseDayGoal(today.getId(), goal );
                                today.incGoalsAttempted();
                                goalsModel.addGoal(goal);
                                updateProgress();
                            }
                        })
                        .show();
            }
        });

        menuMultipleActions.collapse();

    }

    /**
     * Called when the overlay view of the fab is clicked
     * @param view the view that was clicked*/
    public void onClickFabLayout(View view) {
        menuMultipleActions.collapse();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_ADDED_GOAL_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();

                if ( extras != null) {
                    Goal retGoal = extras.getParcelable("Goal");
                    long id = TodayDatabaseHelper.addDatabaseGoal(retGoal);

                    Goal goal = new Goal.Builder(retGoal.getName())
                            .id(id)
                            .description(retGoal.getDescription())
                            .difficulty(retGoal.getDifficulty())
                            .importance(retGoal.getImportance())
                            .isRecursive(retGoal.isRecursive())
                            .labels(retGoal.getLabels())
                            .build();

                    goalsModel.addGoal(goal);
                    /*add goal to dailyapp in database*/
                    TodayDatabaseHelper.addDatabaseDayGoal(today.getId(), goal);
                    today.incGoalsAttempted();
                    updateProgress();
                    /*add labels to database*/
                    Label[] addedLabelsArray = new Label[retGoal.getLabels().size()];
                    new TodayDatabaseHelper.InsertGoalLabelsTask().setGoal(id).execute(retGoal.getLabels().toArray(addedLabelsArray));
                }
            }
        }

        if (requestCode == PICK_EDIT_GOAL_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    Goal goal = extras.getParcelable("Goal");

                    TodayDatabaseHelper.updateDatabaseGoal(goal);
                    goalsModel.updateGoal(goal);

                    /*i have to update the labels in the database*/
                    Label[] selectedLabelsArray = new Label[goal.getLabels().size()];
                    new TodayDatabaseHelper.UpdateGoalLabelsTask().setGoal(goal.getId()).execute(goal.getLabels().toArray(selectedLabelsArray));
                }

            }
        }
    }
}
