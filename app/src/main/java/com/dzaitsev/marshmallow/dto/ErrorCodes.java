package com.dzaitsev.marshmallow.dto;

public enum ErrorCodes {
    AUTH000("Неизвестная ошибка"),
    AUTH001("Код верификации не верный"),
    AUTH002("Код верификации просрочен"),
    AUTH003("Пользователь с такой почтой зарегистрирован"),
    AUTH004("Пользователь не найден"),
    AUTH005("Еще рано отправлять новый нод"),
    AUTH006("Время жизни токена истекло"),
    AUTH007("Неверный логин или пароль"),
    AUTH008("Требуется подтверждение учетной записи"),
    IU001("Пользователь с таким логином не найден")

            ;

    private final String text;

    ErrorCodes(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
