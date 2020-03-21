import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    Map<String, Map<String, Double>> exchangeRates = new HashMap<>();

    CurrencyConverter() {
        setRates();
    }

    public void setRates() {
        Map<String, Double> rubleRates = new HashMap<>();
        rubleRates.put("USD", 0.013);
        rubleRates.put("EUR", 0.012);
        exchangeRates.put("RUB", rubleRates);
        Map<String, Double> dollarRates = new HashMap<>();
        dollarRates.put("RUB", 79.98);
        dollarRates.put("EUR", 0.93);
        exchangeRates.put("USD", dollarRates);
        Map<String, Double> euroRates = new HashMap<>();
        euroRates.put("RUB", 86.08);
        euroRates.put("USD", 1.08);
        exchangeRates.put("EUR", euroRates);
    }

    public Double convert(String defaultCurrency, String requiredCurrency, Double price) {
        Map<String, Double> rates = exchangeRates.get(defaultCurrency);
        return price * rates.get(requiredCurrency);
    }
}
