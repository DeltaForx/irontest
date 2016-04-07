package io.irontest.handlers;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Trevor Li on 7/14/15.
 */
public class DBHandler implements IronTestHandler {
    public DBHandler() { }

    public Object invoke(String request, Map<String, String> details) throws Exception {
        DBI jdbi = new DBI(details.get("url"), details.get("username"), details.get("password"));
        Handle handle = jdbi.open();

        Query<Map<String, Object>> query = handle.createQuery(request);
        List<Map<String, Object>> results = query.list();

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // StringWriter responseWriter = new StringWriter();
        // mapper.writeValue(responseWriter, results);

        handle.close();

        return results;
    }

    public List<String> getProperties() {
        String[] properties = {"url", "username", "password"};
        return Arrays.asList(properties);
    }
}