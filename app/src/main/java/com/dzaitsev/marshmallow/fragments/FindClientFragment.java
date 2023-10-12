package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.ErrorDialog;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.ClientsListAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentFindClientBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindClientFragment extends Fragment {

    private FragmentFindClientBinding binding;

    private ClientsListAdapter mAdapter;

    private Order order;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFindClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        order = requireArguments().getSerializable("order", Order.class);
        try {
            NetworkService.getInstance().getMarshmallowApi().getClients().enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ClientResponse> call, Response<ClientResponse> response) {
                    requireActivity().runOnUiThread(() -> {
                        if (!response.isSuccessful()) {
                            showError("Ошибка получения списка клиентов");
                        } else {
                            mAdapter.setItems(Optional.ofNullable(response.body())
                                    .orElse(new ClientResponse()).getClients());
                        }
                    });
                }

                @Override
                public void onFailure(Call<ClientResponse> call, Throwable t) {
                    showError(t.getMessage());
                }
            });

        } catch (Exception e) {
            showError(e.getMessage());
        }

        final RecyclerView clientList = view.findViewById(R.id.clientsView);
        clientList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new ClientsListAdapter(item -> {
            order.setClient(item);
            order.setDeliveryAddress(item.getDefaultDeliveryAddress());
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", order);
            NavHostFragment.findNavController(FindClientFragment.this)
                    .navigate(R.id.action_findClientFragment_to_orderClientFragment, bundle);
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
    }

    private void showError(String text) {
        requireActivity().runOnUiThread(() -> new ErrorDialog(requireActivity(), text).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}