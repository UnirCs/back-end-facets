package com.unir.facets.config;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.unir.facets.data")
public class ElasticsearchConfig extends AbstractOpenSearchConfiguration {

  @Value("${opensearch.host}")
  private String clusterEndpoint;

  @Value("${opensearch.credentials.user}")
  private String username;

  @Value("${opensearch.credentials.password}")
  private String password;

  @Override
  public RestHighLevelClient opensearchClient() {
    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(clusterEndpoint + ":443")
        .usingSsl()
        .withBasicAuth(username, password)
        .build();
    return RestClients.create(clientConfiguration).rest();
  }
}
