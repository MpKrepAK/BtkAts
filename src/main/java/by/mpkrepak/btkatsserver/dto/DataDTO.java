package by.mpkrepak.btkatsserver.dto;

import lombok.Data;

import java.io.File;

@Data
public class DataDTO {
    private Long id;
    private String caller;
    private String receiver;
    private String duration;
    private String date;
    private String filePath;
    public File getFile() {
        return new File(filePath);
    }
}
