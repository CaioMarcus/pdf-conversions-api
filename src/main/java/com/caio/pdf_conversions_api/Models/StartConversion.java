package com.caio.pdf_conversions_api.Models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


/**
 * O modelo de iniciar a conversão. É o objeto que será recebido pelo request no Controller.
 */
@Data
public class StartConversion {
    private String name;
    private String type;
    private List<MultipartFile> files;

    public void validateConversion(){
        assert(files != null && !files.isEmpty()) : "None Files Founded";
        assert(name == null || name.isEmpty() || name.isBlank()) : "Specified Name Is Invalid";
        assert(type == null || type.isEmpty() || type.isBlank()) : "Specified Type Is Invalid";
    }
}
