package com.example.quan_ly_tro.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_tro.MainActivity;
import com.example.quan_ly_tro.TenantMainActivity;
import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Màn hình Đăng ký cực kỳ tinh gọn và chuẩn xác role
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnSubmit;
    private TextView tvBackToLogin;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        database = AppDatabase.getDatabase(this);

        initViews();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edt_register_email);
        edtPassword = findViewById(R.id.edt_register_password);
        btnSubmit = findViewById(R.id.btn_register_submit);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnSubmit.setOnClickListener(v -> handleRegister());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String email = Objects.requireNonNull(edtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(edtPassword.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang đăng ký...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> setupUserAccount(authResult.getUser()))
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("TẠO TÀI KHOẢN");
                    Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupUserAccount(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;

        final String email = firebaseUser.getEmail();
        final String role = (email != null && email.equalsIgnoreCase("tinhdev9@gmail.com")) 
                ? User.ROLE_LANDLORD 
                : User.ROLE_TENANT;

        // Lưu lên Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", firebaseUser.getUid());
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("ngayTao", System.currentTimeMillis());

        firestore.collection("users").document(firebaseUser.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> saveLocallyAndNavigate(firebaseUser, role))
                .addOnFailureListener(e -> saveLocallyAndNavigate(firebaseUser, role));
    }

    private void saveLocallyAndNavigate(FirebaseUser firebaseUser, String role) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Xóa rác cũ nếu có
                database.phongDao().deleteAll();
                database.khachThueDao().deleteAll();
                database.hoaDonDao().deleteAll();
                database.thuChiDao().deleteAll();
                database.hopDongDao().deleteAll();
                database.suCoDao().deleteAll();
                database.thongBaoDao().deleteAll();

                User user = new User();
                user.setUid(firebaseUser.getUid());
                user.setEmail(firebaseUser.getEmail());
                user.setRole(role);
                database.userDao().insert(user);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    if (User.ROLE_LANDLORD.equals(role)) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        startActivity(new Intent(this, TenantMainActivity.class));
                    }
                    finishAffinity();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Lỗi đăng ký local: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
