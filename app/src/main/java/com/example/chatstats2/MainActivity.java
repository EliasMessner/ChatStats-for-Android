package com.example.chatstats2;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static com.example.chatstats2.Util.getFileContentAsString;
import static com.example.chatstats2.Util.getTextFromContentProviderUri;
import static com.example.chatstats2.Util.stackTraceAsString;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;

public class MainActivity extends AppCompatActivity {

    String filePath;
    ActivityResultLauncher<Intent> activityResultLauncher;
    SettingsHandler settingsHandler;
    ChartProvider chartProvider;

    // GUI Elements
    View activityRootView;
    NestedScrollView scrollView;
    TextView fileOkTextView;
    ExpandableTextViewHolder expandableTextView;
    Button showResultsButton;
    LinearLayout resultSettingsView;
    LinearLayout resultsView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        handleActionGetContent(intent);
                    }
                }
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGuiComponents();
        settingsHandler = new SettingsHandler(this);
        chartProvider = new ChartProvider(this);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ((action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SEND_MULTIPLE)) && type != null && type.equals("text/*")) {
            handleActionSend(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializeGuiComponents() {
        activityRootView = findViewById(R.id.mainLayout);
        scrollView = findViewById(R.id.scrollView);
        fileOkTextView = findViewById(R.id.fileOkTextView);
        expandableTextView = findViewById(R.id.expandableTextView);
        resultSettingsView = findViewById(R.id.resultSettingsView);
        resultsView = findViewById(R.id.resultsView);
        showResultsButton = findViewById(R.id.showResultsButton);
        showResultsButton.setOnClickListener(v -> onClickShowResults());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.browse_files_menu_item) {
            browseFiles();
        } else if (item.getItemId() == R.id.enter_filepath_menu_item) {
            askFilePathDialogShow();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void askFilePathDialogShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Path");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            filePath = input.getText().toString();
            if (filePath.isEmpty()) return;
            loadFileInUI(filePath);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void browseFiles() {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        activityResultLauncher.launch(chooseFileIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onClickShowResults() {
        // start ResultsActivity
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("fileText", expandableTextView.getText().toString());
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadFileInUI(String filePath) {
        verifyFileIOPermissions();
        try {
            String fileText = getFileContentAsString(filePath);
            loadTextInUI(fileText);
        } catch (IOException e) {
            sayFileNotFound(stackTraceAsString(e));
        }
    }

    private void verifyFileIOPermissions() {
        askForPermissionAndExplainIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getInteger(R.integer.BROWSE_FILES_REQUEST));
        askForPermissionAndExplainIfNeeded(Manifest.permission.READ_EXTERNAL_STORAGE, getResources().getInteger(R.integer.BROWSE_FILES_REQUEST));
    }

    private void askForPermissionAndExplainIfNeeded(String permission, int PERMISSION_REQUEST_CODE) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                // TODO
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleActionSend(Intent intent) {
        String text = "";
        try {
            Uri uri = intent.getClipData().getItemAt(0).getUri();
            text = getTextFromContentProviderUri(uri, this);
        } catch (IOException | NullPointerException e) {
            text = stackTraceAsString(e);
        } finally {
            loadTextInUI(text);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleActionGetContent(Intent intent) {
        String rawPath = intent.getDataString();
        String decoded = null;
        try {
            decoded = java.net.URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            showParsingResult(stackTraceAsString(e), "Error loading File :/");
        }
        assert decoded != null;
        String filePath = decoded.substring(decoded.lastIndexOf(':') + 1);
        loadFileInUI(filePath);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadTextInUI(String text) {
        String response = checkIfChatCanBeParsed(text);
        showParsingResult(text, response);
    }

    private void sayFileNotFound(String message) {
        fileOkTextView.setText(getResources().getString(R.string.file_not_found_message));
        fileOkTextView.setTextColor(RED);
        expandableTextView.setText(message);
        resultSettingsView.setVisibility(View.GONE);
    }

    private void toggleFileOkTextView(boolean fileOk, String message) {
        if (fileOk) {
            fileOkTextView.setText(getResources().getString(R.string.good_file_message));
            fileOkTextView.setTextColor(GREEN);
        } else {
            fileOkTextView.setText(getResources().getString(R.string.bad_file_message, message));
            fileOkTextView.setTextColor(RED);
        }
    }

    private void showParsingResult(String text, String response) {
        if (response.equals("ok")) {
            toggleFileOkTextView(true, "");
            resultSettingsView.setVisibility(View.VISIBLE);
        } else {
            toggleFileOkTextView(false, response);
            resultSettingsView.setVisibility(View.GONE);
        }
        expandableTextView.setText(text);
    }

    /**
     * Takes a raw WhatsApp-exported text as input and validates it. If the file is ok, returns 
     * "ok", otherwise returns an error message as string.
     * If the file is not ok on first try, it calls setAutoDateClockPref, and tries again.
     * @param text chat text as exported by WhatsApp
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String checkIfChatCanBeParsed(String text) {
        int tries = 2;
        while (tries > 0) {
            try {
                Chat.parseChat(text, settingsHandler.getDateTimePattern(), settingsHandler.getDateTimeLookAhead());
                return "ok";
            } catch (DateTimeParseException | AssertionError | IllegalArgumentException e) {
                if (tries == 2) {
                    settingsHandler.setAutoDateTimePref(text);
                } else {
                    return stackTraceAsString(e);
                }
            }
            tries--;
        }
        return "Error parsing file :/";
    }

}