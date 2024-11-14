package com.kevin.loginproject.UI.viewModels.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.loginproject.R;

public class ChildViewHolder extends RecyclerView.ViewHolder {
    public TextView txvItemRow;
    public ChildViewHolder(@NonNull View itemView) {
        super(itemView);
        txvItemRow = itemView.findViewById(R.id.txvItemRow);
    }
}



