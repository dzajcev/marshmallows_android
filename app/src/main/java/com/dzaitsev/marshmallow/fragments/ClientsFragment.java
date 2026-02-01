package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;

import com.dzaitsev.marshmallow.adapters.ClientRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.dto.response.ResultResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.util.List;
import java.util.Optional;

import retrofit2.Call;

public class ClientsFragment extends AbstractNsiFragment<Client, ClientRecyclerViewAdapter> {
    public static final String IDENTITY = "clientsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAdapter(new ClientRecyclerViewAdapter());
        requireActivity().setTitle("Клиенты");
        OrderCardBundle orderCardBundle = Optional.ofNullable(getArguments()).map(m -> GsonHelper.deserialize(m.getString("orderCardBundle"),
                OrderCardBundle.class)).orElse(null);
        setSelectListener(Optional.ofNullable(orderCardBundle).map(m -> (SelectItemListener<Client>) item -> {
            m.getOrder().setClient(item);
            m.getOrder().setPhone(item.getPhone());
            m.getOrder().setDeliveryAddress(item.getDefaultDeliveryAddress());
            Bundle bundle = new Bundle();
            m.setActiveTab(0);
            bundle.putString("orderCardBundle", GsonHelper.serialize(m));
            Navigation.getNavigation().back(bundle);
        }).orElse(null));
        setEditItemListener(client -> {
            Bundle bundle = new Bundle();
            bundle.putString("client", GsonHelper.serialize(client));
            Navigation.getNavigation().forward(ClientCardFragment.IDENTITY, bundle);
        });
        setOnCreateListener(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("client", GsonHelper.serialize(new Client()));
            Navigation.getNavigation().forward(ClientCardFragment.IDENTITY, bundle);
        });
    }

    @Override
    protected Call<ResultResponse<List<Client>>> getCall(Boolean bool) {
        return NetworkService.getInstance().getClientsApi().getClients(bool);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}
