package mod

import org.junit.Test;
import org.vertx.java.framework.TestBase;


public class PersistorTest extends TestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();



            System.setProperty("vertx.test.timeout", "3000");
            startApp(mods.eban.alchemy.TestClient.class.getName(),true);


    }



    @Test
    public void testPersistor() throws Exception {

        startTest(getMethodName(),true);
    }

    @Test
    public void testSave() throws Exception {

        startTest(getMethodName(),true);
    }


}
