import io.reactivex.netty.protocol.http.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        // sudo systemctl start mongod
        CurrencyConverter converter = new CurrencyConverter();
        QueryHandler handler = new QueryHandlerImpl();
        HttpServer
                .newServer(8080)
                .start((request, response) ->
                        response.writeString(handler.parseQuery(request, converter))
                ).awaitShutdown();
    }
}
