package com.example.capstonedesign.settings_information;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;

import java.util.regex.Pattern;

public class PasswordChangeActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextView tvNewPasswordHint, tvConfirmPasswordHint;
    private ImageButton btnToggleCurrentPassword, btnToggleNewPassword, btnToggleConfirmPassword;
    private Button btnSubmitPassword;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    private FirebaseAuth mAuth;

    // 비밀번호 정규식: 6자리 이상, 영문, 숫자, 특수문자 포함
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvNewPasswordHint = findViewById(R.id.tvNewPasswordHint);
        tvConfirmPasswordHint = findViewById(R.id.tvConfirmPasswordHint);
        btnSubmitPassword = findViewById(R.id.btnSubmitPassword);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSubmitPassword.setOnClickListener(v -> handleChangePassword());

        // 비밀번호 보기/숨기기 토글 버튼
        btnToggleCurrentPassword.setOnClickListener(v -> {
            isCurrentVisible = !isCurrentVisible;
            togglePasswordVisibility(etCurrentPassword, btnToggleCurrentPassword, isCurrentVisible);
        });
        btnToggleNewPassword.setOnClickListener(v -> {
            isNewVisible = !isNewVisible;
            togglePasswordVisibility(etNewPassword, btnToggleNewPassword, isNewVisible);
        });
        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword, isConfirmVisible);
        });

        // 새 비밀번호 입력 시 실시간 유효성 검사
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateNewPassword(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 확인 입력 시 실시간 일치 여부 검사
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateConfirmPassword(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton, boolean visible) {
        if (visible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_open); // 보이는 눈 아이콘
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_closed); // 감은 눈 아이콘
        }
        editText.setSelection(editText.getText().length());
    }

    private boolean validateNewPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        if (PASSWORD_PATTERN.matcher(newPassword).matches()) {
            tvNewPasswordHint.setVisibility(View.GONE);
            return true;
        } else {
            tvNewPasswordHint.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private boolean validateConfirmPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (newPassword.equals(confirmPassword)) {
            tvConfirmPasswordHint.setVisibility(View.GONE);
            return true;
        } else {
            tvConfirmPasswordHint.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || etConfirmPassword.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateNewPassword() || !validateConfirmPassword()) {
            Toast.makeText(this, "입력한 내용을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "비밀번호 변경 실패: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "기존 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackClicked(View view) {
        finish();
    }
}