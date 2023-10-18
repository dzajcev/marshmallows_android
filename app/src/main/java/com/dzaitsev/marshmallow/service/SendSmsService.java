package com.dzaitsev.marshmallow.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;

public class SendSmsService {

    private static SendSmsService sendSmsService;


    private SendSmsService() {


    }

    public static SendSmsService getInstance() {
        if (sendSmsService == null) {
            sendSmsService = new SendSmsService();
        }
        return sendSmsService;
    }


    public void sendSms(Context context, String number, String text) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("sms:" + number));//, "vnd.android-dir/mms-sms"
        smsIntent.putExtra("sms_body", text);
        smsIntent.setPackage(Telephony.Sms.getDefaultSmsPackage(context));
        context.startActivity(smsIntent);
    }
}
