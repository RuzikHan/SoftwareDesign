import org.bson.Document;

public class User {
    public final int id;
    public final String currency;
    public final String login;

    public User(Document doc) {
        this(doc.getInteger("id"), doc.getString("currency"), doc.getString("login"));
    }

    public User(int id, String currency, String login) {
        this.id = id;
        this.currency = currency;
        this.login = login;
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("login", login)
                .append("currency", currency);
    }

    public String toString() {
        return "User{" +
                "id=" + id +
                ", currency='" + currency + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}