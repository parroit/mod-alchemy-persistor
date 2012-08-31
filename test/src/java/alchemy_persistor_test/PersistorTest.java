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


import org.testng.annotations.*;
import org.vertx.java.framework.testng.TestBase;


public class PersistorTest extends TestBase {

    @Override
    protected void onStart() throws Exception {

        System.setProperty("vertx.test.timeout", "3000");
        startApp(TestClient.class.getName(), true);


    }



    @Test
    public void testDbCreation() throws Exception {

        startTest(getMethodName(), true);
    }

    @Test(dependsOnMethods = "testDbCreation")
    public void testInsert() throws Exception {

        startTest(getMethodName(), true);
    }

    @Test(dependsOnMethods = "testInsert")
    public void testFind() throws Exception {
        startTest(getMethodName(), true);
    }

    @Test(dependsOnMethods = "testInsert")
    public void testFindOne() throws Exception {

        startTest(getMethodName(), true);
    }

    @Test(dependsOnMethods = "testFind")
    public void testUpdate() throws Exception{
        startTest(getMethodName(), true);
    }


    @Test(dependsOnMethods = "testUpdate")
    public void testDelete() throws Exception {

        startTest(getMethodName(), true);
    }


}
