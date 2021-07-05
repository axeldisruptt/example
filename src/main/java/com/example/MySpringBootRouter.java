package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	
    	String wmsUri = "http://10.1.99.27:8080/integrator/receipt-simple-confirm?between=";
    	String dateRange = WmsParams.getDateRange(60 * 60 * 24 * 90); // Poll interval in seconds (3 months)
    	String encodedDateRange = URLEncoder.encode(dateRange, "UTF-8");
    	wmsUri += encodedDateRange;
    	String erpUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=589&deploy=1";

    	
    	from("timer:hello?period={{timer.period}}").routeId("poll")
    		.to("log:DEBUG?showBody=true&showHeaders=true")
    		.to(wmsUri)
        	.to("log:DEBUG?showBody=true&showHeaders=true")
        	.setHeader("CamelHttpMethod", constant("POST"))
        	.process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                	String authHeader = OAuthSign.getAuthHeader();
                    exchange.getMessage().setHeader("Authorization", authHeader);
                }
        	})
        	.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
        	.to("log:DEBUG?showBody=true&showHeaders=true")
        	.to(erpUri + "&throwExceptionOnFailure=false")
        	.to("log:DEBUG?showBody=true&showHeaders=true")
        	.to("stream:out");
    }

}
