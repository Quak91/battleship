package com.vaadingame;

import com.mongodb.*;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.net.UnknownHostException;

public class LoginView extends VerticalLayout implements View{
    final Navigator navigator;

    public LoginView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;

        TextField txtFldName = new TextField("Login");
        PasswordField txtFldPassword = new PasswordField("Hasło");

        final LoginBean bean = new LoginBean();
        bean.setName("");
        bean.setPassword("");

        BeanItem<LoginBean> item = new BeanItem<LoginBean>(bean);
        final FieldGroup binder = new FieldGroup(item);
        binder.bind(txtFldName, "name");
        binder.bind(txtFldPassword, "password");

        txtFldName.addValidator(new BeanValidator(LoginBean.class, "name"));
        txtFldPassword.addValidator(new BeanValidator(LoginBean.class, "password"));
        txtFldName.addValidator(new LoginNameValidator());

        txtFldName.setImmediate(true);
        txtFldPassword.setImmediate(true);

        FormLayout formLayout = new FormLayout();
        formLayout.addComponents(txtFldName, txtFldPassword);
        formLayout.addComponent(new Button("Zaloguj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                try {
                    binder.commit();
                    // sprawdzam login i hasło w bazie
                    try {
                        MongoClient mongoClient = new MongoClient("localhost", 27017);
                        DB db = mongoClient.getDB("baza");
                        DBCollection collection = db.getCollection("users");
                        BasicDBObject query = new BasicDBObject("name",bean.getName());
                        Cursor cursor = collection.find(query);
                        // jeśli hasło prawidłowe to zapisać login w sesji i przekierować do PlayersView
                        if(cursor.next().get("password").equals(bean.getPassword())) {
                            getSession().setAttribute("login", bean.getName());
                            navigator.navigateTo("players");
                        } else {
                            // hasło nieprawidłowe
                            Notification.show("Nieprawidłowe hasło", Notification.Type.ERROR_MESSAGE);
                        }
                    } catch (UnknownHostException e) {
                        System.err.println("Błąd połączenia z bazą danych");
                    }
                } catch (FieldGroup.CommitException e) {
                    // walidacja nie przechodzi
                    Notification.show("Wprowadzono nieprawidłowe dane", Notification.Type.ERROR_MESSAGE);
                }
            }
        }));

        Panel panel = new Panel("Logowanie");
        panel.setSizeUndefined();
        formLayout.setMargin(true);
        panel.setContent(formLayout);
        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(new Label("Nie masz jeszcze konta?&nbsp;", ContentMode.HTML));
        layout.addComponent(new Link("Zarejestruj się", new ExternalResource("/#")));

        addComponent(layout);
        setComponentAlignment(layout, Alignment.TOP_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        // jeśli zalogowany to przekierować do PlayersView
        if(getSession().getAttribute("login")!=null) navigator.navigateTo("players");
    }
}
