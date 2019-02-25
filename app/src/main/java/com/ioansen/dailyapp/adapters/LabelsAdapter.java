package com.ioansen.dailyapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioansen.dailyapp.Label;
import com.ioansen.dailyapp.R;

import java.util.List;

public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.ViewHolder>{

    private List<Label> labels;
    private OnLabelClickListener onLabelClickListener;
    private OnInteractClickListener interactClickListener;
    private int resourceLayout;

    public LabelsAdapter(List<Label> labels, int resourceLayout) {
        this.labels = labels;
        this.resourceLayout = resourceLayout;
    }

    public interface OnLabelClickListener {
        void onLabelClick(int position, Label label);

    }

    public interface OnInteractClickListener{
        void onClearClick(int position, Label label);
        void onNewClick();
    }

    public void setOnLabelClickListener(OnLabelClickListener onLabelClickListener){
        this.onLabelClickListener = onLabelClickListener;
    }

    public void setOnInteractClickListener(OnInteractClickListener onInteractClickListener){
        interactClickListener = onInteractClickListener;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout cardView;

        ViewHolder(LinearLayout v) {
            super(v);
            cardView = v;
        }
    }

    @Override
    public int getItemCount(){
        return labels.size();
    }

    @Override @NonNull
    public LabelsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType){
        LinearLayout cv = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(resourceLayout, parent, false);

        return new ViewHolder(cv);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final LinearLayout cardView = holder.cardView;
        final Label label = labels.get(position);

        TextView textView = (TextView) cardView.findViewById(R.id.label_text);
        textView.setText(label.getText());

        ImageView colorView = (ImageView) cardView.findViewById(R.id.label_icon);
        colorView.setColorFilter(label.getColor());

        switch (resourceLayout){
            case R.layout.card_label:
                ImageView clearView = (ImageView) cardView.findViewById(R.id.label_clear);

                if(label.getId() < 0) {
                    clearView.setVisibility(View.INVISIBLE);
                    clearView.setFocusable(false);
                    clearView.setClickable(false);
                    colorView.setImageResource(R.drawable.ic_add_black_24dp);
                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            interactClickListener.onNewClick();
                        }
                    });
                } else {

                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v){
                            onLabelClickListener.onLabelClick(holder.getAdapterPosition(), label);
                        }
                    });
                    clearView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            interactClickListener.onClearClick(holder.getAdapterPosition(), label);
                        }
                    });
                }

                break;
            case R.layout.label_layout:
                if(label.getId() < 0) {
                    colorView.setImageResource(R.drawable.ic_add_black_14dp);
                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            interactClickListener.onNewClick();
                        }
                    });
                } else {
                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v){
                            onLabelClickListener.onLabelClick(holder.getAdapterPosition(), label);
                        }
                    });
                }
                break;
        }

    }
}
