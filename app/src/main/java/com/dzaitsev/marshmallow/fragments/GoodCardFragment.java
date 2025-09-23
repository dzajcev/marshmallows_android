package com.dzaitsev.marshmallow.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.ImagesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.PriceHistoryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentGoodCardBinding;
import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.api.GoodsApi;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class GoodCardFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "goodCardFragment";

    private FragmentGoodCardBinding binding;
    private Good incomingGood;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Good good;
    private ImagesRecyclerViewAdapter imagesAdapter;
    private final Navigation.OnBackListener backListener = fragment -> {
        if (GoodCardFragment.this == fragment) {
            if (GoodCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> GoodCardFragment.this.save());
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation().back());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    private boolean hasChanges() {
        fillGood();
        if (StringUtils.isEmpty(good.getName()) && good.getPrice() == null) {
            return false;
        }
        return !good.equals(incomingGood);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deleteClient = menu.add("Удалить");
        if (incomingGood.isActive()) {
            deleteClient.setTitle("Удалить");
        } else {
            deleteClient.setTitle("Восстановить");
        }
        deleteClient.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
            builder.setTitle((incomingGood.isActive() ? "Удаление" : "Восстановление") + " зефирки?");
            new NetworkExecutorHelper<>(requireActivity(),
                    NetworkService.getInstance().getGoodsApi().checkGoodOnOrdersAvailability(good.getId()))
                    .invoke(booleanResponse -> {
                        if (!booleanResponse.isSuccessful()) {
                            return;
                        }
                        String text = "";
                        if (incomingGood.isActive() && Boolean.FALSE.equals(booleanResponse.body())) {
                            text = "\nЗапись будет удалена безвозвратно";
                        }
                        builder.setMessage("Вы уверены?" + text);
                        builder.setPositiveButton("Да", (dialog, id) -> {
                            GoodsApi goodsApi = NetworkService.getInstance().getGoodsApi();
                            new NetworkExecutorHelper<>(requireActivity(),
                                    incomingGood.isActive() ? goodsApi.deleteGood(good.getId()) : goodsApi.restoreGood(good.getId()))
                                    .invoke(response -> {
                                        if (response.isSuccessful()) {
                                            Navigation.getNavigation().back();
                                        }
                                    });
                        });
                        builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
                        builder.create().show();
                    });
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        good = GsonHelper.deserialize(requireArguments().getString("good"), Good.class);
        incomingGood = Objects.requireNonNull(good).clone();
        setHasOptionsMenu(good.getId() != null);
        binding = FragmentGoodCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public String getFileName(Context context, Uri uri) {
        String fileName = null;
        if (uri == null) {
            return null;
        }

        // Сначала пытаемся получить имя через ContentResolver (для content:// URI)
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                // Сброс на случай ошибки
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        // Если не удалось получить через ContentResolver или это file:// URI
        if (fileName == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                } else {
                    fileName = path; // Если нет '/', то весь путь - имя файла (маловероятно для валидных URI)
                }
            }
        }

        // Дополнительная обработка, если имя файла содержит закодированные символы (редко для DISPLAY_NAME)
        if (fileName != null) {
            try {
                fileName = URLDecoder.decode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }

        return fileName;
    }

    private String getContentType(Context context, Uri uri) {
        if (uri == null) return null;
        String contentType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) ||
                ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            contentType = context.getContentResolver().getType(uri);
        }
        if (contentType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (fileExtension != null && !fileExtension.isEmpty()) {
                contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return contentType;
    }

    private final View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };
    private int currentPosition = -1;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка зефирки");
        binding.goodCardCancel.setOnClickListener(v -> Navigation.getNavigation().callbackBack());
        Navigation.getNavigation().addOnBackListener(backListener);
        if (good == null || good.getPrices().isEmpty()) {
            binding.tx1.setVisibility(View.GONE);
        }
        binding.goodCardName.setOnKeyListener(keyListener);
        binding.goodCardName.setText(good.getName());
        binding.goodCardPrice.setOnKeyListener(keyListener);
        binding.goodCardPrice.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(good.getPrice()));
        binding.goodCardPrice.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                .setTitle("Укажите сумму")
                .setInitialValue(good.getPrice())
                .setMinValue(1)
                .setMaxValue(100000)
                .positiveButton(value -> {
                    binding.goodCardPrice.setText(String.format("%s", MoneyUtils.getInstance()
                            .moneyWithCurrencyToString(value)));
                    good.setPrice(value);
                })
                .build()
                .show());
        binding.goodCardDescription.setText(good.getDescription());
        binding.goodCardSave.setOnClickListener(v -> save());
        binding.goodCardPriceHistoryList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        PriceHistoryRecyclerViewAdapter priceHistoryRecyclerViewAdapter = new PriceHistoryRecyclerViewAdapter();
        binding.goodCardPriceHistoryList.setAdapter(priceHistoryRecyclerViewAdapter);

        // --- Настройка RecyclerView для изображений ---
        List<Attachment> imageUrls = new ArrayList<>();
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {

                            MultipartBody.Part body = createMultipartBody(selectedImage);

                            new NetworkExecutorHelper<>(requireActivity(),
                                    NetworkService.getInstance().getFilesApi().saveAttachment(body))
                                    .invoke(response -> {
                                        requireActivity().runOnUiThread(() -> {
                                            if (response.isSuccessful() && response.body() != null) {
                                                good.getImages().add(response.body());
                                                imageUrls.set(currentPosition, response.body());
                                                if (imageUrls.size() < 9) imageUrls.add(null);
                                                imagesAdapter.notifyItemChanged(currentPosition);
                                            } else {
                                                Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                        }
                    }
                }
        );

        binding.images.post(() -> {
            int recyclerViewWidth = binding.images.getWidth();
            if (recyclerViewWidth > 0) {
                float v = ((float) recyclerViewWidth / 3);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
                binding.images.setLayoutManager(gridLayoutManager);

                if (binding.images.getAdapter() == null) {
                    imagesAdapter = new ImagesRecyclerViewAdapter(requireContext(), (int) v - 4,
                            position -> {
                                currentPosition = position;
                                openGallery();
                            });
                    binding.images.setAdapter(imagesAdapter);

                    if (good != null && good.getImages() != null && !good.getImages().isEmpty()) {
                        imageUrls.addAll(good.getImages());
                    }
                    if (imageUrls.size() < 9) {
                        imageUrls.add(null);
                    }

                    imagesAdapter.submitList(imageUrls);
                }

            }
        });
        binding.tx1.setOnClickListener(view1 -> {
            ViewGroup.LayoutParams params = binding.goodCardPriceHistoryList.getLayoutParams();
            float scale = binding.goodCardPriceHistoryList.getContext().getResources().getDisplayMetrics().density;
            if (params.height == (int) (40 * scale)) {
                params.height = (int) (80 * scale); // увеличить
            } else {
                params.height = (int) (40 * scale); // вернуть назад
            }
            binding.goodCardPriceHistoryList.setLayoutParams(params);
        });

        priceHistoryRecyclerViewAdapter.setItems(good.getPrices().stream()
                .sorted((price, t1) -> t1.getCreateDate().compareTo(price.getCreateDate())).collect(Collectors.toList()));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private MultipartBody.Part createMultipartBody(Uri fileUri) {
        String fileName = getFileName(requireContext(), fileUri);
        String contentType = getContentType(requireContext(), fileUri);

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(contentType);
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri)) {
                    byte[] buffer = new byte[8192]; // больше буфер
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };

        return MultipartBody.Part.createFormData("file", fileName, requestBody);
    }

    private void save() {
        boolean fail = false;
        if (StringUtils.isEmpty(binding.goodCardName.getText().toString())) {
            binding.goodCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(binding.goodCardPrice.getText().toString())) {
            binding.goodCardPrice.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return;
        }
        fillGood();
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getGoodsApi().saveGood(good)).invoke(response -> {
            incomingGood = response.body();
            if (response.isSuccessful()) {
                Navigation.getNavigation().back();
            }
        });
    }

    private void fillGood() {
        good = new Good();
        good.setId(incomingGood == null ? null : incomingGood.getId());
        good.setName(binding.goodCardName.getText().toString());
        good.setDescription(binding.goodCardDescription.getText().toString());
        good.setImages(imagesAdapter.getCurrentList().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        good.setPrice(MoneyUtils.getInstance().stringToDouble(binding.goodCardPrice.getText().toString()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Navigation.getNavigation().removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}

