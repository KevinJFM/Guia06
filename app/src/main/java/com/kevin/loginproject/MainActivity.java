package com.kevin.loginproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    // Variables para gestionar FirebaseAuth y Google SignIn
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    Button btnCerraSesion, btnEliminarCuenta;
    Button btnAcercaDe;
    Button btnMainActivity2;
    Button btnPoliticasDe;
    private TextView userNombre, userEmail, userID;
    private CircleImageView userImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        userNombre = findViewById(R.id.userNombre);
        userEmail = findViewById(R.id.userEmail);
        userID = findViewById(R.id.userId);
        userImg = findViewById(R.id.userImagen);
        btnCerraSesion = findViewById(R.id.btnLogout); // Botón de cerrar sesión
        btnEliminarCuenta = findViewById(R.id.btnEliminarCta);
        btnAcercaDe = findViewById(R.id.btnAcerca);
        btnMainActivity2 = findViewById(R.id.btnApps);
        btnPoliticasDe = findViewById(R.id.btnPoliticas);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Verificar si ya hay un usuario autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Mostrar datos del usuario
            userID.setText(currentUser.getUid());
            userNombre.setText(currentUser.getDisplayName());
            userEmail.setText(currentUser.getEmail());
            Glide.with(this).load(currentUser.getPhotoUrl()).into(userImg);
        } else {
            // Si no hay usuario, redirigir al login
            Intent intent = new Intent(MainActivity.this, loginActivity.class);
            startActivity(intent);
            finish(); // Cerrar MainActivity para que no quede en el stack
        }

        // Manejar clics en botones
        btnAcercaDe.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AcercaDeActivity.class)));
        btnMainActivity2.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity2.class)));
        btnPoliticasDe.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PoliticasDeActivity.class)));

        // Configurar opciones de Google SignIn
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar el listener del botón de cerrar sesión
        btnCerraSesion.setOnClickListener(view -> logoutUser());

        // Configurar el listener del botón de eliminar cuenta
        btnEliminarCuenta.setOnClickListener(view -> confirmDeleteAccount());
    }

    // Método para confirmar la eliminación de la cuenta
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí", (dialog, which) -> deleteAccount())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Método para eliminar la cuenta del usuario
    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();

                            // Cerrar sesión de Google
                            mGoogleSignInClient.signOut().addOnCompleteListener(this, task1 -> {

                                // Redirigir al usuario al login después de eliminar la cuenta
                                Intent intent = new Intent(MainActivity.this, loginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // Si no se pudo eliminar la cuenta, mostrar el error
                            Toast.makeText(MainActivity.this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "No se puede eliminar la cuenta. Usuario no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cerrar sesion de la cuenta del usuario
    private void logoutUser() {
        // Cerrar sesión de Firebase
        mAuth.signOut();

        // Cerrar sesión de Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Mostrar mensaje de que la sesión fue cerrada
                Toast.makeText(MainActivity.this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();

                // Redirigir al usuario de nuevo a la pantalla de login
                Intent intent = new Intent(MainActivity.this, loginActivity.class);
                startActivity(intent);
                finish(); // Cerrar MainActivity para que no quede en el stack
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }
}
