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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.GridSpacingItemDecoration;
import com.dzaitsev.marshmallow.adapters.ImagesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.InfoViewHolder;
import com.dzaitsev.marshmallow.adapters.PhotoViewHolder;
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
import com.google.android.material.tabs.TabLayoutMediator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GoodCardFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "goodCardFragment";

    private FragmentGoodCardBinding binding;
    private Good incomingGood;
    private Good good;

    // Адаптеры
    private GoodCardPagerAdapter pagerAdapter;
    private ImagesRecyclerViewAdapter imagesAdapter;
    private PriceHistoryRecyclerViewAdapter priceHistoryAdapter;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private int currentPosition = -1;
    private boolean isHistoryExpanded = false;

    private final Navigation.OnBackListener backListener = fragment -> {
        if (GoodCardFragment.IDENTITY.equals(fragment.identity())) {
            if (GoodCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> GoodCardFragment.this.save());
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> {
                    clearImages();
                    Navigation.getNavigation().back();
                });
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    private void clearImages() {
        Set<Integer> existed = incomingGood.getImages()
                .stream()
                .map(Attachment::getId)
                .collect(Collectors.toSet());
        if (imagesAdapter != null) {
            Set<Integer> all = imagesAdapter.getCurrentList()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Attachment::getId)
                    .collect(Collectors.toSet());
            SetUtils.difference(all, existed)
                    .forEach(f -> new NetworkExecutorHelper<>(requireActivity(),
                            NetworkService.getInstance().getFilesApi().delete(f))
                            .invoke());
        }
    }

    private void fillGood() {
        if (pagerAdapter != null && pagerAdapter.getInfoViewHolder() != null) {
            InfoViewHolder holder = pagerAdapter.getInfoViewHolder();
            if (holder.etName.getText() != null) {
                good.setName(holder.etName.getText().toString());
            }
            if (holder.etDesc.getText() != null) {
                good.setDescription(holder.etDesc.getText().toString());
            }
            if (CollectionUtils.isNotEmpty(good.getImages())) {
                if (good.getImages()
                        .stream()
                        .noneMatch(Attachment::isPrimary)) {
                    good.getImages().get(0).setPrimary(true);
                }
            }
            try {
                if (holder.etPrice.getText() != null) {
                    String cleanPrice = holder.etPrice.getText().toString().replaceAll("[^0-9]", "");
                    if (!cleanPrice.isEmpty()) {
                        good.setPrice(Double.parseDouble(cleanPrice));
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private boolean hasChanges() {
        fillGood();
        if (StringUtils.isEmpty(good.getName()) && good.getPrice() == null) {
            return false;
        }
        return !good.equals(incomingGood);
    }

    private boolean validateData() {
        if (pagerAdapter == null || pagerAdapter.getInfoViewHolder() == null) {
            return true;
        }
        InfoViewHolder holder = pagerAdapter.getInfoViewHolder();
        holder.layoutName.setError(null);
        holder.layoutPrice.setError(null);

        boolean isValid = true;
        if (StringUtils.isEmpty(good.getName())) {
            holder.layoutName.setError("Название обязательно");
            isValid = false;
        }
        if (good.getPrice() == null || good.getPrice() <= 0) {
            holder.layoutPrice.setError("Цена должна быть больше нуля");
            isValid = false;
        }

        if (!isValid) {
            binding.viewPager.setCurrentItem(0, true);
        }
        return isValid;
    }

    private void save() {
        fillGood();
        if (!validateData()) {
            return;
        }
        GoodsApi goodsApi = NetworkService.getInstance().getGoodsApi();
        new NetworkExecutorHelper<>(requireActivity(), goodsApi.saveGood(good))
                .invoke(response -> {
                    if (response.isSuccessful()) {
                        if (good.getId() == null && response.body() != null) {
                            good.setId(response.body().getId());
                        }
                        try {
                            incomingGood = good.clone();
                            Bundle result = new Bundle();
                            result.putString("good", GsonHelper.serialize(good));
                            getParentFragmentManager().setFragmentResult("goodUpdate", result);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        Navigation.getNavigation().back();
                    } else {
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deleteClient = menu.add("Удалить");
        if (incomingGood.isActive()) {
            deleteClient.setTitle("Удалить");
        } else {
            deleteClient.setTitle("Восстановить");
        }
        deleteClient.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
            builder.setTitle((incomingGood.isActive() ? "Удаление" : "Восстановление") + " товара?");
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
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            uploadImage(selectedImage);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey("good")) {
            good = GsonHelper.deserialize(requireArguments().getString("good"), Good.class);
        } else {
            good = new Good();
        }

        try {
            incomingGood = Objects.requireNonNull(good).clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        setHasOptionsMenu(good.getId() != null);
        binding = FragmentGoodCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка товара");
        Navigation.getNavigation().addOnBackListener(backListener);

        pagerAdapter = new GoodCardPagerAdapter();
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2); // Загружаем обе вкладки для валидации

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Инфо");
            else tab.setText("Фото");
        }).attach();

        pagerAdapter.setOnViewCreatedListener(new GoodCardPagerAdapter.OnViewCreatedListener() {
            @Override
            public void onInfoViewCreated(InfoViewHolder holder) {
                fillInfoTab(holder);
            }

            @Override
            public void onPhotoViewCreated(PhotoViewHolder holder) {
                fillPhotoTab(holder);
            }
        });

        binding.goodCardSave.setOnClickListener(v -> save());
    }

    private void fillInfoTab(InfoViewHolder holder) {
        holder.etName.setText(good.getName());
        holder.etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.layoutName.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        holder.etPrice.setText(MoneyUtils.moneyWithCurrencyToString(good.getPrice()));
        holder.etPrice.setOnClickListener(v -> MoneyPicker.builder(getContext())
                .setTitle("Укажите сумму")
                .setInitialValue(good.getPrice())
                .setMinValue(1)
                .setMaxValue(100000)
                .positiveButton(value -> {
                    holder.etPrice.setText(String.format("%s", MoneyUtils.moneyWithCurrencyToString(value)));
                    good.setPrice(value);
                    holder.layoutPrice.setError(null);
                })
                .build()
                .show());

        holder.etDesc.setText(good.getDescription());


        holder.recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        priceHistoryAdapter = new PriceHistoryRecyclerViewAdapter();
        if (good != null && good.getPrices() != null) {
            priceHistoryAdapter.setItems(new ArrayList<>(good.getPrices()));
        } else {
            holder.tvHistoryHeader.setVisibility(View.GONE);
        }
        holder.recyclerHistory.setAdapter(priceHistoryAdapter);

        Attachment primary = good.getImages().stream()
                .filter(Attachment::isPrimary)
                .findFirst()
                .orElse(good.getImages().isEmpty() ? null : good.getImages().get(0));

        if (primary != null) {
            String url = (primary.getThumbnailUrl() != null) ? primary.getThumbnailUrl()
                    : (primary.getUrl() != null ? primary.getUrl() : "");
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .error(R.drawable.error)
                    .into(holder.mainPhoto);
        } else {
            holder.mainPhoto.setImageResource(R.drawable.ic_placeholder); // Или null
        }

        holder.mainPhoto.setOnClickListener(v -> binding.viewPager.setCurrentItem(1, true));

        updateHistoryVisibility(holder);
        holder.tvHistoryHeader.setOnClickListener(v -> {
            isHistoryExpanded = !isHistoryExpanded;
            updateHistoryVisibility(holder);
        });
    }

    private void updateHistoryVisibility(InfoViewHolder holder) {
        if (good.getPrices() == null || good.getPrices().isEmpty()) {
            holder.recyclerHistory.setVisibility(View.GONE);
        } else {
            holder.recyclerHistory.setVisibility(isHistoryExpanded ? View.VISIBLE : View.GONE);
        }
    }

    private void fillPhotoTab(PhotoViewHolder holder) {
        holder.btnAddPhoto.setOnClickListener(v -> openGallery());

        List<Attachment> imageUrls = new ArrayList<>();
        if (good != null && good.getImages() != null) {
            imageUrls.addAll(good.getImages());
        }
        imageUrls.sort((o1, o2) -> Boolean.compare(o2.isPrimary(), o1.isPrimary()));
        imagesAdapter = new ImagesRecyclerViewAdapter(requireContext(), new ImagesRecyclerViewAdapter.OnImagePickListener() {
            @Override
            public void onPickImage(int position) {
                currentPosition = position;
                Attachment att = imagesAdapter.getCurrentList().get(position);
                if (att == null) {
                    openGallery();
                }
            }

            @Override
            public void onDeleteImage(int position) {
                Attachment toRemove = imagesAdapter.getCurrentList().get(position);
                if (toRemove != null) {
                    good.getImages().remove(toRemove);
                }
                List<Attachment> currentList = new ArrayList<>(imagesAdapter.getCurrentList());
                currentList.remove(position);
                imagesAdapter.submitList(currentList);
            }

            @Override
            public void onSetPrimary(int position) {
                List<Attachment> currentList = new ArrayList<>();
                for (int i = 0; i < imagesAdapter.getCurrentList().size(); i++) {
                    Attachment item = imagesAdapter.getCurrentList().get(i);
                    if (item != null) {
                        Attachment attachment = item.copy();
                        attachment.setPrimary(i == position);
                        currentList.add(attachment);
                    }
                }
                imagesAdapter.submitList(currentList);
                good.setImages(currentList);
            }
        });
        imagesAdapter.submitList(imageUrls);
        int spanCount = 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), spanCount);
        holder.recyclerPhotos.setLayoutManager(gridLayoutManager);
        holder.recyclerPhotos.setAdapter(imagesAdapter);
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        holder.recyclerPhotos.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        imagesAdapter.submitList(imageUrls);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImage(Uri selectedImage) {
        MultipartBody.Part body = createMultipartBody(selectedImage);
        if (body == null) return;

        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getFilesApi().saveAttachment(body))
                .invoke(response -> requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        good.getImages().add(response.body());
                        if (imagesAdapter != null) {
                            List<Attachment> currentList = new ArrayList<>(imagesAdapter.getCurrentList());
                            if (currentPosition != -1 && currentPosition < currentList.size()) {
                                currentList.set(currentPosition, response.body());
                            } else {
                                currentList.add(response.body());
                            }
                            imagesAdapter.submitList(currentList);
                            currentPosition = -1;
                        }
                    } else {
                        Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private MultipartBody.Part createMultipartBody(Uri imageUri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            String fileName = getFileName(requireContext(), imageUri);
            String mimeType = getContentType(requireContext(), imageUri);

            if (fileName == null) fileName = "image_" + System.currentTimeMillis() + ".jpg";
            if (mimeType == null) mimeType = "image/jpeg";

            InputStream inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream == null) return null;

            File tempFile = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
            return MultipartBody.Part.createFormData("file", fileName, requestFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFileName(Context context, Uri uri) {
        String fileName = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) fileName = cursor.getString(index);
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            assert fileName != null;
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) fileName = fileName.substring(cut + 1);
        }
        return fileName;
    }

    private String getContentType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }


    public static class GoodCardPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Getter
        private InfoViewHolder infoViewHolder;
        @Setter
        private OnViewCreatedListener onViewCreatedListener;

        public interface OnViewCreatedListener {
            void onInfoViewCreated(InfoViewHolder holder);

            void onPhotoViewCreated(PhotoViewHolder holder);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_good_tab_info, parent, false);
                infoViewHolder = new InfoViewHolder(view);
                return infoViewHolder;
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_good_tab_photos, parent, false);
                return new PhotoViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (onViewCreatedListener != null) {
                if (holder instanceof InfoViewHolder) {
                    onViewCreatedListener.onInfoViewCreated((InfoViewHolder) holder);
                } else if (holder instanceof PhotoViewHolder) {
                    onViewCreatedListener.onPhotoViewCreated((PhotoViewHolder) holder);
                }
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

    }
}
