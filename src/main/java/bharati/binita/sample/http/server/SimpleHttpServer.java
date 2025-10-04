package bharati.binita.sample.http.server;

import bharati.binita.cache.contract.CacheContract;
import bharati.binita.cache.contract.common.utils.BusyBee;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleHttpServer {

    private CacheContract cacheImplmn;
    private BusyBee busyBee;
    private ExecutorService tPool = Executors.newFixedThreadPool(1);

    public SimpleHttpServer(
            CacheContract cacheImplmn){
        this.cacheImplmn = cacheImplmn;
    }

    public BusyBee getBusyBee() {
        return busyBee;
    }

    public void setBusyBee(BusyBee busyBee) {
        this.busyBee = busyBee;
    }

    public void startServer(int port) throws IOException {

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Define context (route)
        server.createContext("/fetch", new MyHandler());
        server.createContext("/fetch/randomStr", new MyHandler());

        // Use default executor (can also provide your own thread pool)
        server.setExecutor(null);

        // Start
        server.start();
        System.out.println("HTTP server started at http://localhost:" + port);
    }

    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath(); // e.g. "/fetch/123"
            String response;
            int status;

            if (path.indexOf("randomStr") == -1) {
                String[] parts = path.split("/");
                String key = parts.length > 2 ? parts[2] : null;

                if (key != null && !key.isEmpty()) {
                    try {
                        //String randomString = busyBee.getRandomStringResult();
                        response = cacheImplmn.lookUpKey(Integer.parseInt(key));
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                    status = 200;
                } else {
                    response = "Key not provided!\n";
                    status = 400;
                }
            }
            else {
                response = busyBee.getRandomStringResult();
                status = 200;
            }


            exchange.sendResponseHeaders(status, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
