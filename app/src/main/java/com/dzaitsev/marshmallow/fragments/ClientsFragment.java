package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;

import com.dzaitsev.marshmallow.adapters.ClientRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.util.Optional;

import retrofit2.Call;

public class ClientsFragment extends AbstractNsiFragment<Client, ClientResponse, ClientRecyclerViewAdapter> {
    public static final String IDENTITY = "clientsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAdapter(new ClientRecyclerViewAdapter());
        requireActivity().setTitle("Клиенты");
        Order order = Optional.ofNullable(getArguments()).map(m -> GsonHelper.deserialize(m.getString("order"), Order.class)).orElse(null);
        setSelectListener(Optional.ofNullable(order).map(m -> (SelectItemListener<Client>) item -> {
            m.setClient(item);
            Bundle bundle = new Bundle();
            bundle.putString("order", GsonHelper.serialize(m));
            Navigation.getNavigation().back(bundle);
        }).orElse(null));
        setEditItemListener(client -> {
            Bundle bundle = new Bundle();
            bundle.putString("client",GsonHelper.serialize(client));
            Navigation.getNavigation().goForward(new ClientCardFragment(), bundle);
        });
        setOnCreateListener(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("client", GsonHelper.serialize(new Client()));
            Navigation.getNavigation().goForward(new ClientCardFragment(), bundle);
        });
    }

    @Override
    protected Call<ClientResponse> getCall(Boolean bool) {
        return NetworkService.getInstance().getClientsApi().getClients(bool);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}
