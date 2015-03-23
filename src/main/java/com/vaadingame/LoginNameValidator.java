package com.vaadingame;

import com.mongodb.*;
import com.vaadin.data.Validator;
import java.net.UnknownHostException;

public class LoginNameValidator implements Validator{
    private MongoClient mongoClient;
    private DB db;
    private DBCollection collection;
    private BasicDBObject query;
    private Cursor cursor;

    public LoginNameValidator() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("baza");
            collection = db.getCollection("users");
        } catch (UnknownHostException e) {
            System.err.println("Błąd połączenia z bazą danych");
        }
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        query = new BasicDBObject("name",value);
        cursor = collection.find(query);

        if(!cursor.hasNext()) throw new InvalidValueException("Nie ma użytkownika o takiej nazwie");
    }
}
