package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.ErrorDialog;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderLinesListAdapter extends RecyclerView.Adapter<OrderLinesListAdapter.OrderLinesViewHolder> {
    private List<OrderLine> orderLines = new ArrayList<>();

    private View view;

    private final OrderStatus orderStatus;

    private RemoveListener removeListener;

    public void setRemoveListener(RemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public OrderLinesListAdapter(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public interface RemoveListener {
        void onRemove(int position);
    }

    public class OrderLinesViewHolder extends RecyclerView.ViewHolder {
        private final TextView npp;
        private final Spinner good;
        private final TextView price;
        private final TextView count;
        private final List<Good> goods;

        private OrderLine orderLine;

        private final NumberFormat formatter = new DecimalFormat("#0.00");

        public OrderLinesViewHolder(View itemView, List<Good> goods) {
            super(itemView);
            this.goods = goods;
            npp = itemView.findViewById(R.id.order_line_npp);
            good = itemView.findViewById(R.id.order_line_name);
            final GoodsSpinnerAdapter goodsSpinnerAdapter = new GoodsSpinnerAdapter(itemView.getContext(),
                    R.layout.goods_list_item_spinner_dropdown, R.layout.goods_list_item_spinner_text, goods);

            price = itemView.findViewById(R.id.order_line_price);
            price.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    EditText et = (EditText) v;
                    if (et.getText() != null && !et.getText().toString().isEmpty()) {
                        try {
                            orderLine.setPrice(Objects.requireNonNull(formatter.parse(et.getText().toString())).doubleValue());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            count = itemView.findViewById(R.id.order_line_count);
            count.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    EditText et = (EditText) v;
                    if (et.getText() != null && !et.getText().toString().isEmpty()) {
                        orderLine.setCount(Integer.valueOf(et.getText().toString()));
                    }
                }
            });
            good.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                int iCurrentSelection = good.getSelectedItemPosition();

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (iCurrentSelection == -1) {
                        iCurrentSelection = position;
                        return;
                    }
                    if (iCurrentSelection != position) {
                        Good good = goods.get(position);
                        orderLine.setGood(good);
                        orderLine.setPrice(good.getPrice());
                        orderLine.setCount(1);
                        bind(orderLine);
                    }
                    iCurrentSelection = position;

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            good.setAdapter(goodsSpinnerAdapter);
            ImageButton delete = itemView.findViewById(R.id.orderLineDelete);
            delete.setOnClickListener(v -> removeListener.onRemove(getAdapterPosition()));
            ImageButton done = itemView.findViewById(R.id.orderLineDone);
            if (orderStatus == OrderStatus.NEW) {
                done.setVisibility(View.GONE);
            }
            if (orderStatus == OrderStatus.DONE) {
                delete.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
            }

        }

        public void bind(OrderLine orderLine) {
            this.orderLine = orderLine;
            if (orderLine.getGood() != null) {
                good.setSelection(goods.indexOf(orderLine.getGood()));
            }
            npp.setText(String.format("#%s", orderLine.getNum()));
            price.setText(Optional.ofNullable(orderLine.getPrice()).map(formatter::format).orElse(""));
            count.setText(Optional.ofNullable(orderLine.getCount()).map(String::valueOf).orElse(""));
        }
    }

    private List<Good> getGoods() {
        final List<Good> internalGoods = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            NetworkService.getInstance().getMarshmallowApi().getGoods().enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<GoodsResponse> call, Response<GoodsResponse> response) {
                    internalGoods.clear();
                    internalGoods.addAll(Optional.ofNullable(response.body())
                            .map(GoodsResponse::getGoods).orElse(new ArrayList<>()));
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(Call<GoodsResponse> call, Throwable t) {
                    countDownLatch.countDown();
                    new ErrorDialog(view.getContext(), t.getMessage()).show();
                }
            });

        } catch (Exception e) {
            new ErrorDialog(view.getContext(), e.getMessage()).show();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return internalGoods;
    }

    @Override
    public OrderLinesViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_line_list_item, parent, false);

        return new OrderLinesViewHolder(view, getGoods());
    }

    @Override
    public void onBindViewHolder(OrderLinesViewHolder holder, int position) {
        if (position % 2 == 0) {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey_1));
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