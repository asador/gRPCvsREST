package dev.rnd.rest.client;

import java.time.Duration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
	
	@Bean
	RestClient restClient() {
		return new RestClient();
	}

	@Bean
	RestTemplate restTemplate(@Value("${rest.server.address}") String serverBaseUrl) {
	  PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	  connectionManager.setMaxTotal(100);
	  connectionManager.setDefaultMaxPerRoute(100);

	  HttpClient httpClient = HttpClientBuilder.create()
	      .setConnectionManager(connectionManager)
	      .build();

	  return new RestTemplateBuilder().rootUri(serverBaseUrl)
	      .setConnectTimeout(Duration.ofMillis(10000))
	      .setReadTimeout(Duration.ofMillis(10000))
	      .messageConverters(new StringHttpMessageConverter(), new MappingJackson2HttpMessageConverter())
	      .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
	      .build();
	}

}
