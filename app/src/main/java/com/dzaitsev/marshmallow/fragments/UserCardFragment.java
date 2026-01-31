package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentUserCardBinding;
import com.dzaitsev.marshmallow.dto.authorization.request.ChangePasswordRequest;
import com.dzaitsev.marshmallow.dto.authorization.request.SaveMyInfoRequest;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "userCardFragment";
    private FragmentUserCardBinding binding;

    private final View.OnFocusChangeListener restoreStateListener =
            (v, hasFocus) -> {
                if (hasFocus) {
                    v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                }
            };

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deliverymen = menu.add("Исполнители доставки");
        deliverymen.setOnMenuItemClickListener(item -> {
            Navigation.getNavigation().forward(InviteRequestsFragment.IDENTITY);
            return false;
        });

        MenuItem exit = menu.add("Выйти");
        exit.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены?");
            builder.setPositiveButton("Да", (dialog, id) -> {
                AuthorizationHelper.getInstance().updateSignInRequest(null);
                AuthorizationHelper.getInstance().updateUserData(null);
                NetworkService.getInstance().refreshToken(null);
                Navigation.getNavigation().forward(LoginFragment.IDENTITY);
            });
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
            return false;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentUserCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AuthorizationHelper.getInstance().getUserData()
                .ifPresent(user -> {
                    requireActivity().setTitle(user.getEmail());
                    binding.txtFirstName.setText(user.getFirstName());
                    binding.txtLastName.setText(user.getLastName());
                });
        binding.btnBack.setOnClickListener(v -> Navigation.getNavigation().back());
        binding.btnSave.setOnClickListener(v -> {
            boolean fail = false;
            if (StringUtils.isEmpty(binding.txtFirstName.getText().toString())) {
                binding.txtFirstName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtLastName.getText().toString())) {
                binding.txtLastName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (!fail) {
                new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getUsersApi()
                        .saveMyInfo(new SaveMyInfoRequest(binding.txtFirstName.getText().toString(),
                                binding.txtLastName.getText().toString())))
                        .invoke(response -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Данные успешно сохранены", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        final AtomicBoolean changePasswordIsVisible = new AtomicBoolean(false);
        changeStateChangePassword(changePasswordIsVisible.get());
        binding.btnChangePassword.setOnClickListener(v -> {
            changeStateChangePassword(!changePasswordIsVisible.get());
            changePasswordIsVisible.set(!changePasswordIsVisible.get());
        });
        binding.txtLastName.setOnFocusChangeListener(restoreStateListener);
        binding.txtFirstName.setOnFocusChangeListener(restoreStateListener);
        binding.txtConfirmPassword.setOnFocusChangeListener(restoreStateListener);
        binding.txtChangePassword.setOnFocusChangeListener(restoreStateListener);
        binding.btnSavePassword.setOnClickListener(v -> {
            boolean fail = false;
            String s = binding.txtChangePassword.getText().toString();
            String s1 = binding.txtConfirmPassword.getText().toString();
            if (StringUtils.isEmpty(s)) {
                binding.txtChangePassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(s1)) {
                binding.txtConfirmPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (!s.equals(s1)) {
                Toast.makeText(getContext(), "Введенные пароли не совпадают", Toast.LENGTH_SHORT).show();
                binding.txtChangePassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                binding.txtConfirmPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (!fail) {
                new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getUsersApi()
                        .changePassword(new ChangePasswordRequest(s)))
                        .invoke(response -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                                AuthorizationHelper.getInstance().getSignInRequest()
                                        .ifPresent(signInRequest -> {
                                            signInRequest.setPassword(s);
                                            AuthorizationHelper.getInstance().updateSignInRequest(signInRequest);
                                        });
                                Navigation.getNavigation().forward(LoginFragment.IDENTITY);
                            } else {
                                Toast.makeText(getContext(), "Ошибка при изменении пароля", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void changeStateChangePassword(boolean state) {
        if (state) {
            binding.btnChangePassword.setText("Скрыть");
            binding.btnSavePassword.setVisibility(View.VISIBLE);
            binding.txtChangePassword.setVisibility(View.VISIBLE);
            binding.txtConfirmPassword.setVisibility(View.VISIBLE);
        } else {
            binding.btnChangePassword.setText("Изменить пароль");
            binding.btnSavePassword.setVisibility(View.GONE);
            binding.txtChangePassword.setVisibility(View.GONE);
            binding.txtConfirmPassword.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}