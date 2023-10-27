package com.dzaitsev.marshmallow.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SendWhatsappService {


    private static SendWhatsappService sendWhatsappService;


    private SendWhatsappService() {
    }

    public static SendWhatsappService getInstance() {
        if (sendWhatsappService == null) {
            sendWhatsappService = new SendWhatsappService();
        }
        return sendWhatsappService;
    }


    public void send(Context context, String number, String text) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone="
                    + number
                    + "&text=" + text;
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
