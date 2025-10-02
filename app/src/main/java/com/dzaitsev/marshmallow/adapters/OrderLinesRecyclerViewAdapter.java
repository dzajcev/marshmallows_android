package com.dzaitsev.marshmallow.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.CustomNumberPicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.Setter;

@Setter
public class OrderLinesRecyclerViewAdapter extends AbstractRecyclerViewAdapter<OrderLine, OrderLinesRecyclerViewAdapter.RecyclerViewHolder> {
    private RemoveListener removeListener;

    private SelectGoodListener selectGoodListener;

    private ChangeSumListener changeSumListener;

    private DoneListener doneListener;



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
        private final TextView good;
        private final TextView price;
        private final TextView count;
        private final TextView total;
        private final ImageButton done;

        private final ImageView imageView;
        private final Context context;
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            good = itemView.findViewById(R.id.order_line_name);
            price = itemView.findViewById(R.id.order_line_price);
            total = itemView.findViewById(R.id.order_line_sum);
            imageView = itemView.findViewById(R.id.imageGood);
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
                                    price.setText(String.format("%s", MoneyUtils.moneyWithCurrencyToString(value)));
                                    getItem().setPrice(value);
                                    total.setText(MoneyUtils.moneyWithCurrencyToString(getItem().getPrice() * getItem().getCount()));
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
                                        total.setText(MoneyUtils.moneyWithCurrencyToString(getItem().getPrice() * getItem().getCount()));
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

//            delete = itemView.findViewById(R.id.orderLineDelete);
//            delete.setOnClickListener(v -> {
//                if (removeListener != null) {
//                    int adapterPosition = getAdapterPosition();
//                    removeItem(adapterPosition);
//                    removeListener.onRemove(adapterPosition);
//                }
//            });
            done = itemView.findViewById(R.id.orderLineDone);
            if (doneListener != null) {
                done.setVisibility(View.VISIBLE);
                done.setOnClickListener(v -> {
                    doneListener.onDone(getItem(), RecyclerViewHolder.this);
//                    if (getItem().isDone()) {
//                        delete.setVisibility(View.GONE);
//                    } else {
//                        delete.setVisibility(View.VISIBLE);
//                    }
                });
            }
        }

        public void bind(OrderLine orderLine) {
            super.bind(orderLine);
            good.setText(Optional.ofNullable(orderLine.getGood()).map(Good::getName).orElse(""));
            price.setText(MoneyUtils.moneyWithCurrencyToString(orderLine.getPrice()));
            count.setText(Optional.ofNullable(orderLine.getCount()).map(String::valueOf).orElse(""));
            total.setText(MoneyUtils.moneyWithCurrencyToString(Optional.ofNullable(orderLine.getPrice()).orElse(0d)
                    * Optional.ofNullable(orderLine.getCount()).orElse(0)));
            if (orderLine.getGood() == null) {
                done.setVisibility(View.GONE);
                price.setFocusable(false);
                count.setFocusable(false);
            }else{
                orderLine.getGood().getImages().stream()
                        .filter(Attachment::isPrimary)
                        .findAny()
                        .ifPresent(f -> {
                            ViewTarget<ImageView, Drawable> into = Glide.with(context)
                                    .load(f.getThumbnailUrl())
                                    .centerCrop()
                                    .error(R.drawable.error)
                                    .into(imageView);
                        });
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