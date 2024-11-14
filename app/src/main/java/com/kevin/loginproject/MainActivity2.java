package com.kevin.loginproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.kevin.loginproject.Models.Presupuesto;
import com.kevin.loginproject.UI.viewModels.adapters.ChildRecyclerAdapter;
import com.kevin.loginproject.UI.viewModels.adapters.MainRecyclerAdapter;
import com.kevin.loginproject.UI.viewModels.BudgetVM;
import com.kevin.loginproject.databinding.ActivityMain2Binding;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity implements ChildRecyclerAdapter.OnItemChildClickListener {
    private ActivityMain2Binding binding;
    private BudgetVM viewModel;
    private MainRecyclerAdapter mainRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(BudgetVM.class);
        mainRecyclerAdapter = new MainRecyclerAdapter(new ArrayList<>(), MainActivity2.this);
        binding.mainRecyclerView.setAdapter(mainRecyclerAdapter);
        binding.mainRecyclerView.setHasFixedSize(true);
        viewModel.getBudgetLiveData().observe(this, budgets -> {
            mainRecyclerAdapter.setDataList(budgets);
        });
        binding.imgAgregar.setOnClickListener(v -> {
            AddBudget bottomSheet = new AddBudget();
            bottomSheet.show(getSupportFragmentManager(), "addBudgetFragment");
        });
    }
    @Override
    public void onItemChildClick(Presupuesto mainObject) {
        if (mainObject.isActivo()){
            Intent intent = new Intent(MainActivity2.this, DetailBudget.class);
            intent.putExtra("mainBudget", mainObject);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Lista Completada", Toast.LENGTH_SHORT).show();
        }
    }
}
