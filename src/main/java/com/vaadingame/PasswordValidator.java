package com.vaadingame;

import com.vaadin.data.Validator;
import com.vaadin.ui.PasswordField;

public class PasswordValidator implements Validator{
    private PasswordField passwordField;

    public PasswordValidator(PasswordField passwordField) {
        this.passwordField = passwordField;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if(!(passwordField.getValue().equals(value))) throw new InvalidValueException("Hasła nie są identyczne!");
    }
}
