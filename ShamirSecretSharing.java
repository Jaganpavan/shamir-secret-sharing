import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {
    // Decode a value from the given base to decimal
    private static BigInteger decodeValue(String base, String value) {
        try {
            return new BigInteger(value, Integer.parseInt(base));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error decoding value: " + value + " in base " + base, e);
        }
    }

    // Compute f(0) using Lagrange interpolation
    private static BigInteger lagrangeInterpolation(List<BigInteger[]> points) {
        BigInteger result = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i)[0];
            BigInteger yi = points.get(i)[1];
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = points.get(j)[0];
                    numerator = numerator.multiply(xj.negate()); // (0 - xj)
                    denominator = denominator.multiply(xi.subtract(xj)); // (xi - xj)
                }
            }

            // term = yi * (numerator / denominator)
            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Process a single test case from JSON data
    private static BigInteger findConstantTerm(JSONObject data) {
        // Extract n and k
        JSONObject keys = data.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        // Validate input
        if (k > n) {
            throw new RuntimeException("Invalid input: k cannot be greater than n");
        }

        // Extract and decode points
        List<BigInteger[]> points = new ArrayList<>();
        for (int x = 1; x <= n; x++) {
            String xStr = String.valueOf(x);
            if (data.has(xStr)) {
                JSONObject point = data.getJSONObject(xStr);
                String base = point.getString("base");
                String value = point.getString("value");
                BigInteger y = decodeValue(base, value);
                points.add(new BigInteger[]{BigInteger.valueOf(x), y});
            }
        }

        // Ensure we have enough points
        if (points.size() < k) {
            throw new RuntimeException("Not enough points provided");
        }

        // Use first k points
        List<BigInteger[]> selectedPoints = points.subList(0, k);

        // Compute constant term
        return lagrangeInterpolation(selectedPoints);
    }

    // Read JSON from file
    private static JSONObject readJsonFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JSONObject(content);
    }

    public static void main(String[] args) {
        try {
            // Process Test Case 1
            JSONObject testCase1 = readJsonFile("testcase1.json");
            BigInteger c1 = findConstantTerm(testCase1);
            System.out.println("Secret for Test Case 1: " + c1);

            // Process Test Case 2
            JSONObject testCase2 = readJsonFile("testcase2.json");
            BigInteger c2 = findConstantTerm(testCase2);
            System.out.println("Secret for Test Case 2: " + c2);

        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing test case: " + e.getMessage());
        }
    }
}