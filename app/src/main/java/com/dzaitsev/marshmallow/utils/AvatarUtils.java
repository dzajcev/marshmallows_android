package com.dzaitsev.marshmallow.utils;

import android.graphics.Color;

public class AvatarUtils {

    // Набор приятных Material цветов
    private static final int[] COLORS = new int[] {
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#039BE5"), // Light Blue
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#43A047"), // Green
            Color.parseColor("#689F38"), // Light Green
            Color.parseColor("#EF6C00"), // Orange
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#607D8B")  // Blue Grey
    };

    /**
     * Получает первые буквы первых двух слов
     * "Иванов Иван" -> "ИИ"
     * "ООО Ромашка" -> "ОР"
     * "Google" -> "G"
     */
    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
            // Если есть второе слово
            if (parts.length > 1 && !parts[1].isEmpty()) {
                initials.append(parts[1].charAt(0));
            }
        } else {
            return "?";
        }

        return initials.toString().toUpperCase();
    }

    /**
     * Возвращает цвет на основе хеш-кода строки.
     * Для одного и того же имени всегда будет один и тот же цвет.
     */
    public static int getColorForName(String name) {
        if (name == null || name.isEmpty()) {
            return Color.GRAY;
        }
        // Используем хеш-код для выбора цвета из массива
        int hash = Math.abs(name.hashCode());
        return COLORS[hash % COLORS.length];
    }
}
