package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.Good;

import java.util.List;


public class GoodsResponse {

    private List<Good> goods;

    public List<Good> getGoods() {
        return goods;
    }

    public void setGoods(List<Good> goods) {
        this.goods = goods;
    }
}
