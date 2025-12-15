package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentLoginBinding;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

public class LoginFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "loginFragment";
    private FragmentLoginBinding binding;

    private final View.OnClickListener restoreStateListener =
            v -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("");
        binding.txtPassword.setOnClickListener(restoreStateListener);
        binding.txtLogin.setOnClickListener(restoreStateListener);
        binding.btnLogin.setOnClickListener(v -> {
            boolean fail = false;
            if (StringUtils.isEmpty(binding.txtLogin.getText().toString())) {
                binding.txtLogin.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtPassword.getText().toString())) {
                binding.txtPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (fail) {
                return;
            }
            NetworkExecutorHelper.authorize(requireActivity(), new SignInRequest(
                    binding.txtLogin.getText().toString(),
                    binding.txtPassword.getText().toString()
            ));
        });
        binding.btnRegistration.setOnClickListener(v -> Navigation.getNavigation()
                .forward(RegistrationFragment.IDENTITY));
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