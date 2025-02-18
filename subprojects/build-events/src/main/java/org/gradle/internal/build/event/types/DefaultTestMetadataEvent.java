/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.internal.build.event.types;

import org.gradle.tooling.internal.protocol.events.InternalTestMetadataDescriptor;
import org.gradle.tooling.internal.protocol.events.InternalTestMetadataEvent;

/**
 * Default implementation of the {@code InternalTestMetadataEvent} interface.
 *
 * This is created by the provider side of the tooling API.
 */
public final class DefaultTestMetadataEvent extends AbstractProgressEvent<InternalTestMetadataDescriptor> implements InternalTestMetadataEvent {
    private final String key;
    private final Object value;

    public DefaultTestMetadataEvent(long startTime, InternalTestMetadataDescriptor descriptor, String key, Object value) {
        super(startTime, descriptor);
        this.key = key;
        this.value = value;
    }

    @Override
    public String getDisplayName() {
        return getDescriptor().getDisplayName() + " for " + key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
