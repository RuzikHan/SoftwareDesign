import org.bson.Document;

public class Item {
    public final int id;
    public final String name;
    public final String currency;
    public Double price;

    public Item(Document doc) {
        this(doc.getInteger("id"), doc.getString("name"), doc.getString("currency"), doc.getDouble("price"));
    }

    public Item(int id, String name, String currency, Double price) {
        this.id = id;
        this.name = name;
        this.currency = currency.toUpperCase();
        this.price = price;
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("name", name)
                .append("currency", currency)
                .append("price", price);
    }

    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                "}";
    }

    public String show() {
        return name + ": " + price + "\n";
    }
}
