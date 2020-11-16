/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.jvm.toolchain.internal;

import org.apache.commons.lang.StringUtils;
import org.gradle.internal.jvm.inspection.JvmVendor;
import org.gradle.jvm.toolchain.JvmVendorSpec;

import java.util.function.Predicate;

public class DefaultJvmVendorSpec extends JvmVendorSpec implements Predicate<JavaToolchain> {

    private static final JvmVendorSpec ANY = new DefaultJvmVendorSpec(v -> true, "any");

    private final Predicate<JvmVendor> matcher;
    private final String description;

    public static JvmVendorSpec matching(String match) {
        return new DefaultJvmVendorSpec(vendor -> StringUtils.containsIgnoreCase(vendor.getRawVendor(), match), "matching('" + match + "')");
    }

    public static JvmVendorSpec of(JvmVendor.KnownJvmVendor knownVendor) {
        return new DefaultJvmVendorSpec(vendor -> vendor.getKnownVendor() == knownVendor, knownVendor.toString());
    }

    public static JvmVendorSpec any() {
        return ANY;
    }

    private DefaultJvmVendorSpec(Predicate<JvmVendor> predicate, String description) {
        this.matcher = predicate;
        this.description = description;
    }

    @Override
    public boolean test(JavaToolchain toolchain) {
        final JvmVendor vendor = toolchain.getMetadata().getVendor();
        return test(vendor);
    }

    public boolean test(JvmVendor vendor) {
        return matcher.test(vendor);
    }

    @Override
    public String toString() {
        return description;
    }
}
