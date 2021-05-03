package com.github.wnder;


import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public final class AlertBuilder {
    private AlertBuilder(){
        //Non instanciable class
    }

    public static AlertDialog createAlert(String title, String body, Context ctx){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(body);
        alertDialogBuilder.setPositiveButton("Ok",
                (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }
}
