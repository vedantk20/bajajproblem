import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to JSON file>");
            return;
        }

        String prnNumber = args[0].toLowerCase().replaceAll("\\s+", "");
        String jsonFilePath = args[1];

        try {
            String destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("No 'destination' key found in the JSON file.");
                return;
            }

            String randomString = generateRandomString(8);
            String concatenatedString = prnNumber + destinationValue + randomString;
            String hash = generateMD5Hash(concatenatedString);

            System.out.println(hash + ";" + randomString);

        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating MD5 hash: " + e.getMessage());
        }
    }

    // Step 2: Read and Parse JSON File
    private static String findDestinationValue(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        JsonElement jsonElement = JsonParser.parseReader(reader);
        reader.close();
        return traverseJson(jsonElement);
    }

    // Step 3: Traverse JSON to find the first occurrence of "destination"
    private static String traverseJson(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getKey().equals("destination")) {
                    return entry.getValue().getAsString();
                }
                String result = traverseJson(entry.getValue());
                if (result != null) {
                    return result;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                String result = traverseJson(item);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Step 4: Generate a random 8-character alphanumeric string
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    // Step 5: Generate MD5 hash of the concatenated string
    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
