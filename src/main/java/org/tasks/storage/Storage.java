package org.tasks.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.tasks.model.DataObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public abstract class Storage<T extends DataObject> {
    private static final String NEW_LINE = "\r\n";

    protected Map<String, T> dataMap = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private final String fileName;
    private final Class<T> dataClass;

    protected Storage(String fileName, Class<T> dataClass) {
        this.fileName = fileName;
        this.dataClass = dataClass;
        collectDataFromFile();
    }

    public void addNewDataObject(T dataObject) {
        try {
            String line = getDataObjectAsString(dataObject);
            addLineToFile(line, fileName);
            dataMap.put(dataObject.getPrimary(), dataObject);
        } catch (IOException e) {
            System.out.println("Unable to store " + dataClass + " object");
        }
    }

    public T findDataObjectByPrimary(String primary) {
        return dataMap.get(primary);
    }

    protected int collectDataFromFile() {
        try (ReversedLinesFileReader reverseReader = getReverseReader(fileName)) {
            while (true) {
                String line = reverseReader.readLine();
                if (line == null) {
                    break;
                }

                T dataObject = getDataObject(line);
                if (dataObject != null) {
                    String primary = dataObject.getPrimary();
                    if (!dataMap.containsKey(primary)) {
                        dataMap.put(primary, dataObject);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return dataMap.size();
    }

    private String getDataObjectAsString(T dataObject) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dataObject);
    }

    private T getDataObject(String data) {
        if (data != null) {
            try {
                return objectMapper.readValue(data, dataClass);
            } catch (JsonProcessingException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }

    private void addLineToFile(String newData, String fileName) throws IOException {
        File file = getFile(fileName);
        byte[] bytes = (NEW_LINE + newData).getBytes();
        Files.write(Paths.get(file.getPath()), bytes, StandardOpenOption.APPEND);
    }

    private ReversedLinesFileReader getReverseReader(String fileName) throws IOException {
        return ReversedLinesFileReader.builder()
                .setFile(getFile(fileName))
                .setBufferSize(2048)
                .setCharset(Charset.defaultCharset())
                .get();
    }

    private File getFile(String fileName) throws NoSuchFileException {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            throw new NoSuchFileException(fileName);
        }

        return new File(url.getFile());
    }
}
