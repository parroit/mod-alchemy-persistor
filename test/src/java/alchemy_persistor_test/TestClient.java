package alchemy_persistor_test;
/*
* Copyright 2011-2012 the original author or authors.
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

import org.python.google.common.base.Joiner;
import org.python.google.common.base.Strings;
import org.python.google.common.io.Files;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.framework.TestClientBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Most of the testing is done in JS since it's so much easier to play with JSON in JS rather than Java
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestClient extends TestClientBase {

    private EventBus eb;

    @Override
    public void start() {
        super.start();

        eb = vertx.eventBus();


        JsonObject config;
        try {
            List<String> lines = Files.readLines(new File("test/src/config.json"), Charset.defaultCharset());
            config = new JsonObject(Joiner.on("\n").join(lines));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read module test config",e);
        }



        container.deployModule("eban.alchemy-persistor-v0.1", config, 1, new Handler<String>() {
            public void handle(String res) {

                tu.appReady();
            }
        });



    }


    public void testSave() throws Exception {


        final JsonObject user = new JsonObject()
                .putString("username", "testuser")
                .putString("fullname", "A test user");

        JsonObject json = new JsonObject()
                .putString("collection", "User")
                .putString("action", "delete")
                .putString("matcher", "username=='testuser'");



        //first attempt delete of test user
        eb.send("alchemy-persistor", json, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {


                JsonObject jsonObject = reply.body;
                System.out.println(jsonObject.getString("error"));

                azzertEquals(jsonObject.getString("status"), "ok");

                //check user is not findable
                runPersistoreTest("username=='testuser'", "findone", "User", new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {
                        azzertEquals(event.body.getObject("result") ,null);


                    }
                });


                //save user
                JsonObject json2 = new JsonObject()
                        .putString("collection", "User")
                        .putString("action", "save")
                        .putObject("document", user);

                eb.send("alchemy-persistor", json2, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {

                        azzertEquals(event.body.getString("status"), "ok");

                        //check user is findable and has correct properties
                        runPersistoreTest("username=='testuser'", "findone", "User", new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                tu.azzert(event.body.getObject("result") != null);
                                azzertEquals(event.body, "result", "username", "testuser");
                                azzertEquals(event.body, "result", "fullname", "A test user");
                                tu.testComplete();
                            }
                        });
                    }
                });

            }
        });
    }

    public void testPersistor() throws Exception {


        runPersistoreTest("username=='parroit'", "findone", "User", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                JsonObject jsonObject = reply.body;
                System.out.println(jsonObject.toMap());


                tu.azzert(reply != null);
                tu.azzert(jsonObject != null);
                azzertEquals(jsonObject.getString("status"), "ok");


                azzertEquals(jsonObject, "result", "username", "parroit");
                azzertEquals(jsonObject, "result", "password", "mypassword");
                azzertEquals(jsonObject, "result", "confirmed", "True");
                azzertEquals(jsonObject, "result", "admin", "True");
                azzertEquals(jsonObject, "result", "fullname", "Andrea");
                azzertEquals(jsonObject, "result", "email", "andrea.parodi@ebansoftware.net");


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


    private <T> void azzertEquals(JsonObject jsonObject, String actualMapName, String actualFieldName, T expected) {
        Map<String, Object> result = jsonObject.getObject(actualMapName).toMap();
        Object actual = result.get(actualFieldName);
        azzertEquals(expected, actual);
    }

    private <T> void azzertEquals(T actual, T expected) {
        tu.azzert(
                (expected == null && actual == null) ||
                (expected != null && expected.equals(actual)),
                String.format("Expected %s, actually was %s", expected, actual));
    }

}