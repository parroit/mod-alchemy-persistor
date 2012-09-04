/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.java.testng;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EventFields {

  public static final String TYPE_FIELD = "type";

  public static final String EXCEPTION_EVENT = "exception";

  public static final String EXCEPTION_MESSAGE_FIELD = "message";
  public static final String EXCEPTION_STACKTRACE_FIELD = "stacktrace";

  public static final String TRACE_EVENT = "trace";
  public static final String TRACE_MESSAGE_FIELD = "message";


  public static final String ASSERT_EVENT = "assert";
  public static final String ASSERT_RESULT_FIELD = "result";
  public static final String ASSERT_RESULT_VALUE_PASS = "pass";
  public static final String ASSERT_RESULT_VALUE_FAIL = "fail";
  public static final String ASSERT_MESSAGE_FIELD = "message";
  public static final String ASSERT_STACKTRACE_FIELD = "stacktrace";


  public static final String APP_READY_EVENT = "app_ready";
  public static final String APP_STOPPED_EVENT = "app_stopped";

  public static final String TEST_COMPLETE_EVENT = "test_complete";
  public static final String TEST_COMPLETE_NAME_FIELD = "name";

  public static final String START_TEST_EVENT = "start_test";
  public static final String START_TEST_NAME_FIELD = "name";

}
