/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 2/12/12
 * Time: 11:30 AM
 */
public final class ExceptionUtil {

    private ExceptionUtil() {
        // no instance
    }

    /**
     * Convert stack trace of an exception to string.
     *
     * @param exception exception
     * @return full stack trace as string
     */
    public static String stackTraceToString(final Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }


}
