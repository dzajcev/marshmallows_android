package com.dzaitsev.marshmallow.adapters;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.CustomNumberPicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OrderLinesRecyclerViewAdapter extends AbstractRecyclerViewAdapter<OrderLine, OrderLinesRecyclerViewAdapter.OrderLinesViewHolder> {
    private List<OrderLine> orderLines = new ArrayList<>();

    private View view;

    private RemoveListener removeListener;

    private SelectGoodListener selectGoodListener;

    private ChangeSumListener changeSumListener;

    private DoneListener doneListener;

    public void setRemoveListener(RemoveListener removeListener) {
        this.removeListener = removeListener;
    }


    public void setSelectGoodListener(SelectGoodListener selectGoodListener) {
        this.selectGoodListener = selectGoodListener;
    }

    public void setChangeSumListener(ChangeSumListener changeSumListener) {
        this.changeSumListener = changeSumListener;
    }

    public void setDoneListener(DoneListener doneListener) {
        this.doneListener = doneListener;
    }

    public interface RemoveListener {
        void onRemove(int position);
    }

    public interface SelectGoodListener {
        void onSelectGood(OrderLine orderLine);
    }

    public interface DoneListener {
        void onDone(OrderLine orderLine, View view);
    }

    public interface ChangeSumListener {
        void onChange();
    }

    public class OrderLinesViewHolder extends AbstractRecyclerViewHolder<OrderLine> {
        private final TextView npp;
        private final TextView good;
        private final TextView price;
        private final TextView count;
        private int colorId;


        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        public OrderLinesViewHolder(View itemView) {
            super(itemView);
            npp = itemView.findViewById(R.id.order_line_npp);
            good = itemView.findViewById(R.id.order_line_name);
            price = itemView.findViewById(R.id.order_line_price);

            itemView.setOnClickListener(v -> {
                if (selectGoodListener != null) {
                    selectGoodListener.onSelectGood(getItem());
                }
            });
            price.setOnClickListener(v -> {
                if (getItem().getGood() != null) {
                    MoneyPicker.builder(view.getContext())
                            .setTitle("Укажите сумму")
                            .setInitialValue(getItem().getPrice())
                            .setMinValue(1)
                            .setMaxValue(100000)
                            .positiveButton(value -> {
                                price.setText(String.format("%s", MoneyUtils.getInstance()
                                        .moneyWithCurrencyToString(value)));
                                getItem().setPrice(value);
                                changeSumListener.onChange();
                            })
                            .build()
                            .show();
                }
            });
            count = itemView.findViewById(R.id.order_line_count);
            count.setOnClickListener(v -> {
                if (getItem().getGood() != null) {
                    CustomNumberPicker.builder(view.getContext())
                            .setTitle("Укажите количество")
                            .setInitialValue(getItem().getCount())
                            .setMinValue(1)
                            .setMaxValue(1000)
                            .positiveButton(new Consumer<>() {
                                @Override
                                public void accept(Integer value) {
                                    count.setText(String.format("%s", value));
                                    getItem().setCount(value);
                                    changeSumListener.onChange();
                                }
                            })
                            .dialogShowListener(new BiConsumer<>() {
                                @Override
                                public void accept(DialogInterface dialogInterface, NumberPicker numberPicker) {
                                    if (getItem().getGood() != null) {
                                        int s = Integer.parseInt(count.getText().toString());
                                        if (s < numberPicker.getMinValue()) {
                                            numberPicker.setValue(numberPicker.getMinValue());
                                        } else {
                                            numberPicker.setValue(s);
                                        }
                                    }
                                }
                            })
                            .build()
                            .show();
                }
            });

            ImageButton delete = itemView.findViewById(R.id.orderLineDelete);
            delete.setOnClickListener(v -> removeListener.onRemove(getAdapterPosition()));
            ImageButton done = itemView.findViewById(R.id.orderLineDone);
            if (doneListener != null) {
                done.setVisibility(View.VISIBLE);
                done.setOnClickListener(v -> {
                    getItem().setDone(getItem().getDone() == null || !getItem().getDone());
                    if (getItem().getDone()) {
                        getView().setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.green));
                    } else {
                        getView().setBackgroundColor(colorId);
                    }
                    doneListener.onDone(getItem(), itemView);
                });
            }
        }

        public void bind(OrderLine orderLine) {
            super.bind(orderLine);
            ColorDrawable viewColor = (ColorDrawable) getView().getBackground();
            colorId = viewColor.getColor();
            npp.setText(String.format("#%s", orderLine.getNum()));
            good.setText(Optional.ofNullable(orderLine.getGood()).map(Good::getName).orElse(""));
            price.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(orderLine.getPrice()));
            count.setText(Optional.ofNullable(orderLine.getCount()).map(String::valueOf).orElse(""));
            if (orderLine.getGood() == null) {
                price.setFocusable(false);
                count.setFocusable(false);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NonNull
    @Override
    public OrderLinesViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_line_list_item, parent, false);

        return new OrderLinesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderLinesViewHolder holder, int position) {
        if (position % 2 == 0) {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.row_1));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.row_2));
        }
        holder.bind(orderLines.get(position));

    }

    public void setItems(List<OrderLine> items) {
        orderLines = items;
        notifyDataSetChanged();
    }

    public void addLine(OrderLine orderLine) {
        orderLines.add(orderLine);
        notifyItemInserted(orderLines.size() - 1);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return orderLines.size();
    }

    public List<OrderLine> getItems() {
        return orderLines;
    }

}