package org.opencb.commons.datastore.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class QueryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    enum TestQueryParam implements QueryParam {
        TEST_PARAM_DECIMAL("testParamDecimal", Type.DECIMAL, ""),
        TEST_PARAM_INTEGER_ARRAY("testParamIntegerArray", Type.INTEGER_ARRAY, ""),
        TEST_PARAM_TEXT("testParamText", Type.TEXT, ""),
        TEST_PARAM_BOOLEAN("testParamBoolean", Type.BOOLEAN, ""),
        ;

        TestQueryParam(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        String key;
        Type type;
        String description;

        @Override public String key() {return key;}
        @Override public String description() {return description;}
        @Override public Type type() {return type;}
    }

    @Test
    public void testValidate() throws Exception {
        Query query = new Query(TestQueryParam.TEST_PARAM_BOOLEAN.key(), true)
                .append(TestQueryParam.TEST_PARAM_INTEGER_ARRAY.key(), "1,2,3")
                .append(TestQueryParam.TEST_PARAM_TEXT.key(), "asdf");

        query.validate(TestQueryParam.class);
    }

    @Test
    public void testValidateError() throws Exception {
        Query query = new Query(TestQueryParam.TEST_PARAM_BOOLEAN.key(), true)
                .append(TestQueryParam.TEST_PARAM_INTEGER_ARRAY.key(), "1,2,3,A,FFF")
                .append(TestQueryParam.TEST_PARAM_TEXT.key(), "asdf");

        thrown.expect(NumberFormatException.class);
        query.validate(TestQueryParam.class);
    }

    @Test
    public void testValidateError2() throws Exception {
        Query query = new Query(TestQueryParam.TEST_PARAM_BOOLEAN.key(), true)
                .append("NotAField", "1,2,3,A,FFF")
                .append(TestQueryParam.TEST_PARAM_TEXT.key(), "asdf");

        thrown.expect(EnumConstantNotPresentException.class);
        query.validate(TestQueryParam.class);
    }

    @Test(expected = EnumConstantNotPresentException.class)
    public void testValidateWrongParam() throws Exception {
	Query query = new Query(TestQueryParam.TEST_PARAM_BOOLEAN.key(), true).append("wrongParam", "1");
	query.validate(TestQueryParam.class);
    }
}
