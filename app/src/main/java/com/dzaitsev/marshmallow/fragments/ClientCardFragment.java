package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentClientCardBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.LinkChannel;
import com.dzaitsev.marshmallow.dto.response.ClientResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "clientCardFragment";

    private FragmentClientCardBinding binding;
    private Client incomingClient;
    private Client client;

    private final Navigation.OnBackListener backListener = fragment -> {
        if (ClientCardFragment.this == fragment) {
            if (ClientCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClientCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (ClientCardFragment.this.save()) {
                        Navigation.getNavigation(ClientCardFragment.this.requireActivity()).back();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation(ClientCardFragment.this.requireActivity()).back());
                builder.create().show();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        client = requireArguments().getSerializable("client", Client.class);
        Objects.requireNonNull(client).getLinkChannels().sort(Enum::compareTo);
        incomingClient = Objects.requireNonNull(client).clone();
        binding = FragmentClientCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private final View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка клиента");
        binding.clientCardCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).callbackBack());
        if (client.getId() != null) {
            NetworkExecutor<ClientResponse> orderResponseNetworkExecutor = new NetworkExecutor<>(requireActivity(),
                    NetworkService.getInstance().getMarshmallowApi().getClient(client.getId()),
                    response -> Optional.ofNullable(response.body())
                            .ifPresent(clientResponse -> {
                                if (clientResponse.getClients() != null && !clientResponse.getClients().isEmpty()) {
                                    incomingClient = clientResponse.getClients().iterator().next();
                                }
                            }), true);

            orderResponseNetworkExecutor.invoke();
            if (!orderResponseNetworkExecutor.isSuccess()) {
                Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
                return;
            }
        } else {
            client = new Client();
        }
        Navigation.getNavigation(requireActivity()).addOnBackListener(backListener);
        binding.clientCardName.setText(client.getName());
        binding.clientCardName.setOnKeyListener(keyListener);

        binding.clientCardPhone.setText(client.getPhone());
        binding.clientCardPhone.setOnKeyListener(keyListener);

        binding.clientCardDelivery.setText(client.getDefaultDeliveryAddress());
        binding.clientCardComment.setText(client.getComment());


        binding.clientCardSave.setOnClickListener(v -> {
            if (save()) {
                Navigation.getNavigation(requireActivity()).back();
            }
        });
        binding.linkChannelSelector.setChecked(client.getLinkChannels());
        binding.linkChannelSelector.setOnCheckedChangeListener((linkChannelSelector, channels)
                -> linkChannelSelector.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent)));

    }

    private boolean save() {
        boolean fail = false;
        if (StringUtils.isEmpty(binding.clientCardName.getText().toString())) {
            binding.clientCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(binding.clientCardPhone.getRawText())) {
            binding.clientCardPhone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (binding.linkChannelSelector.isEmpty()) {
            binding.linkChannelSelector.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        fillClient();
        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().saveClient(client),
                true);
        callback.invoke();
        incomingClient = client;
        return callback.isSuccess();
    }

    private void fillClient() {
        client.setId(incomingClient == null ? null : incomingClient.getId());
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