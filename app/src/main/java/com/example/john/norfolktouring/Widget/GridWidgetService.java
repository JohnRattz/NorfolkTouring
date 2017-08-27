package com.example.john.norfolktouring.Widget;

/**
 * Created by John on 8/3/2017.
 *
 * This file was inspired by the Udacity course
 * "Advanced Android App Development" - Lesson 7: Widgets.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  Returns `GridRemoteViewsFactory` objects to widgets.
 *  These objects are used to create the widgets' `View` hierarchies (as `RemoteViews`).
 */
public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }

    /**
     * Creates `View` hierarchies for widgets.
     */
    class GridRemoteViewsFactory implements RemoteViewsFactory {
        Context mContext;
        List<String> mCategories;

        public GridRemoteViewsFactory(Context applicationContext) {
            mContext = applicationContext;
            mCategories = NorfolkTouring.getCategoriesList();
        }

        /**
         * Called when the factory is first constructed. The same factory may be shared across
         * multiple RemoteViewAdapters depending on the intent passed.
         */
        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged() {}

        @Override
        public void onDestroy() {}

        @Override
        public int getCount() {
            return mCategories.size();
        }

        /**
         * This method acts like the onBindViewHolder method in an Adapter
         *
         * @param position The current position of the item in the GridView to be displayed
         * @return The RemoteViews object to display for the provided position
         */
        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
            // Set the text.
            views.setTextViewText(R.id.root_view, NorfolkTouring.getCategoriesList().get(position));
            // Add some extra information to the Intent given to the GridView with
            // setPendingIntentTemplate() in WidgetProvider.getGridRemoteView().
            // Setting PendingIntents on individual collection items in widgets is not allowed.
            Bundle extras = new Bundle();
            extras.putInt(MainActivity.EXTRA_CATEGORY_INDX, position);
            Intent categoryFillInIntent = new Intent();
            categoryFillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.root_view, categoryFillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1; // Treat all items in the GridView the same
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}


