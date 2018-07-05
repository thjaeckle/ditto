/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 *
 */
package org.eclipse.ditto.services.things.persistence.actors.strategies;

import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributesResponse;

/**
 * This strategy handles the {@link RetrieveAttributes} command.
 */
@NotThreadSafe
final class RetrieveAttributesStrategy extends AbstractThingCommandStrategy<RetrieveAttributes> {

    /**
     * Constructs a new {@code RetrieveAttributesStrategy} object.
     */
    public RetrieveAttributesStrategy() {
        super(RetrieveAttributes.class);
    }

    @Override
    protected Result doApply(final Context context, final RetrieveAttributes command) {
        final String thingId = context.getThingId();
        final Thing thing = context.getThing();
        final Optional<Attributes> optionalAttributes = thing.getAttributes();
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        if (optionalAttributes.isPresent()) {
            final Attributes attributes = optionalAttributes.get();
            final Optional<JsonFieldSelector> selectedFields = command.getSelectedFields();
            final JsonObject attributesJson = selectedFields
                    .map(sf -> attributes.toJson(command.getImplementedSchemaVersion(), sf))
                    .orElseGet(() -> attributes.toJson(command.getImplementedSchemaVersion()));
            return result(RetrieveAttributesResponse.of(thingId, attributesJson, dittoHeaders));
        } else {
            return result(attributesNotFound(thingId, dittoHeaders));
        }
    }

}
