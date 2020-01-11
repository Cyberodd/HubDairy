package com.hub.dairy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hub.dairy.models.Animal;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.DATE_FORMAT;
import static com.hub.dairy.helpers.Constants.IMAGE_URL;

public class AnimalActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mName, mBreed, mLocation;
    private CircleImageView mAnimalImage;
    private String gender, animalId;
    private Button mSaveInfo, mAddCategory;
    private RadioGroup mRadioGroup;
    private RadioButton mRdMale, mRdFemale;
    private int IMAGE_REQUEST_CODE = 1001;
    private LinearLayout displayText;
    private StorageReference mStorageReference;
    private CollectionReference colRef;
    private String mDownloadUrl;
    private ProgressBar mProgress;
    private Uri imageUri;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        colRef = database.collection(ANIMALS);
        animalId = colRef.document().getId();
        mStorageReference = FirebaseStorage.getInstance().getReference("uploads");

        initViews();

        mSpinner.setPrompt(getString(R.string.select_a_category));

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Animal");

        mAnimalImage.setOnClickListener(v -> openGallery());

        mSaveInfo.setOnClickListener(v -> saveInfo());
        
        mAddCategory.setOnClickListener(v -> openDialog());

        listenToRadioButton();
    }

    private void openDialog() {
        Toast.makeText(this, "Opening dialog", Toast.LENGTH_SHORT).show();
    }

    private void saveInfo() {
        Animal animal = animalInfo();
        if (animal != null) {
            colRef.document(animalId).set(animal)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            toMainActivity();
                        }
                    }).addOnFailureListener(e ->
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Some text fields are missing",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void toMainActivity() {
        Toast.makeText(this, "Animal saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void listenToRadioButton() {
        mRadioGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.gender_male:
                    gender = mRdMale.getText().toString();
                    break;
                case R.id.gender_female:
                    gender = mRdFemale.getText().toString();
                    break;
            }
        }));
    }

    private Animal animalInfo() {

        String regDate = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        String name = mName.getText().toString().trim();
        String breed = mBreed.getText().toString().trim();
        String location = mLocation.getText().toString().trim();

        return gender != null ?
                new Animal(animalId, !name.isEmpty() ? name : "",
                        !breed.isEmpty() ? breed : "",
                        !location.isEmpty() ? location : "", gender, regDate, mDownloadUrl
                ) : null;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        initiateCrop(requestCode, resultCode, data);
    }

    private void initiateCrop(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            try {
                if (resultCode == RESULT_OK && result != null) {
                    imageUri = result.getUri();
                    uploadImage();
                    mAnimalImage.setImageURI(imageUri);
                    displayText.setVisibility(View.GONE);
                } else {
                    assert result != null;
                    String error = result.getError().toString();
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                String error = ex.getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            mProgress.setVisibility(View.VISIBLE);
            StorageReference fileRef = mStorageReference.child(animalId)
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                        taskSnapshot.getTotalByteCount());
                mProgress.incrementProgressBy((int) progress);
            }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(),
                    Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> mDownloadUrl = uri.toString())
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        e.getMessage(), Toast.LENGTH_SHORT).show());
                        mProgress.setVisibility(View.GONE);
                        Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    });
        } else {
            mProgress.setVisibility(View.GONE);
            mDownloadUrl = IMAGE_URL;
        }
    }

    private void initViews() {
        mToolbar = findViewById(R.id.addAnimalToolbar);
        mName = findViewById(R.id.edName);
        mBreed = findViewById(R.id.edBreed);
        mLocation = findViewById(R.id.edLocation);
        mRadioGroup = findViewById(R.id.radio_group);
        mRdMale = findViewById(R.id.gender_male);
        mRdFemale = findViewById(R.id.gender_female);
        mAnimalImage = findViewById(R.id.animalImage);
        displayText = findViewById(R.id.displayText);
        mSaveInfo = findViewById(R.id.btnSaveInfo);
        mProgress = findViewById(R.id.uploadProgress);
        mAddCategory = findViewById(R.id.btnAddCategory);
        mSpinner = findViewById(R.id.categorySpinner);
    }
}
