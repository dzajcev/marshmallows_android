package com.dzaitsev.marshmallow.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;

public class MoneyUtils {
    private final NumberFormat MONEY_WITH_CURRENCY_FORMATTER = new DecimalFormat("#0.00р");
    private final NumberFormat MONEY_FORMATTER = new DecimalFormat("#0.00");

    private static MoneyUtils moneyUtils;

    public static MoneyUtils getInstance() {
        if (moneyUtils == null) {
            moneyUtils = new MoneyUtils();
        }
        return moneyUtils;
    }

    public String moneyWithCurrencyToString(Double money) {
        return Optional.ofNullable(money)
                .map(m -> {
                    if (money - money.intValue() == 0) {
                        return String.format("%sр", money.intValue());
                    } else {
                        return MONEY_WITH_CURRENCY_FORMATTER.format(money);
                    }
                }).orElse("");
    }

    public String moneyToString(Double money) {
        return Optional.ofNullable(money)
                .map(m -> {
                    if (money - money.intValue() == 0) {
                        return String.format("%s", money.intValue());
                    } else {
                        return MONEY_FORMATTER.format(money);
                    }
                }).orElse("");
    }

    public Double stringToDouble(String money) {
        return Optional.ofNullable(money)
                .map(m -> StringUtils.isEmpty(m) ? null : m)
                .map(m -> {
                    try {
                        return MONEY_FORMATTER.parse(money);
                    } catch (ParseException e) {
                        try {
                            return MONEY_WITH_CURRENCY_FORMATTER.parse(money);
                        } catch (ParseException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                })
                .map(Number::doubleValue)
                .orElse(null);
    }

}
