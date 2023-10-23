package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlertDialogComponent {
    public static void showDialog(Context context, String title, String message, Action... action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        if (action.length > 0) {
            Map<Action.ActionType, Action> actions = Stream.of(action).collect(Collectors.toMap(Action::getAction, y -> y));
            Optional.ofNullable(actions.get(Action.ActionType.POSITIVE))
                    .ifPresent(a -> builder.setPositiveButton("Да", (dialog, id) -> a.doIn()));
            Optional.ofNullable(actions.get(Action.ActionType.NEGATIVE))
                    .ifPresent(a -> builder.setNegativeButton("Нет", (dialog, id) -> a.doIn()));
        }
        builder.create().show();
    }

    public interface Action {
        enum ActionType {
            POSITIVE, NEGATIVE
        }

        void doIn();

        ActionType getAction();
    }
}
