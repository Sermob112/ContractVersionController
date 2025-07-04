package Parser.ProcurementObject;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class LinkTransformer {

    private final String inputFilePath = "contract_detail_links.txt";
    private final String outputFilePath = "procurement_objects_links.txt";

    public void transformLinks() {
        List<String> originalLinks = readLinksFromFile();
        List<String> transformedLinks = transformLinks(originalLinks);
        saveLinksToFile(transformedLinks);
    }

    private List<String> readLinksFromFile() {
        try {
            return Files.readAllLines(Paths.get(inputFilePath))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла с ссылками: " + e.getMessage());
            return List.of();
        }
    }

    private List<String> transformLinks(List<String> originalLinks) {
        return originalLinks.stream()
                .map(link -> link.replace(
                        "common-info.html",
                        "payment-info-and-target-of-order.html"))
                .collect(Collectors.toList());
    }

    private void saveLinksToFile(List<String> links) {
        try {
            Files.write(Paths.get(outputFilePath), links);
            System.out.println("Преобразованные ссылки сохранены в файл: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LinkTransformer transformer = new LinkTransformer();
        transformer.transformLinks();
    }
}