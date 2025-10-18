package bharati.binita.cache1.common.http.server;

import bharati.binita.cache1.common.helpers.CustomerBasicInfoReaderCumUpdater;
import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpServer.class);
    private static final String CUST_ID_NOT_FOUND = "{\"error\": \"404\"}";

    private CacheService cacheService;

    public SimpleHttpServer(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void initServer() throws IOException {
        // Create server bound to port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);

        // Create a context (path) and assign handler
        server.createContext("/cacheservice/lookup/customer", new LookupCustomerHandler());
        // Create a context (path) and assign handler
        server.createContext("/cacheservice/lookup/customer/latest/transactions", new LookupCustomerTrxnsHandler());
        // Use a default executor (creates threads automatically)
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.setExecutor(null);
        // Start server
        server.start();
        log.info("Server started at http://localhost:9090/cacheservice");
    }

    class LookupCustomerHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Full path, e.g. /cacheservice/lookup/cust/12345
            String path = exchange.getRequestURI().getPath();

            // Split by "/" → ["", "cacheservice", "lookup", "cust", "12345"]
            String[] parts = path.split("/");
            String custId = parts[4];

            String response = null;
            try(Arena arena = Arena.ofConfined()) {
                response = cacheService.getBasicCustomerInfo(Integer.parseInt(custId), arena.allocate(Util.CUSTOMER_INFO_JSON_STR_SIZE));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            if (response == null){
                response = CUST_ID_NOT_FOUND;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    class LookupCustomerTrxnsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Full path, e.g. /cacheservice/lookup/customer/latest/transactions/12345
            String path = exchange.getRequestURI().getPath();

            // Split by "/" → ["", "cacheservice", "lookup", "cust", "12345"]
            String[] parts = path.split("/");
            String custId = parts[6];
            log.info("LookupCustomerTrxnsHandler: custId = {}", custId);

            String response = null;
            try (Arena arena = Arena.ofConfined()){
                log.info("LookupCustomerTrxnsHandler: here1 ");

                response = cacheService.getLatestTrxnsForCustomer(Integer.parseInt(custId), arena.allocate(Util.CUSTOMER_TRXN_INFO_JSON_STR_SIZE));
                log.info("LookupCustomerTrxnsHandler: here2 response = {}", response);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            if (response == null){
                response = CUST_ID_NOT_FOUND;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}