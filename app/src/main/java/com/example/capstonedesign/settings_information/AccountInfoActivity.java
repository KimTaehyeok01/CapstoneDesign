package com.example.capstonedesign.settings_information;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class AccountInfoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK    = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference profileImageRef;
    private String uid;

    private TextView tvEmail, tvName, tvAge, tvHeight, tvGender;
    private ImageButton btnAccountBack;
    private TextView textLogout;
    private ImageView arrowLogout;
    private LinearLayout containerSeason, containerLeisure;
    private ImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // View 연결
        imgProfile      = findViewById(R.id.imgProfile);
        tvEmail         = findViewById(R.id.tvEmail);
        tvName          = findViewById(R.id.tvName);
        tvAge           = findViewById(R.id.tvAge);
        tvHeight        = findViewById(R.id.tvHeight);
        tvGender        = findViewById(R.id.tvGender);
        containerSeason = findViewById(R.id.containerSeason);
        containerLeisure= findViewById(R.id.containerLeisure);
        btnAccountBack  = findViewById(R.id.btnAccountBack);
        textLogout      = findViewById(R.id.textLogout);
        arrowLogout     = findViewById(R.id.arrowLogout);

        // 프로필 아이콘 클릭 → 사진 선택 다이얼로그
        imgProfile.setOnClickListener(v -> showImagePickerDialog());

        // 뒤로가기 버튼
        btnAccountBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 로그아웃 처리
        View.OnClickListener logoutClickListener = v -> {
            mAuth.signOut();
            SharedPreferences preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
            preferences.edit().remove("autoLoginEnabled").apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        };
        textLogout.setOnClickListener(logoutClickListener);
        arrowLogout.setOnClickListener(logoutClickListener);

        // 사용자 정보 불러오기
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmail.setText(currentUser.getEmail());
        uid = currentUser.getUid();
        // 프로필 이미지 저장용 참조
        profileImageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("profileImages")
                .child(uid + ".jpg");

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvName.setText(doc.getString("name"));
                        tvAge.setText(String.valueOf(doc.get("age")));
                        tvHeight.setText(String.valueOf(doc.get("height")) + "cm");
                        tvGender.setText(doc.getString("gender"));

                        List<String> seasonList  = (List<String>) doc.get("interestSeasons");
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
                        // 기존에 저장된 프로필 이미지가 있다면 로드
                        String imageUrl = doc.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(imgProfile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("AccountInfo", "Firestore 오류", e);
                });
    }

    // 프로필 사진 선택 다이얼로그
    private void showImagePickerDialog() {
        String[] options = { "사진 찍기", "갤러리에서 선택", "기본 프로필로 되돌리기" };
        new AlertDialog.Builder(this)
                .setTitle("프로필 사진 설정")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: openCamera(); break;
                        case 1: openGallery(); break;
                        case 2: resetToDefaultProfile(); break;
                    }
                })
                .show();
    }

    // 기본 프로필로 되돌리기
    private void resetToDefaultProfile() {
        // 스토리지에서 삭제
        profileImageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Firestore 필드 제거
                    db.collection("users").document(uid)
                            .update("profileImageUrl", "")
                            .addOnSuccessListener(aVoid2 ->
                                    Toast.makeText(this, "기본 프로필로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore 업데이트 실패", Toast.LENGTH_SHORT).show()
                            );
                    // ImageView 리셋
                    imgProfile.setImageResource(R.drawable.ic_default_profile);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "스토리지 삭제 실패", Toast.LENGTH_SHORT).show()
                );
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 카메라 열기
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // 사진 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_IMAGE_PICK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imgProfile.setImageURI(selectedImageUri);
                uploadImageUri(selectedImageUri);
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imgProfile.setImageBitmap(bitmap);
            uploadImageBitmap(bitmap);
        }
    }

    // Uri 이미지 업로드
    private void uploadImageUri(Uri uri) {
        profileImageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        profileImageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String url = downloadUri.toString();
                                    db.collection("users").document(uid)
                                            .update("profileImageUrl", url)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(this, "프로필 이미지 저장 성공", Toast.LENGTH_SHORT).show()
                                            )
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "이미지 URL 저장 실패", Toast.LENGTH_SHORT).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "다운ロード URL 획득 실패", Toast.LENGTH_SHORT).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                );
    }

    // Bitmap 이미지 업로드
    private void uploadImageBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        profileImageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot ->
                        profileImageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String url = downloadUri.toString();
                                    db.collection("users").document(uid)
                                            .update("profileImageUrl", url)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(this, "프로필 이미지 저장 성공", Toast.LENGTH_SHORT).show()
                                            )
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "이미지 URL 저장 실패", Toast.LENGTH_SHORT).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "다운로드 URL 획득 실패", Toast.LENGTH_SHORT).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                );
    }

    private void addCardTo(LinearLayout container, String label, int imageResId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 0, 16, 0);
        card.setLayoutParams(params);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(imageResId);

        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextSize(14);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        card.addView(imageView);
        card.addView(textView);
        container.addView(card);
    }

    private int getSeasonImage(String season) {
        switch (season.trim()) {
            case "봄":   return R.drawable.season1;
            case "여름": return R.drawable.season2;
            case "가을": return R.drawable.season3;
            case "겨울": return R.drawable.season4;
            default:     return R.drawable.ic_question;
        }
    }

    private int getLeisureImage(String leisure) {
        switch (leisure.trim()) {
            case "육상 스포츠": return R.drawable.group1;
            case "해상 스포츠": return R.drawable.group2;
            case "항공 스포츠": return R.drawable.group3;
            default:            return R.drawable.ic_question;
        }
    }
}
