import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

public interface QueryHandler {
    Observable<String> parseQuery(HttpServerRequest<ByteBuf> request, CurrencyConverter converter);
}
