package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.InviteRequestsRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentInviteRequestsBinding;
import com.dzaitsev.marshmallow.dto.ErrorDto;
import com.dzaitsev.marshmallow.dto.InviteRequestDirection;
import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.dto.request.AcceptInviteRequest;
import com.dzaitsev.marshmallow.dto.request.AddInviteRequest;
import com.dzaitsev.marshmallow.dto.response.InviteRequestsResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.api.InviteRequestsApi;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.dzaitsev.marshmallow.utils.orderfilter.FiltersHelper;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class InviteRequestsFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "inviteRequestsFragment";

    private FragmentInviteRequestsBinding binding;

    private InviteRequestsRecyclerViewAdapter mAdapter;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentInviteRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void fillItems() {
        Boolean val = determineRequestValue();
        InviteRequestDirection direction = determineDirection();
        InviteRequestsApi usersApi = NetworkService.getInstance().getInviteRequestsApi();
        FiltersHelper.getInstance().getOrderFilter()
                .ifPresent(filter -> new NetworkExecutorHelper<>(requireActivity(),
                        usersApi.getInviteRequests(direction, val))
                        .invoke(response -> Optional.ofNullable(response.body())
                                .ifPresent(orderResponse -> mAdapter.setItems(Optional.of(orderResponse)
                                        .orElse(new InviteRequestsResponse()).getRequests().stream()
                                        .peek(m -> m.setDirection(direction))
                                        .sorted(Comparator.comparing(inviteRequest -> inviteRequest.getUser().getFullName())
                                        )
                                        .collect(Collectors.toList())))));

    }

    protected Boolean determineRequestValue() {
        return Optional.of(binding.chkInviteRequestStatus.getState()).map(m -> {
            if (m == 1) {
                binding.chkInviteRequestStatus.setText("Принятые");
                return true;
            } else if (m == 0) {
                binding.chkInviteRequestStatus.setText("Не принятые");
                return false;
            } else {
                binding.chkInviteRequestStatus.setText("Все");
                return null;
            }
        }).orElse(null);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.listInviteRequests.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new InviteRequestsRecyclerViewAdapter();
        if (AuthorizationHelper.getInstance().getUserRole() == UserRole.DELIVERYMAN) {
            binding.inviteRequestsGroup.setVisibility(View.GONE);
            requireActivity().setTitle("Входящие приглашения");
            binding.btnInviteRequestsNewRequest.setVisibility(View.GONE);
        } else {
            binding.inviteRequestsGroup.check(R.id.rbtnOutgoing);
            binding.inviteRequestsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbtnIncoming) {
                    requireActivity().setTitle("Входящие приглашения");
                } else {
                    requireActivity().setTitle("Исходящие приглашения");
                }
                fillItems();
            });
            binding.btnInviteRequestsNewRequest.setOnClickListener(v -> {
                EditText editText = new EditText(requireContext());
                new AlertDialog.Builder(requireContext())
                        .setTitle("Введите логин пользователя")
                        .setView(editText)
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                        .setPositiveButton("Ок", (dialog, which) -> new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance()
                                .getInviteRequestsApi().addInviteRequest(new AddInviteRequest(editText.getText().toString())))
                                .setOnErrorListener(new NetworkExecutorHelper.OnErrorListener() {
                                    @Override
                                    public void onError(ErrorDto errorDto) {
                                        Toast.makeText(requireContext(), errorDto.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .invoke(objectResponse -> fillItems()))
                        .create().show();
            });
        }
        fillItems();
        binding.chkInviteRequestStatus.setOnCheckedChangeListener((buttonView, isChecked) -> fillItems());

        mAdapter.setOnAcceptListener(request -> new AlertDialog.Builder(requireContext())
                .setTitle("Принятие приглашения")
                .setMessage("Вы уверены?")
                .setNegativeButton("Нет", (dialog, which) -> dialog.cancel())
                .setPositiveButton("Да", (dialog, which) -> new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance()
                        .getInviteRequestsApi().acceptInviteRequest(new AcceptInviteRequest(request.getId())))
                        .setOnErrorListener(new NetworkExecutorHelper.OnErrorListener() {
                            @Override
                            public void onError(ErrorDto errorDto) {
                                Toast.makeText(requireContext(), errorDto.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .invoke(objectResponse -> fillItems()))
                .create().show());
        mAdapter.setOnDeleteListener(request -> new AlertDialog.Builder(requireContext())
                .setTitle("Удаление приглашения")
                .setMessage("Вы уверены?")
                .setNegativeButton("Нет", (dialog, which) -> dialog.cancel())
                .setPositiveButton("Да", (dialog, which) -> new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance()
                        .getInviteRequestsApi().deleteInviteRequest(request.getId()))
                        .setOnErrorListener(new NetworkExecutorHelper.OnErrorListener() {
                            @Override
                            public void onError(ErrorDto errorDto) {
                                Toast.makeText(requireContext(), errorDto.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .invoke(objectResponse -> fillItems()))
                .create().show());
        binding.searchInviteRequests.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        binding.listInviteRequests.setAdapter(mAdapter);
        binding.btnInviteRequestsBack.setOnClickListener(v -> Navigation.getNavigation().back());
    }

    private InviteRequestDirection determineDirection() {
        if (AuthorizationHelper.getInstance().getUserRole() == UserRole.DELIVERYMAN || binding.inviteRequestsGroup.getCheckedRadioButtonId() == R.id.rbtnIncoming) {
            return InviteRequestDirection.INCOMING;
        } else {
            return InviteRequestDirection.OUTGOING;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAdapter.setEditItemListener(null);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}