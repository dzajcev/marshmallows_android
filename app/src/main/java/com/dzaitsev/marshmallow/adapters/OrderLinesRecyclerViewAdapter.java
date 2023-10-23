package com.dzaitsev.marshmallow.adapters;

import android.content.DialogInterface;
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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OrderLinesRecyclerViewAdapter extends AbstractRecyclerViewAdapter<OrderLine, OrderLinesRecyclerViewAdapter.RecyclerViewHolder> {
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
        void onDone(OrderLine orderLine, RecyclerViewHolder view);
    }

    public interface ChangeSumListener {
        void onChange();
    }

    public class RecyclerViewHolder extends AbstractRecyclerViewHolder<OrderLine> {
        private final TextView npp;
        private final TextView good;
        private final TextView price;
        private final TextView count;
        private final ImageButton done;
        private final ImageButton delete;

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        public RecyclerViewHolder(View itemView) {
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
                    MoneyPicker.builder(getView().getContext())
                            .setTitle("Укажите сумму")
                            .setInitialValue(getItem().getPrice())
                            .setMinValue(1)
                            .setMaxValue(100000)
                            .positiveButton(value -> {
                                if (changeSumListener != null) {
                                    price.setText(String.format("%s", MoneyUtils.getInstance()
                                            .moneyWithCurrencyToString(value)));
                                    getItem().setPrice(value);
                                    changeSumListener.onChange();
                                }
                            })
                            .build()
                            .show();
                }
            });
            count = itemView.findViewById(R.id.order_line_count);
            count.setOnClickListener(v -> {
                if (getItem().getGood() != null) {
                    CustomNumberPicker.builder(getView().getContext())
                            .setTitle("Укажите количество")
                            .setInitialValue(getItem().getCount())
                            .setMinValue(1)
                            .setMaxValue(1000)
                            .positiveButton(new Consumer<>() {
                                @Override
                                public void accept(Integer value) {
                                    if (changeSumListener != null) {
                                        count.setText(String.format("%s", value));
                                        getItem().setCount(value);
                                        changeSumListener.onChange();
                                    }
                                }
                            })
                            .dialogShowListener(new BiConsumer<>() {
                                @Override
                                public void accept(DialogInterface dialogInterface, NumberPicker numberPicker) {
                                    if (getItem().getGood() != null) {
                                        int s = Integer.parseInt(count.getText().toString());
                                        numberPicker.setValue(Math.max(s, numberPicker.getMinValue()));
                                    }
                                }
                            })
                            .build()
                            .show();
                }
            });

            delete = itemView.findViewById(R.id.orderLineDelete);
            delete.setOnClickListener(v -> {
                if (removeListener != null) {
                    int adapterPosition = getAdapterPosition();
                    removeItem(adapterPosition);
                    removeListener.onRemove(adapterPosition);
                }
            });
            done = itemView.findViewById(R.id.orderLineDone);
            if (doneListener != null) {
                done.setVisibility(View.VISIBLE);
                done.setOnClickListener(v -> {
                    doneListener.onDone(getItem(), RecyclerViewHolder.this);
                    if (getItem().isDone()) {
                        delete.setVisibility(View.GONE);
                    } else {
                        delete.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        public void bind(OrderLine orderLine) {
            super.bind(orderLine);
            npp.setText(String.format("#%s", orderLine.getNum()));
            good.setText(Optional.ofNullable(orderLine.getGood()).map(Good::getName).orElse(""));
            price.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(orderLine.getPrice()));
            count.setText(Optional.ofNullable(orderLine.getCount()).map(String::valueOf).orElse(""));
            if (orderLine.getGood() == null) {
                done.setVisibility(View.GONE);
                price.setFocusable(false);
                count.setFocusable(false);
            }
            if (orderLine.isDone()) {
                delete.setVisibility(View.GONE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_line_list_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getShowItems().get(position).isDone()) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.green));
        }
    }

}