package com.vaadingame;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RegistrationBean {

    @NotNull(message = "Nazwa użytkowanika nie może być pusta")
    @Size(min = 4, max = 30, message = "Nazwa użytkownika musi zawierać od 4 do 30 znaków")
    private String name;

    @NotNull(message =  "Hasło nie może być puste")
    @Size(min = 8, message = "Hasło musi zawierać co najmniej 8 znaków")
    private String password;

    private String confirm_password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirm_password() {
        return confirm_password;
    }

    public void setConfirm_password(String confirm_password) {
        this.confirm_password = confirm_password;
    }
}
