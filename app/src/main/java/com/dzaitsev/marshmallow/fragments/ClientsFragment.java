package com.dzaitsev.marshmallow.fragments;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.ClientRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentClientsBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientsFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "clientsFragment";
    private FragmentClientsBinding binding;

    private ClientRecyclerViewAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentClientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Клиенты");
        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getClients(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(clientResponse -> {
                            mAdapter.setItems(Optional.of(response.body())
                                    .orElse(new ClientResponse()).getClients()
                                    .stream()
                                    .sorted(Comparator.comparing(Client::getName)).collect(Collectors.toList()));
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
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("order", m);
                    Navigation.getNavigation(requireActivity()).back(bundle);
                }).orElse(null));
        mAdapter.setFilterPredicate(s -> client -> client.getName().toLowerCase().contains(s.toLowerCase()));
        mAdapter.setEditItemListener(client -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("client", client);
            Navigation.getNavigation(requireActivity()).goForward(new ClientCardFragment(), bundle);
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
            Bundle bundle = new Bundle();
            bundle.putSerializable("client", new Client());
            Navigation.getNavigation(requireActivity()).goForward(new ClientCardFragment(), bundle);
        });
        ColorStateList colorStateList = ColorStateList.valueOf(getBackgroundColor(view));
        binding.clientListCreate.setBackgroundTintList(colorStateList);
    }
    private int getBackgroundColor(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable colorDrawable) {
            return colorDrawable.getColor();
        } else {
            return ContextCompat.getColor(requireContext(), R.color.white);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAdapter = null;
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}