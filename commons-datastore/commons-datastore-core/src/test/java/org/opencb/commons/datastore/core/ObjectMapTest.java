/*
 * Copyright 2015 OpenCB
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

package org.opencb.commons.datastore.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by imedina on 24/03/14.
 */
public class ObjectMapTest {

    private ObjectMap objectMap;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        objectMap = new ObjectMap();
        objectMap.put("string", "hello");
        objectMap.put("stringInteger", "1");
        objectMap.put("stringFloat", "1.0");
        objectMap.put("integer", 1);
        objectMap.put("long", 123_456_789_000L);
        objectMap.put("boolean", true);
        objectMap.put("float", 1.0f);
        objectMap.put("double", 1.0);
        ArrayList<String> list = new ArrayList<>();
        list.add("elem1");
        list.add("elem2");
        objectMap.put("list", list);
        objectMap.put("listCsv", "1,2,3,4,5");
        objectMap.put("listCsvBad", "1,2,X,4,5");
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        objectMap.put("map", map);
        objectMap.put("l1", new ObjectMap("l2", new ObjectMap("l3", new ObjectMap("l4", "value"))));
        objectMap.put("myModel", new MyModel("a", "b"));
        List<ObjectMap> values = Arrays.asList(
                new ObjectMap("id", "abc").append("name", "ABC").append("nested", new ObjectMap("value", "A")),
                new ObjectMap("id", "def").append("name", "DEF").append("nested", new ObjectMap("value", "D")),
                new ObjectMap("id", "ghi").append("name", "GHI").append("nested", new ObjectMap("value", "G"))
        );
        objectMap.put("objectList", values);

        List<ObjectMap> aValues = Arrays.asList(
                new ObjectMap("id", "Aabc").append("name", "AABC").append("nested", new ObjectMap("value", "AA")),
                new ObjectMap("id", "Adef").append("name", "ADEF").append("nested", new ObjectMap("value", "AD")),
                new ObjectMap("id", "Aghi").append("name", "AGHI").append("nested", new ObjectMap("value", "AG"))
        );
        List<ObjectMap> bValues = Arrays.asList(
                new ObjectMap("id", "Babc").append("name", "BABC").append("nested", new ObjectMap("value", "BA")),
                new ObjectMap("id", "Bdef").append("name", "BDEF").append("nested", new ObjectMap("value", "BD")),
                new ObjectMap("id", "Bghi").append("name", "BGHI").append("nested", new ObjectMap("value", "BG"))
        );
        List<ObjectMap> cValues = Arrays.asList(
                new ObjectMap("id", "Cabc").append("name", "CABC").append("nested", new ObjectMap("value", "CA")),
                new ObjectMap("id", "Cdef").append("name", "CDEF").append("nested", new ObjectMap("value", "CD")),
                new ObjectMap("id", "Cghi").append("name", "CGHI").append("nested", new ObjectMap("value", "CG"))
        );
        List<ObjectMap> nestedListValues = Arrays.asList(
                new ObjectMap("id", "abc").append("name", "ABC").append("nested", new ObjectMap("value", "A").append("list", aValues)),
                new ObjectMap("id", "def").append("name", "DEF").append("nested", new ObjectMap("value", "D").append("list", bValues)),
                new ObjectMap("id", "ghi").append("name", "GHI").append("nested", new ObjectMap("value", "G").append("list", cValues))
        );
        objectMap.put("nestedList", nestedListValues);
    }

    private static class MyModel {
        public MyModel() {
        }

        public MyModel(String key1, String key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public String key1;
        public String key2;
    }

    @Test
    public void testToJson() throws Exception {
        System.out.println(objectMap.toJson());
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        System.out.println(jsonObjectMapper.writeValueAsString(objectMap));

    }

    @Test
    public void testSafeToString() throws Exception {

    }

    @Test
    public void testGet() throws Exception {
        Integer s = (Integer) objectMap.get("integer");
        System.out.println(s);
    }

    @Test
    public void testGetString() throws Exception {
        assertEquals(objectMap.getString("string"), "hello");
        assertEquals(objectMap.getString("integer"), "1");
    }

    @Test
    public void testGetInt() throws Exception {
        assertEquals(objectMap.getInt("integer"), 1);
        assertEquals(objectMap.getInt("float"), 1);
        assertEquals(objectMap.getInt("double"), 1);
        assertEquals(objectMap.getInt("stringInteger"), 1);

        assertEquals(objectMap.getInt("stringFloat", 25), 25);
    }

    @Test
    public void testGetLong() throws Exception {
        assertEquals(objectMap.getLong("long"), 123_456_789_000L);
        assertEquals(objectMap.getLong("integer"), 1l);
        assertEquals(objectMap.getLong("float"), 1l);
        assertEquals(objectMap.getLong("double"), 1l);
        assertEquals(objectMap.getLong("stringInteger"), 1l);

        assertEquals(objectMap.getLong("stringFloat", 25), 25);
    }

    @Test
    public void testGetFloat() throws Exception {
        assertEquals(objectMap.getFloat("integer"), 1.0f, 0.0);
        assertEquals(objectMap.getFloat("float"), 1.0f, 0.0);
        assertEquals(objectMap.getFloat("double"), 1.0f, 0.0);
        assertEquals(objectMap.getFloat("stringInteger"), 1.0f, 0.0);
        assertEquals(objectMap.getFloat("stringFloat"), 1.0f, 0.0);
    }

    @Test
    public void testGetDouble() throws Exception {
        assertEquals(objectMap.getDouble("integer"), 1.0d, 0.0);
        assertEquals(objectMap.getDouble("float"), 1.0d, 0.0);
        assertEquals(objectMap.getDouble("double"), 1.0d, 0.0);
        assertEquals(objectMap.getDouble("stringInteger"), 1.0d, 0.0);
        assertEquals(objectMap.getDouble("stringFloat"), 1.0d, 0.0);
    }

    @Test
    public void testGetBoolean() throws Exception {

    }

    @Test
    public void testGetList() throws Exception {
        List<Object> list = objectMap.getList("list");
        System.out.println(list);
        System.out.println((String)list.get(0));
    }

    @Test
    public void testGetListAs() throws Exception {
        List<String> list = objectMap.getAsList("list", String.class);
        System.out.println(list);
        System.out.println(list.get(0));
    }

    @Test
    public void testGetAsList() throws Exception {
        List<String> list = objectMap.getAsStringList("list");
        assertEquals(list, objectMap.get("list"));
        assertEquals(list, objectMap.getAsList("list"));

        list = objectMap.getAsStringList("listCsv", ":");
        assertEquals(list.get(0), objectMap.getString("listCsv"));

        list = objectMap.getAsStringList("listCsv");
        assertEquals(list, Arrays.asList("1", "2", "3", "4", "5"));

        List<Integer> listCsv = objectMap.getAsIntegerList("listCsv");
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), listCsv);

        List<Double> listDCsv = objectMap.getAsDoubleList("listCsv");
        assertEquals(Arrays.asList(1d, 2d, 3d, 4d, 5d), listDCsv);

        list = objectMap.getAsStringList("unExisting");
        assertTrue(list.isEmpty());
    }

    @Test
    public void getBadList() {
        thrown.expect(NumberFormatException.class);
        objectMap.getAsIntegerList("listCsvBad");
    }

    @Test
    public void getBadList2() {
        thrown.expect(NumberFormatException.class);
        objectMap.getAsDoubleList("listCsvBad");
    }

    @Test
    public void testGetMap() throws Exception {

    }

    @Test
    public void testContainsKey() {
        assertEquals("{\"l2\":{\"l3\":{\"l4\":\"value\"}}}" ,objectMap.getNestedMap("l1").toJson());
        assertEquals("{\"l3\":{\"l4\":\"value\"}}" ,objectMap.getNestedMap("l1.l2").toJson());
        assertEquals("{\"l4\":\"value\"}" ,objectMap.getNestedMap("l1.l2.l3").toJson());
        assertEquals("value" ,objectMap.getNested("l1.l2.l3.l4"));
        assertEquals("value", objectMap.getNested("map.key"));

        assertEquals("a", objectMap.getNested("myModel.key1"));
        assertEquals("b", objectMap.getNested("myModel.key2"));
        assertEquals("b", objectMap.put("myModel.key2", "c", true));
        assertEquals("c", objectMap.getNested("myModel.key2"));

        assertEquals("D", objectMap.getNested("objectList[def].nested.value"));
        assertEquals("DEF", objectMap.getNested("objectList[def].name"));
        assertNull(objectMap.getNested("objectList[defg].name"));
        assertTrue(objectMap.getNested("objectList") instanceof List);

        objectMap.put("l1.l2.l3.l4.l5.l6.l7", "value", true, true);
        assertEquals("{\"l2\":{\"l3\":{\"l4\":{\"l5\":{\"l6\":{\"l7\":\"value\"}}}}}}", objectMap.getNestedMap("l1").toJson());

        objectMap.put("transformed", true);
        assertFalse(objectMap.getBoolean("transformed.isolated"));
        objectMap.put("transformed.isolated", true);
        assertTrue(objectMap.getBoolean("transformed.isolated"));
    }

    @Test
    public void testGetWithFilterFromList() {
        assertEquals("DEF", objectMap.get("objectList[def].name"));
        assertEquals("DEF", objectMap.get("objectList[id=def].name"));
        assertEquals("GHI", objectMap.get("objectList[id=ghi].name"));
        assertEquals("DEF", objectMap.get("objectList[nested.value=D].name"));
        assertEquals("D", objectMap.get("objectList[def].nested.value"));
        assertEquals("BA", objectMap.get("nestedList[def].nested.list[name=BABC].nested.value"));
        assertEquals("CG", objectMap.get("nestedList[nested.value=G].nested.list[id=Cghi].nested.value"));
        assertEquals("CGHI", objectMap.get("nestedList[nested.value=G].nested.list[id=Cghi].name"));
    }

    @Test
    public void testPatternListSplit() {
        List<String> originalValues = Arrays.asList("disorder1", "disorder2, blabla", "disorder3");
        objectMap.put("key", StringUtils.join(originalValues, ","));
        objectMap.put("key1", "");
        objectMap.put("key2", "my value");
        objectMap.put("key3", Arrays.asList("1", "2"));
        objectMap.put("key4", Arrays.asList(1, 2));

        List<String> values = objectMap.getAsStringList("key", ObjectMap.COMMA_SEPARATED_LIST_SPLIT_PATTERN);
        assertEquals(originalValues.size(), values.size());
        assertTrue(originalValues.containsAll(values));

        values = objectMap.getAsStringList("key1", ObjectMap.COMMA_SEPARATED_LIST_SPLIT_PATTERN);
        assertEquals(1, values.size());
        assertEquals("", values.get(0));

        values = objectMap.getAsStringList("key2", ObjectMap.COMMA_SEPARATED_LIST_SPLIT_PATTERN);
        assertEquals(1, values.size());
        assertEquals("my value", values.get(0));

        values = objectMap.getAsStringList("key3", ObjectMap.COMMA_SEPARATED_LIST_SPLIT_PATTERN);
        assertEquals(2, values.size());
        assertEquals("1", values.get(0));
        assertEquals("2", values.get(1));

        values = objectMap.getAsStringList("key4", ObjectMap.COMMA_SEPARATED_LIST_SPLIT_PATTERN);
        assertEquals(2, values.size());
        assertEquals("1", values.get(0));
        assertEquals("2", values.get(1));
    }
}
