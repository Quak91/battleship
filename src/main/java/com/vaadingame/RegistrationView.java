package com.vaadingame;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.net.UnknownHostException;

@DesignRoot
public class RegistrationView extends VerticalLayout implements View {
    public RegistrationView(final Navigator navigator) {
        setSizeFull();

        TextField txtFldName = new TextField("Login");
        PasswordField txtFldPassword = new PasswordField("Hasło");
        PasswordField txtFldConfirmPass = new PasswordField("Potwierdź hasło");

        final RegistrationBean bean = new RegistrationBean();
        bean.setName("");
        bean.setPassword("");
        bean.setConfirm_password("");

        BeanItem<RegistrationBean> item = new BeanItem<RegistrationBean>(bean);
        final FieldGroup binder = new FieldGroup(item);
        binder.bind(txtFldName, "name");
        binder.bind(txtFldPassword, "password");
        binder.bind(txtFldConfirmPass, "confirm_password");

        txtFldName.addValidator(new BeanValidator(RegistrationBean.class, "name"));
        txtFldName.addValidator(new RegisterNameValidator());
        txtFldPassword.addValidator(new BeanValidator(RegistrationBean.class, "password"));
        txtFldConfirmPass.addValidator(new BeanValidator(RegistrationBean.class, "confirm_password"));
        txtFldConfirmPass.addValidator(new PasswordValidator(txtFldPassword));

        txtFldName.setImmediate(true);
        txtFldPassword.setImmediate(true);
        txtFldConfirmPass.setImmediate(true);

        FormLayout formLayout = new FormLayout();
        formLayout.addComponents(txtFldName, txtFldPassword, txtFldConfirmPass);
        formLayout.addComponent(new Button("Zarejestruj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                try {
                    binder.commit();
                    // rejestracja w bazie danych
                    try {
                        MongoClient mongoClient = new MongoClient("localhost", 27017);
                        DB db = mongoClient.getDB("battleship");
                        DBCollection collection = db.getCollection("users");
                        collection.insert(new BasicDBObject("name", bean.getName()).append("password", bean.getPassword()));
                    } catch (UnknownHostException e) {
                        System.err.println("Błąd połączenia z bazą danych");
                    }

                    /*
                     * okienko z wiadomością o pomyślnej rejestracji
                     * oraz z możliwością przejścia do logowania
                     */
                    final Window window = new Window("Zarejestrowano");
                    VerticalLayout windowLayout = new VerticalLayout();
                    windowLayout.setMargin(true);
                    window.setContent(windowLayout);
                    windowLayout.addComponent(new Label("Rejestracja przebiegła pomyślnie."));
                    windowLayout.addComponent(new Label("Czy chcesz przejść do logowania?"));
                    windowLayout.addComponent(new Label(""));
                    Button btnOk = new Button("Tak", new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            navigator.navigateTo("login");
                            window.close();
                        }
                    });
                    Button btnNo = new Button("Nie", new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            window.close();
                        }
                    });
                    HorizontalLayout buttonsLayout = new HorizontalLayout(btnOk, new Label("&nbsp;&nbsp;&nbsp;",ContentMode.HTML), btnNo);
                    buttonsLayout.setSizeUndefined();
                    windowLayout.addComponent(buttonsLayout);
                    windowLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
                    window.center();
                    getUI().addWindow(window);
                } catch (FieldGroup.CommitException e) {
                    // walidacja nie przechodzi
                    Notification.show("Wprowadzono nieprawidłowe dane", Notification.Type.ERROR_MESSAGE);
                }
            }
        }));

        Panel panel = new Panel("Rejestracja");
        panel.setSizeUndefined();
        formLayout.setMargin(true);
        panel.setContent(formLayout);
        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(new Label("Masz już konto?&nbsp;", ContentMode.HTML));
        layout.addComponent(new Link("Zaloguj się", new ExternalResource("/#!login")));

        addComponent(layout);
        setComponentAlignment(layout, Alignment.TOP_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        Page.getCurrent().setTitle("Battleship - rejestracja");

    }
}
