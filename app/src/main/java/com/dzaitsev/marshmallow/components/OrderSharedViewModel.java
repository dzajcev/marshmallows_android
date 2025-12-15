package com.dzaitsev.marshmallow.components;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dzaitsev.marshmallow.dto.Order;

public class OrderSharedViewModel extends ViewModel {

    private final MutableLiveData<Void> sumsChanged = new MutableLiveData<>();
    private final MutableLiveData<Void> doneChanged = new MutableLiveData<>();

    private final MutableLiveData<Order> orderLiveData = new MutableLiveData<>();

    public LiveData<Order> getOrder() {
        return orderLiveData;
    }

    public void setOrder(Order order) {
        orderLiveData.setValue(order);
    }
    public LiveData<Void> getSumsChanged() {
        return sumsChanged;
    }

    public LiveData<Void> getDoneChanged() {
        return doneChanged;
    }

    public void notifySumsChanged() {
        sumsChanged.setValue(null);
    }

    public void notifyDoneChanged() {
        doneChanged.setValue(null);
    }
}