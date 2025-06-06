package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class WithdrawActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        initFirebase();
        setupCancelButton();
        setupDeleteButton();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupCancelButton() {
        Button cancelBtn = findViewById(R.id.btnCancelDelete);
        cancelBtn.setOnClickListener(v -> closeScreen());
    }

    private void setupDeleteButton() {
        Button deleteBtn = findViewById(R.id.btnDeleteConfirm);
        deleteBtn.setOnClickListener(v -> promptForPassword());
    }

    private void closeScreen() {
        finish();
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }

    private void promptForPassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showToast("로그인된 사용자가 없습니다.");
            return;
        }

        EditText input = new EditText(this);
        input.setHint("비밀번호");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("비밀번호 확인")
                .setMessage("계정 삭제를 위해 비밀번호를 다시 입력하세요.")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String pw = input.getText().toString().trim();
                    if (pw.isEmpty()) {
                        showToast("비밀번호를 입력해주세요.");
                    } else {
                        reauthenticateAndDelete(pw);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // 사용자 상태 갱신 후 Firestore + Auth 삭제
                    user.reload()
                            .addOnSuccessListener(reloadedUser -> {
                                deleteFirestoreDataAndAccount();
                            })
                            .addOnFailureListener(e -> {
                                showToast("사용자 정보 갱신 실패: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e ->
                        showToast("재인증 실패: " + e.getMessage())
                );
    }

    private void deleteFirestoreDataAndAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        CollectionReference favRef = firestore
                .collection("users")
                .document(uid)
                .collection("favorites");

        favRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                showToast("즐겨찾기 조회 실패: " +
                        task.getException().getMessage());
                return;
            }

            WriteBatch batch = firestore.batch();
            task.getResult().forEach(doc -> batch.delete(doc.getReference()));

            batch.commit()
                    .addOnSuccessListener(aVoid ->
                            deleteUserDocAndAuth(user)
                    )
                    .addOnFailureListener(e ->
                            showToast("즐겨찾기 삭제 실패: " + e.getMessage())
                    );
        });
    }

    private void deleteUserDocAndAuth(FirebaseUser user) {
        String uid = user.getUid();

        firestore.collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    showToast("탈퇴가 완료되었습니다.");
                                    auth.signOut();
                                    navigateToLogin();
                                } else {
                                    showToast("계정 삭제 실패: " +
                                            authTask.getException().getMessage());
                                }
                            });
                })
                .addOnFailureListener(e ->
                        showToast("회원 정보 삭제 실패: " + e.getMessage())
                );
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );
        startActivity(intent);
        finish();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
