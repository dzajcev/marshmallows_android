package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.Client;

import java.util.ArrayList;
import java.util.List;


public class ClientResponse {

    private List<Client> clients=new ArrayList<>();

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}
