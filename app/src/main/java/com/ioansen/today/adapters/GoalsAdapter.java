package com.ioansen.today.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ioansen.today.Goal;
import com.ioansen.today.Label;
import com.ioansen.today.R;
import com.ioansen.today.repositories.LabelsRepository;

import java.util.Collections;
import java.util.List;


public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder>
    implements SwipeAndDragHelper.ActionCompletionContract {

    private List<Goal> goals;
    private Listener listener;
    private OnStartDragListener onStartDragListener;
    private OnGoalDoneListener onGoalDoneListener;
    private int resourceLayout;
    private Context context;

    public interface Listener {
        void onSwipeGoal(Goal goal, int position);
        void onClickGoal(Goal goal, int position);
        //void onMoveGoal(Goal goal, int oldPos, int newPos);
    }


    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnGoalDoneListener {

        void onGoalDone(Goal goal, int position);
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }
    public void setOnStartDragListener (OnStartDragListener  onStartDragListener){
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnGoalDoneListener(OnGoalDoneListener onGoalDoneListener) {
        this.onGoalDoneListener = onGoalDoneListener;
    }

    public GoalsAdapter(List<Goal> goals, int resourceLayout) {
        this.goals = goals;
        this.resourceLayout = resourceLayout;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements SwipeAndDragHelper.ItemTouchHelperViewHolder {

        private RelativeLayout cardView;

        ViewHolder(RelativeLayout v) {
            super(v);
            cardView = v;
        }

        @Override
        public void onItemSelected() {

            cardView.setBackgroundResource(R.drawable.layout_pressed);
            cardView.findViewById(R.id.overlay).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onItemClear() {
            cardView.setBackgroundResource(R.drawable.layout_transparent_clickable);
            if ( cardView.findViewById(R.id.overlay_boolean).getVisibility() == View.VISIBLE){
                cardView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
            }
        }
    }



    @Override
    public int getItemCount(){
        return goals.size();
    }

    @Override @NonNull
    public GoalsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType){
        RelativeLayout cv = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(resourceLayout, parent, false);

        return new ViewHolder(cv);
    }

    @Override @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position){
        RelativeLayout cardView = holder.cardView;

        final Goal goal = goals.get(position);
        TextView nameView = (TextView) cardView.findViewById(R.id.name_text);
        nameView.setText(goal.getName());

        switch (resourceLayout) {
            case R.layout.card_collection_goal:
                break;

            case R.layout.card_goal:
               /* TextView diffView = (TextView) cardView.findViewById(R.id.diff_text);
                diffView.setText(String.valueOf(goal.getDifficulty()));

                TextView impView = (TextView) cardView.findViewById(R.id.imp_text);
                impView.setText(String.valueOf(goal.getImportance()));*/

                TextView descView = (TextView) cardView.findViewById(R.id.desc_text);
                if (goal.getDescription() != null && !goal.getDescription().equals("")){
                    descView.setText(String.valueOf(goal.getDescription()));
                }else {
                    descView.setHeight(0);
                }

                LabelsRepository labelsRepository = new LabelsRepository().init();
                labelsRepository.loadGoalLabels(goal.getId());
                List<Label> labels = labelsRepository.getLabels();

                LinearLayout labelsLayout = (LinearLayout) cardView.findViewById(R.id.labels_layout);
                labelsLayout.removeAllViews();
                for (Label label: labels){
                    ImageView labelIcon = new ImageView(context);
                    labelIcon.setImageResource(R.drawable.ic_fiber_manual_record_white_14dp);
                    labelIcon.setColorFilter(label.getColor());
                    labelIcon.setScaleType(ImageView.ScaleType.CENTER);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    lp.setMargins(0, 3, 0, 0);
                    lp.gravity = Gravity.CENTER_VERTICAL;
                    labelIcon.setLayoutParams(lp);
                    labelsLayout.addView(labelIcon);
                }

                View overlay = cardView.findViewById(R.id.overlay);
                View overlayBoolean = cardView.findViewById(R.id.overlay_boolean);

                if (goal.isDone()){
                    overlay.setVisibility(View.VISIBLE);
                    overlayBoolean.setVisibility(View.VISIBLE);
                }


                final ImageView doneView = cardView.findViewById(R.id.done);
                if (goal.isDone()) { doneView.setImageResource(R.drawable.ic_done_green_48dp);}
                else { doneView.setImageResource(R.drawable.ic_done_black_48dp); }
                doneView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        onGoalDoneListener.onGoalDone(goals.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                        //doneView.setImageResource(R.drawable.ic_done_green_48dp);
                    }
                });
                ImageView handleView = (ImageView) cardView.findViewById(R.id.handle);
                handleView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        onStartDragListener.onStartDrag(holder);
                        return false;
                    }

                });
                break;
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                listener.onClickGoal(goal, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(goals, i, i + 1);
            }
        } else {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(goals, i, i - 1);
            }
        }
        notifyItemMoved(oldPosition, newPosition);
        //listener.onMoveGoal(goals.get(newPosition), oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        listener.onSwipeGoal(goals.get(position), position);
    }

}
