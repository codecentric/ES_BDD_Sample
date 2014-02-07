package de.codecentric.esbdd.example;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.jbehave.core.annotations.*;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class TweetRetrievalTest{

    public ElasticsearchTestNode testNode = new ElasticsearchTestNode();

    @BeforeStory
    public void setUp() throws Throwable {
        testNode.before();

        testNode.getClient().admin().indices().create(new CreateIndexRequest("twitter")).actionGet();
        testNode.getClient().admin().indices()
                .preparePutMapping("twitter")
                .setType("tweets")
                .setSource(mapping())
                .execute().actionGet();
    }

    @AfterStory
    public void after(){
        testNode.getClient().admin().indices().prepareGetFieldMappings("twitter").execute().actionGet();
        testNode.after();
    }


    private SearchResponse response;

    @Given("A user $user submitted a tweet $tweet")
    public void userTweets(@Named("tweet") String tweet , @Named("user") String user) throws IOException {
        testNode.getClient().prepareIndex("twitter", "tweets", "1")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user", user)
                        .field("message", tweet)
                        .endObject())
                .execute()
                .actionGet();
    }

    @When("We list all tweets for the user $user")
    public void retreiveTweetsForUser(@Named("user") String user) {
        response = testNode.getClient().prepareSearch("twitter").
                setTypes("tweets")
                .setQuery(QueryBuilders.termQuery("user", user))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();

    }

    @Then("A tweet with the text $text will be found")
    public void expectTweet(@Named("tweet") String tweet)  {
        for (SearchHit hitFields : response.getHits().getHits()) {
            if(hitFields.field("tweet").getValue().equals(tweet)) {
                return;
            }
        }
        // not found
        // throw new RuntimeException("expected Tweet " + tweet + "not found");
    }

    /**
     * Overriding mapping with french content
     */
    public XContentBuilder mapping() throws Exception {
        XContentBuilder xbMapping =
                jsonBuilder()
                        .startObject()
                        .startObject("tweet")
                        .startObject("properties")
                        .startObject("source")
                        .field("type", "string")
                        .endObject()
                        .startObject("user")
                        .field("type", "string")
                        .endObject()
                        .startObject("message")
                        .field("type", "string")
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject();
        return xbMapping;
    }

}
