package com.example.capstonedesign.settings_information;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountInfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ImageView imgProfile; // CircleImageView -> ImageView
    private TextView tvEmail, tvName, tvAge, tvHeight, tvGender;
    private ImageButton btnAccountBack;
    private LinearLayout btnLogout;
    private LinearLayout containerSeason, containerLeisure;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Uri tempImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();
        setupLaunchers();

        imgProfile.setOnClickListener(v -> showPhotoSourceDialog());
        btnAccountBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());

        loadUserInfo();
    }

    private void initializeViews() {
        imgProfile = findViewById(R.id.imgProfile);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvHeight = findViewById(R.id.tvHeight);
        tvGender = findViewById(R.id.tvGender);
        containerSeason = findViewById(R.id.containerSeason);
        containerLeisure = findViewById(R.id.containerLeisure);
        btnAccountBack = findViewById(R.id.btnAccountBack);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        if (tempImageUri != null) {
                            uploadImageToFirebase(tempImageUri);
                        }
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showPhotoSourceDialog() {
        final CharSequence[] options = {"사진 촬영", "갤러리에서 선택", "기본 이미지로 변경"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("프로필 사진 설정");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("사진 촬영")) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else if (options[item].equals("갤러리에서 선택")) {
                openGallery();
            } else if (options[item].equals("기본 이미지로 변경")) {
                setDefaultProfileImage();
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            tempImageUri = FileProvider.getUriForFile(this,
                    "com.example.capstonedesign.fileprovider",
                    photoFile);
            takePictureLauncher.launch(tempImageUri);
        } catch (IOException ex) {
            Toast.makeText(this, "사진 파일을 생성하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void logout() {
        mAuth.signOut();
        SharedPreferences preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
        preferences.edit().remove("autoLoginEnabled").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        tvEmail.setText(currentUser.getEmail());
        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvName.setText(doc.getString("name"));
                        Long age = doc.getLong("age");
                        if (age != null) tvAge.setText(String.valueOf(age));
                        Long height = doc.getLong("height");
                        if (height != null) tvHeight.setText(height + "cm");
                        tvGender.setText(doc.getString("gender"));

                        String profileImageUrl = doc.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Glide를 사용하여 이미지를 로드하고 원형으로 자름
                            Glide.with(this).load(profileImageUrl).circleCrop().into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_default_profile);
                        }

                        List<String> seasonList = (List<String>) doc.get("interestSeasons");
                        List<String> leisureList = (List<String>) doc.get("interestCategory");

                        containerSeason.removeAllViews();
                        containerLeisure.removeAllViews();

                        if (seasonList != null) {
                            for (String season : seasonList) {
                                addCardTo(containerSeason, season, getSeasonImage(season));
                            }
                        }

                        if (leisureList != null) {
                            for (String leisure : leisureList) {
                                addCardTo(containerLeisure, leisure, getLeisureImage(leisure));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AccountInfo", "Firestore 오류", e));
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Glide.with(this).load(imageUri).circleCrop().into(imgProfile);

        StorageReference storageRef = storage.getReference().child("profileImages/" + user.getUid() + "/profile.jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            db.collection("users").document(user.getUid())
                                    .update("profileImageUrl", imageUrl)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(AccountInfoActivity.this, "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(AccountInfoActivity.this, "사진 URL 저장에 실패했습니다.", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e -> Log.e("StorageUpload", "Upload failed", e));
    }

    private void setDefaultProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", FieldValue.delete());
        db.collection("users").document(user.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    imgProfile.setImageResource(R.drawable.ic_default_profile);
                    Toast.makeText(this, "기본 이미지로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "기본 이미지 변경에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    // 수정된 부분: 별도의 XML 레이아웃 파일 없이 동적으로 카드 생성
    private void addCardTo(LinearLayout container, String label, int imageResId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(8, 8, 8, 8); // 간격 조정

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(100), // 카드 너비 (예: 100dp)
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(8), 0, dpToPx(8), 0); // 카드 간 간격
        card.setLayoutParams(params);

        // 이미지 뷰 (프로필 이미지와 동일하게 둥근 배경 적용)
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(80)); // 이미지 크기
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(imageResId);
        imageView.setBackgroundResource(R.drawable.circle_background_light); // 새롭게 생성할 둥근 배경
        imageView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // 이미지와 배경 테두리 사이 간격
        card.addView(imageView);

        // 텍스트 뷰
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dpToPx(8);
        textView.setLayoutParams(textParams);
        textView.setText(label);
        textView.setTextSize(14);
        textView.setTextColor(getColor(android.R.color.black));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        card.addView(textView);

        container.addView(card);
    }

    // dp 값을 픽셀 값으로 변환하는 헬퍼 함수
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getSeasonImage(String season) {
        if(season == null) return R.drawable.ic_question;
        switch (season.trim()) {
            case "봄": return R.drawable.season1;
            case "여름": return R.drawable.season2;
            case "가을": return R.drawable.season3;
            case "겨울": return R.drawable.season4;
            default: return R.drawable.ic_question;
        }
    }

    private int getLeisureImage(String leisure) {
        if(leisure == null) return R.drawable.ic_question;
        switch (leisure.trim()) {
            case "육상 스포츠": return R.drawable.group1;
            case "해상 스포츠": return R.drawable.group2;
            case "항공 스포츠": return R.drawable.group3;
            default: return R.drawable.ic_question;
        }
    }
}