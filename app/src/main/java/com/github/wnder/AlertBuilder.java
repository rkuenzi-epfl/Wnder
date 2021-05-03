package com.github.wnder;


import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public final class AlertBuilder {
    private AlertBuilder(){
        //Non instanciable class
    }

    public static AlertDialog createAlert(String title, String body, Context ctx){
        AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(ctx);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(body);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }
}
