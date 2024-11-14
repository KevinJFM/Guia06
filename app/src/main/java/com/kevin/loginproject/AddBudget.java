package com.kevin.loginproject;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.kevin.loginproject.Models.Presupuesto;
import com.kevin.loginproject.UI.viewModels.BudgetVM;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kevin.loginproject.databinding.ActivityAddBudgetBinding;

public class AddBudget extends BottomSheetDialogFragment {

    private ActivityAddBudgetBinding binding;
    private BudgetVM viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(BudgetVM.class);
        binding = ActivityAddBudgetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Configurando el botón "Guardar"
        binding.btnGuardarPresupuesto.setOnClickListener(v -> {
            String titulo = binding.edtATitulo.getText().toString().trim();
            String montoTexto = binding.edtAMonto.getText().toString().trim();

            // Validar los campos
            if (titulo.isEmpty()) {
                Toast.makeText(getContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (montoTexto.isEmpty()) {
                Toast.makeText(getContext(), "El monto no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double monto = Double.parseDouble(montoTexto);

                // Crear el objeto Presupuesto
                Presupuesto mObject = new Presupuesto(titulo, monto, true);

                // Guardar el presupuesto usando el ViewModel
                viewModel.addBudget(
                        mObject,
                        documentReference -> {
                            this.dismiss();
                            Toast.makeText(getContext(), "Guardado correctamente el presupuesto", Toast.LENGTH_SHORT).show();
                        },
                        e -> Toast.makeText(getContext(), "Error: no se guardó el presupuesto", Toast.LENGTH_SHORT).show()
                );
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "El monto ingresado no es válido", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
