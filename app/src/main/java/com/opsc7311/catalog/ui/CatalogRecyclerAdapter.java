package com.opsc7311.catalog.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.opsc7311.catalog.R;
import com.opsc7311.catalog.model.Catalog;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CatalogRecyclerAdapter extends RecyclerView.Adapter<CatalogRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Catalog> catalogList;


    public CatalogRecyclerAdapter(Context context, List<Catalog> catalogList) {
        this.context = context;
        this.catalogList = catalogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.catalog_row, viewGroup, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogRecyclerAdapter.ViewHolder viewHolder, int position) {
        Catalog catalog = catalogList.get(position);
        String imageUrl;

        viewHolder.title.setText(catalog.getTitle());
        viewHolder.category.setText(catalog.getCategory());
        viewHolder.description.setText(catalog.getDescription());
        viewHolder.name.setText(catalog.getUserName());
        imageUrl = catalog.getImageUrl();
        /**
         * Author: Shaktisinh Jadeja
         * Date: Apr 12, 2018
         * Using android time ago feature:
         * URL: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
         * Code: String timeAgo = DateUtils.getRelativeTimeSpanString()
         */
        // * 1000 --> milliseconds
        String timeAgo = (String) DateUtils
                .getRelativeTimeSpanString(catalog
                        .getTimeAdded().getSeconds() * 1000);
        viewHolder.dateAdded.setText(timeAgo);

        /**
         * Use Picasso library to download and show image
         * Author: Picasso library
         * URL: https://square.github.io/picasso/
         * Code: Picasso.get().load("https://i.imgur.com/DvpvklR.png").into(imageView);
         */
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .fit()
                .into(viewHolder.image);

    }

    /**
     * Get amount of items in catalogList
     * @return
     */
    @Override
    public int getItemCount() {
        return catalogList.size();
    }

    /**
     * Will consist of all the widgets in catalog_row.xml
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        //TextView's
        public TextView title;      // Catalog name
        public TextView category;
        public TextView description;
        public TextView dateAdded;
        public TextView name;       // Current user's name

        //Image View
        public ImageView image;     // Catalog image

        public ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.catalog_title_list);
            category = itemView.findViewById(R.id.catalog_category_list);
            description = itemView.findViewById(R.id.catalog_description_list);
            dateAdded = itemView.findViewById(R.id.catalog_timestamp_list);
            image = itemView.findViewById(R.id.catalog_image_list);
            name = itemView.findViewById(R.id.catalog_row_username);
            deleteButton = itemView.findViewById(R.id.catalog_row_delete_button);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // To-do: Delete a single catalog
                    }
            });

        }
    }
}
