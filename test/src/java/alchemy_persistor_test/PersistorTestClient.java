/*
 * Copyright 2011-2012 Andrea Parodi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alchemy_persistor_test;

import org.python.google.common.base.Joiner;
import org.python.google.common.io.Files;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.framework.testng.TestClientBase;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;


public class PersistorTestClient extends TestClientBase {

    private EventBus eb;
    private static final Handler<Message<JsonObject>> EMPTY_HANDLER = new Handler<Message<JsonObject>>() {
        @Override
        public void handle(Message<JsonObject> jsonObjectMessage) {
        }
    };

    @Override
    public void start() {
        super.start();

        eb = vertx.eventBus();


        JsonObject config;
        try {
            List<String> lines = Files.readLines(new File("test/src/resources/config.json"), Charset.defaultCharset());
            config = new JsonObject(Joiner.on("\n").join(lines));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read module test config", e);
        }

        File db = new File("test/db/testing.db");
        if (db.exists())
            db.delete();
        tu.azzert(!db.exists());
//
//        container.deployModule("eban.alchemy-persistor-v0.1", config, 1, new Handler<String>() {
//            public void handle(String res) {
//
//
//                tu.appReady();
//            }
//        });

        container.deployVerticle("alchemy_persistor/AlchemyPersistor.py", config, 1, new Handler<String>() {
            public void handle(String res) {

                System.out.print(res);
                tu.appReady();
            }
        });

    }

    public void testDbCreation() throws Exception {

        File db = new File("test/db/testing.db");

        tu.azzert(db.exists());
        tu.testComplete();
    }
    final JsonObject testUser = new JsonObject()
            .putString("username", "testuser")
            .putString("fullname", "A test testUser")
            .putString("password", "mypassword")
            .putBoolean("confirmed", true)
            .putBoolean("admin", true)
            .putString("email", "andrea.parodi@ebansoftware.net");


    public void testMultipleTypes_insert() throws Exception {
        final BigInteger oneFrillion = BigInteger.valueOf(1_000_000).multiply(BigInteger.valueOf(1_000_000));
        final String japanese = "私は学生です";       //no, unfortunately, I'm not http://bit.ly/QN7Bix
        final JsonObject testRow = new JsonObject()
                .putString("aString", "really a complex row")
                .putNumber("aInteger", 1234)
                .putNumber("aSmallInteger", 34)
                .putNumber("aBigInteger", oneFrillion)
                .putNumber("aNumeric", 6666.66)
                .putNumber("aFloat", 5555.55)
                .putString("aDateTime", "2012-12-25 23:00:00")
                .putString("aDate", "2012-12-25")
                .putString("aTime", "23:00:00")
                //.putString("aLargeBinary", "")
                //.putString("aBinary", "")
                .putBoolean("aBoolean", true)
                .putString("aUnicode", japanese)
                .putString("aUnicodeText", japanese)
                //.putString("aInterval", "")
                //.putString("aEnum", "")
        ;

        Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                tu.azzertResponseSuccess(msg);
                JsonObject jsonObject = msg.body;
                tu.azzert(jsonObject.getObject("document") != null);
                jsonObject= jsonObject.getObject("document");
                tu.azzertEquals(jsonObject.getString("aString"), "really a complex row");
                tu.azzertEquals(jsonObject.getNumber("aInteger"),1234 );
                tu.azzertEquals(jsonObject.getNumber("aSmallInteger"),34 );
//                tu.azzertEquals(jsonObject.getNumber("aBigInteger"),oneFrillion);
                tu.azzertEquals(jsonObject.getNumber("aNumeric"), 6666.66);
                tu.azzertEquals(jsonObject.getNumber("aFloat"),5555.55 );
                tu.azzertEquals(jsonObject.getString("aDateTime"), "2012-12-25 23:00:00" );
                tu.azzertEquals(jsonObject.getString("aDate"),"2012-12-25" );
                tu.azzertEquals(jsonObject.getString("aTime"),  "23:00:00");
                tu.azzertEquals(jsonObject.getBoolean("aBoolean"),true );
                tu.azzertEquals(jsonObject.getString("aUnicode"),japanese);
                tu.azzertEquals(jsonObject.getString("aUnicodeText"),japanese );

                tu.testComplete();

            }
        };
        save(testRow, messageHandler,"MultipleTypes");


    }





    public void testInsert() throws Exception {


        saveUser(testUser, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {


                tu.azzertResponseSuccess(event);
                tu.testComplete();
            }

        });
    }

    public void testFind() throws Exception {
        runPersistoreTest("username=='testuser'", "find", "User", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                tu.azzertResponseSuccess(event);

                JsonObject jsonObject = event.body;
                tu.azzert(jsonObject.getArray("rows") != null);
                jsonObject= (JsonObject) jsonObject.getArray("rows").iterator().next();
                tu.azzertEquals(jsonObject,  "username", "testuser");
                tu.azzertEquals(jsonObject,  "fullname", "A test testUser");
                tu.azzertEquals(jsonObject,  "password", "mypassword");
                tu.azzertEquals(jsonObject,  "confirmed", true);
                tu.azzertEquals(jsonObject,  "admin", true);
                tu.azzertEquals(jsonObject,  "email", "andrea.parodi@ebansoftware.net");
                tu.testComplete();
            }
        });
    }

    public void testFindOne() throws Exception {
        runPersistoreTest("username=='testuser'", "findone", "User", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                tu.azzertResponseSuccess(event);

                JsonObject jsonObject = event.body;
                tu.azzert(jsonObject.getObject("result") != null);
                tu.azzertEquals(jsonObject, "result", "username", "testuser");
                tu.azzertEquals(jsonObject, "result", "fullname", "A test testUser");
                tu.azzertEquals(jsonObject, "result", "password", "mypassword");
                tu.azzertEquals(jsonObject, "result", "confirmed", true);
                tu.azzertEquals(jsonObject, "result", "admin", true);
                tu.azzertEquals(jsonObject, "result", "email", "andrea.parodi@ebansoftware.net");
                tu.testComplete();
            }
        });
    }



    public void testUpdate() throws Exception {
        JsonObject testUser2=testUser.copy();
        testUser2.putString( "email", "someone.else@ebansoftware.net");

        saveUser(testUser2, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                JsonObject jsonObject = event.body;
                tu.azzertResponseSuccess(event);
                tu.azzertEquals(jsonObject, "document", "email", "someone.else@ebansoftware.net");
                tu.testComplete();
            }

        });
    }



    private void saveUser(JsonObject user, Handler<Message<JsonObject>> handler) {
        //save user
        save(user, handler, "User");
    }

    private void save(JsonObject user, Handler<Message<JsonObject>> handler, String collection) {
        JsonObject json2 = new JsonObject()
                .putString("collection", collection)
                .putString("action", "save")
                .putObject("document", user);
        eb.send("alchemy-persistor", json2, handler);
    }

    private void checkUserDoesNotExist(String username) {
        //check user is not findable
        runPersistoreTest(String.format("username=='%s'", username), "findone", "User", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                tu.azzertEquals(event.body.getObject("result"), null);
            }
        });
    }

    private void deleteTestUser(Handler<Message<JsonObject>> handler) {
        JsonObject json = new JsonObject()
                .putString("collection", "User")
                .putString("action", "delete")
                .putString("matcher", "username=='testuser'");

        eb.send("alchemy-persistor", json, handler);
    }


    public void testDelete() throws Exception {

        //checkUserDoesNotExist("testuser");

        //final JsonObject user = new JsonObject().putString("username", "testuser");

        //saveUser(user, EMPTY_HANDLER);

        deleteTestUser(new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                tu.azzertResponseSuccess(msg);
                checkUserDoesNotExist("testuser");
                tu.testComplete();
            }
        });


    }

    private void runPersistoreTest(String matcher, String findone, String user, Handler<Message<JsonObject>> replyTestHandler) {
        JsonObject json = new JsonObject()
                .putString("collection", user)
                .putString("action", findone)
                .putNumber("page", 1)
                .putString("matcher", matcher);


        eb.send("alchemy-persistor", json, replyTestHandler);
    }



}