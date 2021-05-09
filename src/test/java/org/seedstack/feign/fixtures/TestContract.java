/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

import feign.Body;
import feign.Contract;
import feign.HeaderMap;
import feign.Headers;
import feign.MethodMetadata;
import feign.Param;
import feign.QueryMap;
import feign.Request;
import feign.RequestLine;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestContract extends Contract.BaseContract {

    private static boolean used = false;

    public static boolean hasBeenUsed() {
        return used;
    }

    private static void use() {
        used = true;
    }

    private static <K, V> boolean searchMapValuesContainsSubstring(Map<K, Collection<String>> map,
            String search) {
        Collection<Collection<String>> values = map.values();
        if (values == null) {
            return false;
        }

        for (Collection<String> entry : values) {
            for (String value : entry) {
                if (value.indexOf(search) != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Map<String, Collection<String>> toMap(String[] input) {
        Map<String, Collection<String>> result = new LinkedHashMap<String, Collection<String>>(
                input.length);
        for (String header : input) {
            int colon = header.indexOf(':');
            String name = header.substring(0, colon);
            if (!result.containsKey(name)) {
                result.put(name, new ArrayList<String>(1));
            }
            result.get(name).add(header.substring(colon + 2));
        }
        return result;
    }

    // Copycat of DefaultContract with test-flag
    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> targetType) {
        use();
        if (targetType.isAnnotationPresent(Headers.class)) {
            String[] headersOnType = targetType.getAnnotation(Headers.class).value();
            checkState(headersOnType.length > 0, "Headers annotation was empty on type %s.",
                    targetType.getName());
            Map<String, Collection<String>> headers = toMap(headersOnType);
            headers.putAll(data.template().headers());
            data.template().headers(null); // to clear
            data.template().headers(headers);
        }
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation,
            Method method) {
        use();
        Class<? extends Annotation> annotationType = methodAnnotation.annotationType();
        if (annotationType == RequestLine.class) {
            String requestLine = RequestLine.class.cast(methodAnnotation).value();
            checkState(emptyToNull(requestLine) != null,
                    "RequestLine annotation was empty on method %s.", method.getName());
            if (requestLine.indexOf(' ') == -1) {
                checkState(requestLine.indexOf('/') == -1,
                        "RequestLine annotation didn't start with an HTTP verb on method %s.",
                        method.getName());
                data.template().method(Request.HttpMethod.valueOf(requestLine));
                return;
            }
            data.template().method(Request.HttpMethod.valueOf(requestLine.substring(0, requestLine.indexOf(' '))));
            if (requestLine.indexOf(' ') == requestLine.lastIndexOf(' ')) {
                // no HTTP version is ok
                data.template().uri(requestLine.substring(requestLine.indexOf(' ') + 1));
            } else {
                // skip HTTP version
                data.template().uri(
                        requestLine.substring(requestLine.indexOf(' ') + 1,
                                requestLine.lastIndexOf(' ')));
            }

            data.template().decodeSlash(RequestLine.class.cast(methodAnnotation).decodeSlash());

        } else if (annotationType == Body.class) {
            String body = Body.class.cast(methodAnnotation).value();
            checkState(emptyToNull(body) != null, "Body annotation was empty on method %s.",
                    method.getName());
            data.template().body(Request.Body.create(body, Charset.defaultCharset()));
        } else if (annotationType == Headers.class) {
            String[] headersOnMethod = Headers.class.cast(methodAnnotation).value();
            checkState(headersOnMethod.length > 0, "Headers annotation was empty on method %s.",
                    method.getName());
            data.template().headers(toMap(headersOnMethod));
        }

    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations,
            int paramIndex) {
        use();
        boolean isHttpAnnotation = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == Param.class) {
                String name = ((Param) annotation).value();
                checkState(emptyToNull(name) != null, "Param annotation was empty on param %s.",
                        paramIndex);
                nameParam(data, name, paramIndex);
                if (annotationType == Param.class) {
                    Class<? extends Param.Expander> expander = ((Param) annotation).expander();
                    if (expander != Param.ToStringExpander.class) {
                        data.indexToExpanderClass().put(paramIndex, expander);
                    }
                }
                isHttpAnnotation = true;
                String varName = '{' + name + '}';
                if (data.template().url().indexOf(varName) == -1 &&
                        !searchMapValuesContainsSubstring(data.template().queries(), varName) &&
                        !searchMapValuesContainsSubstring(data.template().headers(), varName)) {
                    data.formParams().add(name);
                }
            } else if (annotationType == QueryMap.class) {
                checkState(data.queryMapIndex() == null,
                        "QueryMap annotation was present on multiple parameters.");
                data.queryMapIndex(paramIndex);
                data.queryMapEncoded(QueryMap.class.cast(annotation).encoded());
                isHttpAnnotation = true;
            } else if (annotationType == HeaderMap.class) {
                checkState(data.headerMapIndex() == null,
                        "HeaderMap annotation was present on multiple parameters.");
                data.headerMapIndex(paramIndex);
                isHttpAnnotation = true;
            }
        }
        return isHttpAnnotation;
    }

}
