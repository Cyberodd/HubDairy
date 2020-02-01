package com.hub.dairy.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hub.dairy.R;
import com.hub.dairy.dialogs.CategoryDialog;
import com.hub.dairy.dialogs.ParentDialog;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.Category;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.AVAILABLE;
import static com.hub.dairy.helpers.Constants.CATEGORIES;
import static com.hub.dairy.helpers.Constants.LONG_DATE;
import static com.hub.dairy.helpers.Constants.IMAGE_URL;
import static com.hub.dairy.helpers.Constants.UPLOADS;

public class AnimalActivity extends AppCompatActivity implements CategoryDialog.CategoryInterface,
        ParentDialog.ParentSet {

    private static final String TAG = "AnimalActivity";
    private Toolbar mToolbar;
    private EditText mName, mBreed, mLocation;
    private CircleImageView mAnimalImage;
    private String gender, animalId, mDownloadUrl, mAnimalBreed;
    private String mUserId, mCategory, mAnimalStatus, mRegDate, mAnimalName, mAnimalLocation;
    private Button mSaveInfo;
    private RadioGroup mRadioGroup;
    private RadioButton mRdMale, mRdFemale;
    private int IMAGE_REQUEST_CODE = 1001;
    private LinearLayout displayText;
    private StorageReference mStorageReference;
    private CollectionReference colRef, mCategoryRef;
    private ProgressBar mProgress;
    private Uri imageUri;
    private Spinner mSpinner, mStatus;
    private List<Category> mCategories = new ArrayList<>();
    private TextInputLayout inputName, inputBreed, inputLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference(UPLOADS);
        mCategoryRef = database.collection(CATEGORIES);

        if (user != null) {
            mUserId = user.getUid();
            colRef = database.collection(ANIMALS);
            animalId = colRef.document().getId();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Animal");

        mAnimalImage.setOnClickListener(v -> openGallery());

        mSaveInfo.setOnClickListener(v -> getAnimalInfo());

        updateCategories();

        getStatus();

        listenToRadioButton();
    }

    private void openDialog() {
        CategoryDialog dialog = new CategoryDialog();
        dialog.show(getSupportFragmentManager(), "CategoryDialog");
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

    public void updateCategories() {
        mCategoryRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mCategories.clear();
                mCategories.addAll(queryDocumentSnapshots.toObjects(Category.class));
                loadSpinner(mCategories);
            } else {
                openDialog();
            }
        });
    }

    private void loadSpinner(List<Category> categories) {
        List<String> titles = new ArrayList<>();
        for (Category category : categories) {
            String title = category.getCategoryName();
            titles.add(title);
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setPrompt(getString(R.string.select_a_category));
        mSpinner.setAdapter(adapter);
    }

    private void getStatus() {
        String[] statuses = getResources().getStringArray(R.array.status);
        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatus.setAdapter(statusAdapter);
    }

    private void getAnimalInfo() {
        mCategory = mSpinner.getSelectedItem().toString();
        mAnimalStatus = mStatus.getSelectedItem().toString();
        mRegDate = new SimpleDateFormat(LONG_DATE, Locale.getDefault()).format(new Date());
        mAnimalName = mName.getText().toString().trim();
        mAnimalBreed = mBreed.getText().toString().trim();
        mAnimalLocation = mLocation.getText().toString().trim();

        if (mAnimalStatus.equals("Born on Farm")) {
            initDialog(mCategory);
        } else {
            doProceed("", "");
        }
    }

    private void initDialog(String category) {
        ParentDialog parentDialog = new ParentDialog();
        Bundle args = new Bundle();
        args.putString("category", category);
        parentDialog.setArguments(args);
        parentDialog.show(getSupportFragmentManager(), "ParentDialog");
    }

    private void doProceed(String father, String mother) {
        if (!mCategory.isEmpty()) {
            if (!mAnimalName.isEmpty()) {
                if (!mAnimalBreed.isEmpty()) {
                    if (!mAnimalLocation.isEmpty()) {
                        if (!mAnimalStatus.isEmpty() && !mAnimalStatus.equals("Choose One")) {
                            if (gender != null) {
                                saveInfo(gender, father, mother);
                            } else {
                                Toast.makeText(this, "Please choose animal gender",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Please choose status",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        inputLocation.setError("Please enter animal location");
                    }
                } else {
                    inputBreed.setError("Please enter animal breed");
                }
            } else {
                inputName.setError("Please enter animal name");
            }
        } else {
            Toast.makeText(this, "Add a category first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInfo(String gender, String father, String mother) {
        if (mDownloadUrl != null) {
            doSaveInfo(mDownloadUrl, gender, father, mother);
        } else {
            mDownloadUrl = IMAGE_URL;
            doSaveInfo(mDownloadUrl, gender, father, mother);
        }
    }

    private void doSaveInfo(String downloadUrl, String gender, String father, String mother) {
        Animal animal = new Animal(animalId, mAnimalName, mAnimalBreed, mAnimalLocation, gender,
                mRegDate, downloadUrl, mCategory, mAnimalStatus, AVAILABLE, father, mother, mUserId);
        colRef.document(animalId).set(animal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        toMainActivity();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(),
                Toast.LENGTH_SHORT).show());
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
                        mSaveInfo.setEnabled(false);
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    mSaveInfo.setEnabled(true);
                                    mDownloadUrl = uri.toString();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        e.getMessage(), Toast.LENGTH_SHORT).show());
                        mProgress.setVisibility(View.GONE);
                        Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    });
        } else {
            mProgress.setVisibility(View.GONE);
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
        mSpinner = findViewById(R.id.categorySpinner);
        mStatus = findViewById(R.id.statusSpinner);
        inputName = findViewById(R.id.inputName);
        inputBreed = findViewById(R.id.inputBreed);
        inputLocation = findViewById(R.id.inputLocation);
    }

    @Override
    public void notifyUpdate() {
        updateCategories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.animal_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_category) {
            openDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void proceed(String father, String mother) {
        doProceed(father, mother);
    }
}
