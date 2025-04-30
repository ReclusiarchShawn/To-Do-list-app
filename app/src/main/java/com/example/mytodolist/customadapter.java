package com.example.mytodolist;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class customadapter extends RecyclerView.Adapter<customadapter.ViewHolder> {
    private List<modelclass> list;
    private Context context;
    private DB db;

    public customadapter(Context context, List<modelclass> list,DB db) {
        this.context=context;
        this.list=list;
        this.db=db;
    }

    @NonNull
    @Override
    public customadapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customtodo,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        modelclass var = list.get(position);
        int finalposition=position;
        holder.cb.setText(var.getText());
        holder.cb.setChecked(var.isCompleted());
        updateTaskAppearance(holder, var.isCompleted());
        holder.editconstraint.setVisibility(View.GONE);
         holder.date.setText(var.getFormattedDate());
         holder.noteconstraint.setVisibility(View.GONE);
        holder.mainconstraint.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            if (holder.editconstraint.getVisibility() == View.VISIBLE) {
                holder.editconstraint.setVisibility(View.GONE);
            } else {
                holder.editconstraint.setVisibility(View.VISIBLE);
                holder.editText.setText(var.getText());
                holder.editText.requestFocus();
                ((RecyclerView) holder.itemView.getParent()).smoothScrollToPosition(position);
            }
        });
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(var.isCompleted());
        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            var.setCompleted(isChecked);
            db.updateTask(var);
            updateTaskAppearance(holder, isChecked);
            if(isChecked&&!TextUtils.isEmpty(var.getSidenote())){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("SIDE NOTE");
                builder.setMessage(var.getSidenote());
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });
//        holder.cb.setOnClickListener(v -> {
//            holder.isHandlingCheckboxClick = true;
//            holder.cb.setChecked(!holder.cb.isChecked());
//            holder.isHandlingCheckboxClick = false;
//        });

        holder.savebtn.setOnClickListener(v -> {
           String newdate=holder.date.getText().toString();
            String newText = holder.editText.getText().toString().trim();
            if (newText.isEmpty()) {
                Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newText.equals(var.getText())) {
                holder.editconstraint.setVisibility(View.GONE);
                return;
            }
            var.setDate(newdate);
            var.setText(newText);
            if (db.updateTask(var)) {
                notifyItemChanged(position);
                holder.editconstraint.setVisibility(View.GONE);
                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
            }
        });

        holder.delbtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context).setTitle("DELETE").setMessage("Confirm Delete?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteTask(var.getId());
                        list.remove(finalposition);
                        notifyItemRemoved(finalposition);
                        notifyItemRangeChanged(finalposition, list.size());
                    }).setNegativeButton("Cancel", null).show();
        });
        holder.addsidenote.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            if(holder.isNoteShowing){
                holder.noteconstraint.setVisibility(View.GONE);
            }else{
                holder.noteconstraint.setVisibility(View.VISIBLE);
                holder.notetext.setText(var.getSidenote());
                holder.notetext.requestFocus();
                ((RecyclerView) holder.itemView.getParent()).smoothScrollToPosition(position);
            }
        });
        holder.savenote.setOnClickListener(v -> {
            String note=holder.notetext.getText().toString().trim();
            var.setSidenote(note);
            db.updateTask(var);
            holder.noteconstraint.setVisibility(View.GONE);
            holder.isNoteShowing = false;
            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show();
        });


    }
    private void updateTaskAppearance(ViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.cb.setPaintFlags(holder.cb.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mainconstraint.setBackgroundColor(ContextCompat.getColor(context, R.color.completed_task_bg));
        } else {
            holder.cb.setPaintFlags(holder.cb.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.mainconstraint.setBackgroundColor(ContextCompat.getColor(context, R.color.normal_task_bg));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb;
         Button delbtn,savebtn, addsidenote,savenote;
         EditText editText,notetext;
         TextView date;
        boolean isNoteShowing = false;

        ConstraintLayout mainconstraint, editconstraint,noteconstraint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            savenote=itemView.findViewById(R.id.savenote);
            noteconstraint=itemView.findViewById(R.id.noteconstraint);
            notetext=itemView.findViewById(R.id.note);
            addsidenote =itemView.findViewById(R.id.sidenote);
            date=itemView.findViewById(R.id.Autodate);
            mainconstraint=itemView.findViewById(R.id.mainconstraint);
            editconstraint=itemView.findViewById(R.id.editconstraint);
            cb=itemView.findViewById(R.id.checkBox);
            delbtn=itemView.findViewById(R.id.deleteButton);
            //editbtn=itemView.findViewById(R.id.editButton);
            editText=itemView.findViewById(R.id.edittext);
            savebtn=itemView.findViewById(R.id.savebutton);

        }
    }
}
