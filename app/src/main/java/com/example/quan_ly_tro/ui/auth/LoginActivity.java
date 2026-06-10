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
import com.example.quan_ly_tro.sync.FirebaseSyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

/**
 * Màn hình đăng nhập và phân quyền cực kỳ nghiêm ngặt
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        database = AppDatabase.getDatabase(this);

        // Kiểm tra nếu đã đăng nhập rồi
        if (auth.getCurrentUser() != null) {
            checkUserRoleAndNavigate(auth.getCurrentUser());
        }

        initViews();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> handleLogin());
        
        // Disable register for tenants directly. Landlords create them.
        // For landlords, they can register if needed.
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = Objects.requireNonNull(edtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(edtPassword.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkUserRoleAndNavigate(authResult.getUser()))
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("ĐĂNG NHẬP");
                    Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkUserRoleAndNavigate(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            btnLogin.setEnabled(true);
            btnLogin.setText("ĐĂNG NHẬP");
            return;
        }

        firestore.collection("users").document(firebaseUser.getUid()).get()
            .addOnSuccessListener(doc -> {
                String role = doc.getString("role");
                if (role == null) {
                    role = User.ROLE_TENANT; // Default fallback
                }
                proceedWithRole(firebaseUser, role);
            })
            .addOnFailureListener(e -> {
                // Fallback
                proceedWithRole(firebaseUser, User.ROLE_TENANT);
            });
    }
    
    private void proceedWithRole(FirebaseUser firebaseUser, String role) {
        final String email = firebaseUser.getEmail();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Kiểm tra xem user hiện tại trong máy có giống user đang đăng nhập không
                User existingUser = database.userDao().getUserByUidSync(firebaseUser.getUid());
                
                // Nếu là tài khoản khác (hoặc app mới cài), thì dọn dẹp dữ liệu cũ
                if (existingUser == null) {
                    database.phongDao().deleteAll();
                    database.khachThueDao().deleteAll();
                    database.hoaDonDao().deleteAll();
                    database.thuChiDao().deleteAll();
                    database.hopDongDao().deleteAll();
                    database.suCoDao().deleteAll();
                    database.thongBaoDao().deleteAll();
                    database.userDao().deleteAll(); // Xóa sạch user cũ
                }
                
                // 2. Lưu User hiện tại (Session)
                User user = new User();
                user.setUid(firebaseUser.getUid());
                user.setEmail(email);
                user.setRole(role);
                
                database.userDao().insert(user);

                // 3. Chuyển màn hình
                runOnUiThread(() -> {
                    if (User.ROLE_LANDLORD.equals(role)) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, TenantMainActivity.class));
                    }
                    finish();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("ĐĂNG NHẬP");
                    Toast.makeText(LoginActivity.this, "Lỗi khởi tạo session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
