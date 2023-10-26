package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentLoginBinding;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.request.SignInRequest;
import com.dzaitsev.marshmallow.service.NetworkExecutorWrapper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Optional;
import java.util.function.Consumer;

import retrofit2.Response;

public class LoginFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "loginFragment";
    private FragmentLoginBinding binding;
    private SharedPreferences preferences;

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
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
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
            authorize(new SignInRequest(
                    binding.txtLogin.getText().toString(),
                    binding.txtPassword.getText().toString()
            ));
        });
        binding.btnRegistration.setOnClickListener(v -> Navigation.getNavigation(requireActivity())
                .goForward(new RegistrationFragment()));
    }

    private void authorize(SignInRequest signInRequest) {
        new NetworkExecutorWrapper<>(requireActivity(), NetworkService.getInstance().getAuthorizationApi().signIn(signInRequest))
                .invoke((Consumer<Response<JwtAuthenticationResponse>>) response -> Optional.ofNullable(response.body())
                        .map(JwtAuthenticationResponse::getToken)
                        .ifPresent(s -> {
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("authorization-data", GsonExt.getGson().toJson(signInRequest));
                            edit.apply();
                            NetworkService.getInstance().refreshToken(s);
                            Navigation.getNavigation(requireActivity()).goForward(new OrdersFragment());
                        }));
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