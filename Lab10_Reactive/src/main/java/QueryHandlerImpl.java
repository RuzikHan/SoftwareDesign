import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QueryHandlerImpl implements QueryHandler {

    public MongoDatabase database = MongoClients.create("mongodb://localhost:27017").getDatabase("web-server");

    @Override
    public Observable<String> parseQuery(HttpServerRequest<ByteBuf> request, CurrencyConverter converter) {
        String requestName = request.getDecodedPath().substring(1);
        Map<String, List<String>> parameters = request.getQueryParameters();
        switch (requestName) {
            case "add-user":
                try {
                    User user = getUser(parameters);
                    if (!converter.exchangeRates.containsKey(user.currency)) {
                        return Observable.just("Unexpected currency " + user.currency + "\nAllowed currencies: RUB, USD, EUR");
                    }
                    return getUserById(user.id)
                            .singleOrDefault(null)
                            .flatMap(existingUser -> {
                                if (existingUser != null) {
                                    return Observable.just("User with id = " + user.id + " already exists");
                                } else {
                                    return database
                                            .getCollection("users")
                                            .insertOne(user.toDocument())
                                            .asObservable()
                                            .isEmpty()
                                            .map(bool -> !bool)
                                            .map(Object::toString);
                                }
                            });
                } catch (Exception e) {
                    return Observable.just("Expected 3 parameters: id, login, currency");
                }
            case "add-item":
                try {
                    Item item = getItem(parameters);
                    if (!converter.exchangeRates.containsKey(item.currency)) {
                        return Observable.just("Unexpected currency " + item.currency + "\nAllowed currencies: RUB, USD, EUR");
                    }
                    return database
                            .getCollection("items")
                            .find(Filters.eq("id", item.id))
                            .toObservable()
                            .map(Item::new)
                            .singleOrDefault(null)
                            .flatMap(existingItem -> {
                                if (existingItem != null) {
                                    return Observable.just("Item with id = " + item.id + " already exists");
                                } else {
                                    return database
                                            .getCollection("items")
                                            .insertOne(item.toDocument())
                                            .asObservable()
                                            .isEmpty()
                                            .map(bool -> !bool)
                                            .map(Objects::toString);
                                }
                            });
                } catch (Exception e) {
                    return Observable.just("Expected 4 parameters: id, name, currency, price");
                }
            case "show-items":
                try {
                    int id = Integer.parseInt(parameters.get("id").get(0));
                    return getUserById(id)
                            .single()
                            .flatMap(user ->
                                    database
                                            .getCollection("items")
                                            .find()
                                            .toObservable()
                                            .map(Item::new)
                                            .map(item -> {
                                                if (!item.currency.equals(user.currency)) {
                                                    item.price = converter.convert(item.currency, user.currency, item.price);
                                                }
                                                return item.show();
                                            }));
                } catch (Exception e) {
                    return Observable.just("Expected 1 parameter: user id");
                }
            case "delete-users":
                return database
                        .getCollection("users")
                        .drop()
                        .map(Enum::toString);
            case "delete-items":
                return database
                        .getCollection("items")
                        .drop()
                        .map(Enum::toString);
            default:
                return Observable.just("Unexpected command\nList of commands: add-user, add-item, show-items, delete-users, delete-items");
        }
    }

    private Observable<User> getUserById(int id) {
        return database
                .getCollection("users")
                .find(Filters.eq("id", id))
                .toObservable()
                .map(User::new);
    }

    private User getUser(Map<String, List<String>> parameters) {
        return new User(Integer.parseInt(parameters.get("id").get(0)), parameters.get("currency").get(0).toUpperCase(), parameters.get("login").get(0));
    }

    private Item getItem(Map<String, List<String>> parameters) {
        return new Item(Integer.parseInt(parameters.get("id").get(0)), parameters.get("name").get(0), parameters.get("currency").get(0).toUpperCase(), Double.parseDouble(parameters.get("price").get(0)));
    }
}
