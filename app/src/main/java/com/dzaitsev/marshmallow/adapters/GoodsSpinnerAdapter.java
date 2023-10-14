//package com.dzaitsev.marshmallow.adapters;
//
//import android.content.Context;
//import android.graphics.Rect;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import androidx.annotation.LayoutRes;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.dzaitsev.marshmallow.R;
//import com.dzaitsev.marshmallow.dto.Good;
//import com.dzaitsev.marshmallow.utils.MoneyUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//public class GoodsSpinnerAdapter extends ArrayAdapter<Good> {
//    private final int group;
//
//    private final int text;
//    private final List<Good> list;
//    private final LayoutInflater inflator;
//
//
//    public GoodsSpinnerAdapter(@NonNull Context context, @LayoutRes int resource,
//                               @LayoutRes int textViewResourceId, @NonNull List<Good> list) {
//        super(context, resource, list);
//        this.list = list;
//        Good good = new Good();
//        good.setId(-1);
//        good.setName("Выберите товар");
//        list.add(0, good);
//        this.inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        this.group = resource;
//        this.text = textViewResourceId;
//    }
//
//
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        View itemView = inflator.inflate(text, parent, false);
//        TextView name = itemView.findViewById(R.id.good_list_spinner_text_name);
//        name.setText(list.get(position).getName());
//        return itemView;
//    }
//
//    @Override
//    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        View itemView = inflator.inflate(group, parent, false);
//        TextView name = itemView.findViewById(R.id.good_list_spinner_dropdown_name);
//        name.setText(list.get(position).getName());
//        TextView price = itemView.findViewById(R.id.good_list_spinner_dropdown_price);
//        price.setText(Optional.ofNullable(list.get(position).getPrice())
//                .map(m-> MoneyUtils.getInstance().moneyWithCurrencyToString(m)).orElse(""));
//        WindowManager windowManager = (WindowManager) itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
//        Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
//        itemView.setMinimumWidth(bounds.right);
//        return itemView;
//    }
//}
