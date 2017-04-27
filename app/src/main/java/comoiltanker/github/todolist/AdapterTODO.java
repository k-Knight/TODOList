package comoiltanker.github.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import comoiltanker.github.todolist.TODOResurces.DataSetChangedListener;
import comoiltanker.github.todolist.TODOResurces.TODOResourceManager;

public class AdapterTODO extends ArrayAdapter<TODOItem> implements DataSetChangedListener {
    private Activity activityListholder;
    private ArrayList<TODOItem> listItems;

    private okhttp3.OkHttpClient okHttp3Client = new okhttp3.OkHttpClient();
    private OkHttp3Downloader okHttp3Downloader = new OkHttp3Downloader(okHttp3Client);
    private Picasso picasso;

    AdapterTODO(Activity activity) {
        super(activity, R.layout.item_todo_list, TODOResourceManager.getInstance(activity).getAllTODOItems());
        this.activityListholder = activity;
        this.listItems = TODOResourceManager.getInstance(activity).getAllTODOItems();
        notifyDataSetChanged();

        Picasso picasso = new Picasso.Builder(activityListholder)
                .downloader(okHttp3Downloader)
                .build();
    }

    class TODOViewHolder {
        TextView title;
        TextView date;
        CheckBox check;
        Button delButton;
        ImageView image;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        TODOViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new TODOViewHolder();
            LayoutInflater inflater = activityListholder.getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_todo_list, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.todoItemTitle);
            viewHolder.date = (TextView) convertView.findViewById(R.id.todoItemDate);
            viewHolder.check = (CheckBox) convertView.findViewById(R.id.todoItemCheck);
            viewHolder.delButton = (Button) convertView.findViewById(R.id.todoItemDel);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.todoItemImage);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (TODOViewHolder) convertView.getTag();
        }


        viewHolder.title.setText(listItems.get(position).title);

        viewHolder.check.setChecked(listItems.get(position).checked);
        viewHolder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TODOResourceManager.getInstance(activityListholder).updateChecked(
                        listItems.get(position).id, listItems.get(position).checked ^ true, activityListholder);
            }
        });

        viewHolder.date.setText(DateFormat.format("dd-MM-yyyy", listItems.get(position).date));
        viewHolder.delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builderItemDel = new AlertDialog.Builder(activityListholder);
                builderItemDel.setTitle("Please confirm");
                builderItemDel.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        TODOResourceManager.getInstance(activityListholder).deleteItem(listItems.get(position).id, activityListholder);
                        listItems.remove(position);
                        ((AdapterTODO) ((ListView) view.getParent().getParent()).getAdapter()).notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });
                builderItemDel.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                });
                AlertDialog dialog = builderItemDel.create();
                dialog.show();
            }
        });

        if (!listItems.get(position).imageURL.equals("")) {
            final Activity context = activityListholder;
            final ImageView imageView = viewHolder.image;
            picasso.with(context)
                    .load(listItems.get(position).imageURL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error_fallback)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            picasso.with(context)
                                    .load(listItems.get(position).imageURL)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_error_fallback)
                                    .into(imageView);
                        }
                    });
        }
        else {
            Drawable drawable = activityListholder.getResources().getDrawable(R.drawable.ic_error_fallback);
            viewHolder.image.setImageDrawable(drawable);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create todo_contents activity
                Intent intent = new Intent(activityListholder, TODODetailedViewActivity.class);
                intent.putExtra("itemID", listItems.get(position).id);
                activityListholder.startActivity(intent);
            }
        });

        return convertView;
    }
}
