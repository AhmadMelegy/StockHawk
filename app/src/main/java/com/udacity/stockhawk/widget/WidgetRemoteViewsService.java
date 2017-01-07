package com.udacity.stockhawk.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by ahmad on 1/6/17.
 */


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    public class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private Cursor data = null;

        @Override
        public void onCreate() {

        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) data.close();

            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @SuppressLint("PrivateResource")
        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.list_item_quote);

            String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            String stockHistory = data.getString(Contract.Quote.POSITION_HISTORY);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            int backgroundDrawable;

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+");
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);

            if (absoluteChange > 0) {
                backgroundDrawable = R.drawable.percent_change_pill_green;
            } else {
                backgroundDrawable = R.drawable.percent_change_pill_red;
            }

            remoteViews.setTextViewText(R.id.symbol, stockSymbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
            remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
            remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);
            remoteViews.setInt(R.id.list_item, "setBackgroundResource", R.color.material_grey_850);

            Intent detailsIntent = new Intent(getApplicationContext(), StockDetailsActivity.class);
            detailsIntent.putExtra(MainActivity.STOCK_NAME, stockSymbol);
            detailsIntent.putExtra(MainActivity.STOCK_HISTORY, stockHistory);

            remoteViews.setOnClickFillInIntent(R.id.list_item, detailsIntent);

            return remoteViews;

        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return data.moveToPosition(i) ? data.getLong(Contract.Quote.POSITION_ID) : i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
