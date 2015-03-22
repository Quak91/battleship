package com.vaadingame;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

@DesignRoot
public class RegistrationView extends VerticalLayout implements View {
    public RegistrationView(final Navigator navigator) {
        setSizeFull();

        TextField txtFldName = new TextField("Name");
        PasswordField txtFldPassword = new PasswordField("Password");
        PasswordField txtFldConfirmPass = new PasswordField("Confirm password");

        RegistrationBean bean = new RegistrationBean();
        BeanItem<RegistrationBean> item = new BeanItem<RegistrationBean>(bean);
        final FieldGroup binder = new FieldGroup(item);
        binder.bind(txtFldName, "name");
        binder.bind(txtFldPassword, "password");
        binder.bind(txtFldConfirmPass, "confirm_password");
        txtFldName.addValidator(new BeanValidator(RegistrationBean.class, "name"));
        txtFldPassword.addValidator(new BeanValidator(RegistrationBean.class, "password"));
        // TODO własne walidatory (czy nazwa już zajęta, czy password i confirm password są takie same)
        txtFldName.setImmediate(true);
        txtFldPassword.setImmediate(true);

        FormLayout formLayout = new FormLayout();
        formLayout.addComponent(txtFldName);
        formLayout.addComponent(txtFldPassword);
        formLayout.addComponent(txtFldConfirmPass);
        formLayout.addComponent(new Button("Zarejestruj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                try {
                    binder.commit();
                    //TODO dodanie użytkownika do bazy i komunikat
                } catch (FieldGroup.CommitException e) {
                    //TODO komunikat o błędzie
                }
            }
        }));

        Panel panel = new Panel("Rejestracja");
        panel.setSizeUndefined();
        formLayout.setMargin(true);
        panel.setContent(formLayout);
        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
