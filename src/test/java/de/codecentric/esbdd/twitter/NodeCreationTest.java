package de.codecentric.esbdd.twitter;

import de.codecentric.esbdd.example.ElasticsearchTestNode;
import org.elasticsearch.action.get.GetResponse;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class NodeCreationTest {

    @Rule
    public ElasticsearchTestNode testNode = new ElasticsearchTestNode();

    @Test

    public void indexAndGet() throws IOException {
        testNode.getClient().prepareIndex("myindex", "document", "1")
                .setSource(jsonBuilder().startObject().field("test", "123").endObject())
                .execute()
                .actionGet();

        GetResponse response = testNode.getClient().prepareGet("myindex", "document", "1").execute().actionGet();
        assertThat((String) response.getSource().get("test"),equalTo("123"));
    }

}
