package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentRegistrationBinding;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.dto.authorization.request.SignUpRequest;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.Optional;


public class RegistrationFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "registrationFragment";
    private FragmentRegistrationBinding binding;
    private final View.OnFocusChangeListener restoreStateListener =
            (v, hasFocus) -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener
            = (buttonView, isChecked) -> {
        binding.chkDeveloper.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
        binding.chkDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
    };

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(v -> Navigation.getNavigation().back());
        binding.txtLogin.setOnFocusChangeListener(restoreStateListener);
        binding.txtPassword.setOnFocusChangeListener(restoreStateListener);
        binding.txtRegistrationConfirmPassword.setOnFocusChangeListener(restoreStateListener);
        binding.txtFirstName.setOnFocusChangeListener(restoreStateListener);
        binding.txtLastName.setOnFocusChangeListener(restoreStateListener);

        binding.chkDelivery.setOnCheckedChangeListener(onCheckedChangeListener);
        binding.chkDeveloper.setOnCheckedChangeListener(onCheckedChangeListener);
        binding.btnRegistration.setOnClickListener(v -> {

            boolean fail = false;
            if (StringUtils.isEmpty(binding.txtFirstName.getText().toString())) {
                binding.txtFirstName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtLastName.getText().toString())) {
                binding.txtLastName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtLogin.getText().toString())) {
                binding.txtLogin.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtPassword.getText().toString())) {
                binding.txtPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (StringUtils.isEmpty(binding.txtRegistrationConfirmPassword.getText().toString())) {
                binding.txtRegistrationConfirmPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (!binding.txtPassword.getText().toString().equals(binding.txtRegistrationConfirmPassword.getText().toString())) {
                binding.txtPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                binding.txtRegistrationConfirmPassword.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                Toast.makeText(requireContext(), "Введенные пароли не совпадают", Toast.LENGTH_SHORT).show();
                fail = true;
            }
            if (!binding.chkDelivery.isChecked() && !binding.chkDeveloper.isChecked()) {
                binding.chkDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                binding.chkDeveloper.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
                fail = true;
            }
            if (!fail) {
                SignUpRequest request = new SignUpRequest();
                request.setEmail(binding.txtLogin.getText().toString());
                request.setFirstName(binding.txtFirstName.getText().toString());
                request.setLastName(binding.txtLastName.getText().toString());
                request.setPassword(binding.txtPassword.getText().toString());
                if (binding.chkDeveloper.isChecked()) {
                    request.setRole(UserRole.DEVELOPER);
                }
                if (binding.chkDelivery.isChecked()) {
                    request.setRole(UserRole.DELIVERYMAN);
                }
                NetworkService.getInstance().refreshToken(null);
                new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getAuthorizationApi().signUp(request))
                        .invoke(objectResponse -> {
                            if (objectResponse.isSuccessful()) {
                                Optional.ofNullable(objectResponse.body())
                                        .ifPresent(jwtSignUpResponse -> {
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("ttlCode", jwtSignUpResponse.getVerificationCodeTtl());
                                            bundle.putString("token", jwtSignUpResponse.getToken());
                                            bundle.putString("login", request.getEmail());
                                            bundle.putString("password", request.getPassword());
                                            Navigation.getNavigation().forward( ConfirmRegistrationFragment.IDENTITY, bundle);
                                        });
                            }
                        });
            }

        });

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