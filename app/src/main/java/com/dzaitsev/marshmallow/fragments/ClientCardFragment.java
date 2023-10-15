package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.LinkChannelSelector;
import com.dzaitsev.marshmallow.databinding.FragmentClientCardBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.LinkChannel;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientCardFragment extends Fragment {


    private FragmentClientCardBinding binding;
    private Client incomingClient;

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (save()) {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                });
                builder.create().show();
            } else {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        }
    };

    private boolean hasChanges() {
        Client client = constructClient();
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

        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity ma) {
            ma.setNavigationBackListener(() -> {
                if (hasChanges()) {
                    requireActivity().onBackPressed();
                    return false;
                } else {
                    return true;
                }
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentClientCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private final View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };
    LinkChannelSelector.OnCheckedChangeListener selectorChangeListener = new LinkChannelSelector.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(LinkChannelSelector linkChannelSelector, boolean isChecked) {
            binding.linkSelectorLayout.setBackgroundColor(ContextCompat.getColor(linkChannelSelector.getContext(), R.color.field_background));
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Client client = requireArguments().getSerializable("client", Client.class);
        Objects.requireNonNull(client).getLinkChannels().sort(Enum::compareTo);
        incomingClient = Objects.requireNonNull(client).clone();

        binding.clientCardName.setText(client.getName());
        binding.clientCardName.setOnKeyListener(keyListener);

        binding.clientCardPhone.setText(client.getPhone());
        binding.clientCardPhone.setOnKeyListener(keyListener);

        binding.clientCardDelivery.setText(client.getDefaultDeliveryAddress());
        binding.clientCardComment.setText(client.getComment());

        binding.clientCardCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.clientCardSave.setOnClickListener(v -> {
            if (save()) {
                requireActivity().onBackPressed();
            }
        });
        binding.phoneSelector.setChecked(client.getLinkChannels().contains(LinkChannel.PHONE));
        binding.smsSelector.setChecked(client.getLinkChannels().contains(LinkChannel.SMS));
        binding.whatsappSelector.setChecked(client.getLinkChannels().contains(LinkChannel.WHATSAPP));
        binding.telegramSelector.setChecked(client.getLinkChannels().contains(LinkChannel.TELEGRAM));

        binding.phoneSelector.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.phone_64));
        binding.smsSelector.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.sms_64));
        binding.whatsappSelector.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.whatsapp_64));
        binding.telegramSelector.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.telegram_64));
        binding.phoneSelector.setOnCheckedChangeListener(selectorChangeListener);
        binding.smsSelector.setOnCheckedChangeListener(selectorChangeListener);
        binding.whatsappSelector.setOnCheckedChangeListener(selectorChangeListener);
        binding.telegramSelector.setOnCheckedChangeListener(selectorChangeListener);
    }

    private boolean save() {
        boolean fail = false;
        if (StringUtils.isEmpty(binding.clientCardName.getText().toString())) {
            binding.clientCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(binding.clientCardPhone.getText().toString())) {
            binding.clientCardPhone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (!binding.phoneSelector.isChecked()
                && !binding.smsSelector.isChecked()
                && !binding.whatsappSelector.isChecked()
                && !binding.telegramSelector.isChecked()) {
            binding.linkSelectorLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        Client client = constructClient();
        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().saveClient(client),
                true);
        callback.invoke();
        incomingClient = client;
        return callback.isSuccess();
    }

    private Client constructClient() {
        Client client = new Client();
        client.setId(incomingClient == null ? null : incomingClient.getId());
        client.setName(binding.clientCardName.getText().toString());
        client.setComment(binding.clientCardComment.getText().toString());
        client.setLinkChannels(getLinkChannels());
        client.setDefaultDeliveryAddress(binding.clientCardDelivery.getText().toString());
        client.setPhone(binding.clientCardPhone.getText().toString());
        return client;
    }

    private List<LinkChannel> getLinkChannels() {
        List<LinkChannel> list = new ArrayList<>();
        if (binding.telegramSelector.isChecked()) {
            list.add(LinkChannel.TELEGRAM);
        }
        if (binding.whatsappSelector.isChecked()) {
            list.add(LinkChannel.WHATSAPP);
        }
        if (binding.phoneSelector.isChecked()) {
            list.add(LinkChannel.PHONE);
        }
        if (binding.smsSelector.isChecked()) {
            list.add(LinkChannel.SMS);
        }
        list.sort(Enum::compareTo);
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity ma) {
            ma.setNavigationBackListener(null);
        }
    }

}