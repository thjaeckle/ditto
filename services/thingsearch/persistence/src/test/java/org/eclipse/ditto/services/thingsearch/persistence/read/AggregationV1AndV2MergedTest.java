/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.thingsearch.persistence.read;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.services.thingsearch.persistence.AbstractThingSearchPersistenceTestBase;
import org.eclipse.ditto.services.thingsearch.querymodel.query.PolicyRestrictedSearchAggregation;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests whether there are also found things which have V1 and Things with V2 in one query.
 */
public class AggregationV1AndV2MergedTest extends AbstractReadPersistenceTestBase {

    private static final String THING_V1_ID1 = "thingsearch.read:thingV1_1";
    private static final String THING_V1_ID2 = "thingsearch.read:thingV1_2";
    private static final String THING_V2_ID1 = "thingsearch.read:thingV2_1";
    private static final String THING_V2_ID2 = "thingsearch.read:thingV2_2";

    private static final String KNOWN_NUMBER_ATTR = "magicNo";
    private static final String KNOWN_STRING_ATTR = "cuttingEdge";

    private static final String KNOWN_STRING_ATTR_VALUE_THING_1_V1 = "reactive";
    private static final String KNOWN_STRING_ATTR_VALUE_THING_2_V1 = "functional";
    private static final String KNOWN_STRING_ATTR_VALUE_THING_1_V2 = "nonblocking";
    private static final String KNOWN_STRING_ATTR_VALUE_THING_2_V2 = "resilient";

    private static final int KNOWN_NUMBER_ATTR_VALUE_THING_1_V1 = 23;
    private static final int KNOWN_NUMBER_ATTR_VALUE_THING_2_V1 = 42;
    private static final int KNOWN_NUMBER_ATTR_VALUE_THING_1_V2 = 101;
    private static final int KNOWN_NUMBER_ATTR_VALUE_THING_2_V2 = 1337;

    @Before
    @Override
    public void before() {
        super.before();
        insertThings();
    }


    private void insertThings() {
        final Attributes attributes1 = Attributes.newBuilder()
                .set(JsonFactory.newKey(KNOWN_STRING_ATTR), KNOWN_STRING_ATTR_VALUE_THING_1_V1)
                .set(JsonFactory.newKey(KNOWN_NUMBER_ATTR), KNOWN_NUMBER_ATTR_VALUE_THING_1_V1)
                .build();

        final Attributes attributes2 = Attributes.newBuilder()
                .set(JsonFactory.newKey(KNOWN_STRING_ATTR), KNOWN_STRING_ATTR_VALUE_THING_2_V1)
                .set(JsonFactory.newKey(KNOWN_NUMBER_ATTR), KNOWN_NUMBER_ATTR_VALUE_THING_2_V1)
                .build();

        final Attributes attributes3 = Attributes.newBuilder()
                .set(JsonFactory.newKey(KNOWN_STRING_ATTR), KNOWN_STRING_ATTR_VALUE_THING_1_V2)
                .set(JsonFactory.newKey(KNOWN_NUMBER_ATTR), KNOWN_NUMBER_ATTR_VALUE_THING_1_V2)
                .build();

        final Attributes attributes4 = Attributes.newBuilder()
                .set(JsonFactory.newKey(KNOWN_STRING_ATTR), KNOWN_STRING_ATTR_VALUE_THING_2_V2)
                .set(JsonFactory.newKey(KNOWN_NUMBER_ATTR), KNOWN_NUMBER_ATTR_VALUE_THING_2_V2)
                .build();

        persistThingV1(createThingV1(THING_V1_ID1).setAttributes(attributes1));
        persistThingV1(createThingV1(THING_V1_ID2).setAttributes(attributes2));
        persistThingV2(createThingV2(THING_V2_ID1).setAttributes(attributes3));
        persistThingV2(createThingV2(THING_V2_ID2).setAttributes(attributes4));
    }

    /** */
    @Test
    public void findAllByLtNumber() {
        final PolicyRestrictedSearchAggregation aggregation1 = AbstractThingSearchPersistenceTestBase.abf
                .newBuilder(AbstractThingSearchPersistenceTestBase.cf.fieldCriteria(
                        AbstractThingSearchPersistenceTestBase.fef.filterByAttribute(KNOWN_NUMBER_ATTR),
                        AbstractThingSearchPersistenceTestBase.cf.lt(200)))
                .authorizationSubjects(AbstractThingSearchPersistenceTestBase.KNOWN_SUBJECTS)
                .build();

        final Collection<String> result = findAll(aggregation1);
        assertThat(result).containsOnly(THING_V1_ID1, THING_V1_ID2, THING_V2_ID1);
    }

    /** */
    @Test
    public void findAllByStrings() {
        final PolicyRestrictedSearchAggregation aggregation1 = AbstractThingSearchPersistenceTestBase.abf
                .newBuilder(AbstractThingSearchPersistenceTestBase.cf.or(Arrays.asList(

                        AbstractThingSearchPersistenceTestBase.cf.fieldCriteria(
                                AbstractThingSearchPersistenceTestBase.fef.filterByAttribute(KNOWN_STRING_ATTR),
                                AbstractThingSearchPersistenceTestBase.cf.eq(KNOWN_STRING_ATTR_VALUE_THING_1_V1)),

                        AbstractThingSearchPersistenceTestBase.cf.fieldCriteria(
                                AbstractThingSearchPersistenceTestBase.fef.filterByAttribute
                                        (KNOWN_STRING_ATTR),
                                AbstractThingSearchPersistenceTestBase.cf.eq(KNOWN_STRING_ATTR_VALUE_THING_2_V2))
                )))
                .authorizationSubjects(AbstractThingSearchPersistenceTestBase.KNOWN_SUBJECTS)
                .build();

        final Collection<String> result = findAll(aggregation1);
        assertThat(result).containsOnly(THING_V1_ID1, THING_V2_ID2);
    }

}
