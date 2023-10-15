package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.ClientRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentClientsBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Optional;

public class ClientsFragment extends Fragment {

    private FragmentClientsBinding binding;

    private ClientRecyclerViewAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentClientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getClients(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(clientResponse -> {
                            mAdapter.setItems(Optional.of(response.body())
                                    .orElse(new ClientResponse()).getClients());
                            if (!StringUtils.isEmpty(binding.searchClientFld.getQuery().toString())) {
                                mAdapter.filter(binding.searchClientFld.getQuery().toString());
                            }
                        })).invoke();
        final RecyclerView clientList = view.findViewById(R.id.clientsView);
        clientList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new ClientRecyclerViewAdapter();
        mAdapter.setSelectItemListener(Optional.ofNullable(getArguments())
                .map(m -> m.getSerializable("order", Order.class))
                .map(m -> (ClientRecyclerViewAdapter.SelectItemListener) item -> {
                    m.setClient(item);
                    m.setDeliveryAddress(item.getDefaultDeliveryAddress());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("order", m);
                    NavHostFragment.findNavController(ClientsFragment.this)
                            .navigate(R.id.action_clientsFragment_to_orderClientFragment, bundle);
                }).orElse(null));
        mAdapter.setFilterPredicate(s -> client -> client.getName().toLowerCase().contains(s.toLowerCase()));
        mAdapter.setEditItemListener(client -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("client", client);
            NavHostFragment.findNavController(ClientsFragment.this)
                    .navigate(R.id.action_clientsFragment_to_clientCardFragment, bundle);
        });
        clientList.setAdapter(mAdapter);
        binding.searchClientFld.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return false;
            }
        });
        binding.clientListCreate.setOnClickListener(view1 -> {
            Bundle newBundle = new Bundle();
            newBundle.putSerializable("client", new Client());
            NavHostFragment.findNavController(ClientsFragment.this)
                    .navigate(R.id.action_clientsFragment_to_clientCardFragment, newBundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}