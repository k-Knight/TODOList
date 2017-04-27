package comoiltanker.github.todolist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.GregorianCalendar;

import comoiltanker.github.todolist.TODOResurces.TODOResourceManager;

public class TODOListActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private AdapterTODO adapterTODOList;
    private TODOResourceManager resourceManager;
    private FirebaseAuth fireAuth;
    private int currentTheme = 0;
    private static boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (firstRun) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            firstRun = false;
        }
        if (resourceManager == null)
            resourceManager = TODOResourceManager.getInstance(this);
        if (adapterTODOList == null) {
            adapterTODOList = new AdapterTODO(this);
            resourceManager.addListener(adapterTODOList);
        }

        currentTheme = TODOResourceManager.getTheme();
        setTheme(TODOResourceManager.getTheme());
        setContentView(R.layout.activity_todolist);

        fireAuth = FirebaseAuth.getInstance();
        if (fireAuth.getCurrentUser() != null) {
            // already signed in
            resourceManager.bindFirebase(fireAuth.getCurrentUser().getUid());
        } else {
            // not signed in
            requestFirebaseSignIn();
        }

        ListView listView = (ListView) findViewById(R.id.todoList);
        listView.setAdapter(adapterTODOList);

        final EditText newItemTitle = (EditText) findViewById(R.id.addTODOItemText);
        Button addButton = (Button) findViewById(R.id.addTODOItemButton);
        final Activity currentActivity = this;

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTitle = newItemTitle.getText().toString();
                if (newTitle.matches(".+")) {
                    resourceManager.addTODOItem(newTitle, new GregorianCalendar(), false, currentActivity);
                    newItemTitle.setText("");
                    TODOListActivity.hideKeyboard(TODOListActivity.this);
                }
            }
        });

        hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsMenuItem: {
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
            } break;
            case R.id.signOutMenuItem: {
                fireAuth.signOut();
                recreate();
            } break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void requestFirebaseSignIn() {
        new AlertDialog.Builder(this)
                .setTitle("Firebase")
                .setMessage("To use this application you need to sing into Firebase first.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(
                                // Get an instance of AuthUI based on the default app
                                AuthUI.getInstance().createSignInIntentBuilder()
                                        .setProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                        ))
                                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                        .build(),
                                RC_SIGN_IN);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .show();
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        View emptyView = findViewById(R.id.firstFocus);
        emptyView.requestFocus();
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != TODOResourceManager.getTheme())
            recreate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if user tried to sign in.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                Toast.makeText(this, "Sign in successful", Toast.LENGTH_LONG).show();
                resourceManager.bindFirebase(fireAuth.getCurrentUser().getUid());
                return;
            } else {
                // Sign in failed
                String errorMsg = new String();
                if (response == null) {
                    // User pressed back button
                    errorMsg += "You did not sign in.";
                }
                else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    errorMsg += "No network available.";
                }
                else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    errorMsg += "Unknown error occurred during sign in process.";
                }

                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                requestFirebaseSignIn();
                return;
            }
        }
    }
}
