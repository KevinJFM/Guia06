package com.kevin.loginproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class loginActivity extends AppCompatActivity {
    private final String TAG = "loginActivity";
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignInUp;
    private Button btngoogleButton;
    private TextView tvSignInUp;
    private TextView forgotPasswordLink;
    private boolean createNewAccount = true;
    private ActivityResultLauncher<Intent> signInLauncher;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setListeners();

        mAuth = FirebaseAuth.getInstance();

        configureGoogleSignIn();

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Google sign in failed", e);
                        }
                    }
                });

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btngoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    // Método para iniciar sesión con Google
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Navegar a la pantalla principal (MainActivity)
    private void irHome() {
        Intent intent = new Intent(loginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignInUp = findViewById(R.id.btnSignInUp);
        btngoogleButton = findViewById(R.id.btngoogleButton);
        tvSignInUp = findViewById(R.id.tvSignInUp);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
    }

    // Configurar los listeners para los botones y enlaces
    private void setListeners() {
        // Enlace para recuperar contraseña
        forgotPasswordLink.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (email.isEmpty()) {
                etEmail.setError("Por favor, ingresa tu correo electrónico");
                return;
            }
            sendPasswordResetEmail(email);
        });

        // Cambio entre registro e inicio de sesión
        tvSignInUp.setOnClickListener(view -> {
            createNewAccount = !createNewAccount;
            if (createNewAccount) {
                onChangeContent(R.string.create_account, R.string.sign_in_free);
            } else {
                onChangeContent(R.string.action_sign_in, R.string.create_account);
            }
        });

        // Inicio de sesión o registro
        btnSignInUp.setOnClickListener(view -> {
            if (checkCredentials(etEmail.getText().toString(), etPassword.getText().toString())) {
                if (createNewAccount) {
                    registerNewUser(etEmail.getText().toString(), etPassword.getText().toString());
                } else {
                    loginUser(etEmail.getText().toString(), etPassword.getText().toString());
                }
            }
        });

        // Iniciar sesión con Google
        btngoogleButton.setOnClickListener(view -> signInWithGoogle());
    }

    // Configurar opciones para inicio de sesión con Google
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Iniciar sesión con Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificar la conexión a internet antes de continuar
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Conéctese a Internet", Toast.LENGTH_SHORT).show();
            return; // Salir si no hay conexión
        }

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error de autenticación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            irHome();
                        } else {
                            Toast.makeText(loginActivity.this, "Fallo en la autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Autenticar con Firebase usando Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(loginActivity.this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Enviar correo de recuperación de contraseña
    private void sendPasswordResetEmail(String email) {
        // Verificar la conexión a internet antes de proceder
        if (!isNetworkConnected()) {
            Toast.makeText(loginActivity.this, "Conéctese a Internet", Toast.LENGTH_SHORT).show();
            return; // Salir si no hay conexión
        }

        // Crear un AlertDialog para preguntar al usuario si desea enviar el correo de recuperación
        new AlertDialog.Builder(this)
                .setMessage("¿Quieres enviar un correo de recuperación de contraseña?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(loginActivity.this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(loginActivity.this, "Error al enviar el correo de recuperación", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Si selecciona "No", se cierra el diálogo
                .show();
    }

    // Verificar credenciales de usuario
    private boolean checkCredentials(String email, String password) {
        if (!email.contains("@") || email.length() < 6) {
            etEmail.setError("Correo electrónico no válido");
            etEmail.requestFocus();
            return false;
        } else if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    // Registrar nuevo usuario
    private void registerNewUser(String email, String password) {
        // Verificar la conexión a internet antes de proceder
        if (!isNetworkConnected()) {
            Toast.makeText(loginActivity.this, "Conéctese a Internet", Toast.LENGTH_SHORT).show();
            return; // Salir si no hay conexión
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        sendEmailVerification(user);
                    } else {
                        Toast.makeText(loginActivity.this, "Error: Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Enviar correo de verificación
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verifica tu correo para completar el registro", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Iniciar sesión con usuario y contraseña
    private void loginUser(String email, String password) {
        // Verificar la conexión a internet antes de proceder
        if (!isNetworkConnected()) {
            Toast.makeText(loginActivity.this, "Conéctese a Internet", Toast.LENGTH_SHORT).show();
            return; // Salir si no hay conexión
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(loginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Actualizar la interfaz de usuario al iniciar sesión
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Cambiar el texto del botón y enlace de registro/inicio de sesión
    private void onChangeContent(int btnTextId, int textViewTextId) {
        tvSignInUp.setText(textViewTextId);
        btnSignInUp.setText(btnTextId);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irHome();
        }
    }
}