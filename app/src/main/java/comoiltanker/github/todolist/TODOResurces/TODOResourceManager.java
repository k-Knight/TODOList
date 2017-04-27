package comoiltanker.github.todolist.TODOResurces;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import comoiltanker.github.todolist.R;
import comoiltanker.github.todolist.TODOItem;

public class TODOResourceManager {
    protected static class ToDoInfo {
        String title;
        String description;
        String date;
        Boolean checked;
        String imageURL;

        public ToDoInfo() { }
        public ToDoInfo(String title, String description, String date, Boolean checked, String imageURL) {
            this.title = title;
            this.description = description;
            this.date = date;
            this.checked = checked;
            this.imageURL = imageURL;
        }
    }

    private static Context currentContext = null;
    private static TODOResourceManager ourInstance = null;

    private StorageReference fireUserStorage;
    private DatabaseReference fireUserDatabase;

    private ArrayList<TODOItem> todoItems =  new ArrayList<>();
    private List<DataSetChangedListener> onDataChangedListeners = new ArrayList<DataSetChangedListener>();

    private static String APP_PREF_NAME = "TestAppPrefs";
    private static SharedPreferences preferences = null;
    private static SharedPreferences.Editor preferencesEditor = null;
    private static int currentTheme = -1;

    private ProgressDialog mProgress = null;

    public static TODOResourceManager getInstance(Activity activity) {
        if (ourInstance == null)
            ourInstance = new TODOResourceManager(activity);

        if (currentContext != activity.getApplicationContext())
            return null;

        return ourInstance;
    }

    private TODOResourceManager(Activity activity) {
        currentContext = activity.getApplicationContext();
    }

    public ArrayList<TODOItem> getAllTODOItems() {
        return todoItems;
    }

    public TODOItem getTODOItem(String itemID) throws Exception {
        for (TODOItem td : todoItems)
            if (td.id.equals(itemID))
                return td;

        throw new Exception("No such TODO item");
    }

    public void addTODOItem(String title, Calendar date, boolean check, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Creatin new TODO item");
        mProgress.show();

        DatabaseReference newTODOItem = fireUserDatabase.push();
        newTODOItem.setValue(new ToDoInfo(
                title,
                "",
                DataFormatContract.iso8601Format.format(date.getTime()),
                check,
                ""
        )).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void updateDescription(String itemID, String newDescription, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Updating description");
        mProgress.show();

        fireUserDatabase.child(itemID).child("description").setValue(newDescription).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void updateTitle(String itemID, String newTitle, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Updating title");
        mProgress.show();

        fireUserDatabase.child(itemID).child("title").setValue(newTitle).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void updateDate(String itemID, String newISO8601Date, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Updating date");
        mProgress.show();

        fireUserDatabase.child(itemID).child("date").setValue(newISO8601Date).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void updateChecked(String itemID, boolean newCheckedState, Activity activity) {
        mProgress = new ProgressDialog(activity);
        if (newCheckedState)
            mProgress.setMessage("Flagging activity");
        else
            mProgress.setMessage("Unflagging activity");
        mProgress.show();

        fireUserDatabase.child(itemID).child("checked").setValue(newCheckedState).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void updateImage(final String itemID, final Bitmap newImage) throws Exception {

        if (!getTODOItem(itemID).imageURL.equals(""))
            fireUserStorage.child(itemID + ".png").delete();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final StorageReference imageReference = fireUserStorage.child(itemID + ".png");

        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("LOL", "\n.\n.\n Failed to write \n.\n.\n");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    fireUserDatabase.child(itemID).child("imageURL").setValue(downloadUrl.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(currentContext, "Image updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
    }

    public void deleteItem(String itemID, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Deleting TODO item");
        mProgress.show();

        fireUserDatabase.child(itemID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
            }
        });
    }

    public void bindFirebase(String UserId) {
        if (fireUserStorage == null)
            fireUserStorage = FirebaseStorage.getInstance().getReference().child("users").child(UserId);
        if (fireUserDatabase == null) {
            fireUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(UserId);

            fireUserDatabase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (mProgress != null)
                        mProgress.dismiss();
                    final String todoId = dataSnapshot.getKey();
                    for (TODOItem td : todoItems)
                        if (td.id.equals(todoId))
                            return;

                    Calendar newItemDate = new GregorianCalendar();
                    try {
                        newItemDate.setTime(DataFormatContract.iso8601Format.parse(dataSnapshot.child("date").getValue(String.class)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    TODOItem newItem = new TODOItem(
                        todoId,
                        dataSnapshot.child("title").getValue(String.class),
                        newItemDate,
                        dataSnapshot.child("checked").getValue(Boolean.class),
                        dataSnapshot.child("description").getValue(String.class),
                        dataSnapshot.child("imageURL").getValue(String.class)
                    );
                    todoItems.add(newItem);
                    for (DataSetChangedListener dscl : onDataChangedListeners)
                        dscl.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (mProgress != null)
                        mProgress.dismiss();
                    Log.d("LOL", "\n.\n.\n.\n shit lel, starting removing \n.\n.\n.\n");
                    for (TODOItem td : todoItems)
                        if (td.id.equals(dataSnapshot.getKey())) {
                            todoItems.remove(td);
                            Log.d("LOL", "\n.\n.\n.\n removed \n.\n.\n.\n");
                            break;
                        }
                    for (DataSetChangedListener dscl : onDataChangedListeners)
                        dscl.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (mProgress != null)
                        mProgress.dismiss();
                    TODOItem changedTODOItem;
                    for (TODOItem td : todoItems)
                        if (td.id.equals(dataSnapshot.getKey())) {
                            changedTODOItem = td;

                            if (dataSnapshot.hasChild("title"))
                                changedTODOItem.title = dataSnapshot.child("title").getValue(String.class);
                            if (dataSnapshot.hasChild("checked"))
                                changedTODOItem.checked = dataSnapshot.child("checked").getValue(Boolean.class);
                            if (dataSnapshot.hasChild("description"))
                                changedTODOItem.description = dataSnapshot.child("description").getValue(String.class);
                            if (dataSnapshot.hasChild("date")) {
                                try {
                                    changedTODOItem.date.setTime(DataFormatContract.iso8601Format.parse(dataSnapshot.child("date").getValue(String.class)));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (dataSnapshot.hasChild("imageURL")) {
                                changedTODOItem.imageURL = dataSnapshot.child("imageURL").getValue(String.class);

                            }
                            
                            break;
                        }

                    for (DataSetChangedListener dscl : onDataChangedListeners)
                        dscl.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
    }

    public void addListener(DataSetChangedListener newListener) {
        onDataChangedListeners.add(newListener);
    }

    public void removeListener(DataSetChangedListener delListener) {
        onDataChangedListeners.remove(delListener);
    }

    public static int getTheme() {
        if (currentTheme == -1) {
            if (preferences == null || preferencesEditor == null)
                preferences = currentContext.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
            preferencesEditor = preferences.edit();
            currentTheme = preferences.getInt("themeID", R.style.BlueTheme);
        }

        return currentTheme;
    }

    public static void setTheme(int themeID) {
        currentTheme = themeID;
        preferencesEditor.putInt("themeID", currentTheme);
        preferencesEditor.commit();
    }

    public void displayProgressBarTillResponse(String message, Activity activity) {
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage(message);
        mProgress.show();
    }
}
