package comoiltanker.github.todolist;


import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import comoiltanker.github.todolist.TODOResurces.DataFormatContract;
import comoiltanker.github.todolist.TODOResurces.DataSetChangedListener;
import comoiltanker.github.todolist.TODOResurces.TODOResourceManager;

public class TODODetailedViewActivity extends AppCompatActivity implements DataSetChangedListener {
    private View.OnClickListener shareListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String cb;
            if (tdItem.checked) cb = "\u2611";
            else cb = "\u2610";

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "\t" + cb + "◄" + tdItem.title.toUpperCase() + "►\n\n" + tdItem.description + "\n\n----------\n  " + DateFormat.format("dd/MM/yyyy", tdItem.date);
            String shareSub = tdItem.title;
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        }
    };
    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builderItemDel = new AlertDialog.Builder(detailedViewActivity);
            builderItemDel.setTitle("Please confirm");
            builderItemDel.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    detailedViewActivity.finish();
                    resourceManager.deleteItem(itemID, detailedViewActivity);
                    dialog.dismiss();
                }
            });
            builderItemDel.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
            });
            AlertDialog dialog = builderItemDel.create();
            dialog.show();
        }
    };
    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (selectedTODOField) {
                case 1: {
                    tdItem.description = todoDesc.getText().toString();
                    resourceManager.updateDescription(itemID, todoDesc.getText().toString(), detailedViewActivity);
                } break;
                case 2: {
                    tdItem.title = todoTitle.getText().toString();
                    resourceManager.updateTitle(itemID, todoTitle.getText().toString(), detailedViewActivity);

                } break;
                default: break;
            }
            returnStandartButtons();
        }
    };
    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (selectedTODOField) {
                case 1: {
                    todoDesc.setText(tdItem.description);
                } break;
                case 2: {
                    todoTitle.setText(tdItem.title);
                } break;
                default: break;
            }
            returnStandartButtons();
        }
    };

    private TODOResourceManager resourceManager;
    private TODOItem tdItem;
    private Activity detailedViewActivity = this;
    private String itemID;
    private EditText todoDesc;
    private EditText todoTitle;
    private TextView todoDate;
    private  Button btnDate;
    private Button btnLeft;
    private Button btnRight;
    private ImageView todoImage;
    //private Bitmap mBitmap;
    private boolean editButtonSetActive;
    private int currentTheme = 0;
    private int selectedTODOField = 0;

    private void returnStandartButtons() {
        btnLeft.setText("SHARE");
        btnRight.setText("DELETE");
        btnLeft.setOnClickListener(shareListener);
        btnRight.setOnClickListener(deleteListener);
        editButtonSetActive = false;

        View emptyView = findViewById(R.id.firstFocus);
        emptyView.requestFocus();
    }

    private void convertToEditButtons() {
        btnLeft.setText("SAVE");
        btnLeft.setOnClickListener(saveListener);
        btnRight.setText("CANCEL");
        btnRight.setOnClickListener(cancelListener);
        editButtonSetActive = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentTheme = TODOResourceManager.getTheme();
        setTheme(TODOResourceManager.getTheme());
        setContentView(R.layout.activity_tododetails);

        if (savedInstanceState == null) editButtonSetActive = false;
        else editButtonSetActive = savedInstanceState.getBoolean("editButtonSetActive", false);

        resourceManager = TODOResourceManager.getInstance(this);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("itemID");
        try {
            tdItem = resourceManager.getTODOItem(itemID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        todoTitle = (EditText) findViewById(R.id.todoDetailedTitle);
        todoDesc = (EditText) findViewById(R.id.todoDetailedDesc);
        todoDate = (TextView) findViewById(R.id.todoDetailedDate);
        btnDate = (Button) findViewById(R.id.todoDetailedDateEdit);
        btnLeft = (Button) findViewById(R.id.todoDetailedButtonL);
        btnRight = (Button) findViewById(R.id.todoDetailedButtonR);

        todoTitle.setText(tdItem.title);
        todoDesc.setText(tdItem.description);
        todoDate.setText(DataFormatContract.humanDateFormat.format(tdItem.date.getTime()));

        if (!editButtonSetActive) {
            btnLeft.setOnClickListener(shareListener);
            btnRight.setOnClickListener(deleteListener);
        }
        else convertToEditButtons();

        final Activity context = this;
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog pickDate = new DatePickerDialog(
                        context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar c = Calendar.getInstance();
                                c.set(year, month, dayOfMonth);
                                tdItem.date = c;
                                todoDate.setText(DataFormatContract.humanDateFormat.format(tdItem.date.getTime()));
                                TODOResourceManager.getInstance(context).updateDate(
                                        tdItem.id,
                                        DataFormatContract.iso8601Format.format(tdItem.date.getTime()),
                                        context
                                        );
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                pickDate.setTitle("Pick a date");
                pickDate.show();
            }
        });

        todoDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedTODOField = 1;
                    convertToEditButtons();
                }
                else {
                    selectedTODOField = 0;
                    todoDesc.setText(tdItem.description);
                    returnStandartButtons();
                }
            }
        });

        todoTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedTODOField = 2;
                    convertToEditButtons();
                }
                else {
                    selectedTODOField = 0;
                    todoTitle.setText(tdItem.title);
                    returnStandartButtons();
                }
            }
        });



        todoImage = (ImageView) findViewById(R.id.todoDetailedIamge);
        if (!tdItem.imageURL.equals("")) {
            Picasso.with(context)
                    .load(tdItem.imageURL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error_fallback)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(todoImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(tdItem.imageURL)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_error_fallback)
                                    .into(todoImage);
                        }
                    });
        }
        else {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_error_fallback);
            todoImage.setImageDrawable(drawable);
        }

        todoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = new CharSequence[]{"Take a photo", "Select file"};

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        // Asking permissions in case we don't have them (more advanced API)
                                        ActivityCompat.requestPermissions(
                                                context,
                                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                                                1
                                        );
                                        break;
                                    case 1:
                                        // Asking permissions in case we don't have them (more advanced API)
                                        ActivityCompat.requestPermissions(
                                                context,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                0
                                        );
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != TODOResourceManager.getTheme())
            recreate();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("editButtonSetActive", editButtonSetActive);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 0: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent pickPhoto = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    );
                    startActivityForResult(pickPhoto, 1); // taking piture from gallery (1 is for activity result)
                } else {
                    Toast.makeText(this, "Storage access permission not granted", Toast.LENGTH_LONG).show();
                }
            } break;
            case 1: {
                if (grantResults.length > 1) {
                    boolean permissionsGranted = true;
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        permissionsGranted = false;
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                    }
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        permissionsGranted = false;
                        Toast.makeText(this, "Storege permission is required", Toast.LENGTH_LONG) .show();
                    }
                    if (permissionsGranted) {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePicture.putExtra("return-data", true);
                        startActivityForResult(takePicture, 0); // taking piture from camera (0 is for activity result)
                    }
                }
            } break;

            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults); break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // result from storage
                if (resultCode == RESULT_OK) {

                    Uri selectedImage = data.getData();

                    ContentResolver contentResolver = getContentResolver();
                    InputStream input;
                    try {
                        input = contentResolver.openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        input = null;
                        e.printStackTrace();
                    }

                    if (input != null) {
                        todoImage.setImageBitmap(BitmapFactory.decodeStream(input));
                    }
                }
                break;

            case 0: // result from camera
                Bitmap bitmap;
                if (resultCode == RESULT_OK) {
                    if (data.getExtras() != null)
                        bitmap = (Bitmap) data.getExtras().get("data");
                    else bitmap = null;

                    if (bitmap != null)
                        todoImage.setImageBitmap(bitmap);
                }
                break;
        }

        if (todoImage != null) {
            final Activity context = this;
            TODOResourceManager.getInstance(context).displayProgressBarTillResponse("Updating image", context);
            final Bitmap imageBitmap = ((BitmapDrawable) todoImage.getDrawable()).getBitmap();
            AsyncTask uploadTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    try {
                        TODOResourceManager.getInstance(context).updateImage(tdItem.id, imageBitmap);



                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };
            uploadTask.execute();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        recreate();
    }
}
