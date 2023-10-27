package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.databinding.FragmentConfirmRegistrationBinding;
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.request.VerificationCodeRequest;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;

import java.util.Optional;

public class ConfirmRegistrationFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "confirmRegistrationFragment";
    private FragmentConfirmRegistrationBinding binding;

    private CountDownTimer timer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentConfirmRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("");
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int ttlCode = requireArguments().getInt("ttlCode");
        String token = requireArguments().getString("token");
        String login = requireArguments().getString("login");
        String password = requireArguments().getString("password");
        timer = new CountDownTimer(ttlCode * 1000L, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnRequestCode.setText(String.format("Через %s сек", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                binding.btnRequestCode.setText("Запросить");
                binding.btnRequestCode.setEnabled(true);

            }
        }.start();
        binding.btnCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).goForward(new LoginFragment()));
        binding.btnRequestCode.setEnabled(false);
        timer.start();
        binding.btnRequestCode.setOnClickListener(v -> {
            binding.btnRequestCode.setEnabled(false);
            timer.start();
            NetworkService.getInstance().refreshToken(token);
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getAuthorizationApi()
                    .sendCode()).invoke(jwtAuthenticationResponseResponse -> {
                if (jwtAuthenticationResponseResponse.isSuccessful()) {
                    Toast.makeText(requireContext(), "Код отправлен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Код не отправлен", Toast.LENGTH_SHORT).show();
                }
            });
        });
        binding.btnSend.setOnClickListener(v -> {
            NetworkService.getInstance().refreshToken(token);
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getAuthorizationApi()
                    .verify(new VerificationCodeRequest(binding.txtCode.getText().toString())))
                    .invoke(jwtAuthenticationResponseResponse -> {
                        if (jwtAuthenticationResponseResponse.isSuccessful()) {
                            Optional.ofNullable(jwtAuthenticationResponseResponse.body())
                                    .map(JwtAuthenticationResponse::getToken)
                                    .ifPresent(s -> {
                                        AuthorizationHelper.getInstance().updateSignInRequest(new SignInRequest(login, password));
                                        NetworkService.getInstance().refreshToken(s);
                                        timer.cancel();
                                        Navigation.getNavigation(requireActivity()).goForward(new OrdersFragment());
                                    });
                        }
                    });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel();
        binding = null;

    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}