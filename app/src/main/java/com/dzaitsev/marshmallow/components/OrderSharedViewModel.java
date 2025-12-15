package com.dzaitsev.marshmallow.components;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dzaitsev.marshmallow.dto.Order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSharedViewModel extends ViewModel {

    private final MutableLiveData<Void> sumsChanged = new MutableLiveData<>();
    private final MutableLiveData<Void> doneChanged = new MutableLiveData<>();
    private final MutableLiveData<Void> deliveryChanged = new MutableLiveData<>();

    private final MutableLiveData<Order> orderLiveData = new MutableLiveData<>();


    public void notifySumsChanged() {
        sumsChanged.setValue(null);
    }

    public void notifyDoneChanged() {
        doneChanged.setValue(null);
    }

    public void notifyDeliveryChanged() {
        deliveryChanged.setValue(null);
    }

    public void setOrder(Order order) {
        orderLiveData.setValue(order);
    }

}