package com.dzaitsev.marshmallow.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Optional;

import lombok.Setter;

@Setter
public class OrderLinesRecyclerViewAdapter extends AbstractRecyclerViewAdapter<OrderLine, OrderLinesRecyclerViewAdapter.RecyclerViewHolder> {
    private RemoveListener removeListener;

    private SelectGoodListener selectGoodListener;

    private ChangeSumListener changeSumListener;

    private DoneListener doneListener;


    public OrderLinesRecyclerViewAdapter() {
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
        private final TextView titleGood;
        private final EditText price;
        private final TextView count;
        private final TextView total;
        private final CheckBox done;


        private final ImageButton minus;
        private final ImageButton plus;
        private final ImageView imageView;
        private final Context context;

        private boolean lock;

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            titleGood = itemView.findViewById(R.id.titleGood);
            price = itemView.findViewById(R.id.price);
            total = itemView.findViewById(R.id.total);
            imageView = itemView.findViewById(R.id.imageGood);
            count = itemView.findViewById(R.id.count);
            TextView currency = itemView.findViewById(R.id.curr_txt);
            minus = itemView.findViewById(R.id.minus);
            plus = itemView.findViewById(R.id.plus);
            itemView.setOnClickListener(v -> {
                if (lock) {
                    return;
                }
                if (selectGoodListener != null) {
                    selectGoodListener.onSelectGood(getItem());
                }
            });
            View.OnClickListener editPriceClickListener = v -> {
                if (lock) {
                    return;
                }
                price.setFocusable(true);
                price.setFocusableInTouchMode(true);
                price.setCursorVisible(true);
                price.setClickable(true);

                price.post(() -> {
                    price.requestFocus();
                    price.setSelection(price.getText().length());
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(price, InputMethodManager.SHOW_IMPLICIT);
                });
            };
            price.setOnClickListener(editPriceClickListener);
            currency.setOnClickListener(editPriceClickListener);
            done = itemView.findViewById(R.id.done);

            done.setOnCheckedChangeListener((b, checked) -> {
                itemView.setAlpha(checked ? 0.5f : 1f);
                titleGood.setPaintFlags(
                        checked
                                ? titleGood.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                                : titleGood.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
                );
            });
            if (doneListener != null) {
                done.setVisibility(View.VISIBLE);
                done.setOnClickListener(v -> {
                    doneListener.onDone(getItem(), RecyclerViewHolder.this);
                    setLock(!lock);
                });
            } else {
                done.setVisibility(View.GONE);
            }
        }

        private void setLock(boolean lock) {
            this.lock = lock;

            if (lock) {
                // Заблокирован — выглядит как TextView
                price.setFocusable(false);
                price.setFocusableInTouchMode(false);
                price.setCursorVisible(false);
                price.setClickable(false);
                price.setBackground(null); // убираем бордер EditText
            } else {
                // Разблокирован — можно редактировать
                price.setFocusable(true);
                price.setFocusableInTouchMode(true);
                price.setCursorVisible(true);
                price.setClickable(true);
            }

            minus.setEnabled(!lock);
            plus.setEnabled(!lock);
        }

        public void bind(OrderLine orderLine) {
            super.bind(orderLine);
            done.setChecked(orderLine.isDone());
            titleGood.setText(Optional.ofNullable(orderLine.getGood()).map(Good::getName).orElse(""));
            price.setText(MoneyUtils.moneyToString(orderLine.getPrice()));
            count.setText(Optional.ofNullable(orderLine.getCount()).map(String::valueOf).orElse(""));
            updateTotal(orderLine);
            if (orderLine.getGood() == null) {
                done.setVisibility(View.GONE);
                count.setFocusable(false);
            } else {
                orderLine.getGood().getImages().stream()
                        .filter(Attachment::isPrimary)
                        .findAny()
                        .ifPresent(f -> Glide.with(context)
                                .load(f.getThumbnailUrl())
                                .centerCrop()
                                .error(R.drawable.error)
                                .into(imageView));
            }
            price.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    double prePay = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                    if (prePay < 1) {
                        price.setError("Количество не может быть меньше 1");
                        price.setText("1");
                    }
                    updateTotal(orderLine);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            plus.setOnClickListener(v -> {
                int c = Integer.parseInt(count.getText().toString()) + 1;
                count.setText(String.valueOf(c));
                updateTotal(orderLine);
            });

            minus.setOnClickListener(v -> {
                int c = Math.max(0, Integer.parseInt(count.getText().toString()) - 1);
                if (c > 0) {
                    count.setText(String.valueOf(c));
                    updateTotal(orderLine);
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Удаление товара")
                            .setMessage("Вы хотите удалить эту запись?")
                            .setPositiveButton("Да", (dialog, which) -> {
                                dialog.dismiss();
                                if (removeListener != null) {
                                    int adapterPosition = getBindingAdapterPosition();
                                    removeItem(adapterPosition);
                                    removeListener.onRemove(adapterPosition);
                                }
                            })
                            .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }

        private void updateTotal(OrderLine orderLine) {
            double price = Optional.ofNullable(this.price.getText())
                    .map(Object::toString)
                    .map(MoneyUtils::stringToDouble)
                    .orElse(0d);
            int count = Optional.ofNullable(this.count.getText())
                    .map(Object::toString)
                    .filter(f -> !StringUtils.isEmpty(f))
                    .map(Integer::parseInt)
                    .orElse(0);
            double oldTotal = Optional.ofNullable(orderLine.getCount()).orElse(0)
                    * Optional.ofNullable(orderLine.getPrice()).orElse(0d);
            orderLine.setCount(count);
            orderLine.setPrice(price);
            double total = price * count;
            this.total.setText(String.format("Итого: %s ₽", total));
            if (changeSumListener != null && total != oldTotal) {
                changeSumListener.onChange();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_line, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setLock(getShowItems().get(position).isDone());
    }

}
