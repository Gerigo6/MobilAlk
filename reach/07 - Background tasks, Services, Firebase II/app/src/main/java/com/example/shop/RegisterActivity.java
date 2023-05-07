package com.example.shop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;
    EditText phoneEditText;
    Spinner spinner;
    RadioGroup gender;

    ImageView reaching;

    EditText cityEditText;

    String userID;

    private FirebaseFirestore mFirestore;

// Initialize Firestore


    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar);

        // Bundle bundle = getIntent().getExtras();
        // int secret_key = bundle.getInt("SECRET_KEY");
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }

        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordAgainEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        spinner = findViewById(R.id.phoneSpinner);
        spinner.setPrompt("Gender");
        gender = findViewById(R.id.accountTypeGroup);
        cityEditText = findViewById(R.id.cityEditText);
        reaching = findViewById(R.id.reaching);
            reaching.setPadding(0,120,0,0);

        mFirestore = FirebaseFirestore.getInstance();

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");
        String city = preferences.getString("city", "");
        String userID = preferences.getString("userId", "");

        userNameEditText.setText(userName);
        passwordEditText.setText(password);
        passwordConfirmEditText.setText(password);
        cityEditText.setText(city);


        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.orszagok, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "Nem egyenlő a jelszó és a megerősítése.");
            return;
        }

        String orszagok = phoneEditText.getText().toString();
        String orszag = spinner.getSelectedItem().toString();

        int genderId = gender.getCheckedRadioButtonId();
        View radioButton = gender.findViewById(genderId);
        int id = gender.indexOfChild(radioButton);
        String genderType =  ((RadioButton)gender.getChildAt(id)).getText().toString();

        Log.i(LOG_TAG, "Regisztrált: " + userName + ", e-mail: " + email);
        // startShopping();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Kérem töltse ki a szükséges mezőket.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){

                Log.d(LOG_TAG, "User created successfully");
                Log.d(LOG_TAG, "User data saved successfully");
                Toast.makeText(RegisterActivity.this, "Sikeres regisztráció", Toast.LENGTH_LONG).show();
                userID = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = mFirestore.collection("users").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("userName", userName);
                user.put("userID", userID);
                user.put("email", email);
                user.put("city", city);
                user.put("orszag", orszag);
                user.put("gender", genderType);
                user.put("phone", phone);
                user.put("postNum", 0);
                user.put("friends", Arrays.asList(userID));
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        NotificationHelper.showNotification(getApplicationContext(), "Üdvözöljük!", "Köszönjük, hogy regisztrált a reach. alkalmazásban.");
                        Log.d(LOG_TAG, "onSuccess: user Profile is created for " + userID);
                    }
                });
                startProfile();
                // Save user data to Firestore

            } else {
                Log.d(LOG_TAG, "User wasn't created successfully");
                Toast.makeText(RegisterActivity.this, "User was't created successfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void cancel(View view) {
        finish();
    }


    private void startProfile() {
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
