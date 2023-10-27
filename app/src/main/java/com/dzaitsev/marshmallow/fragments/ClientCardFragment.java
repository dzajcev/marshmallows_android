package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.AlertDialogComponent;
import com.dzaitsev.marshmallow.databinding.FragmentClientCardBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.LinkChannel;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.api.ClientsApi;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.List;
import java.util.Objects;

public class ClientCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "clientCardFragment";

    private FragmentClientCardBinding binding;
    private Client incomingClient;
    private Client client;


    private final Navigation.OnBackListener backListener = fragment -> {
        if (ClientCardFragment.this == fragment) {
            if (ClientCardFragment.this.hasChanges()) {
                AlertDialogComponent.showDialog(requireContext(), "Запись изменена.", "Сохранить?",
                        new AlertDialogComponent.Action() {
                            @Override
                            public void doIn() {
                                ClientCardFragment.this.save();
                            }

                            @Override
                            public ActionType getAction() {
                                return ActionType.POSITIVE;
                            }
                        }, new AlertDialogComponent.Action() {
                            @Override
                            public void doIn() {
                                Navigation.getNavigation(ClientCardFragment.this.requireActivity()).back();
                            }

                            @Override
                            public ActionType getAction() {
                                return ActionType.NEGATIVE;
                            }
                        });
            } else {
                Navigation.getNavigation(ClientCardFragment.this.requireActivity()).back();
            }
        }
        return false;
    };

    private boolean hasChanges() {
        fillClient();
        if (StringUtils.isEmpty(client.getName())
                && StringUtils.isEmpty(client.getComment())
                && StringUtils.isEmpty(client.getDefaultDeliveryAddress())
                && StringUtils.isEmpty(client.getPhone())
                && client.getLinkChannels().isEmpty()) {
            return false;
        }
        return !client.equals(incomingClient);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deleteClient = menu.add("Удалить");
        if (incomingClient.isActive()) {
            deleteClient.setTitle("Удалить");
        } else {
            deleteClient.setTitle("Восстановить");
        }
        deleteClient.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ClientCardFragment.this.getActivity());
            builder.setTitle((incomingClient.isActive() ? "Удаление" : "Восстановление") + " клиента?");
            new NetworkExecutorHelper<>(requireActivity(),
                    NetworkService.getInstance().getClientsApi().checkClientOnOrdersAvailability(client.getId()))
                    .invoke(booleanResponse -> {
                        if (!booleanResponse.isSuccessful()) {
                            return;
                        }
                        String text = "";
                        if (incomingClient.isActive() && Boolean.FALSE.equals(booleanResponse.body())) {
                            text = "\nЗапись будет удалена безвозвратно";
                        }
                        builder.setMessage("Вы уверены?" + text);
                        builder.setPositiveButton("Да", (dialog, id) -> {
                            ClientsApi clientsApi = NetworkService.getInstance().getClientsApi();
                            new NetworkExecutorHelper<>(requireActivity(),
                                    incomingClient.isActive() ? clientsApi.deleteClient(client.getId()) : clientsApi.restoreClient(client.getId()))
                                    .invoke(response -> {
                                        if (response.isSuccessful()) {
                                            Navigation.getNavigation(getActivity()).back();
                                        }
                                    });

                        });
                        builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
                        builder.create().show();
                    });
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        client = requireArguments().getSerializable("client", Client.class);
        Objects.requireNonNull(client).getLinkChannels().sort(Enum::compareTo);
        setHasOptionsMenu(client.getId() != null);
        incomingClient = Objects.requireNonNull(client).clone();
        binding = FragmentClientCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private final View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка клиента");
        binding.clientCardCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).callbackBack());
        Navigation.getNavigation(requireActivity()).addOnBackListener(backListener);
        binding.clientCardName.setText(client.getName());
        binding.clientCardName.setOnKeyListener(keyListener);

        binding.clientCardPhone.setText(client.getPhone());
        binding.clientCardPhone.setOnKeyListener(keyListener);

        binding.clientCardDelivery.setText(client.getDefaultDeliveryAddress());
        binding.clientCardComment.setText(client.getComment());


        binding.clientCardSave.setOnClickListener(v -> save());
        binding.linkChannelSelector.setChecked(client.getLinkChannels());
        binding.linkChannelSelector.setOnCheckedChangeListener((linkChannelSelector, channels)
                -> linkChannelSelector.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent)));
    }

    private void save() {
        boolean fail = false;
        if (StringUtils.isEmpty(binding.clientCardName.getText().toString())) {
            binding.clientCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(binding.clientCardPhone.getRawText())
                || binding.clientCardPhone.getRawText().length() != 10) {
            binding.clientCardPhone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (binding.linkChannelSelector.isEmpty()) {
            binding.linkChannelSelector.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return;
        }
        fillClient();
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getClientsApi().saveClient(client)).invoke(response -> {
            incomingClient = client;
            if (response.isSuccessful()) {
                Navigation.getNavigation(ClientCardFragment.this.requireActivity()).back();
            }
        });
    }

    private void fillClient() {
        client.setId(incomingClient == null ? null : incomingClient.getId());
        client.setActive(true);
        client.setName(binding.clientCardName.getText().toString());
        client.setComment(binding.clientCardComment.getText().toString());
        client.setLinkChannels(getLinkChannels());
        client.setDefaultDeliveryAddress(binding.clientCardDelivery.getText().toString());
        client.setPhone(binding.clientCardPhone.getRawText());
    }

    private List<LinkChannel> getLinkChannels() {
        return binding.linkChannelSelector.getSelectedChannels();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}